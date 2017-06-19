package com.topwu.petsadopt.util

object TransitionUtils {
    private val DEFAULT_TRANSITION_NAME = "transition"

    fun getItemPositionFromTransition(transitionName: String): Int {
        return Integer.parseInt(transitionName.substring(transitionName.length - 1))
    }

    fun getRecyclerViewTransitionName(position: Int): String {
        return DEFAULT_TRANSITION_NAME + position
    }
}