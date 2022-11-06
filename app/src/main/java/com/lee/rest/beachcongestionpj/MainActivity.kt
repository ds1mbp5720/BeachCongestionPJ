package com.lee.rest.beachcongestionpj

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
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
    private var inbeachList = ArrayList<BeachInfo>() // json정보 저장용 리스트
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
    }

    override fun onResume() {
        super.onResume()
        coroutineCallRetrofit() // retrofit 동작 함수
    }

    private var job: Job? = null
    private fun coroutineCallRetrofit() {
        job = CoroutineScope(Dispatchers.IO).launch {
            var response = RetrofitCoroutineService.getInstance()
            withContext(Dispatchers.Main) {
                    with(binding.beachList) {
                        response.beachCongestion().enqueue(object: Callback<ResponseBody> {
                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                binding.progressBar.visibility = View.GONE
                                try {
                                    var beachSize = 50
                                    val str = response.body()!!.string() as String
                                    var jsonBeach =  JSONObject(str)
                                    // 각 바다 정보들 리스트로 변경
                                    for(i in 0 until beachSize){
                                        val beach = BeachInfo(congestion = "", etlDt = "", poiNm = "", seqId = -1, uniqPop = -1)
                                        var jsonObject = jsonBeach.getJSONObject("Beach$i")
                                        beach.seqId = jsonObject.getInt("seqId")
                                        beach.uniqPop = jsonObject.getInt("uniqPop")
                                        beach.etlDt = jsonObject.getString("etlDt")
                                        beach.poiNm = jsonObject.getString("poiNm")
                                        //beach.congestion = jsonObject.getString("$")  // openapi에서 혼잡도 받기
                                        beach.congestion = "${(1..3).random()}"  // 랜덤으로 혼잡도 1~3 생성
                                        inbeachList.add(beach)
                                    }
                                    println(inbeachList) // 내부 값들 확인
                                    adapter = BeachAdapter(inbeachList)
                                    communicationAdapter = adapter as BeachAdapter
                                    settingImageClick(adapter as BeachAdapter, inbeachList) // 클릭이벤트 설정

                                    /*congestionCheck((adapter as BeachAdapter).getPosition()) // 혼잡도 체크하여 신호 반영
                                    println((adapter as BeachAdapter).getPosition())*/

                                }catch (e:Exception){ }
                            }
                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                binding.progressBar.visibility = View.GONE
                            }
                        })

                    }
                    Log.e("TAG","성공")
                job?.cancel()
                binding.progressBar.visibility = View.INVISIBLE
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        var userAction = event?.action
        when(userAction){
            MotionEvent.ACTION_DOWN -> {
                congestionCheck(communicationAdapter.getPosition())
                println("변하는중 ${communicationAdapter.getPosition()}")
            }
        }
        return super.dispatchTouchEvent(event)
    }

    /**
     * 이미지 클릭시 구글맵 띄우는 함수
     */
    private fun settingImageClick(ImageClickAdapter: BeachAdapter, beaches: ArrayList<BeachInfo>){
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
    private fun congestionCheck(position: Int){
        when (inbeachList[position].congestion) {
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
