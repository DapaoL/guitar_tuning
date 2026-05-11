package com.dp.truning.data.dao

import androidx.room.*
import com.dp.truning.data.entity.ExampleEntity

@Dao interface ExampleDao {

    /**
     * 获取 example data。
     */
    @Query("SELECT * FROM example_table")
    fun getExampleData(): List<ExampleEntity>

    /**
     * 处理 insert 相关逻辑。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(search: ExampleEntity?)

    /**
     * 处理 update 相关逻辑。
     */
    @Update
    suspend fun update(search: ExampleEntity)

    /**
     * 处理 delete 相关逻辑。
     */
    @Delete
    suspend fun delete(search: ExampleEntity)
    
}
