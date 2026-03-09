# API RESPONSE
````
{
"type": "https://stack-app.com/probs/auth",   // 에러 타입 URI
"title": "Unauthorized",                      // 에러 제목
"status": 401,                                // HTTP 상태 코드
"detail": "Full authentication is required",  // 상세 메시지
"instance": "/api/account",                    // 요청 경로 (path 대신 instance)
"errors": [ ... ]                             // 추가 에러 목록 (검증 실패 등)
}
````
