package com.cardvr.app.di

import com.cardvr.app.data.network.DvrTcpClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDvrTcpClient(): DvrTcpClient = DvrTcpClient()
}
