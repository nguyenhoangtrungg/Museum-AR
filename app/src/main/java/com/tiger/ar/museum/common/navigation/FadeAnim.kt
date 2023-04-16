package com.tiger.ar.museum.common.navigation

class FadeAnim : IScreenAnim {
    override fun enter() = android.R.anim.fade_in
    override fun exit() = android.R.anim.fade_out
    override fun popEnter() = android.R.anim.fade_in
    override fun popExit() = android.R.anim.fade_out
}
