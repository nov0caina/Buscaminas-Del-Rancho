#!/bin/bash

echo "=== Generador de Release (AAB) para Google Play ==="

ENV_FILE="keystore_env.sh"

# 1. Verificar si existen las variables de entorno para el Keystore
if [ -f "$ENV_FILE" ]; then
    echo "Cargando variables de entorno desde $ENV_FILE..."
    source "./$ENV_FILE"
else
    echo "⚠️  ADVERTENCIA: No se encontró el archivo $ENV_FILE."
    echo "Si no has generado tu Keystore, cancela este proceso (Ctrl+C) y ejecuta primero ./generate_keystore.sh"
    echo "Continuando en 3 segundos (la compilación fallará si las claves son obligatorias)..."
    sleep 3
fi

# 2. Limpiar compilaciones anteriores (opcional pero recomendado para un release limpio)
echo "Limpiando el proyecto..."
./gradlew clean

# 3. Ejecutar la compilación del App Bundle
echo "Iniciando la construcción del Android App Bundle (AAB)..."
./gradlew bundleRelease

# 4. Verificar si la compilación fue exitosa
if [ $? -eq 0 ]; then
    echo "✅ Compilación exitosa."
    
    # Ruta donde Gradle genera el AAB por defecto
    AAB_SOURCE="app/build/outputs/bundle/release/app-release.aab"
    
    if [ -f "$AAB_SOURCE" ]; then
        # Crear directorio de releases en la raíz si no existe
        mkdir -p releases
        
        # Obtener fecha para el nombre del archivo
        DATE=$(date +%Y%m%d_%H%M)
        AAB_DEST="releases/buscaminas_release_${DATE}.aab"
        
        # Copiar el archivo a la carpeta releases
        cp "$AAB_SOURCE" "$AAB_DEST"
        
        echo ""
        echo "========================================================"
        echo "🎉 ¡Tu App Bundle está listo para subir a Google Play!"
        echo "📂 Archivo guardado en: $AAB_DEST"
        echo "========================================================"
    else
        echo "⚠️  La compilación terminó, pero no se encontró el archivo en $AAB_SOURCE."
    fi
else
    echo ""
    echo "❌ Hubo un error durante la compilación. Revisa los mensajes de error arriba."
fi
