# BeachCongestionPJ
## 해수욕장 혼잡도 안내 App  
> openAPI 활용 국내 해수욕장 혼잡도를 신호등으로 안내합니다. 이미지 클릭시 googleMap으로 해당 해수욕장 위치를 알려줍니다.   
> 검색 기능을 추가하였습니다.('22.11.12)  
> Kotlin, retrofit, viewpager2, 
------------
### 프로젝트 목적
+ retrofit2를 활용 openApi에서 원하는 data 추출 및 활용  
+ recyclerView에 검색기능을 통한 filter 
+ (추가 예정)사진 대신 지도 출력  
+ UI 연습
------------
### 주요기능
+ 해수욕장 가로 슬라이드 형식으로 표시
+ 신호등(빨강, 초록, 노랑) 으로 혼잡도 표시
+ 이미지 터치시 googleMap에 해당 해수욕장 위치 제공
+ 검색을 통해 원하는 해수욕장 보기
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
![KakaoTalk_20221106_211307764](https://user-images.githubusercontent.com/37658906/200170132-6c00cce3-7fad-4437-80ca-ad4dfeed6123.jpg)

![KakaoTalk_20221112_184127516](https://user-images.githubusercontent.com/37658906/201468459-b953251a-4fcf-4117-b66a-a5f62862c8cc.jpg)
