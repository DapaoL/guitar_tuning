package com.dp.truning.di.repository

import com.dp.truning.data.dao.ExampleDao
import com.dp.truning.data.repository.ExampleRepositoryImp
import com.dp.truning.data.services.AppService
import com.dp.truning.domain.repository.ExampleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import retrofit2.Retrofit


@Module
@InstallIn(ViewModelComponent::class)
class AppModule {

    /**
     * 提供 app service。
     */
    @Provides
    internal fun provideAppService(retrofit: Retrofit): AppService {
        return retrofit.create(AppService::class.java)
    }

    /**
     * 提供 example repository。
     */
    @Provides
    internal fun provideExampleRepository(appService: AppService, exampleDao: ExampleDao): ExampleRepository {
        return ExampleRepositoryImp(appService, exampleDao)
    }
}