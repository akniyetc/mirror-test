package com.vp.detail

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.vp.detail.databinding.ActivityDetailBinding
import com.vp.detail.viewmodel.DetailsViewModel
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject
import kotlin.run

class DetailActivity : DaggerAppCompatActivity() {

    private lateinit var detailViewModel: DetailsViewModel

    private var menu: Menu? = null

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        detailViewModel = ViewModelProviders.of(this, factory).get(DetailsViewModel::class.java)
        binding.viewModel = detailViewModel
        binding.lifecycleOwner = this

        detailViewModel.fetchDetails(getMovieId())
        detailViewModel.title().observe(this, Observer {
            supportActionBar?.title = it
        })
        detailViewModel.isFavorite().observe(this, Observer { isFavorite ->
            if (isFavorite) {
                menu?.findItem(R.id.star)?.setIcon(R.drawable.ic_star_filled)
            } else {
                menu?.findItem(R.id.star)?.setIcon(R.drawable.ic_star)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        this.menu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.star -> {
                detailViewModel.favoriteClicked()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getMovieId(): String {
        return intent?.data?.getQueryParameter("imdbID") ?: run {
            throw IllegalStateException("You must provide movie id to display details")
        }
    }
}
