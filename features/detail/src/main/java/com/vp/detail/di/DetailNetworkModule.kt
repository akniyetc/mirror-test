package com.vp.detail.di

import com.vp.detail.service.DetailService

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class DetailNetworkModule {

    @Provides
    internal fun providesDetailService(retrofit: Retrofit): DetailService {
        return retrofit.create(DetailService::class.java)
    }
}