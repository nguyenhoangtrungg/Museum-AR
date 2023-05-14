package com.tiger.ar.museum.presentation.collection

import android.annotation.SuppressLint
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tiger.ar.museum.R
import com.tiger.ar.museum.common.extension.getAppString
import com.tiger.ar.museum.common.extension.gone
import com.tiger.ar.museum.common.extension.loadImage
import com.tiger.ar.museum.common.extension.setOnSafeClick
import com.tiger.ar.museum.common.extension.show
import com.tiger.ar.museum.common.recycleview.BaseVH
import com.tiger.ar.museum.common.recycleview.MuseumAdapter
import com.tiger.ar.museum.databinding.CollectionDescriptionItemBinding
import com.tiger.ar.museum.databinding.CollectionHeaderItemBinding
import com.tiger.ar.museum.databinding.CollectionItemsItemBinding
import com.tiger.ar.museum.databinding.CollectionStoriesItemBinding
import com.tiger.ar.museum.domain.model.Item
import com.tiger.ar.museum.domain.model.MCollection
import com.tiger.ar.museum.domain.model.Story
import com.tiger.ar.museum.presentation.favorite.FavoriteStoryAdapter
import com.tiger.ar.museum.presentation.widget.COLLECTION_MODE

class CollectionAdapter : MuseumAdapter() {
    companion object {
        const val HEADER_VIEW_TYPE = 1409
        const val DESCRIPTION_TYPE = 1412
        const val STORY_VIEW_TYPE = 1410
        const val ITEM_VIEW_TYPE = 1411

        const val FOLLOW_PAYLOAD = "FOLLOW_PAYLOAD"
    }

    var listener: IListener? = null

    override fun getDiffUtil(oldList: List<Any>, newList: List<Any>): DiffUtil.Callback {
        return CollectionDiffUtil(oldList, newList)
    }

    override fun getItemViewTypeCustom(position: Int): Int {
        return when (getDataAtPosition(position)) {
            is HeaderDisplay -> HEADER_VIEW_TYPE
            is DescriptionDisplay -> DESCRIPTION_TYPE
            is StoriesDisplay -> STORY_VIEW_TYPE
            is ItemsDisplay -> ITEM_VIEW_TYPE
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getLayoutResource(viewType: Int): Int {
        return when (viewType) {
            HEADER_VIEW_TYPE -> R.layout.collection_header_item
            DESCRIPTION_TYPE -> R.layout.collection_description_item
            STORY_VIEW_TYPE -> R.layout.collection_stories_item
            ITEM_VIEW_TYPE -> R.layout.collection_items_item
            else -> throw IllegalArgumentException("getLayoutResource: viewType is invalid")
        }
    }

    override fun onCreateViewHolder(viewType: Int, binding: ViewDataBinding): BaseVH<*>? {
        return when (viewType) {
            HEADER_VIEW_TYPE -> HeaderVH(binding as CollectionHeaderItemBinding)
            DESCRIPTION_TYPE -> DescriptionVH(binding as CollectionDescriptionItemBinding)
            STORY_VIEW_TYPE -> StoriesVH(binding as CollectionStoriesItemBinding)
            ITEM_VIEW_TYPE -> ItemsVH(binding as CollectionItemsItemBinding)
            else -> throw IllegalArgumentException("onCreateViewHolder: viewType is invalid")
        }
    }

    inner class HeaderVH(private val binding: CollectionHeaderItemBinding) : BaseVH<HeaderDisplay>(binding) {
        init {
            binding.mcvCollectionHeaderFollow.setOnSafeClick {
                getItem {
                    val isFollow = it.collection?.safeIsLiked() ?: false
                    if (isFollow) {
                        listener?.onUnFollowClick()
                    } else {
                        listener?.onFollowClick()
                    }
                }
            }

            binding.ivCollectionHeaderShare.setOnSafeClick {
                listener?.onShareClick()
            }
        }

        override fun onBind(data: HeaderDisplay) {
            binding.apply {
                ivCollectionHeaderThumbnail.loadImage(data.collection?.thumbnail)
                ivCollectionHeaderIcon.loadImage(data.collection?.icon)
                tvCollectionHeaderName.text = data.collection?.name
                tvCollectionHeaderPlace.text = data.collection?.place
            }
            setFollowStatus(data)
        }

        override fun onBind(data: HeaderDisplay, payloads: List<Any>) {
            binding.apply {
                (payloads.firstOrNull() as? List<*>)?.forEach {
                    when (it) {
                        FOLLOW_PAYLOAD -> setFollowStatus(data)
                    }
                }
            }
        }

        private fun setFollowStatus(data: HeaderDisplay) {
            val isFollow = data.collection?.safeIsLiked() ?: false
            if (isFollow) {
                binding.llCollectionHeaderFollowing.show()
                binding.llCollectionHeaderFollow.gone()
            } else {
                binding.llCollectionHeaderFollowing.gone()
                binding.llCollectionHeaderFollow.show()
            }
        }
    }

    inner class DescriptionVH(private val binding: CollectionDescriptionItemBinding) : BaseVH<DescriptionDisplay>(binding) {
        init {
            binding.tvCollectionDescriptionReadMore.setOnSafeClick {
                binding.tvCollectionDescription.apply {
                    maxLines = if (maxLines == 3) {
                        Integer.MAX_VALUE
                    } else {
                        3
                    }
                }
                binding.tvCollectionDescriptionReadMore.apply {
                    text = if (text == getAppString(R.string.read_more)) {
                        getAppString(R.string.read_less)
                    } else {
                        getAppString(R.string.read_more)
                    }
                }
            }
        }

        override fun onBind(data: DescriptionDisplay) {
            binding.tvCollectionDescription.text = data.description
        }
    }

    inner class StoriesVH(private val binding: CollectionStoriesItemBinding) : BaseVH<StoriesDisplay>(binding) {
        private val adapter by lazy { FavoriteStoryAdapter() }

        init {
            adapter.listener = object : FavoriteStoryAdapter.IListener {
                override fun onStoryClick(storyId: String?) {
                    listener?.onStoryClick(storyId)
                }
            }
            binding.cvCollectionStories.apply {
                setAdapter(this@StoriesVH.adapter)
                setLayoutManager(COLLECTION_MODE.HORIZONTAL)
            }
            binding.tvCollectionStoriesViewAll.setOnSafeClick { getItem { listener?.onViewAllStories() } }
        }

        override fun onBind(data: StoriesDisplay) {
            if (data.stories == null) {
                getDataFromDb(data)
            } else {
                updateTitle(data)
                binding.cvCollectionStories.submitList(data.stories)
            }
        }

        @SuppressLint("SetTextI18n")
        private fun updateTitle(data: StoriesDisplay) {
            binding.tvCollectionStoriesTitle.text = data.stories?.size.toString() + " " + getAppString(R.string.stories_lower_case)
        }

        private fun getDataFromDb(data: StoriesDisplay) {
            val db = Firebase.firestore
            val storiesRef = db.collection("stories").whereEqualTo("collectionId", data.collectionId)
            storiesRef.get().addOnSuccessListener { storiesSnapshot ->
                if (storiesSnapshot.documents.isEmpty()) {
                    data.stories = emptyList()
                } else {
                    data.stories = storiesSnapshot.documents.mapNotNull {
                        it.toObject(Story::class.java)?.apply { key = it.id }
                    }
                    updateTitle(data)
                    binding.cvCollectionStories.submitList(data.stories)
                }
            }
        }
    }

    inner class ItemsVH(private val binding: CollectionItemsItemBinding) : BaseVH<ItemsDisplay>(binding) {
        init {

        }

        override fun onBind(data: ItemsDisplay) {
        }
    }

    class HeaderDisplay {
        var collection: MCollection? = null

        fun copy(): HeaderDisplay {
            val newItem = HeaderDisplay()
            newItem.collection = collection?.copy()?.apply {
                key = collection?.key
                mapIsLiked()
            }
            return newItem
        }
    }

    data class DescriptionDisplay(
        val description: String? = null
    )

    data class StoriesDisplay(
        var collectionId: String? = null,
        var stories: List<Story>? = null
    )

    data class ItemsDisplay(
        var collectionId: String? = null,
        val items: List<Item>? = null
    )

    interface IListener {
        fun onItemClick(itemId: String?)
        fun onStoryClick(storyId: String?)
        fun onFollowClick()
        fun onUnFollowClick()
        fun onShareClick()
        fun onViewAllStories()
    }
}
