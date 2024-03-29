package com.vp.list

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vp.list.model.ListItem
import com.vp.list.model.MovieItem
import com.vp.list.viewmodel.ListState
import com.vp.list.viewmodel.ListViewModel
import com.vp.list.viewmodel.SearchResult
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_list.*
import java.lang.UnsupportedOperationException
import javax.inject.Inject

class ListFragment : Fragment() {

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory

    private lateinit var listViewModel: ListViewModel
    private var gridPagingScrollListener: GridPagingScrollListener? = null
    private var listAdapter: ListAdapter? = null

    private var currentQuery: String = "Interview"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        listViewModel = ViewModelProviders.of(this, factory).get(ListViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            currentQuery = savedInstanceState.getString(CURRENT_QUERY)
        }

        initBottomNavigation()
        initList()
        initSwipeRefreshLayout()

        listViewModel.observeMovies().observe(this, Observer { searchResult ->
            if (searchResult != null) {
                handleResult(listAdapter, searchResult)
            }
        })
        listViewModel.searchMoviesByTitle(currentQuery, 1)
        showProgressBar()
    }

    private fun initSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener { listViewModel.searchMoviesByTitle(currentQuery, 1) }
    }

    private fun initBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.favorites) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("app://movies/favorites"))
                intent.setPackage(requireContext().packageName)
                startActivity(intent)
            }
            true
        }
    }

    private fun initList() {
        listAdapter = ListAdapter()
        listAdapter?.onItemClickListener = { imdbID ->
            openDetails(imdbID)
        }
        recyclerView.adapter = listAdapter
        recyclerView.setHasFixedSize(true)

        val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            GRID_PORTRAIT_ORIENTATION_SPAN_COUNT
        } else GRID_LANDSCAPE_ORIENTATION_SPAN_COUNT

        val layoutManager = GridLayoutManager(context, spanCount).also {

            it.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (listAdapter?.getItemViewType(position)) {
                        ListItem.PROGRESS_ITEM -> GRID_DATA_SPAN_COUNT
                        ListItem.DATA_ITEM -> GRID_PROGRESS_SPAN_COUNT //number of columns of the grid
                        else -> throw UnsupportedOperationException()
                    }
                }
            }
        }

        recyclerView.layoutManager = layoutManager

        // Pagination
        gridPagingScrollListener = GridPagingScrollListener(layoutManager).also {
            it.loadMoreItemsListener = { page ->
                it.markLoading(true)
                listAdapter?.showLoadingIndicator()
                listViewModel.searchMoviesByTitle(currentQuery, page)
            }
        }
        gridPagingScrollListener?.let {
            recyclerView.addOnScrollListener(it)
        }
    }

    private fun showProgressBar() {
        viewAnimator.displayedChild = viewAnimator.indexOfChild(progressBar)
    }

    private fun showList() {
        viewAnimator.displayedChild = viewAnimator.indexOfChild(swipeRefreshLayout)
    }

    private fun showError() {
        viewAnimator.displayedChild = viewAnimator.indexOfChild(errorText)
    }

    private fun hideRefreshingProgress() {
        swipeRefreshLayout.isRefreshing = false
    }

    private fun handleResult(listAdapter: ListAdapter?, searchResult: SearchResult) {
        hideRefreshingProgress()
        listAdapter?.hideLoadingIndicator()
        when (searchResult.listState) {
            ListState.LOADED -> {
                setItemsData(listAdapter, searchResult)
                showList()
            }
            ListState.IN_PROGRESS -> {
                showProgressBar()
            }
            else -> {
                showError()
            }
        }
        gridPagingScrollListener?.markLoading(false)
    }

    private fun setItemsData(listAdapter: ListAdapter?, searchResult: SearchResult) {
        listAdapter?.let { adapter ->
            adapter.items = searchResult.items.map {
                MovieItem(it)
            }.toMutableList()
            if (searchResult.totalResult <= adapter.itemCount) {
                gridPagingScrollListener?.markLastPage(true)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CURRENT_QUERY, currentQuery)
    }

    fun submitSearchQuery(query: String) {
        currentQuery = query
        listAdapter?.clearItems()
        listViewModel.searchMoviesByTitle(query, 1)
        showProgressBar()
    }

    private fun openDetails(imdbID: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("app://movies/detail?imdbID=$imdbID"))
        intent.setPackage(requireContext().packageName)
        startActivity(intent)
    }

    companion object {
        const val TAG = "ListFragment"
        private const val CURRENT_QUERY = "current_query"
        private const val GRID_PORTRAIT_ORIENTATION_SPAN_COUNT = 2
        private const val GRID_LANDSCAPE_ORIENTATION_SPAN_COUNT = 3
        private const val GRID_DATA_SPAN_COUNT = 2
        private const val GRID_PROGRESS_SPAN_COUNT = 1
    }
}
