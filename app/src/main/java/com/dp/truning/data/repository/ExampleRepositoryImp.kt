package com.dp.truning.data.repository

import com.dp.truning.data.dao.ExampleDao
import com.dp.truning.data.services.AppService
import com.dp.truning.domain.repository.ExampleRepository
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