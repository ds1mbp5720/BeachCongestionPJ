package com.lee.rest.beachcongestionpj

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
    private  var nowPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also{
            setContentView(it.root)
        }
        binding.progressBar.visibility = View.VISIBLE // 로딩 보이기
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
                                    var inbeachList = ArrayList<BeachInfo>()
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
                                        beach.congestion = jsonObject.getString("congestion")
                                        inbeachList.add(beach)
                                    }

                                    adapter = BeachAdapter(inbeachList)
                                    settingImageClick(adapter as BeachAdapter, inbeachList)
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
            }
        })
    }

}
