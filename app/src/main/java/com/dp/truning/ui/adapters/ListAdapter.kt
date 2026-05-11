package com.dp.truning.ui.adapters

import com.dp.truning.databinding.ListItemBinding
import com.dp.truning.interfaces.ItemClickListener
import com.dp.truning.ui.base.BaseAdapter

class ListAdapter(list: List<String>, itemClickListener: ItemClickListener) : BaseAdapter<ListItemBinding, String>(ListItemBinding::inflate, list, itemClickListener) {
    private val TAG = ListAdapter::class.java.simpleName

    /**
     * 绑定指定位置的数据到 ViewHolder。
     */
    override fun onBindViewHolder(holder: BaseViewHolder<ListItemBinding>, position: Int) {
        super.onBindViewHolder(holder, position)

    }

}

