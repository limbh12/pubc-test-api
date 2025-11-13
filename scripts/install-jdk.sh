#!/bin/bash

###############################################################################
# JDK 1.8 로컬 설치 스크립트
# 목적: 프로젝트 디렉토리 내에 JDK 1.8을 독립적으로 설치
# 시스템 JDK와 충돌 없이 사용 가능
###############################################################################

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
JDK_DIR="$PROJECT_ROOT/env/jdk"
JDK_VERSION="8.0.472"
ZULU_VERSION="8.90.0.19"

echo "=========================================="
echo "JDK 1.8 로컬 설치 시작 (Azul Zulu)"
echo "=========================================="
echo "프로젝트 루트: $PROJECT_ROOT"
echo "JDK 설치 경로: $JDK_DIR"
echo ""

# OS 감지
OS_TYPE="$(uname -s)"
case "$OS_TYPE" in
    Darwin*)
        OS="macos"
        ARCH="$(uname -m)"
        if [ "$ARCH" = "arm64" ]; then
            echo "Apple Silicon (M1/M2) 감지"
            JDK_ARCH="aarch64"
            JDK_FILE="zulu${ZULU_VERSION}-ca-jdk${JDK_VERSION}-macosx_aarch64.tar.gz"
            JDK_URL="https://cdn.azul.com/zulu/bin/${JDK_FILE}"
        else
            echo "Intel Mac 감지"
            JDK_ARCH="x64"
            JDK_FILE="zulu${ZULU_VERSION}-ca-jdk${JDK_VERSION}-macosx_x64.tar.gz"
            JDK_URL="https://cdn.azul.com/zulu/bin/${JDK_FILE}"
        fi
        ;;
    Linux*)
        OS="linux"
        JDK_ARCH="x64"
        JDK_FILE="zulu${ZULU_VERSION}-ca-jdk${JDK_VERSION}-linux_x64.tar.gz"
        JDK_URL="https://cdn.azul.com/zulu/bin/${JDK_FILE}"
        ;;
    *)
        echo "지원하지 않는 OS: $OS_TYPE"
        exit 1
        ;;
esac

echo "운영체제: $OS"
echo "아키텍처: $JDK_ARCH"
echo ""

# 이미 설치되어 있는지 확인 (Zulu 디렉토리 패턴)
if [ -d "$JDK_DIR/zulu${ZULU_VERSION}-ca-jdk${JDK_VERSION}"* ] || \
   [ -d "$JDK_DIR/jdk8u"* ] || \
   [ -d "$JDK_DIR/jdk1.8.0"* ]; then
    echo "JDK가 이미 설치되어 있습니다."

    # JAVA_HOME 찾기
    JAVA_HOME_PATH=$(find "$JDK_DIR" -mindepth 1 -maxdepth 1 -type d \( -name "zulu*" -o -name "jdk8u*" -o -name "jdk1.8.0*" \) | head -n 1)

    if [ -n "$JAVA_HOME_PATH" ] && [ -f "$JAVA_HOME_PATH/bin/java" ]; then
        JAVA_VERSION=$("$JAVA_HOME_PATH/bin/java" -version 2>&1 | head -n 1)
        echo "설치된 버전: $JAVA_VERSION"
        echo "설치 경로: $JAVA_HOME_PATH"
        echo ""
        echo "재설치하려면 먼저 다음 명령어로 삭제하세요:"
        echo "  rm -rf $JDK_DIR/*"
        exit 0
    fi
fi

# JDK 다운로드 URL (Azul Zulu)
echo "다운로드 URL: $JDK_URL"

echo ""
echo "JDK 다운로드 중..."
echo ""

# 임시 디렉토리 생성
TEMP_DIR="$PROJECT_ROOT/env/.tmp"
mkdir -p "$TEMP_DIR"

# 다운로드
cd "$TEMP_DIR"
if command -v curl &> /dev/null; then
    curl -L -o "$JDK_FILE" "$JDK_URL"
elif command -v wget &> /dev/null; then
    wget -O "$JDK_FILE" "$JDK_URL"
else
    echo "오류: curl 또는 wget이 필요합니다."
    exit 1
fi

echo ""
echo "JDK 압축 해제 중..."

# 압축 해제
tar -xzf "$JDK_FILE" -C "$JDK_DIR"

# 원래 디렉토리로 돌아가기
cd "$PROJECT_ROOT"

# 정리
rm -rf "$TEMP_DIR"

echo ""
echo "=========================================="
echo "Azul Zulu JDK 1.8 설치 완료!"
echo "=========================================="

# JAVA_HOME 경로 찾기 (Zulu 디렉토리 패턴)
JAVA_HOME_PATH=$(find "$JDK_DIR" -mindepth 1 -maxdepth 1 -type d \( -name "zulu*" -o -name "jdk*" \) | head -n 1)

if [ -n "$JAVA_HOME_PATH" ] && [ -f "$JAVA_HOME_PATH/bin/java" ]; then
    echo "설치 경로: $JAVA_HOME_PATH"
    echo ""

    # 버전 확인
    echo "Java 버전 확인:"
    "$JAVA_HOME_PATH/bin/java" -version

    echo ""
    echo "환경 변수 설정 방법:"
    echo "  source $PROJECT_ROOT/bin/setenv.sh"
    echo ""
else
    echo "오류: JDK 설치 경로를 찾을 수 없습니다."
    exit 1
fi
