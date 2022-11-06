package com.lee.rest.beachcongestionpj

data class BeachInfo(
    var congestion: String,
    var etlDt: String,
    var poiNm: String,
    var seqId: Int,
    var uniqPop: Int
    )