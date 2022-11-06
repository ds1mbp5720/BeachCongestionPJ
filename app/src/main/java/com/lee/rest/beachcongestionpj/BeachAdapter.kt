package com.lee.rest.beachcongestionpj

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lee.rest.beachcongestionpj.databinding.BeachAdapterLayoutBinding

const val TARGET_ADDRESS = "https://www.tournmaster.com/seantour_map/travel/" // 바꿔주기

class BeachViewHolder(val binding: BeachAdapterLayoutBinding) : RecyclerView.ViewHolder(binding.root)

class BeachAdapter(private val beaches: List<BeachInfo>) : RecyclerView.Adapter<BeachViewHolder>() {
    private var thisPosition = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeachViewHolder {
        val binding = BeachAdapterLayoutBinding.inflate(
            LayoutInflater.from(parent.context)
            , parent, false)
        return BeachViewHolder(binding)
    }
    override fun onBindViewHolder(holder: BeachViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val beach = beaches[position]
        holder.binding.name.text = beach.poiNm
        thisPosition = position
        println("지금 위치는 $thisPosition")
        //Glide.with(holder.itemView.context).load("""$TARGET_ADDRESS${movie.poiNm}""").into(holder.binding.imageview)
        // 이미지 클릭 이벤트 만들기
        holder.itemView.setOnClickListener{
            itemClickListener.onClick(it,position)
        }
    }

    override fun getItemId(position: Int): Long {
        return beaches[position].congestion.toLong()
    }
    override fun getItemCount(): Int {
        return beaches.size
    }
    fun getPosition(): Int{
        return thisPosition
    }
    // 클릭 반응을 받기위한 함수들
    interface OnItemClickListener{
        fun onClick(v: View, position: Int)
    }
    fun setItemClickListener(onItemClickListener: BeachAdapter.OnItemClickListener){
        this. itemClickListener = onItemClickListener
    }
    private lateinit var itemClickListener: OnItemClickListener
}