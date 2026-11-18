package io.github.rosyidputrar.kembar.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.rosyidputrar.kembar.BuildConfig
import io.github.rosyidputrar.kembar.core.common.app.AppBuildInfo
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providesAppBuildInfo(): AppBuildInfo = object : AppBuildInfo {
        override val versionName: String = BuildConfig.VERSION_NAME
        override val versionCode: Int = BuildConfig.VERSION_CODE
        override val isDebug: Boolean = BuildConfig.DEBUG
    }
}
