package com.lee.rest.beachcongestionpj

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.lee.rest.beachcongestionpj.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    //layout 세팅
    private lateinit var binding: ActivityMainBinding
    private var inbeachList = ArrayList<BeachCongestionInfo>() // json 혼잡도 정보 저장용 리스트
    private var beachInfoList = ArrayList<BeachInfo>() // json 바다 정보 저장용
    private var combineBeachList = ArrayList<CombineBeachInfo>()  // 두개 json 합친 리스트
    private var nowPosition = 0 // 현재 페이지 값
    private lateinit var communicationAdapter: BeachAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also{
            setContentView(it.root)
        }
        binding.progressBar.visibility = View.VISIBLE // 로딩 보이기
        // 신호등 투명도 30%(4D)로 설정
        binding.redLight.setColorFilter(Color.parseColor("#4DFF0000"), PorterDuff.Mode.SRC_IN)
        binding.greenLight.setColorFilter(Color.parseColor("#4D00FF00"), PorterDuff.Mode.SRC_IN)
        binding.yellowLight.setColorFilter(Color.parseColor("#4DFFFF00"), PorterDuff.Mode.SRC_IN)

        /**
         * 현재 viewpager 상태 값 확인 후 신호등 표시
         * 화면 떠날때 unregister on page changecallback 해줘야함, 옵저버  /
         * inner class로해서 ondestroy 부분에서 처리, 브로드캐스트
         * */
        binding.beachList.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                nowPosition = position
                congestionCheck(inbeachList[nowPosition].congestion)
            }
        })
        coroutineCallRetrofit() // retrofit 동작 함수
    }

    // 현재 상태 저장
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("position",nowPosition) // 현재 표시 위치 저장
    }

    // 저장한 값들 다시 호출
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val rePosition = savedInstanceState.getInt("position")
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }

    private var job: Job? = null
    private fun coroutineCallRetrofit() {
        job = CoroutineScope(Dispatchers.IO).launch {
            var response = RetrofitCoroutineService.getInstance()
            withContext(Dispatchers.Main) {
                    with(binding.beachList) {
                        response.beachInfo().enqueue(object : Callback<ResponseBody>{
                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>
                            ) {
                                beachInfoList.add(BeachInfo( seqId = 0, capacity = 0, area = 0, beachName = "",
                                    length = 0, width = 0, areaName = "", areaName2 = "", openingYmd = "",
                                    closingYmd = "", useAt = ""))  // 초기 데이터 명이 1부터 시작해서 조정
                                try {
                                    var beachSize2 = 273
                                    val str2 = response.body()!!.string() as String
                                    var jsonBeach2 =  JSONObject(str2)
                                    // 각 바다 정보들 리스트로 변경
                                    for(i in 1 .. beachSize2){
                                        val beach = BeachInfo( seqId = 0, capacity = 0, area = 0, beachName = "",
                                        length = 0, width = 0, areaName = "", areaName2 = "", openingYmd = "",
                                        closingYmd = "", useAt = "")
                                        var jsonObject2 = jsonBeach2.getJSONObject("Jnumber$i")
                                        beach.seqId = jsonObject2.getInt("seqId") // 바다정보 api 랑 연결고리
                                        beach.capacity = jsonObject2.getInt("capacity")
                                        beach.area = jsonObject2.getInt("area")
                                        beach.beachName = jsonObject2.getString("beachName")
                                        beach.length = jsonObject2.getInt("length")
                                        beach.width = jsonObject2.getInt("width")
                                        beach.areaName = jsonObject2.getString("areaName")
                                        beach.areaName2 = jsonObject2.getString("areaName2")
                                        beach.openingYmd = jsonObject2.getString("openingYmd")
                                        beach.closingYmd = jsonObject2.getString("closingYmd")
                                        beach.useAt = jsonObject2.getString("useAt")

                                        beachInfoList.add(beach)
                                    }
                                }catch (e:Exception){
                                }
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            }
                        })

                        response.beachCongestion().enqueue(object: Callback<ResponseBody> {
                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                binding.progressBar.visibility = View.GONE
                                try {
                                    var beachSize = 50
                                    val str = response.body()!!.string() as String
                                    var jsonBeach =  JSONObject(str)
                                    // 각 바다 정보들 리스트로 변경
                                    for(i in 0 until beachSize){
                                        val beach = BeachCongestionInfo(congestion = "", etlDt = "", poiNm = "", seqId = -1, uniqPop = -1)
                                        var jsonObject = jsonBeach.getJSONObject("Beach$i")
                                        beach.seqId = jsonObject.getInt("seqId") // 바다정보 api 랑 연결고리
                                        beach.uniqPop = jsonObject.getInt("uniqPop")
                                        beach.etlDt = jsonObject.getString("etlDt")
                                        beach.poiNm = jsonObject.getString("poiNm")
                                        //beach.congestion = jsonObject.getString("$")  // openapi에서 혼잡도 받기
                                        beach.congestion = "${(1..3).random()}"  // 랜덤으로 혼잡도 1~3 생성
                                        inbeachList.add(beach)
                                    }
                                    println(inbeachList) // 내부 값들 확인

                                }catch (e:Exception){ }
                                combineBeachData()
                                print("바다 정보")
                                println(beachInfoList) // 내부 값들 확인
                                print("혼잡도 정보")
                                println(inbeachList)
                                print("윱합 데이터 정보")
                                println(combineBeachList)
                                adapter = BeachAdapter(combineBeachList)
                                communicationAdapter = adapter as BeachAdapter
                                settingImageClick(adapter as BeachAdapter, inbeachList) // 클릭이벤트 설정
                            }
                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                binding.progressBar.visibility = View.GONE
                            }
                        })

                    }
                Log.e("TAG","성공")
                binding.progressBar.visibility = View.INVISIBLE

            }
        }
    }
    /**
     * data 두개 융합 함수
     */
    fun combineBeachData(){
        for(i in 0..49){
            val beach = CombineBeachInfo("","",0,"","","","")
            beach.congestion = inbeachList[i].congestion
            beach.poiNm = inbeachList[i].poiNm
            beach.capacity = beachInfoList[inbeachList[i].seqId].capacity
            beach.areaName = beachInfoList[inbeachList[i].seqId].areaName
            beach.areaName2 = beachInfoList[inbeachList[i].seqId].areaName2
            beach.openingYmd = beachInfoList[inbeachList[i].seqId].openingYmd
            beach.closingYmd = beachInfoList[inbeachList[i].seqId].closingYmd

            combineBeachList.add(beach)
        }
    }
    /**
     * 이미지 클릭시 구글맵 띄우는 함수
     */
    private fun settingImageClick(ImageClickAdapter: BeachAdapter, beaches: ArrayList<BeachCongestionInfo>){
        ImageClickAdapter.setItemClickListener(object : BeachAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                //해수욕장이름으로 구글지도 띄우는 코드
                val gmmIntentUri = Uri.parse("geo:37,127?q=" +
                        Uri.encode(beaches[position].poiNm))
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            // 현재지도 끄면 위치 초기화됨
            }
        })
    }

    /**
     * 혼잡도 확인하여 신호 반영 함수
     */
    private fun congestionCheck(congestion: String){
        when (congestion) {
            "1" -> {
                binding.greenLight.setColorFilter(Color.parseColor("#00FF00"), PorterDuff.Mode.SRC_IN)
                binding.redLight.setColorFilter(Color.parseColor("#4DFF0000"), PorterDuff.Mode.SRC_IN)
                binding.yellowLight.setColorFilter(Color.parseColor("#4DFFFF00"), PorterDuff.Mode.SRC_IN)
            }
            "2" -> {
                binding.yellowLight.setColorFilter(Color.parseColor("#FFFF00"), PorterDuff.Mode.SRC_IN)
                binding.redLight.setColorFilter(Color.parseColor("#4DFF0000"), PorterDuff.Mode.SRC_IN)
                binding.greenLight.setColorFilter(Color.parseColor("#4D00FF00"), PorterDuff.Mode.SRC_IN)
            }
            else -> {
                binding.redLight.setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.SRC_IN)
                binding.greenLight.setColorFilter(Color.parseColor("#4D00FF00"), PorterDuff.Mode.SRC_IN)
                binding.yellowLight.setColorFilter(Color.parseColor("#4DFFFF00"), PorterDuff.Mode.SRC_IN)
            }
        }
    }
}
