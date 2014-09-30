CAUPortalCrawler
================
## Description
중앙대 E-Class는 왜 push notification이 없는가? ->  없으면 만들면 되지! -> 일단 크롤링을 해야지 -> 어... 그냥 웹  크롤링이 안되네? ->  ajax가 걸림돌이 되면 웹뷰를 가지고 긁으면 되지! -> 그래서 만듭니다. 안드로이드 웹뷰로 E-Class  주기적으로 긁어서 변경점 알려주는 어플.

## Current State
 * State pattern 을 굳이 Activity 밖에 둘 이유가 없어서 다시 하나로 합침.
 * AlertDialog를 무시하는 방법을 알아내었더, 거기에 그걸 통해서 결과 값을 받아 올 수 있자는 사실도 알아냄.
 * 현재 자바스크립트 브릿지로 쓴 코드가 이상 작동해서 더 이상 진행이 안되고 있다.
 * http://portal.cau.ac.kr 을 이용하는 방법  
  * 웹뷰 로드와 javaScript 실행을 통해 이클래스 홈 -> 강의 게시판 홈 까지 입장하는데 성공.  
  * iFrame이 너무 많아 고통 받고 있음.  
 * http://cautis.cau.ac.kr/SMT/main.jsp 를 이용하는 방법 (모바일 페이지)  
  * 로그인에서 막힘. 로그인을 App에서 수행하는 바람에 뭐 할게 없다.
  * 일단 공식 포탈앱을 뒤져보니 로그인 결과를 '파일로' 저장하는건 확인 (뭐지 이거)
  * 하여간 로그인 기능을 구현하기 위해 .apk를 .jar로 변환, 리버스 엔지니어링을 시도했으나, 가장 중요한 로그인 부분만 코드가 안보이는 상황.
  * 데스크탑 페이지와, 모바일 페이지는 세션 호환 조차 이루어지지 않고 있는 상황. 음....
  * 쿠키 변조..?? 패킷 변조..?? 그런 수단 밖에 안 남았음. 근데 그걸 한다고 해서 될것 같지도 않고....
 
## Note
 * data/data/pkg_path/files/ ... 여기 매우 중요한게 있음.
 * 리버스 엔지니어링 하는 법 : http://stackoverflow.com/questions/12732882/reverse-engineering-from-an-apk-file-to-a-project
 * 
