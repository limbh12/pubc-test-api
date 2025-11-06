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
JDK_VERSION="8u422"

echo "=========================================="
echo "JDK 1.8 로컬 설치 시작"
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
            JDK_FILE="OpenJDK8U-jdk_aarch64_mac_hotspot_${JDK_VERSION}.tar.gz"
        else
            echo "Intel Mac 감지"
            JDK_ARCH="x64"
            JDK_FILE="OpenJDK8U-jdk_x64_mac_hotspot_${JDK_VERSION}.tar.gz"
        fi
        ;;
    Linux*)
        OS="linux"
        JDK_ARCH="x64"
        JDK_FILE="OpenJDK8U-jdk_x64_linux_hotspot_${JDK_VERSION}.tar.gz"
        ;;
    *)
        echo "지원하지 않는 OS: $OS_TYPE"
        exit 1
        ;;
esac

echo "운영체제: $OS"
echo "아키텍처: $JDK_ARCH"
echo ""

# 이미 설치되어 있는지 확인
if [ -d "$JDK_DIR/jdk8u422-b05" ] || [ -d "$JDK_DIR/jdk1.8.0_422" ]; then
    echo "JDK가 이미 설치되어 있습니다."

    # JAVA_HOME 찾기
    if [ -d "$JDK_DIR/jdk8u422-b05" ]; then
        JAVA_HOME_PATH="$JDK_DIR/jdk8u422-b05"
    else
        JAVA_HOME_PATH="$JDK_DIR/jdk1.8.0_422"
    fi

    if [ -f "$JAVA_HOME_PATH/bin/java" ]; then
        JAVA_VERSION=$("$JAVA_HOME_PATH/bin/java" -version 2>&1 | head -n 1)
        echo "설치된 버전: $JAVA_VERSION"
        echo ""
        echo "재설치하려면 먼저 다음 명령어로 삭제하세요:"
        echo "  rm -rf $JDK_DIR/*"
        exit 0
    fi
fi

# JDK 다운로드 URL (Adoptium Eclipse Temurin)
JDK_URL="https://github.com/adoptium/temurin8-binaries/releases/download/jdk${JDK_VERSION}-b05/${JDK_FILE}"

echo "JDK 다운로드 중..."
echo "URL: $JDK_URL"
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

# 정리
rm -rf "$TEMP_DIR"

echo ""
echo "=========================================="
echo "JDK 1.8 설치 완료!"
echo "=========================================="

# JAVA_HOME 경로 찾기
if [ -d "$JDK_DIR/jdk8u422-b05" ]; then
    JAVA_HOME_PATH="$JDK_DIR/jdk8u422-b05"
elif [ -d "$JDK_DIR/jdk1.8.0_422" ]; then
    JAVA_HOME_PATH="$JDK_DIR/jdk1.8.0_422"
else
    # 첫 번째 jdk* 디렉토리 찾기
    JAVA_HOME_PATH=$(find "$JDK_DIR" -maxdepth 1 -type d -name "jdk*" | head -n 1)
fi

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
