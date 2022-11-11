package com.lee.rest.beachcongestionpj

data class CombineBeachInfo( // 두종류 data 합친 data
    var congestion: String,
    var poiNm: String,
    var capacity: Int,
    var areaName: String,
    var areaName2: String,
    var openingYmd: String,
    var closingYmd: String

)
data class BeachCongestionInfo( // 혼잡도 data call
    var congestion: String,
    var etlDt: String,
    var poiNm: String,
    var seqId: Int,
    var uniqPop: Int
    )

data class  BeachInfo(  // 바다 정보 data class
    var seqId: Int,
    var capacity: Int,  // 수용 가능 인원
    var area: Int,
    var beachName: String,
    var length: Int,
    var width: Int,
    var areaName: String,
    var areaName2: String,
    var openingYmd: String,
    var closingYmd: String,
    var useAt: String
)