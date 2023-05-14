package com.tiger.ar.museum.presentation.item

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.tiger.ar.museum.R
import com.tiger.ar.museum.common.binding.MuseumFragment
import com.tiger.ar.museum.common.extension.loadImage
import com.tiger.ar.museum.common.extension.toast
import com.tiger.ar.museum.common.extension.toastUndeveloped
import com.tiger.ar.museum.databinding.ItemFragmentBinding
import com.tiger.ar.museum.presentation.RealMainActivity
import com.tiger.ar.museum.presentation.ZoomFragment
import com.tiger.ar.museum.presentation.camera.view3d.View3dActivity
import com.tiger.ar.museum.presentation.collection.CollectionFragment
import com.tiger.ar.museum.presentation.favorite.item.ItemListFragment
import com.tiger.ar.museum.presentation.streetview.StreetViewFragment
import com.tiger.ar.museum.presentation.widget.COLLECTION_MODE

class ItemFragment : MuseumFragment<ItemFragmentBinding>(R.layout.item_fragment) {
    companion object {
        const val ITEM_ID_KEY = "ITEMS_KEY"
    }

    private val adapter by lazy { ItemAdapter() }
    private val viewModel by viewModels<ItemViewModel>()
    private var oldScroll = false

    override fun onPrepareInitView() {
        super.onPrepareInitView()
        viewModel.itemId = arguments?.getString(ITEM_ID_KEY)
    }

    override fun onInitView() {
        super.onInitView()
        (museumActivity as? RealMainActivity)?.apply {
            setBackIcon()
            oldScroll = enableScrollHideActionBar(false)
        }
        initAction()
        initBackDrop()
        viewModel.getItemData(
            onSuccessAction = {
                binding.ivItem.loadImage(viewModel.item?.thumbnail)
                binding.cvItemBackDrop.submitList(viewModel.itemData)
            },
            onFailureAction = {
                toast("Get item data fail: $it")
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        realMainActivity.apply {
            enableScrollHideActionBar(oldScroll)
            if (supportFragmentManager.fragments.lastOrNull() is ItemListFragment) {
                realMainActivity.enableFragmentContainerScrollingBehavior()
            }
        }
    }

    private fun initAction() {
        adapter.listener = object : ItemAdapter.IListener {
            override fun onActionClick(actionType: ACTION_TYPE) {
                when (actionType) {
                    ACTION_TYPE.ZOOM_IN -> {
                        realMainActivity.addFragmentNew(
                            ZoomFragment(),
                            bundleOf(ZoomFragment.IMAGE_URL_KEY to viewModel.item?.thumbnail),
                            containerId = R.id.flRealMainContainer
                        )
                    }

                    ACTION_TYPE.AR -> {
                        if (viewModel.item?.model3d == null) {
                            toast("Tác phẩm này không có mô hình")
                        } else {
                            navigateTo(View3dActivity::class.java, bundleOf(View3dActivity.ITEM_ID_KEY to viewModel.itemId))
                        }
                    }

                    ACTION_TYPE.STREET -> {
                        if (viewModel.item?.streetView == null) {
                            toast("Tác phẩm này không có street view")
                        } else {
                            museumActivity.addFragmentNew(
                                StreetViewFragment().apply { this.location = viewModel.item?.streetView },
                                containerId = R.id.flRealMainContainer
                            )
                        }
                    }
                }
            }

            override fun onDetailTitleClick(isOpen: Boolean) {
                if (isOpen) {
                    viewModel.updateDetailOpen()
                    binding.cvItemBackDrop.submitList(viewModel.itemData)
                } else {
                    if (viewModel.itemDataDetail.isEmpty()) {
                        viewModel.mapDataDetailForAdapter {
                            binding.cvItemBackDrop.submitList(viewModel.itemDataDetail)
                        }
                    } else {
                        viewModel.updateDetailOpen()
                        binding.cvItemBackDrop.submitList(viewModel.itemDataDetail)
                    }
                }
            }

            override fun onLikeClick() {
                viewModel.likeUpdate(
                    true,
                    onSuccessAction = {
                        binding.cvItemBackDrop.submitList(viewModel.itemData)
                        realMainActivity.reloadFavorite()
                    },
                    onFailureAction = {
                        toast("Like fail: $it")
                    }
                )
            }

            override fun onDislikeClick() {
                viewModel.likeUpdate(
                    false,
                    onSuccessAction = {
                        binding.cvItemBackDrop.submitList(viewModel.itemData)
                        (museumActivity as RealMainActivity).reloadFavorite()
                    },
                    onFailureAction = {
                        toast("Like fail: $it")
                    }
                )
            }

            override fun onShareClick() {
                toastUndeveloped()
            }

            override fun onCollectionClick() {
                addFragmentNew(
                    CollectionFragment(),
                    bundleOf(CollectionFragment.COLLECTION_ID_KEY to viewModel.item?.collectionId),
                    containerId = R.id.flRealMainContainer
                )
            }

            override fun onRecommendedItemClick(itemId: String?) {
                addFragmentNew(
                    ItemFragment(),
                    bundleOf(ItemFragment.ITEM_ID_KEY to itemId),
                    containerId = R.id.flRealMainContainer
                )
            }
        }
    }

    private fun initBackDrop() {
        binding.cvItemBackDrop.apply {
            setAdapter(this@ItemFragment.adapter)
            setLayoutManager(COLLECTION_MODE.VERTICAL)
        }
    }
}
