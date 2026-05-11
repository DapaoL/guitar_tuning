package com.dp.truning.data.services

import com.dp.truning.data.model.ExampleModel
import retrofit2.Response
import retrofit2.http.*

interface AppService {

    /**
     * 获取 example result。
     */
    @GET("/api/v1/example")
    suspend fun getExampleResult(): Response<List<ExampleModel>>

}