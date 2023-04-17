package com.tiger.ar.museum.presentation.widget

import com.tiger.ar.museum.common.recycleview.BaseAdapter

abstract class BaseGridAdapter : BaseAdapter() {

    abstract fun getItemCountInRow(viewType: Int): Int

    private fun getMaxItemHorizontalByViewType(viewType: Int): Int {
        return when (viewType) {
            LOAD_MORE_VIEW_TYPE,
            EMPTY_VIEW_TYPE,
            LOADING_VIEW_TYPE -> 1
            else -> getItemCountInRow(viewType)
        }
    }

    fun getItemSpanSize(position: Int, spanCount: Int): Int {
        val viewType = getItemViewType(position)
        val maxItemHorizontal = getMaxItemHorizontalByViewType(viewType)
        return spanCount / maxItemHorizontal
    }
}
