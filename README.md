# BeachCongestionPJ
## 해수욕장 혼잡도 안내 App
> openAPI 활용 국내 해수욕장 혼잡도를 신호등으로 안내합니다. 이미지 클릭시 googleMap으로 해당 해수욕장 위치를 알려줍니다. 
> Kotlin, retrofit, viewpager2, 
------------
### 주요기능
+ 해수욕장 가로 슬라이드 형식으로 표시
+ 신호등(빨강, 초록, 노랑) 으로 혼잡도 표시
+ 이미지 터치시 googleMap에 해당 해수욕장 위치 제공
------------
### 설계
#### 1. 핵심 변수, 함수  
  - openApi의 각 해수욕장 저장용 data class
  
         data class BeachInfo( var congestion: String, var etlDt: String, var poiNm: String, var seqId: Int, var uniqPop: Int )        
      
  - coroutineCallRetrofit() : 코루틴을 동작시켜 open api를 읽어오고 변환 및 처리 함수
  
#### 2. 동작 방식
  - activity에 retroft2를 통해 정보 저장 및 adpter로 전달
  - adapter를 통해 viewpager2활용 해수욕장 정보 제공, 터치 이벤트 설정
  - main에 함수로 이미지 터치시 googlemap에 해당 해수욕장 위치 제공

------------
### 실행사진

