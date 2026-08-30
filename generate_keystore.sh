#!/bin/bash

KEYSTORE_FILE="my-upload-key.jks"
ENV_FILE="keystore_env.sh"

echo "=== Generador de Keystore para Buscaminas Del Rancho ==="

# 1. Verificar si ya existe para no sobreescribir
if [ -f "$KEYSTORE_FILE" ]; then
    echo "⚠️  El archivo $KEYSTORE_FILE ya existe. No se sobreescribirá por seguridad."
    exit 1
fi

# 2. Pedir contraseña de forma segura
read -s -p "Ingresa una contraseña segura para tu Keystore (no se mostrará en pantalla): " PASSWORD
echo
read -s -p "Confirma la contraseña: " PASSWORD_CONFIRM
echo

# 3. Validar contraseñas
if [ "$PASSWORD" != "$PASSWORD_CONFIRM" ]; then
    echo "❌ Las contraseñas no coinciden. Intenta de nuevo."
    exit 1
fi

if [ ${#PASSWORD} -lt 6 ]; then
    echo "❌ La contraseña debe tener al menos 6 caracteres."
    exit 1
fi

echo "Generando $KEYSTORE_FILE..."

# 4. Generar el Keystore usando keytool
keytool -genkey -v \
    -keystore "$KEYSTORE_FILE" \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -alias upload \
    -storepass "$PASSWORD" \
    -keypass "$PASSWORD" \
    -dname "CN=Buscaminas Developer, OU=Game Dev, O=Buscaminas Del Rancho, L=Sinaloa, S=Sinaloa, C=MX"

if [ $? -eq 0 ]; then
    echo "✅ Keystore generado exitosamente: $KEYSTORE_FILE"
    
    # 5. Generar un script de variables de entorno para usar antes de compilar
    echo "#!/bin/bash" > $ENV_FILE
    echo "export STORE_PASSWORD=\"$PASSWORD\"" >> $ENV_FILE
    echo "export KEY_PASSWORD=\"$PASSWORD\"" >> $ENV_FILE
    echo "export KEYSTORE_PATH=\"\${PWD}/$KEYSTORE_FILE\"" >> $ENV_FILE
    
    # Dar permisos de ejecución al nuevo script
    chmod +x $ENV_FILE
    
    echo "✅ Archivo de variables de entorno creado: $ENV_FILE"
    echo ""
    echo "========================================================"
    echo "⚠️  INSTRUCCIONES FINALES:"
    echo "========================================================"
    echo "Antes de empaquetar tu aplicación (.aab), debes ejecutar:"
    echo ""
    echo "    source ./$ENV_FILE"
    echo "    ./gradlew bundleRelease"
    echo ""
    echo "========================================================"
else
    echo "❌ Hubo un error al generar el Keystore."
fi
