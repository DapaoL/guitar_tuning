package com.dp.guitartuning.data.services

import com.dp.guitartuning.data.model.ExampleModel
import retrofit2.Response
import retrofit2.http.*

interface AppService {

    /**
     * 获取 example result。
     */
    @GET("/api/v1/example")
    suspend fun getExampleResult(): Response<List<ExampleModel>>

}