package com.vp.list

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GridPagingScrollListener constructor(private val layoutManager: GridLayoutManager)
    : RecyclerView.OnScrollListener() {

    var loadMoreItemsListener: (Int) -> Unit = {}

    private var isLastPage = false
    private var isLoading = false

    private val isNotFirstPage: Boolean
        get() = layoutManager.findFirstVisibleItemPosition() >= 0 && layoutManager.itemCount >= PAGE_SIZE

    private val isNotLoadingInProgress: Boolean
        get() = !isLoading

    private val nextPageNumber: Int
        get() = layoutManager.itemCount / PAGE_SIZE + 1

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        if (shouldLoadNextPage()) {
            loadMoreItemsListener.invoke(nextPageNumber)
        }
    }

    private fun shouldLoadNextPage(): Boolean {
        return isNotLoadingInProgress && userScrollsToNextPage() && isNotFirstPage && hasNextPage()
    }

    private fun userScrollsToNextPage(): Boolean {
        return layoutManager.childCount + layoutManager.findFirstVisibleItemPosition() >= layoutManager.itemCount
    }

    private fun hasNextPage(): Boolean {
        return !isLastPage
    }

    fun markLoading(isLoading: Boolean) {
        this.isLoading = isLoading
    }

    fun markLastPage(isLastPage: Boolean) {
        this.isLastPage = isLastPage
    }

    companion object {
        private const val PAGE_SIZE = 10
    }
}
