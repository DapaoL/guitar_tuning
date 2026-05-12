package com.dp.guitartuning.ui.adapters

import com.dp.guitartuning.databinding.ListItemBinding
import com.dp.guitartuning.interfaces.ItemClickListener
import com.dp.guitartuning.ui.base.BaseAdapter

class ListAdapter(list: List<String>, itemClickListener: ItemClickListener) : BaseAdapter<ListItemBinding, String>(ListItemBinding::inflate, list, itemClickListener) {
    private val TAG = ListAdapter::class.java.simpleName

    /**
     * 绑定指定位置的数据到 ViewHolder。
     */
    override fun onBindViewHolder(holder: BaseViewHolder<ListItemBinding>, position: Int) {
        super.onBindViewHolder(holder, position)

    }

}

