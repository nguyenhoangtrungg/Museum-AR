package com.tiger.ar.museum.presentation.search

import androidx.databinding.ViewDataBinding
import com.tiger.ar.museum.R
import com.tiger.ar.museum.common.extension.setOnSafeClick
import com.tiger.ar.museum.common.recycleview.BaseVH
import com.tiger.ar.museum.common.recycleview.MuseumAdapter
import com.tiger.ar.museum.databinding.SearchKeyItemBinding

class SearchKeyAdapter : MuseumAdapter() {
    var listener: IListener? = null

    override fun getLayoutResource(viewType: Int): Int {
        return R.layout.search_key_item
    }

    override fun onCreateViewHolder(viewType: Int, binding: ViewDataBinding): BaseVH<*>? {
        return SearchKeyVH(binding as SearchKeyItemBinding)
    }

    inner class SearchKeyVH(val binding: SearchKeyItemBinding) : BaseVH<SearchKeyDisplay>(binding) {
        init {
            binding.mcvSearchKey.setOnSafeClick { getItem { listener?.onSearchKeyClick(it.text) } }
        }

        override fun onBind(data: SearchKeyDisplay) {
            updateType(data)
            binding.tvSearchKey.text = data.text
        }

        private fun updateType(data: SearchKeyDisplay) {
            if (data.type == SEARCH_TYPE.SUGGEST) {
                binding.ivSearchKey.setImageResource(R.drawable.ic_search_gray)
            } else {
                binding.ivSearchKey.setImageResource(R.drawable.ic_history_gray)
            }
        }
    }

    data class SearchKeyDisplay(
        var text: String,
        var type: SEARCH_TYPE = SEARCH_TYPE.SUGGEST,
    )

    interface IListener {
        fun onSearchKeyClick(text: String)
    }
}

enum class SEARCH_TYPE {
    HISTORY,
    SUGGEST,
}
