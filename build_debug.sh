#!/usr/bin/env bash
# ============================================================================
#  build_debug.sh — Compila el debug APK de Buscaminas Del Rancho
# ============================================================================
#  Uso:   ./build_debug.sh
#         ./build_debug.sh --clean     (limpia el cache antes de compilar)
#         ./build_debug.sh --install   (instala en dispositivo conectado vía ADB)
# ============================================================================

set -euo pipefail

# ── Colores para output ─────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # Sin color

# ── Directorio raíz del proyecto (donde está este script) ───────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# ── Configuración ───────────────────────────────────────────────────────────
APK_OUTPUT="app/build/outputs/apk/debug/app-debug.apk"
GRADLEW="./gradlew"

# ── Funciones auxiliares ────────────────────────────────────────────────────
log_info()    { echo -e "${CYAN}[INFO]${NC}    $1"; }
log_success() { echo -e "${GREEN}[OK]${NC}      $1"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC}    $1"; }
log_error()   { echo -e "${RED}[ERROR]${NC}   $1"; }
log_header()  { echo -e "\n${BOLD}═══════════════════════════════════════════════════${NC}"; echo -e "${BOLD}  $1${NC}"; echo -e "${BOLD}═══════════════════════════════════════════════════${NC}\n"; }

# ── Parsear argumentos ──────────────────────────────────────────────────────
DO_CLEAN=false
DO_INSTALL=false

for arg in "$@"; do
    case "$arg" in
        --clean)   DO_CLEAN=true ;;
        --install) DO_INSTALL=true ;;
        --help|-h)
            echo ""
            echo "Uso: ./build_debug.sh [opciones]"
            echo ""
            echo "Opciones:"
            echo "  --clean     Limpia el build cache antes de compilar"
            echo "  --install   Instala el APK en un dispositivo conectado vía ADB"
            echo "  --help, -h  Muestra esta ayuda"
            echo ""
            exit 0
            ;;
        *)
            log_error "Argumento desconocido: $arg"
            echo "Usa --help para ver las opciones disponibles."
            exit 1
            ;;
    esac
done

# ════════════════════════════════════════════════════════════════════════════
log_header "Buscaminas Del Rancho — Debug Build"
# ════════════════════════════════════════════════════════════════════════════

# ── 1. Verificar Java ───────────────────────────────────────────────────────
log_info "Verificando Java..."
if command -v java &>/dev/null; then
    JAVA_VER=$(java -version 2>&1 | head -1)
    log_success "Java encontrado: $JAVA_VER"
else
    log_error "Java no está instalado. Instálalo con:"
    echo "  sudo apt install openjdk-21-jdk"
    exit 1
fi

# ── 2. Verificar/configurar ANDROID_HOME ────────────────────────────────────
log_info "Verificando Android SDK..."
if [ -z "${ANDROID_HOME:-}" ]; then
    # Intentar detectar ubicaciones comunes
    POSSIBLE_PATHS=(
        "$HOME/Android/Sdk"
        "$HOME/android-sdk"
        "/usr/local/android-sdk"
        "/opt/android-sdk"
    )
    for sdk_path in "${POSSIBLE_PATHS[@]}"; do
        if [ -d "$sdk_path" ]; then
            export ANDROID_HOME="$sdk_path"
            break
        fi
    done
fi

if [ -z "${ANDROID_HOME:-}" ] || [ ! -d "$ANDROID_HOME" ]; then
    log_error "No se encontró el Android SDK."
    echo "  Configura la variable ANDROID_HOME:"
    echo "  export ANDROID_HOME=\$HOME/Android/Sdk"
    exit 1
fi

export ANDROID_SDK_ROOT="$ANDROID_HOME"
log_success "Android SDK: $ANDROID_HOME"

# Verificar que existan las plataformas necesarias
if [ ! -d "$ANDROID_HOME/platforms" ] || [ -z "$(ls -A "$ANDROID_HOME/platforms" 2>/dev/null)" ]; then
    log_error "No hay plataformas SDK instaladas en $ANDROID_HOME/platforms"
    echo "  Instala la plataforma con:"
    echo "  \$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager 'platforms;android-36'"
    exit 1
fi
log_success "Plataformas SDK encontradas: $(ls "$ANDROID_HOME/platforms" | tr '\n' ' ')"

# ── 3. Verificar gradlew ───────────────────────────────────────────────────
log_info "Verificando Gradle Wrapper..."
if [ ! -f "$GRADLEW" ]; then
    log_error "No se encontró gradlew en el directorio del proyecto."
    exit 1
fi

if [ ! -x "$GRADLEW" ]; then
    log_warn "gradlew no tiene permisos de ejecución. Asignando..."
    chmod +x "$GRADLEW"
fi
log_success "Gradle Wrapper listo"

# ── 4. Limpiar build (si se solicitó) ──────────────────────────────────────
if [ "$DO_CLEAN" = true ]; then
    log_info "Limpiando build anterior..."
    "$GRADLEW" clean --no-daemon --console=plain --quiet 2>&1
    log_success "Build limpiado"
fi

# ── 5. Compilar el debug APK ──────────────────────────────────────────────
log_header "Compilando Debug APK..."

BUILD_START=$(date +%s)

"$GRADLEW" assembleDebug \
    --no-daemon \
    --console=plain \
    --warning-mode=summary \
    2>&1 | while IFS= read -r line; do
        # Filtrar output para mostrar solo lo relevante
        case "$line" in
            *"BUILD SUCCESSFUL"*)   echo -e "${GREEN}$line${NC}" ;;
            *"BUILD FAILED"*)       echo -e "${RED}$line${NC}" ;;
            *"FAILURE"*)            echo -e "${RED}$line${NC}" ;;
            *"error:"*)             echo -e "${RED}$line${NC}" ;;
            *"warning:"*)           echo -e "${YELLOW}$line${NC}" ;;
            "> Task"*)              echo -e "${CYAN}  $line${NC}" ;;
            *)                      echo "  $line" ;;
        esac
    done

BUILD_END=$(date +%s)
BUILD_TIME=$((BUILD_END - BUILD_START))

# ── 6. Verificar resultado ─────────────────────────────────────────────────
echo ""
if [ -f "$APK_OUTPUT" ]; then
    APK_SIZE=$(du -h "$APK_OUTPUT" | cut -f1)
    APK_FULL_PATH="$(cd "$(dirname "$APK_OUTPUT")" && pwd)/$(basename "$APK_OUTPUT")"

    log_header "✅ Build Exitoso"
    echo -e "  ${BOLD}APK:${NC}      $APK_FULL_PATH"
    echo -e "  ${BOLD}Tamaño:${NC}   $APK_SIZE"
    echo -e "  ${BOLD}Tiempo:${NC}   ${BUILD_TIME}s"
    echo -e "  ${BOLD}Tipo:${NC}     Debug (firmado con debug keystore)"
    echo ""

    # ── 7. Instalar en dispositivo (si se solicitó) ────────────────────────
    if [ "$DO_INSTALL" = true ]; then
        log_info "Instalando APK en dispositivo conectado..."
        if command -v adb &>/dev/null || [ -x "$ANDROID_HOME/platform-tools/adb" ]; then
            ADB_CMD="${ANDROID_HOME}/platform-tools/adb"
            if ! command -v adb &>/dev/null && [ ! -x "$ADB_CMD" ]; then
                log_error "ADB no encontrado. Instala platform-tools."
                exit 1
            fi
            [ -x "$ADB_CMD" ] || ADB_CMD="adb"

            DEVICE_COUNT=$("$ADB_CMD" devices 2>/dev/null | grep -c "device$" || true)
            if [ "$DEVICE_COUNT" -eq 0 ]; then
                log_error "No hay dispositivos conectados. Conecta un teléfono con depuración USB activada."
                exit 1
            fi

            "$ADB_CMD" install -r "$APK_OUTPUT"
            log_success "APK instalado exitosamente en el dispositivo"
        else
            log_error "ADB no está disponible. No se puede instalar."
            exit 1
        fi
    else
        log_info "Para instalar en un dispositivo, ejecuta:"
        echo -e "  ${CYAN}./build_debug.sh --install${NC}"
        echo -e "  o manualmente: ${CYAN}adb install -r $APK_OUTPUT${NC}"
    fi
else
    log_header "❌ Build Fallido"
    log_error "No se generó el APK. Revisa los errores arriba."
    exit 1
fi

echo ""
