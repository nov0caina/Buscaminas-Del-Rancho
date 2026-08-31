#!/usr/bin/env bash
# ============================================================================
#  build_release.sh — Compila y firma el Android App Bundle (AAB) para Google Play
# ============================================================================
#  Uso:   ./build_release.sh
#         ./build_release.sh --bump        (incrementa automáticamente el versionCode +1)
#         ./build_release.sh --clean       (limpia caché antes de compilar)
# ============================================================================

set -euo pipefail

# ── Colores para output ─────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # Sin color

# ── Directorio raíz del proyecto ────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

log_info()    { echo -e "${CYAN}[INFO]${NC}    $1"; }
log_success() { echo -e "${GREEN}[OK]${NC}      $1"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC}    $1"; }
log_error()   { echo -e "${RED}[ERROR]${NC}   $1"; }
log_header()  { echo -e "\n${BOLD}═══════════════════════════════════════════════════${NC}"; echo -e "${BOLD}  $1${NC}"; echo -e "${BOLD}═══════════════════════════════════════════════════${NC}\n"; }

DO_CLEAN=false
DO_BUMP=false

for arg in "$@"; do
    case "$arg" in
        --clean) DO_CLEAN=true ;;
        --bump)  DO_BUMP=true ;;
        --help|-h)
            echo ""
            echo "Uso: ./build_release.sh [opciones]"
            echo ""
            echo "Opciones:"
            echo "  --bump      Incrementa automáticamente el versionCode (+1) antes de compilar"
            echo "  --clean     Ejecuta clean antes de compilar el bundle"
            echo "  --help, -h  Muestra esta ayuda"
            echo ""
            exit 0
            ;;
        *)
            log_error "Opción desconocida: $arg"
            exit 1
            ;;
    esac
done

log_header "Buscaminas Del Rancho — Release Build (Google Play)"

# ── 1. Verificar Java ───────────────────────────────────────────────────────
log_info "Verificando Java..."
if command -v java &>/dev/null; then
    JAVA_VER=$(java -version 2>&1 | head -1)
    log_success "Java encontrado: $JAVA_VER"
else
    log_error "Java no está instalado."
    exit 1
fi

# ── 2. Configurar ANDROID_HOME ──────────────────────────────────────────────
log_info "Verificando Android SDK..."
if [ -z "${ANDROID_HOME:-}" ]; then
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
    log_error "No se encontró el Android SDK. Configura export ANDROID_HOME=\$HOME/Android/Sdk"
    exit 1
fi
log_success "Android SDK: $ANDROID_HOME"

# ── 3. Verificar Keystore de Release ────────────────────────────────────────
log_info "Verificando firma de Release..."
if [ -f "rancho-release.jks" ]; then
    log_success "Keystore oficial encontrado: rancho-release.jks"
else
    log_warn "No se encontró rancho-release.jks en la raíz. Se usarán variables de entorno o keystore.properties si existen."
fi

# ── 4. Incrementar VersionCode (si se pasó --bump) ───────────────────────────
GRADLE_FILE="app/build.gradle.kts"
CURRENT_VERSION_CODE=$(grep -oE "versionCode = [0-9]+" "$GRADLE_FILE" | grep -oE "[0-9]+" || echo "1")
CURRENT_VERSION_NAME=$(grep -oE 'versionName = "[^"]+"' "$GRADLE_FILE" | cut -d'"' -f2 || echo "1.0.0")

if [ "$DO_BUMP" = true ]; then
    NEW_VERSION_CODE=$((CURRENT_VERSION_CODE + 1))
    sed -i "s/versionCode = $CURRENT_VERSION_CODE/versionCode = $NEW_VERSION_CODE/" "$GRADLE_FILE"
    log_success "VersionCode incrementado: $CURRENT_VERSION_CODE ➔ $NEW_VERSION_CODE"
    CURRENT_VERSION_CODE=$NEW_VERSION_CODE
fi

log_info "Versión objetivo: ${BOLD}${CURRENT_VERSION_NAME}${NC} (Código: ${BOLD}${CURRENT_VERSION_CODE}${NC})"

# ── 5. Limpieza previa ──────────────────────────────────────────────────────
if [ "$DO_CLEAN" = true ]; then
    log_info "Limpiando proyecto..."
    ./gradlew clean
fi

# ── 6. Compilar Release Bundle ──────────────────────────────────────────────
log_header "Construyendo Android App Bundle Oficial (Release)..."
BUILD_START=$(date +%s)

./gradlew bundleRelease

BUILD_END=$(date +%s)
BUILD_TIME=$((BUILD_END - BUILD_START))

# ── 7. Copiar y organizar paquetes ──────────────────────────────────────────
AAB_SOURCE="app/build/outputs/bundle/release/app-release.aab"

if [ -f "$AAB_SOURCE" ]; then
    AAB_SIZE=$(du -h "$AAB_SOURCE" | cut -f1)
    mkdir -p releases google_play_assets

    DATE=$(date +%Y%m%d_%H%M)
    DEST_PLAY="google_play_assets/buscaminas_del_rancho_v${CURRENT_VERSION_CODE}_release.aab"
    DEST_ARCHIVE="releases/buscaminas_v${CURRENT_VERSION_CODE}_${DATE}.aab"

    cp "$AAB_SOURCE" "$DEST_PLAY"
    cp "$AAB_SOURCE" "$DEST_ARCHIVE"

    log_header "🎉 ¡App Bundle de Producción Listo!"
    echo -e "  ${BOLD}Archivo para Play Console:${NC}  ${GREEN}$DEST_PLAY${NC}"
    echo -e "  ${BOLD}Copia de Respaldo:${NC}          $DEST_ARCHIVE"
    echo -e "  ${BOLD}Código de Versión:${NC}          ${CYAN}$CURRENT_VERSION_CODE${NC}"
    echo -e "  ${BOLD}Nombre de Versión:${NC}          ${CYAN}$CURRENT_VERSION_NAME${NC}"
    echo -e "  ${BOLD}Tamaño del Bundle:${NC}          $AAB_SIZE (Optimizado con R8 + Símbolos Nativos)"
    echo -e "  ${BOLD}Tiempo de Compilación:${NC}      ${BUILD_TIME}s"
    echo ""
    echo -e "${BOLD}📲 Pasos para subir a Google Play Console:${NC}"
    echo -e "  1. Entra a ${CYAN}Prueba interna${NC} (o Prueba cerrada)."
    echo -e "  2. Haz clic en el botón azul ${CYAN}Crear nueva versión${NC}."
    echo -e "  3. Sube el archivo: ${BOLD}$DEST_PLAY${NC}"
    echo -e "  4. Guarda y publica la versión."
    echo ""
else
    log_header "❌ Falló la Construcción del Bundle"
    log_error "No se encontró el archivo $AAB_SOURCE. Revisa los errores anteriores."
    exit 1
fi
