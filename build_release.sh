#!/usr/bin/env bash
# ============================================================================
#  build_release.sh — Compila y firma el Android App Bundle (AAB) para Google Play
# ============================================================================
#  Uso:
#    ./build_release.sh                       (compila con la versión actual)
#    ./build_release.sh --patch | --bump      (Versión Menor / Parche: 1.0.0 ➔ 1.0.1, code +1)
#    ./build_release.sh --minor               (Versión Intermedia:     1.0.1 ➔ 1.1.0, code +1)
#    ./build_release.sh --major               (Versión Mayor:          1.1.0 ➔ 2.0.0, code +1)
#    ./build_release.sh --version-name "1.2.0" (Versión personalizada,         code +1)
#    ./build_release.sh --clean               (Limpia caché antes de compilar)
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
BUMP_TYPE=""
CUSTOM_VERSION_NAME=""
EXTRA_GRADLE_ARGS=()

while [[ $# -gt 0 ]]; do
    case "$1" in
        --clean)
            DO_CLEAN=true
            shift
            ;;
        --patch|--bump|--bump-patch)
            BUMP_TYPE="patch"
            shift
            ;;
        --minor|--bump-minor|--intermedia)
            BUMP_TYPE="minor"
            shift
            ;;
        --major|--bump-major|--mayor)
            BUMP_TYPE="major"
            shift
            ;;
        --version-name)
            if [ -n "${2:-}" ]; then
                CUSTOM_VERSION_NAME="$2"
                BUMP_TYPE="custom"
                shift 2
            else
                log_error "Debes especificar una versión después de --version-name (ej. 1.2.0)"
                exit 1
            fi
            ;;
        --extra-args|--gradle-args)
            if [ -n "${2:-}" ]; then
                EXTRA_GRADLE_ARGS+=($2)
                shift 2
            else
                log_error "Debes especificar argumentos después de $1"
                exit 1
            fi
            ;;
        --help|-h)
            echo ""
            echo "Uso: ./build_release.sh [opciones de versión] [opciones de build] [-- args_gradle]"
            echo ""
            echo "Opciones de Incremento de Versión Semántica (SemVer):"
            echo "  --patch, --bump   Incrementa versión Menor/Parche   (ej. 1.0.0 ➔ 1.0.1 y versionCode +1)"
            echo "  --minor           Incrementa versión Intermedia     (ej. 1.0.1 ➔ 1.1.0 y versionCode +1)"
            echo "  --major           Incrementa versión Mayor          (ej. 1.1.0 ➔ 2.0.0 y versionCode +1)"
            echo "  --version-name X  Establece versionName personalizado y sube versionCode +1"
            echo ""
            echo "Opciones de Build:"
            echo "  --clean           Limpia el proyecto antes de compilar"
            echo "  --extra-args      Pasa argumentos adicionales a Gradle"
            echo "  --help, -h        Muestra esta ayuda"
            echo ""
            exit 0
            ;;
        --)
            shift
            while [[ $# -gt 0 ]]; do
                EXTRA_GRADLE_ARGS+=("$1")
                shift
            done
            break
            ;;
        *)
            EXTRA_GRADLE_ARGS+=("$1")
            shift
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
if [ -z "${ANDROID_HOME:-}" ] && [ -f "local.properties" ]; then
    SDK_FROM_PROP=$(grep -E "^sdk\.dir=" local.properties | cut -d'=' -f2 | sed 's/\\:/:/g' | sed 's/\\\\/\//g' || true)
    if [ -n "$SDK_FROM_PROP" ] && [ -d "$SDK_FROM_PROP" ]; then
        export ANDROID_HOME="$SDK_FROM_PROP"
    fi
fi

if [ -z "${ANDROID_HOME:-}" ]; then
    POSSIBLE_PATHS=(
        "$HOME/Android/Sdk"
        "$HOME/android-sdk"
        "/usr/local/android-sdk"
        "/opt/android-sdk"
        "/usr/lib/android-sdk"
    )
    for sdk_path in "${POSSIBLE_PATHS[@]}"; do
        if [ -d "$sdk_path" ]; then
            export ANDROID_HOME="$sdk_path"
            break
        fi
    done
fi

if [ -n "${ANDROID_HOME:-}" ] && [ -d "$ANDROID_HOME" ]; then
    export ANDROID_SDK_ROOT="$ANDROID_HOME"
    log_success "Android SDK: $ANDROID_HOME"
else
    log_warn "ANDROID_HOME no está configurado explícitamente. Gradle utilizará su configuración por defecto."
fi

# Configurar ANDROID_USER_HOME si ~/.android no tiene permisos de escritura
if [ -z "${ANDROID_USER_HOME:-}" ]; then
    if [ ! -w "${HOME:-}/.android" ] 2>/dev/null; then
        export ANDROID_USER_HOME="$SCRIPT_DIR/.android"
        mkdir -p "$ANDROID_USER_HOME"
    fi
fi

# ── 3. Verificar Keystore de Release ────────────────────────────────────────
log_info "Verificando firma de Release..."
if [ -f "rancho-release.jks" ]; then
    log_success "Keystore oficial encontrado: rancho-release.jks"
else
    log_warn "No se encontró rancho-release.jks en la raíz. Se usarán variables de entorno o keystore.properties si existen."
fi

# ── 4. Gestionar Versiones (SemVer y Code) ──────────────────────────────────
GRADLE_FILE="app/build.gradle.kts"
CURRENT_VERSION_CODE=$(grep -oE "versionCode = [0-9]+" "$GRADLE_FILE" | grep -oE "[0-9]+" || echo "1")
CURRENT_VERSION_NAME=$(grep -oE 'versionName = "[^"]+"' "$GRADLE_FILE" | cut -d'"' -f2 || echo "1.0.0")

TARGET_VERSION_CODE=$CURRENT_VERSION_CODE
TARGET_VERSION_NAME=$CURRENT_VERSION_NAME

if [ -n "$BUMP_TYPE" ]; then
    # Desglosar SemVer (Major.Minor.Patch)
    IFS='.' read -r V_MAJOR V_MINOR V_PATCH <<< "$CURRENT_VERSION_NAME"
    V_MAJOR="${V_MAJOR:-1}"
    V_MINOR="${V_MINOR:-0}"
    V_PATCH="${V_PATCH:-0}"

    case "$BUMP_TYPE" in
        patch)
            V_PATCH=$((V_PATCH + 1))
            TARGET_VERSION_NAME="${V_MAJOR}.${V_MINOR}.${V_PATCH}"
            TARGET_VERSION_CODE=$((CURRENT_VERSION_CODE + 1))
            log_info "Tipo de versión: ${BOLD}Menor / Parche${NC} (correcciones y ajustes)"
            ;;
        minor)
            V_MINOR=$((V_MINOR + 1))
            V_PATCH=0
            TARGET_VERSION_NAME="${V_MAJOR}.${V_MINOR}.${V_PATCH}"
            TARGET_VERSION_CODE=$((CURRENT_VERSION_CODE + 1))
            log_info "Tipo de versión: ${BOLD}Intermedia${NC} (nuevas funciones y modos)"
            ;;
        major)
            V_MAJOR=$((V_MAJOR + 1))
            V_MINOR=0
            V_PATCH=0
            TARGET_VERSION_NAME="${V_MAJOR}.${V_MINOR}.${V_PATCH}"
            TARGET_VERSION_CODE=$((CURRENT_VERSION_CODE + 1))
            log_info "Tipo de versión: ${BOLD}Mayor${NC} (gran lanzamiento o rediseño)"
            ;;
        custom)
            TARGET_VERSION_NAME="$CUSTOM_VERSION_NAME"
            TARGET_VERSION_CODE=$((CURRENT_VERSION_CODE + 1))
            log_info "Tipo de versión: ${BOLD}Personalizada${NC}"
            ;;
    esac

    # Aplicar cambios en app/build.gradle.kts
    sed -i "s/versionCode = $CURRENT_VERSION_CODE/versionCode = $TARGET_VERSION_CODE/" "$GRADLE_FILE"
    sed -i "s/versionName = \"$CURRENT_VERSION_NAME\"/versionName = \"$TARGET_VERSION_NAME\"/" "$GRADLE_FILE"

    echo ""
    log_success "Versión actualizada:"
    echo -e "  Anterior: ${YELLOW}${CURRENT_VERSION_NAME}${NC} (Code: ${YELLOW}${CURRENT_VERSION_CODE}${NC})"
    echo -e "  Nueva:    ${GREEN}${BOLD}${TARGET_VERSION_NAME}${NC} (Code: ${GREEN}${BOLD}${TARGET_VERSION_CODE}${NC})"
else
    log_info "Manteniendo versión actual:"
    echo -e "  Versión: ${BOLD}${TARGET_VERSION_NAME}${NC} (Code: ${BOLD}${TARGET_VERSION_CODE}${NC})"
fi

# ── 5. Limpieza previa ──────────────────────────────────────────────────────
if [ "$DO_CLEAN" = true ]; then
    log_info "Limpiando proyecto con gradlew clean..."
    ./gradlew clean
fi

# ── 6. Compilar Release Bundle ──────────────────────────────────────────────
log_header "Construyendo Android App Bundle Oficial (Release)..."
BUILD_START=$(date +%s)

./gradlew bundleRelease "${EXTRA_GRADLE_ARGS[@]}"

BUILD_END=$(date +%s)
BUILD_TIME=$((BUILD_END - BUILD_START))

# ── 7. Copiar y organizar paquetes ──────────────────────────────────────────
AAB_SOURCE="app/build/outputs/bundle/release/app-release.aab"

if [ -f "$AAB_SOURCE" ]; then
    AAB_SIZE=$(du -h "$AAB_SOURCE" | cut -f1)
    mkdir -p releases google_play_assets

    DATE=$(date +%Y%m%d_%H%M)
    DEST_PLAY="google_play_assets/buscaminas_del_rancho_v${TARGET_VERSION_CODE}_v${TARGET_VERSION_NAME}.aab"
    DEST_ARCHIVE="releases/buscaminas_v${TARGET_VERSION_CODE}_v${TARGET_VERSION_NAME}_${DATE}.aab"

    cp "$AAB_SOURCE" "$DEST_PLAY"
    cp "$AAB_SOURCE" "$DEST_ARCHIVE"

    log_header "🎉 ¡App Bundle de Producción Listo!"
    echo -e "  ${BOLD}Archivo para Play Console:${NC}  ${GREEN}$DEST_PLAY${NC}"
    echo -e "  ${BOLD}Copia de Respaldo:${NC}          $DEST_ARCHIVE"
    echo -e "  ${BOLD}Versión Semántica:${NC}          ${CYAN}v${TARGET_VERSION_NAME}${NC}"
    echo -e "  ${BOLD}Código de Versión:${NC}          ${CYAN}${TARGET_VERSION_CODE}${NC}"
    echo -e "  ${BOLD}Tamaño del Bundle:${NC}          $AAB_SIZE (Optimizado con R8 + Símbolos Nativos)"
    echo -e "  ${BOLD}Tiempo de Compilación:${NC}      ${BUILD_TIME}s"
    echo ""
    echo -e "${BOLD}📲 Pasos para subir a Google Play Console:${NC}"
    echo -e "  1. Entra a ${CYAN}Prueba interna${NC} (o Prueba cerrada)."
    echo -e "  2. Haz clic en el botón azul ${CYAN}Crear nueva versión${NC}."
    echo -e "  3. Sube el archivo: ${BOLD}$DEST_PLAY${NC}"
    echo -e "  4. En nombre de versión pon: ${BOLD}${TARGET_VERSION_NAME} (Build ${TARGET_VERSION_CODE})${NC}"
    echo -e "  5. Guarda y publica la versión."
    echo ""
else
    log_header "❌ Falló la Construcción del Bundle"
    log_error "No se encontró el archivo $AAB_SOURCE. Revisa los errores anteriores."
    exit 1
fi
