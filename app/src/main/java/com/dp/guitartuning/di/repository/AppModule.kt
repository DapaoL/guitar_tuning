package com.dp.guitartuning.di.repository

import com.dp.guitartuning.data.dao.ExampleDao
import com.dp.guitartuning.data.repository.ExampleRepositoryImp
import com.dp.guitartuning.data.services.AppService
import com.dp.guitartuning.domain.repository.ExampleRepository
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