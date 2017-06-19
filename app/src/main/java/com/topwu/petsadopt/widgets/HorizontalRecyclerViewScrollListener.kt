package com.topwu.petsadopt.widgets

import android.support.v7.widget.RecyclerView

class HorizontalRecyclerViewScrollListener(private val listener: OnItemCoverListener) : RecyclerView.OnScrollListener() {
    private val OFFSET_RANGE = 50
    private val COVER_FACTOR = 0.7

    private var itemBounds = IntArray(0)

    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        if (recyclerView == null) {
            return
        }
        if (itemBounds.isEmpty()) {
            fillItemBounds(recyclerView.adapter.itemCount, recyclerView)
        }

        itemBounds.forEachIndexed { i, it ->
            if (isInChildItemsRange(recyclerView.computeHorizontalScrollOffset(), it, OFFSET_RANGE)) {
                listener.onItemCover(i)
            }
        }
    }

    private fun fillItemBounds(placesCount: Int, recyclerView: RecyclerView) {
        itemBounds = IntArray(placesCount)

        val childWidth = (recyclerView.computeHorizontalScrollRange() - recyclerView.computeHorizontalScrollExtent()) / placesCount
        for (i in 0..placesCount - 1) {
            itemBounds[i] = ((childWidth * i + childWidth * (i + 1)) / 2 * COVER_FACTOR).toInt()
        }
    }

    private fun isInChildItemsRange(offset: Int, itemBound: Int, range: Int): Boolean {
        val rangeMin = itemBound - range
        val rangeMax = itemBound + range
        return Math.min(rangeMin, rangeMax) <= offset && Math.max(rangeMin, rangeMax) >= offset
    }

    interface OnItemCoverListener {
        fun onItemCover(position: Int)
    }
}
