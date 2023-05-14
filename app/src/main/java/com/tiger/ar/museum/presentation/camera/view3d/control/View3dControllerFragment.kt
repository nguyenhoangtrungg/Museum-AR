package com.tiger.ar.museum.presentation.camera.view3d.control

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.fragment.app.activityViewModels
import com.tiger.ar.museum.R
import com.tiger.ar.museum.common.binding.MuseumFragment
import com.tiger.ar.museum.common.extension.toast
import com.tiger.ar.museum.databinding.View3dControllerFragmentBinding
import com.tiger.ar.museum.presentation.camera.view3d.DOWNLOAD_STATUS
import com.tiger.ar.museum.presentation.camera.view3d.View3dViewModel
import com.tiger.ar.museum.presentation.widget.COLLECTION_MODE

class View3dControllerFragment : MuseumFragment<View3dControllerFragmentBinding>(R.layout.view_3d_controller_fragment) {
    private val adapter by lazy { View3dControllerAdapter() }
    private val viewModel by activityViewModels<View3dViewModel>()
    private val downloadReceiver = DownloadReceiver()

    override fun onInitView() {
        super.onInitView()
        initRecyclerView()
        requireContext().registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        viewModel.getListItem(
            onSuccessAction = {
                binding.cvView3d.submitList(viewModel.listItem)
            }, onFailAction = {
                toast("Fail: $it")
            }
        )
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        requireContext().unregisterReceiver(downloadReceiver)
//        museumActivity.navigateBack()
//    }

    private fun initRecyclerView() {
        adapter.listener = object : View3dControllerAdapter.IListener {
            override fun onItemClick(itemDisplay: View3dControllerAdapter.ItemDisplay) {
                viewModel.updateSelectedItem(itemDisplay.item.key, onSuccessAction = {
                    binding.cvView3d.submitList(viewModel.listItem)
                })
            }
        }
        binding.cvView3d.apply {
            setAdapter(this@View3dControllerFragment.adapter)
            setLayoutManager(COLLECTION_MODE.HORIZONTAL)
        }
    }

    inner class DownloadReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (downloadId != -1L) {
                    val itemKey = viewModel.downloadMap[downloadId]
                    Log.d(TAG, "suck: Tải mô hình $itemKey thành công")
                    viewModel.updateItemDownloadState(itemKey, DOWNLOAD_STATUS.DOWNLOADED,
                        onSuccessAction = {
                            binding.cvView3d.submitList(viewModel.listItem)
                        }
                    )
                }
            }
        }
    }
}
