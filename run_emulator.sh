#!/usr/bin/env bash
# ============================================================================
#  run_emulator.sh — Lanza el emulador, compila e instala Buscaminas Del Rancho
# ============================================================================
#  Uso:   ./run_emulator.sh              (lanza emulador + compila + instala + abre app)
#         ./run_emulator.sh --clean       (limpia cache antes de compilar)
#         ./run_emulator.sh --emulator    (solo lanza el emulador, sin compilar)
#         ./run_emulator.sh --build       (solo compila e instala, sin lanzar emulador)
#         ./run_emulator.sh --kill        (cierra el emulador)
# ============================================================================

set -euo pipefail

# ── Colores ─────────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

# ── Directorio del proyecto ─────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# ── Configuración ──────────────────────────────────────────────────────────
AVD_NAME="Pixel_7"
APP_PACKAGE="com.aistudio.buscaminas.rancho.sinaloa.vnfyzm"
MAIN_ACTIVITY="com.example.MainActivity"
APK_OUTPUT="app/build/outputs/apk/debug/app-debug.apk"
GRADLEW="./gradlew"
BOOT_TIMEOUT=120  # Segundos máximo para esperar el boot

# ── Detectar Android SDK ───────────────────────────────────────────────────
if [ -z "${ANDROID_HOME:-}" ]; then
    for p in "$HOME/Android/Sdk" "$HOME/android-sdk" "/usr/local/android-sdk"; do
        [ -d "$p" ] && export ANDROID_HOME="$p" && break
    done
fi
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
export ANDROID_SDK_ROOT="$ANDROID_HOME"

ADB="$ANDROID_HOME/platform-tools/adb"
EMULATOR="$ANDROID_HOME/emulator/emulator"

# ── Funciones ──────────────────────────────────────────────────────────────
log_info()    { echo -e "${CYAN}[INFO]${NC}    $1"; }
log_ok()      { echo -e "${GREEN}[OK]${NC}      $1"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC}    $1"; }
log_error()   { echo -e "${RED}[ERROR]${NC}   $1"; }
log_header()  {
    echo ""
    echo -e "${BOLD}═══════════════════════════════════════════════════${NC}"
    echo -e "${BOLD}  $1${NC}"
    echo -e "${BOLD}═══════════════════════════════════════════════════${NC}"
    echo ""
}

emulator_is_running() {
    "$ADB" devices 2>/dev/null | grep -q "emulator-"
}

wait_for_boot() {
    local elapsed=0
    log_info "Esperando boot del emulador (máx ${BOOT_TIMEOUT}s)..."
    while [ $elapsed -lt $BOOT_TIMEOUT ]; do
        local status
        status=$("$ADB" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r\n' || true)
        if [ "$status" = "1" ]; then
            log_ok "Emulador arrancado en ${elapsed}s"
            return 0
        fi
        sleep 2
        elapsed=$((elapsed + 2))
        # Mostrar progreso cada 10 segundos
        if [ $((elapsed % 10)) -eq 0 ]; then
            echo -e "  ${CYAN}...${elapsed}s${NC}"
        fi
    done
    log_error "Timeout: el emulador no arrancó en ${BOOT_TIMEOUT}s"
    return 1
}

show_help() {
    echo ""
    echo "Uso: ./run_emulator.sh [opciones]"
    echo ""
    echo "Opciones:"
    echo "  (sin args)     Lanza emulador + compila + instala + abre la app"
    echo "  --clean        Limpia el build cache antes de compilar"
    echo "  --emulator     Solo lanza el emulador (sin compilar)"
    echo "  --build        Solo compila e instala (asume emulador ya corriendo)"
    echo "  --kill         Cierra el emulador"
    echo "  --help, -h     Muestra esta ayuda"
    echo ""
    exit 0
}

# ── Parsear argumentos ─────────────────────────────────────────────────────
DO_CLEAN=false
ONLY_EMULATOR=false
ONLY_BUILD=false
DO_KILL=false

for arg in "$@"; do
    case "$arg" in
        --clean)     DO_CLEAN=true ;;
        --emulator)  ONLY_EMULATOR=true ;;
        --build)     ONLY_BUILD=true ;;
        --kill)      DO_KILL=true ;;
        --help|-h)   show_help ;;
        *) log_error "Argumento desconocido: $arg"; show_help ;;
    esac
done

# ── Validaciones previas ──────────────────────────────────────────────────
if [ ! -x "$EMULATOR" ]; then
    log_error "Emulador no encontrado en: $EMULATOR"
    echo "  Instálalo con:"
    echo "  \$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager \"emulator\" \"system-images;android-34;google_apis;x86_64\""
    exit 1
fi

if [ ! -x "$ADB" ]; then
    log_error "ADB no encontrado en: $ADB"
    exit 1
fi

# ── Comando: --kill ────────────────────────────────────────────────────────
if [ "$DO_KILL" = true ]; then
    log_header "Cerrando emulador"
    if emulator_is_running; then
        "$ADB" -s emulator-5554 emu kill 2>/dev/null || true
        log_ok "Emulador cerrado"
    else
        log_warn "No hay emulador corriendo"
    fi
    exit 0
fi

# ════════════════════════════════════════════════════════════════════════════
log_header "🎮 Buscaminas Del Rancho — Run"
# ════════════════════════════════════════════════════════════════════════════

# ── Paso 1: Lanzar emulador (si no está corriendo) ────────────────────────
if [ "$ONLY_BUILD" = false ]; then
    if emulator_is_running; then
        log_ok "Emulador ya está corriendo"
    else
        log_info "Lanzando emulador $AVD_NAME..."

        # Verificar que el AVD existe
        if [ ! -d "$HOME/.android/avd/${AVD_NAME}.avd" ]; then
            log_error "AVD '$AVD_NAME' no existe."
            echo "  Créalo con:"
            echo "  \$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager create avd -n $AVD_NAME -k \"system-images;android-34;google_apis;x86_64\" -d \"pixel_7\""
            exit 1
        fi

        # Lanzar en background
        nohup "$EMULATOR" -avd "$AVD_NAME" \
            -gpu auto \
            -no-snapshot-load \
            -no-boot-anim \
            > /dev/null 2>&1 &

        EMULATOR_PID=$!
        log_info "Emulador iniciado (PID: $EMULATOR_PID)"

        # Esperar a que ADB detecte el dispositivo
        "$ADB" wait-for-device 2>/dev/null

        # Esperar boot completo
        if ! wait_for_boot; then
            exit 1
        fi
    fi

    if [ "$ONLY_EMULATOR" = true ]; then
        log_ok "Emulador listo. No se compilará la app (modo --emulator)."
        exit 0
    fi
fi

# ── Paso 2: Verificar que hay un emulador/dispositivo conectado ───────────
if [ "$ONLY_BUILD" = true ]; then
    if ! emulator_is_running; then
        DEVICE_COUNT=$("$ADB" devices 2>/dev/null | grep -c "device$" || true)
        if [ "$DEVICE_COUNT" -eq 0 ]; then
            log_error "No hay dispositivo ni emulador conectado."
            echo "  Lanza el emulador primero: ./run_emulator.sh --emulator"
            exit 1
        fi
    fi
fi

# ── Paso 3: Compilar ─────────────────────────────────────────────────────
log_header "Compilando Debug APK..."

if [ ! -x "$GRADLEW" ]; then
    chmod +x "$GRADLEW"
fi

GRADLE_ARGS="assembleDebug --no-daemon --console=plain --warning-mode=summary"
if [ "$DO_CLEAN" = true ]; then
    GRADLE_ARGS="clean $GRADLE_ARGS"
    log_info "Limpiando build anterior..."
fi

BUILD_START=$(date +%s)

"$GRADLEW" $GRADLE_ARGS 2>&1 | while IFS= read -r line; do
    case "$line" in
        *"BUILD SUCCESSFUL"*)  echo -e "${GREEN}  $line${NC}" ;;
        *"BUILD FAILED"*)      echo -e "${RED}  $line${NC}" ;;
        *"FAILURE"*)           echo -e "${RED}  $line${NC}" ;;
        *"error:"*)            echo -e "${RED}  $line${NC}" ;;
        *"warning:"*)          echo -e "${YELLOW}  $line${NC}" ;;
        "> Task"*)             echo -e "${CYAN}  $line${NC}" ;;
        *)                     ;; # Silenciar output genérico de Gradle
    esac
done

BUILD_END=$(date +%s)
BUILD_TIME=$((BUILD_END - BUILD_START))

# ── Paso 4: Verificar APK ────────────────────────────────────────────────
if [ ! -f "$APK_OUTPUT" ]; then
    log_error "No se generó el APK. Revisa los errores arriba."
    exit 1
fi

APK_SIZE=$(du -h "$APK_OUTPUT" | cut -f1)
log_ok "APK compilado (${APK_SIZE}, ${BUILD_TIME}s)"

# ── Paso 5: Instalar en el emulador/dispositivo ──────────────────────────
log_info "Instalando en el dispositivo..."
if "$ADB" install -r "$APK_OUTPUT" 2>&1 | grep -q "Success"; then
    log_ok "APK instalado exitosamente"
else
    log_error "Error al instalar el APK"
    "$ADB" install -r "$APK_OUTPUT" 2>&1
    exit 1
fi

# ── Paso 6: Abrir la app automáticamente ─────────────────────────────────
log_info "Abriendo la app..."
"$ADB" shell am start -n "${APP_PACKAGE}/${MAIN_ACTIVITY}" 2>/dev/null
log_ok "App lanzada"

# ── Resumen final ────────────────────────────────────────────────────────
log_header "✅ Listo"
echo -e "  ${BOLD}APK:${NC}       ${APK_SIZE}"
echo -e "  ${BOLD}Build:${NC}     ${BUILD_TIME}s"
echo -e "  ${BOLD}Emulador:${NC}  $AVD_NAME"
echo -e "  ${BOLD}Package:${NC}   $APP_PACKAGE"
echo ""
echo -e "  ${CYAN}Tip:${NC} Para ver logs en tiempo real:"
echo -e "  ${BOLD}$ADB logcat -s \"$APP_PACKAGE\"${NC}"
echo ""
