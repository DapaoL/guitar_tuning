package com.dp.guitartuning.data.repository

import com.dp.guitartuning.data.dao.ExampleDao
import com.dp.guitartuning.data.services.AppService
import com.dp.guitartuning.domain.repository.ExampleRepository
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ExampleRepositoryImp @Inject constructor(private val appService: AppService, private val exampleDao: ExampleDao) : ExampleRepository {
    /**
     * 处理 test 相关逻辑。
     */
    override suspend fun test() {


    }
}