package com.dp.guitartuning.ui.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.dp.guitartuning.interfaces.ItemClickListener

abstract class BaseAdapter<VB : ViewBinding, M>(private val bindingFactory: (LayoutInflater, ViewGroup?, Boolean) -> VB, private val list: List<M>, private val itemClickListener: ItemClickListener) :
    RecyclerView.Adapter<BaseAdapter.BaseViewHolder<VB>>() {
    private val TAG = "BaseAdapter"

    class BaseViewHolder<VB : ViewBinding>(binding: VB) : RecyclerView.ViewHolder(binding.root) {
        var binding: VB

        init {
            this.binding = binding
        }
    }

    /**
     * 创建列表项对应的 ViewHolder。
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<VB> {
        val binding = bindingFactory(LayoutInflater.from(parent.context), parent, false)
        return BaseViewHolder(binding)
    }

    /**
     * 绑定指定位置的数据到 ViewHolder。
     */
    override fun onBindViewHolder(holder: BaseViewHolder<VB>, position: Int) {
        val device = list[position]
        holder.itemView.setOnClickListener {
            itemClickListener.onClick(device)
        }
    }

    /**
     * 返回当前列表的数据数量。
     */
    override fun getItemCount(): Int {
        return list.size
    }

}

