package com.lee.rest.beachcongestionpj

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lee.rest.beachcongestionpj.databinding.BeachAdapterLayoutBinding

const val TARGET_ADDRESS = "https://www.tournmaster.com/seantour_map/travel/" // 바꿔주기

class BeachViewHolder(val binding: BeachAdapterLayoutBinding) : RecyclerView.ViewHolder(binding.root)

class BeachAdapter(private val beaches: List<CombineBeachInfo>) : RecyclerView.Adapter<BeachViewHolder>(), Filterable {

    private var filteredBeach = ArrayList<CombineBeachInfo>() // 검색으로 필터링된 리스트
    var itemFiler = ItemFilter()

    private lateinit var nowBeach : CombineBeachInfo

    init {
        filteredBeach.addAll(beaches)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeachViewHolder {
        val binding = BeachAdapterLayoutBinding.inflate(
            LayoutInflater.from(parent.context)
            , parent, false)
        filteredBeach.addAll(beaches)
        return BeachViewHolder(binding)
    }
    override fun onBindViewHolder(holder: BeachViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val beach = filteredBeach[position]
        holder.binding.name.text = beach.poiNm
        holder.binding.address.text = beach.areaName + " " + beach.areaName2
        holder.binding.usingday.text = "개장기간: " + beach.openingYmd + " ~ " + beach.closingYmd
        holder.binding.usingPeopleNum.text = "최대 이용객: " + beach.capacity
        //Glide.with(holder.itemView.context).load("""$TARGET_ADDRESS${movie.poiNm}""").into(holder.binding.imageview)
        // 이미지 클릭 이벤트 만들기
        holder.itemView.setOnClickListener{
            itemClickListener.onClick(it,position)
        }
        nowBeach = beach
    }


    override fun getItemId(position: Int): Long {
        return filteredBeach[position].congestion.toLong()
    }
    override fun getItemCount(): Int {
        return filteredBeach.size
    }
    fun getNowBeaches(): CombineBeachInfo{
        return nowBeach
    }
    // 클릭 반응을 받기위한 함수들
    interface OnItemClickListener{
        fun onClick(v: View, position: Int)
    }
    fun setItemClickListener(onItemClickListener: BeachAdapter.OnItemClickListener){
        this. itemClickListener = onItemClickListener
    }
    private lateinit var itemClickListener: OnItemClickListener

    //검색에 대한 필터링 함수
    override fun getFilter(): Filter {
        return itemFiler
    }
    inner class ItemFilter: Filter(){
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filterString = constraint.toString()
            val results = FilterResults()
            Log.d(TAG, "charSequence : $constraint")

            //검색이 필요없을 경우를 위해 원본 배열을 복제
            val filteredList: ArrayList<CombineBeachInfo> = ArrayList<CombineBeachInfo>()
            //공백제외 아무런 값이 없을 경우 -> 원본 배열
            if (filterString.trim { it <= ' ' }.isEmpty()) {
                results.values = beaches
                results.count = beaches.size

                return results
                //공백제외 2글자 이인 경우 -> 이름으로만 검색
            } else if (filterString.trim { it <= ' ' }.length <= 2) {
                for (i in beaches) {
                    if (i.poiNm.contains(filterString)) {
                        filteredList.add(i)
                    }
                }
            }
            results.values = filteredList
            results.count = filteredList.size

            return results
        }
        @SuppressLint("NotifyDataSetChanged")
        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            filteredBeach.clear()
            filteredBeach.addAll(results.values as ArrayList<CombineBeachInfo>)
            notifyDataSetChanged()
        }
    }
}