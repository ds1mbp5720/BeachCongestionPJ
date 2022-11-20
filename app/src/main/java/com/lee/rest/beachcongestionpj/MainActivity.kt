package com.lee.rest.beachcongestionpj

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.lee.rest.beachcongestionpj.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.MessageDigest


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    //layout 세팅
    private lateinit var binding: ActivityMainBinding
    private var inbeachList = ArrayList<BeachCongestionInfo>() // json 혼잡도 정보 저장용 리스트
    private var filteredBeachList = ArrayList<CombineBeachInfo>() // 검색 후 필터링된 바다 리스트
    private var beachInfoList = ArrayList<BeachInfo>() // json 바다 정보 저장용
    private var combineBeachList = ArrayList<CombineBeachInfo>()  // 두개 json 합친 리스트
    private var nowPosition = 0 // 현재 페이지 값
    private var findBeach = "" // 검색어 저장용
    private lateinit var geoCoder: Geocoder
    private lateinit var communicationAdapter: BeachAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also{
            setContentView(it.root)
        }
        //카카오 map 연결 및 생성
        MapSetting()

        geoCoder = Geocoder(this) // 주소 지도에 표시용 지오코더

        with(binding){
            progressBar.visibility = View.VISIBLE // 로딩 보이기
        }
        // 신호등 투명도 30%(4D)로 설정
        /*binding.redLight.setColorFilter(Color.parseColor("#4DFF0000"), PorterDuff.Mode.SRC_IN)
        binding.greenLight.setColorFilter(Color.parseColor("#4D00FF00"), PorterDuff.Mode.SRC_IN)
        binding.yellowLight.setColorFilter(Color.parseColor("#4DFFFF00"), PorterDuff.Mode.SRC_IN)*/

        /**
         * 현재 viewpager 상태 값 확인 후 신호등 표시
         * 화면 떠날때 unregister on page changecallback 해줘야함, 옵저버  /
         * inner class로해서 ondestroy 부분에서 처리, 브로드캐스트
         * */
        binding.beachList.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                nowPosition = position
                if(filteredBeachList.size>0){ // 필터링된 배열 null이 아닐시
                    congestionCheck(filteredBeachList[nowPosition].congestion) // 검색 반영 혼잡도 출력
                    mapFocusToBeach() // 마커, 지도 포커스 설정 함수
                }
            }
        })
        coroutineCallRetrofit() // retrofit 동작 함수

        /**
         * 해수욕장 검색 기능
         */
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                //키보드에서 검색 버튼 누를시
                findBeach = ""
                return false
            }
            //검색어가 입력되는 순간
            override fun onQueryTextChange(newText: String?): Boolean {
                findBeach = newText.toString()
                communicationAdapter.filter.filter(newText)
                filteredBeachList = communicationAdapter.getFilterBeach()
                Log.e(TAG,"검색어: $findBeach")
                return false
            }
        })
        getAppKeyHash() // 키 확인
    }
    /**
     * 키 연결용 해쉬키 확인 함수
     */
    fun getAppKeyHash() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (i in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(i.toByteArray())

                val something = String(Base64.encode(md.digest(), 0)!!)
                Log.e("Debug key", something)
            }
        } catch (e: Exception) {
            Log.e("Not found", e.toString())
        }
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
                                filteredBeachList = communicationAdapter.getFilterBeach()
                                settingImageClick(adapter as BeachAdapter, filteredBeachList) // 클릭이벤트 설정
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
    private fun settingImageClick(ImageClickAdapter: BeachAdapter, beaches: ArrayList<CombineBeachInfo>){
        ImageClickAdapter.setItemClickListener(object : BeachAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                var checkSearch:String // 검색 구분
                //해수욕장이름으로 구글지도 띄우는 코드
                checkSearch = beaches[position].poiNm
                val gmmIntentUri = Uri.parse("geo:37,127?q=" +
                        Uri.encode(checkSearch))
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
    @SuppressLint("Deprecated")
    private fun congestionCheck(congestion: String){
        when (congestion) {
            /*binding.redLight.setColorFilter(Color.parseColor("#4DFF0000"), PorterDuff.Mode.SRC_IN)
            binding.greenLight.setColorFilter(Color.parseColor("#4D00FF00"), PorterDuff.Mode.SRC_IN)
            binding.yellowLight.setColorFilter(Color.parseColor("#4DFFFF00"), PorterDuff.Mode.SRC_IN)*/
            "1" -> {
                //binding.greenLight.setColorFilter(Color.parseColor("#00FF00"), PorterDuff.Mode.SRC_IN)
                binding.congestionIcon.setColorFilter(resources.getColor(R.color.green))
                println("진입")
            }
            "2" -> {
                //binding.yellowLight.setColorFilter(Color.parseColor("#FFFF00"), PorterDuff.Mode.SRC_IN)
                binding.congestionIcon.setColorFilter(resources.getColor(R.color.yellow))
            }
            else -> {
                //binding.redLight.setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.SRC_IN)
                binding.congestionIcon.setColorFilter(resources.getColor(R.color.red))
            }
        }
    }
    //카카오 지도 설정
    private fun MapSetting() {
        with(binding) {
            beachMap.setDaumMapApiKey(DAUM_MAPS_ANDROID_APP_API_KEY)
            beachMap.mapType = MapView.MapType.Standard
            beachMap.setZoomLevel(7, true)
            beachMap.zoomIn(true)
            beachMap.zoomOut(true)
        }
    }
    // 해수욕장에 따라 지도 위치 변경
    private fun mapFocusToBeach(){
        var marker = MapPOIItem() // 현 해수욕장 마커
        marker.itemName = "${filteredBeachList[nowPosition].poiNm}" // 마커 터치시 나오는 이름
        marker.markerType = MapPOIItem.MarkerType.BluePin // 마커 색
        // 해수욕장 이름을 좌표로 바꾸는 geocoder, 현재 해수욕장 이름에 정확한 검색을 위한 단어 추가
        var cor = geoCoder.getFromLocationName(filteredBeachList[nowPosition].poiNm + " 해수욕장",1)
        if(cor?.isEmpty() == true){ // 해수욕장으로 안될시 해변으로
            cor = geoCoder.getFromLocationName(filteredBeachList[nowPosition].poiNm + "해변",1)
        }
        println("좌표값: $cor") // 결과 확인용 출력문
        with(binding.beachMap){
            if(cor?.isNotEmpty() == true){ // 좌표 정상 획득시
                setMapCenterPoint(MapPoint.mapPointWithGeoCoord(cor[0].latitude, cor[0].longitude), true)
                marker.mapPoint =MapPoint.mapPointWithGeoCoord(cor[0].latitude, cor[0].longitude) // 마커도 해당 위치에 셋팅
            }
            removeAllPOIItems() // 기존 마커 제거
            addPOIItem(marker) // 현 마커 추가
        }


    }
}
