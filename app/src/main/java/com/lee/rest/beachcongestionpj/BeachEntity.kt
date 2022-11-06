package com.lee.rest.beachcongestionpj

data class BeachEntity(
    var congestion: String = "",
    val etlDt: String = "",
    var poiNm: String = "",
    val seqId: Int = -1,
    val uniqPop: Int = -1
)