package com.tiger.ar.museum.presentation.favorite

import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tiger.ar.museum.AppPreferences
import com.tiger.ar.museum.common.BaseViewModel
import com.tiger.ar.museum.domain.model.*
import kotlinx.coroutines.launch

class FavoriteViewModel : BaseViewModel() {
    var listFavorite: MutableList<Any> = initFavoriteTabData()
    var listGallery: MutableList<Any> = mockGalleriesData()

    fun getFavoriteData(onSuccessAction: () -> Unit, onFailureAction: (message: String) -> Unit) {
        viewModelScope.launch {

            // Get favorites data of user
            val ref = FirebaseDatabase.getInstance().getReference("Users/${AppPreferences.getUserInfo().key}/favorites")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favoriteData = snapshot.getValue(FavoriteData::class.java)

                    // init list favorite with 2 tab
                    val newListFavorite = initFavoriteTabData()

                    // remove null in items
                    favoriteData?.items?.removeIf { it?.key == null }

                    // get items from list key, max 4 items
                    val itemList = mutableListOf<Item>()
                    var count = favoriteData?.items?.size ?: 0
                    if (count > 4) count = 4
                    favoriteData?.items?.subList(0, count)?.forEach {
                        val itemRef = FirebaseDatabase.getInstance().getReference("Items/${it?.key}")
                        itemRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                count--
                                val item = snapshot.getValue(Item::class.java)
                                if (item != null) {
                                    itemList.add(item)

                                    // do success action when get 4 items or end of list
                                    if (count <= 0 || itemList.size == 4) {
                                        (this@FavoriteViewModel.listFavorite.getOrNull(1) as? FavoriteAdapter.ItemDisplay)?.let { itemDisplay ->
                                            itemDisplay.count = favoriteData.items?.size ?: 0
                                            itemDisplay.itemList = itemList
                                        }
                                        onSuccessAction.invoke()
                                        return
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                count--
                                onFailureAction.invoke(error.message)
                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onFailureAction.invoke(error.message)
                }
            })
        }
    }

    private fun mockGalleriesData(): MutableList<Any> {
        val list = mutableListOf<Any>()
        list.add(FavoriteAdapter.HeaderDisplay().apply {
            avatarUrl = AppPreferences.getUserInfo().avatar
        })
        list.add(Gallery().apply {
            title = "Gallery 1"
            description = "Description 1"
            items = listOf(
                Item(thumbnail = "https://i.pinimg.com/736x/10/2e/19/102e192f5ec83d1c10fa9b3241a50b1f.jpg")
            )
        })
        list.add(Gallery().apply {
            title = "Gallery 2"
            description = "Description 2"
            items = listOf(
                Item(thumbnail = "https://i.pinimg.com/736x/10/2e/19/102e192f5ec83d1c10fa9b3241a50b1f.jpg"),
                Item(thumbnail = "https://i.pinimg.com/564x/a3/0d/94/a30d9464d035e1f30dd088d2ba89103d.jpg"),
                Item(thumbnail = "https://i.pinimg.com/564x/e4/78/fa/e478fae5d5ecf6f8537f248f6cab0d15.jpg")
            )
        })
        list.add(Gallery().apply {
            title = "Gallery 3"
            description = "Description 3"
            items = listOf(
                Item(thumbnail = "https://i.pinimg.com/736x/10/2e/19/102e192f5ec83d1c10fa9b3241a50b1f.jpg"),
                Item(thumbnail = "https://i.pinimg.com/564x/a3/0d/94/a30d9464d035e1f30dd088d2ba89103d.jpg")
            )
        })
        return list
    }

    private fun mockFavoriteTabData(): MutableList<Any> {
        val list = mutableListOf<Any>()
        list.add(FavoriteAdapter.HeaderDisplay().apply {
            avatarUrl = AppPreferences.getUserInfo().avatar
        })
        list.add(FavoriteAdapter.ItemDisplay().apply {
            count = 42
            itemList = listOf(
                Item(thumbnail = "https://i.pinimg.com/736x/10/2e/19/102e192f5ec83d1c10fa9b3241a50b1f.jpg"),
                Item(thumbnail = "https://i.pinimg.com/564x/a3/0d/94/a30d9464d035e1f30dd088d2ba89103d.jpg"),
                Item(thumbnail = "https://i.pinimg.com/564x/e4/78/fa/e478fae5d5ecf6f8537f248f6cab0d15.jpg"),
                Item(thumbnail = "https://i.pinimg.com/564x/75/bd/2e/75bd2e6f99d705642531b3f214ff4f70.jpg")
            )
        })
        list.add(FavoriteAdapter.StoryDisplay().apply {
            count = 30
            storyList = listOf(
                Story(
                    thumbnail = "https://i.pinimg.com/736x/10/2e/19/102e192f5ec83d1c10fa9b3241a50b1f.jpg",
                    title = "Story 1",
                    description = "Description 1"
                ),
                Story(
                    thumbnail = "https://i.pinimg.com/564x/a3/0d/94/a30d9464d035e1f30dd088d2ba89103d.jpg",
                    title = "Story 2",
                    description = "Description 2"
                ),
                Story(
                    thumbnail = "https://i.pinimg.com/564x/e4/78/fa/e478fae5d5ecf6f8537f248f6cab0d15.jpg",
                    title = "Story 3",
                    description = "Description 3"
                ),
                Story(
                    thumbnail = "https://i.pinimg.com/564x/75/bd/2e/75bd2e6f99d705642531b3f214ff4f70.jpg",
                    title = "Story 4",
                    description = "Description 4"
                ),
                Story(
                    thumbnail = "https://i.pinimg.com/736x/10/2e/19/102e192f5ec83d1c10fa9b3241a50b1f.jpg",
                    title = "Story 5",
                    description = "Description 5"
                ),
                Story(
                    thumbnail = "https://i.pinimg.com/564x/a3/0d/94/a30d9464d035e1f30dd088d2ba89103d.jpg",
                    title = "Story 6",
                    description = "Description 6"
                ),
            )
        })
        list.add(FavoriteAdapter.CollectionDisplay().apply {
            count = 13
            collectionList = listOf(
                MCollection(thumbnail = "https://i.pinimg.com/736x/10/2e/19/102e192f5ec83d1c10fa9b3241a50b1f.jpg"),
                MCollection(thumbnail = "https://i.pinimg.com/564x/a3/0d/94/a30d9464d035e1f30dd088d2ba89103d.jpg"),
                MCollection(thumbnail = "https://i.pinimg.com/564x/e4/78/fa/e478fae5d5ecf6f8537f248f6cab0d15.jpg"),
                MCollection(thumbnail = "https://i.pinimg.com/564x/75/bd/2e/75bd2e6f99d705642531b3f214ff4f70.jpg"),
                MCollection(thumbnail = "https://i.pinimg.com/736x/10/2e/19/102e192f5ec83d1c10fa9b3241a50b1f.jpg"),
                MCollection(thumbnail = "https://i.pinimg.com/564x/a3/0d/94/a30d9464d035e1f30dd088d2ba89103d.jpg")
            )
        })
        return list
    }

    private fun initFavoriteTabData(): MutableList<Any> {
        val list = mutableListOf<Any>()
        list.add(FavoriteAdapter.HeaderDisplay().apply {
            avatarUrl = AppPreferences.getUserInfo().avatar
        })
        list.add(FavoriteAdapter.ItemDisplay().apply {
            count = 0
            itemList = listOf()
        })
        return list
    }
}
