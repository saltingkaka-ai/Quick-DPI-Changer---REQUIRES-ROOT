#!/bin/bash

# Warna untuk output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

PROJECT_DIR="."
TOTAL_ERRORS=0
TOTAL_WARNINGS=0

echo -e "${BLUE}🔍 DPIChanger Project Validator${NC}"
echo -e "${BLUE}================================${NC}\n"

# Fungsi cek file exists
check_file() {
    local file=$1
    local required=$2
    
    if [ -f "$file" ]; then
        local size=$(stat -f%z "$file" 2>/dev/null || stat -c%s "$file" 2>/dev/null || echo "0")
        if [ "$size" -eq 0 ]; then
            if [ "$required" = "required" ]; then
                echo -e "${RED}❌ $file (KOSONG - WAJIB DIISI)${NC}"
                ((TOTAL_ERRORS++))
            else
                echo -e "${YELLOW}⚠️  $file (kosong)${NC}"
                ((TOTAL_WARNINGS++))
            fi
        else
            echo -e "${GREEN}✅ $file (${size} bytes)${NC}"
        fi
        return 0
    else
        if [ "$required" = "required" ]; then
            echo -e "${RED}❌ $file (TIDAK DITEMUKAN - WAJIB ADA)${NC}"
            ((TOTAL_ERRORS++))
        else
            echo -e "${YELLOW}⚠️  $file (tidak ditemukan)${NC}"
            ((TOTAL_WARNINGS++))
        fi
        return 1
    fi
}

# Fungsi cek folder exists
check_dir() {
    local dir=$1
    if [ -d "$dir" ]; then
        local count=$(find "$dir" -type f | wc -l)
        echo -e "${GREEN}📁 $dir/ ($count files)${NC}"
        return 0
    else
        echo -e "${RED}❌ $dir/ (TIDAK DITEMUKAN)${NC}"
        ((TOTAL_ERRORS++))
        return 1
    fi
}

# Fungsi cek isi spesifik di file
check_content() {
    local file=$1
    local pattern=$2
    local desc=$3
    
    if [ -f "$file" ]; then
        if grep -q "$pattern" "$file" 2>/dev/null; then
            echo -e "${GREEN}   ✓ $desc${NC}"
            return 0
        else
            echo -e "${YELLOW}   ⚠️  $desc (tidak ditemukan)${NC}"
            ((TOTAL_WARNINGS++))
            return 1
        fi
    fi
    return 1
}

echo -e "${BLUE}📂 STRUKTUR FOLDER${NC}"
echo "-------------------"
check_dir "$PROJECT_DIR"
check_dir "$PROJECT_DIR/.github"
check_dir "$PROJECT_DIR/.github/workflows"
check_dir "$PROJECT_DIR/app"
check_dir "$PROJECT_DIR/app/src"
check_dir "$PROJECT_DIR/app/src/main"
check_dir "$PROJECT_DIR/app/src/main/java"
check_dir "$PROJECT_DIR/app/src/main/res"
check_dir "$PROJECT_DIR/gradle"
check_dir "$PROJECT_DIR/gradle/wrapper"

echo ""
echo -e "${BLUE}⚙️  FILE KONFIGURASI ROOT${NC}"
echo "-------------------------"
cd "$PROJECT_DIR" 2>/dev/null || { echo -e "${RED}❌ Folder $PROJECT_DIR tidak ditemukan!${NC}"; exit 1; }

check_file "build.gradle" "required"
check_file "settings.gradle" "required"
check_file "gradle.properties" "optional"
check_file "local.properties" "optional"
check_file "gradlew" "required"
check_file "gradlew.bat" "optional"

echo ""
echo -e "${BLUE}🔧 GRADLE WRAPPER${NC}"
echo "-----------------"
check_file "gradle/wrapper/gradle-wrapper.properties" "required"

# Cek isi gradle-wrapper.properties
if [ -f "gradle/wrapper/gradle-wrapper.properties" ]; then
    check_content "gradle/wrapper/gradle-wrapper.properties" "distributionUrl" "Gradle distribution URL"
    check_content "gradle/wrapper/gradle-wrapper.properties" "gradle-8" "Gradle 8.x version"
fi

echo ""
echo -e "${BLUE}📱 APP LEVEL${NC}"
echo "------------"
check_file "app/build.gradle" "required"
check_file "app/proguard-rules.pro" "optional"

# Cek isi app/build.gradle
if [ -f "app/build.gradle" ]; then
    check_content "app/build.gradle" "com.android.application" "Plugin aplikasi Android"
    check_content "app/build.gradle" "compose" "Jetpack Compose enabled"
    check_content "app/build.gradle" "DPIChanger" "Application ID"
fi

echo ""
echo -e "${BLUE}📋 ANDROID MANIFEST${NC}"
echo "-------------------"
check_file "app/src/main/AndroidManifest.xml" "required"

if [ -f "app/src/main/AndroidManifest.xml" ]; then
    check_content "app/src/main/AndroidManifest.xml" "POST_NOTIFICATIONS" "Notification permission"
    check_content "app/src/main/AndroidManifest.xml" "FOREGROUND_SERVICE" "Foreground service"
    check_content "app/src/main/AndroidManifest.xml" "MainActivity" "MainActivity declaration"
    check_content "app/src/main/AndroidManifest.xml" "DPIService" "DPIService declaration"
fi

echo ""
echo -e "${BLUE}☕ KOTLIN SOURCE FILES${NC}"
echo "----------------------"

KOTLIN_FILES=(
    "app/src/main/java/com/dpi/changer/MainActivity.kt"
    "app/src/main/java/com/dpi/changer/DPIApp.kt"
    "app/src/main/java/com/dpi/changer/data/model/Preset.kt"
    "app/src/main/java/com/dpi/changer/data/local/PresetDataStore.kt"
    "app/src/main/java/com/dpi/changer/service/DPIService.kt"
    "app/src/main/java/com/dpi/changer/receiver/BootReceiver.kt"
    "app/src/main/java/com/dpi/changer/util/RootUtil.kt"
    "app/src/main/java/com/dpi/changer/ui/navigation/NavHost.kt"
    "app/src/main/java/com/dpi/changer/ui/screens/MainScreen.kt"
    "app/src/main/java/com/dpi/changer/ui/screens/PresetScreen.kt"
    "app/src/main/java/com/dpi/changer/ui/components/LiquidGlass.kt"
    "app/src/main/java/com/dpi/changer/ui/theme/Theme.kt"
    "app/src/main/java/com/dpi/changer/ui/theme/Color.kt"
    "app/src/main/java/com/dpi/changer/ui/theme/Type.kt"
)

for file in "${KOTLIN_FILES[@]}"; do
    check_file "$file" "required"
done

echo ""
echo -e "${BLUE}🎨 RESOURCES${NC}"
echo "------------"

RES_FILES=(
    "app/src/main/res/layout/notification_dpi_control.xml"
    "app/src/main/res/drawable/bg_notification.xml"
    "app/src/main/res/drawable/bg_input.xml"
    "app/src/main/res/drawable/ic_dpi.xml"
    "app/src/main/res/values/colors.xml"
    "app/src/main/res/values/strings.xml"
    "app/src/main/res/values/themes.xml"
    "app/src/main/res/values-night/themes.xml"
)

for file in "${RES_FILES[@]}"; do
    check_file "$file" "required"
done

echo ""
echo -e "${BLUE}🚀 GITHUB ACTIONS${NC}"
echo "-----------------"
check_file ".github/workflows/build.yml" "required"

if [ -f ".github/workflows/build.yml" ]; then
    check_content ".github/workflows/build.yml" "actions/checkout" "Checkout action"
    check_content ".github/workflows/build.yml" "setup-java" "Java setup"
    check_content ".github/workflows/build.yml" "assembleDebug" "Debug build"
    check_content ".github/workflows/build.yml" "upload-artifact" "Artifact upload"
fi

echo ""
echo -e "${BLUE}================================${NC}"
echo -e "${BLUE}📊 RINGKASAN VALIDASI${NC}"
echo -e "${BLUE}================================${NC}"

if [ $TOTAL_ERRORS -eq 0 ] && [ $TOTAL_WARNINGS -eq 0 ]; then
    echo -e "${GREEN}🎉 SEMUA FILE LENGKAP DAN TERISI!${NC}"
    echo -e "${GREEN}Project siap di-build!${NC}"
    exit 0
elif [ $TOTAL_ERRORS -eq 0 ]; then
    echo -e "${YELLOW}⚠️  $TOTAL_WARNINGS peringatan ditemukan${NC}"
    echo -e "${YELLOW}Project bisa di-build tapi mungkin ada fitur yang kurang${NC}"
    exit 0
else
    echo -e "${RED}❌ $TOTAL_ERRORS error ditemukan${NC}"
    echo -e "${YELLOW}⚠️  $TOTAL_WARNINGS peringatan ditemukan${NC}"
    echo -e "${RED}Project BELUM SIAP di-build!${NC}"
    echo ""
    echo -e "${BLUE}💡 Langkah perbaikan:${NC}"
    echo "1. Pastikan semua file yang ditandai ❌ sudah dibuat"
    echo "2. Isi semua file yang masih kosong (0 bytes)"
    echo "3. Copy isi kode dari jawaban sebelumnya"
    exit 1
fi
