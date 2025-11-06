#!/bin/bash

###############################################################################
# JBoss EAP 7.4 로컬 설치 스크립트
# 목적: 프로젝트 디렉토리 내에 JBoss EAP를 독립적으로 설치
# 시스템 JBoss와 충돌 없이 사용 가능
###############################################################################

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
JBOSS_DIR="$PROJECT_ROOT/env/jboss"
JBOSS_VERSION="7.4.0"

echo "=========================================="
echo "JBoss EAP 7.4 로컬 설치 시작"
echo "=========================================="
echo "프로젝트 루트: $PROJECT_ROOT"
echo "JBoss 설치 경로: $JBOSS_DIR"
echo ""

# JDK 확인
JAVA_HOME_LOCAL="$PROJECT_ROOT/env/jdk"
if [ -d "$JAVA_HOME_LOCAL/jdk8u422-b05" ]; then
    JAVA_HOME_PATH="$JAVA_HOME_LOCAL/jdk8u422-b05"
elif [ -d "$JAVA_HOME_LOCAL/jdk1.8.0_422" ]; then
    JAVA_HOME_PATH="$JAVA_HOME_LOCAL/jdk1.8.0_422"
else
    JAVA_HOME_PATH=$(find "$JAVA_HOME_LOCAL" -maxdepth 1 -type d -name "jdk*" | head -n 1)
fi

if [ -z "$JAVA_HOME_PATH" ] || [ ! -f "$JAVA_HOME_PATH/bin/java" ]; then
    echo "오류: 로컬 JDK를 찾을 수 없습니다."
    echo "먼저 다음 명령어로 JDK를 설치하세요:"
    echo "  ./scripts/install-jdk.sh"
    exit 1
fi

echo "로컬 JDK 경로: $JAVA_HOME_PATH"
JAVA_VERSION=$("$JAVA_HOME_PATH/bin/java" -version 2>&1 | head -n 1)
echo "Java 버전: $JAVA_VERSION"
echo ""

# 이미 설치되어 있는지 확인
if [ -d "$JBOSS_DIR/jboss-eap-7.4" ]; then
    echo "JBoss EAP가 이미 설치되어 있습니다."
    echo "설치 경로: $JBOSS_DIR/jboss-eap-7.4"
    echo ""
    echo "재설치하려면 먼저 다음 명령어로 삭제하세요:"
    echo "  rm -rf $JBOSS_DIR/*"
    exit 0
fi

echo "=========================================="
echo "JBoss EAP 다운로드 방법"
echo "=========================================="
echo ""
echo "⚠️  JBoss EAP는 Red Hat 구독이 필요한 상용 제품입니다."
echo ""
echo "다음 두 가지 방법 중 하나를 선택하세요:"
echo ""
echo "1. WildFly 사용 (무료 오픈소스)"
echo "   - JBoss EAP의 커뮤니티 버전"
echo "   - 다운로드: https://www.wildfly.org/downloads/"
echo "   - 권장 버전: WildFly 26.x (Jakarta EE 8 호환)"
echo ""
echo "2. JBoss EAP 사용 (상용 - 개발자 구독)"
echo "   - Red Hat 개발자 구독 (무료):"
echo "     https://developers.redhat.com/products/eap/download"
echo "   - 다운로드 후 다음 경로에 배치:"
echo "     $JBOSS_DIR/jboss-eap-7.4.zip"
echo ""
echo "=========================================="
echo ""

# 사용자 선택
read -p "어떤 방식을 선택하시겠습니까? (1: WildFly, 2: JBoss EAP 수동 다운로드, q: 종료): " choice

case $choice in
    1)
        echo ""
        echo "WildFly 26 다운로드 중..."
        WILDFLY_VERSION="26.1.3.Final"
        WILDFLY_FILE="wildfly-${WILDFLY_VERSION}.tar.gz"
        WILDFLY_URL="https://github.com/wildfly/wildfly/releases/download/${WILDFLY_VERSION}/${WILDFLY_FILE}"

        TEMP_DIR="$PROJECT_ROOT/env/.tmp"
        mkdir -p "$TEMP_DIR"

        cd "$TEMP_DIR"
        if command -v curl &> /dev/null; then
            curl -L -o "$WILDFLY_FILE" "$WILDFLY_URL"
        elif command -v wget &> /dev/null; then
            wget -O "$WILDFLY_FILE" "$WILDFLY_URL"
        else
            echo "오류: curl 또는 wget이 필요합니다."
            exit 1
        fi

        echo ""
        echo "WildFly 압축 해제 중..."
        tar -xzf "$WILDFLY_FILE" -C "$JBOSS_DIR"

        # 심볼릭 링크 생성 (jboss-eap-7.4 → wildfly-26.x)
        ln -sf "$JBOSS_DIR/wildfly-${WILDFLY_VERSION}" "$JBOSS_DIR/jboss-eap-7.4"

        rm -rf "$TEMP_DIR"

        echo ""
        echo "=========================================="
        echo "WildFly 26 설치 완료!"
        echo "=========================================="
        echo "설치 경로: $JBOSS_DIR/wildfly-${WILDFLY_VERSION}"
        echo "심볼릭 링크: $JBOSS_DIR/jboss-eap-7.4 → wildfly-${WILDFLY_VERSION}"
        ;;

    2)
        echo ""
        echo "JBoss EAP ZIP 파일 확인 중..."

        # ZIP 파일 찾기
        ZIP_FILE=$(find "$JBOSS_DIR" -maxdepth 1 -name "jboss-eap-*.zip" | head -n 1)

        if [ -z "$ZIP_FILE" ]; then
            echo ""
            echo "오류: JBoss EAP ZIP 파일을 찾을 수 없습니다."
            echo ""
            echo "다음 단계를 수행하세요:"
            echo "1. https://developers.redhat.com/products/eap/download 접속"
            echo "2. Red Hat 계정으로 로그인 (무료 개발자 구독)"
            echo "3. JBoss EAP 7.4 ZIP 파일 다운로드"
            echo "4. 다운로드한 파일을 다음 경로로 이동:"
            echo "   $JBOSS_DIR/jboss-eap-7.4.0.zip"
            echo "5. 이 스크립트를 다시 실행"
            echo ""
            exit 1
        fi

        echo "ZIP 파일 발견: $ZIP_FILE"
        echo ""
        echo "JBoss EAP 압축 해제 중..."

        unzip -q "$ZIP_FILE" -d "$JBOSS_DIR"

        echo ""
        echo "=========================================="
        echo "JBoss EAP 7.4 설치 완료!"
        echo "=========================================="
        echo "설치 경로: $JBOSS_DIR/jboss-eap-7.4"
        ;;

    q|Q)
        echo "설치를 취소했습니다."
        exit 0
        ;;

    *)
        echo "잘못된 선택입니다."
        exit 1
        ;;
esac

echo ""
echo "관리자 사용자 생성 중..."
echo ""

# JBoss 홈 설정
if [ -L "$JBOSS_DIR/jboss-eap-7.4" ]; then
    JBOSS_HOME=$(readlink "$JBOSS_DIR/jboss-eap-7.4")
else
    JBOSS_HOME="$JBOSS_DIR/jboss-eap-7.4"
fi

# 관리자 사용자 생성
echo "기본 관리자 계정:"
echo "  Username: admin"
echo "  Password: admin123!"
echo ""

"$JBOSS_HOME/bin/add-user.sh" -u admin -p admin123! -s

echo ""
echo "=========================================="
echo "설치 완료 및 다음 단계"
echo "=========================================="
echo ""
echo "환경 변수 설정:"
echo "  source $PROJECT_ROOT/bin/setenv.sh"
echo ""
echo "JBoss 실행:"
echo "  $PROJECT_ROOT/bin/jboss-start.sh"
echo ""
echo "관리 콘솔 접속:"
echo "  http://localhost:9990"
echo "  Username: admin"
echo "  Password: admin123!"
echo ""
