<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PUBC Test API</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
        }
        .container {
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            padding: 40px;
            max-width: 800px;
            width: 100%;
        }
        h1 {
            color: #667eea;
            margin-bottom: 10px;
            font-size: 2.5em;
        }
        .subtitle {
            color: #666;
            margin-bottom: 30px;
            font-size: 1.1em;
        }
        .api-section {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 10px;
            margin: 20px 0;
        }
        .api-section h2 {
            color: #764ba2;
            margin-bottom: 15px;
            font-size: 1.5em;
        }
        .api-endpoint {
            background: white;
            padding: 15px;
            border-left: 4px solid #667eea;
            margin: 10px 0;
            border-radius: 5px;
        }
        .method {
            display: inline-block;
            background: #667eea;
            color: white;
            padding: 5px 10px;
            border-radius: 5px;
            font-weight: bold;
            font-size: 0.9em;
            margin-right: 10px;
        }
        .endpoint-url {
            font-family: 'Courier New', monospace;
            color: #333;
            font-size: 0.95em;
        }
        .description {
            color: #666;
            margin-top: 8px;
            font-size: 0.9em;
        }
        .service-keys {
            background: #fffbea;
            border-left: 4px solid #f59e0b;
            padding: 15px;
            margin: 20px 0;
            border-radius: 5px;
        }
        .service-keys h3 {
            color: #f59e0b;
            margin-bottom: 10px;
        }
        .key-list {
            font-family: 'Courier New', monospace;
            color: #333;
            line-height: 1.8;
        }
        .footer {
            text-align: center;
            margin-top: 30px;
            color: #999;
            font-size: 0.9em;
        }
        a {
            color: #667eea;
            text-decoration: none;
        }
        a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🏛️ PUBC Test API</h1>
        <p class="subtitle">문화시설 정보 조회 OpenAPI 테스트 프로젝트</p>

        <div class="api-section">
            <h2>📡 REST API Endpoints</h2>

            <div class="api-endpoint">
                <span class="method">GET</span>
                <span class="endpoint-url">/api/facilities</span>
                <p class="description">문화시설 목록 조회 (페이징, 필터링 지원)</p>
            </div>

            <div class="api-endpoint">
                <span class="method">GET</span>
                <span class="endpoint-url">/api/facilities/{facilityId}</span>
                <p class="description">문화시설 상세 조회</p>
            </div>

            <div class="api-endpoint">
                <span class="method">GET</span>
                <span class="endpoint-url">/api/facilities/types</span>
                <p class="description">문화시설 유형 목록 조회</p>
            </div>
        </div>

        <div class="service-keys">
            <h3>🔑 테스트 서비스 키 (Mock Mode)</h3>
            <div class="key-list">
                • TEST_KEY_001 - 테스트 사용자 1<br>
                • TEST_KEY_002 - 테스트 사용자 2<br>
                • DEMO_KEY - 데모 사용자<br>
                • DEV_KEY - 개발자 사용자
            </div>
        </div>

        <div class="api-section">
            <h2>📝 사용 예제</h2>
            <div class="api-endpoint">
                <p class="endpoint-url">
                    GET /api/facilities?serviceKey=TEST_KEY_001&amp;pageNum=1&amp;pageSize=10
                </p>
                <p class="description">서비스 키를 사용한 문화시설 목록 조회</p>
            </div>

            <div class="api-endpoint">
                <p class="endpoint-url">
                    GET /api/facilities/F001?serviceKey=TEST_KEY_001
                </p>
                <p class="description">특정 문화시설 상세 정보 조회</p>
            </div>
        </div>

        <div class="footer">
            <p>
                <a href="https://github.com/limbh12/pubc-test-api" target="_blank">GitHub Repository</a> |
                <a href="/services" target="_blank">CXF Services</a>
            </p>
            <p style="margin-top: 10px;">
                Built with Spring 4.3.30, Apache CXF 3.3.13, MyBatis 3.5.13 | JDK 1.8
            </p>
        </div>
    </div>
</body>
</html>
