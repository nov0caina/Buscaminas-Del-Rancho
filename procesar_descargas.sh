#!/usr/bin/env bash

# --- CONFIGURACIÓN ---
CARPETA_DESCARGAS="$HOME/Downloads" # Cambiar a "$HOME/Downloads" si tu sistema está en inglés
ARCHIVO_OBJETIVO="buscaminas-del-rancho.zip"
RUTA_DESTINO="/home/nov0caina/Documents/VSC_Env/Workspace/buscaminas-del-rancho"
CARPETA_BACKUPS="$RUTA_DESTINO/backups"
# ---------------------

mkdir -p "$RUTA_DESTINO"

echo "Vigilando $CARPETA_DESCARGAS a la espera de $ARCHIVO_OBJETIVO..."

inotifywait -m -e close_write,moved_to --format "%f" "$CARPETA_DESCARGAS" | while read -r ARCHIVO
do
    if [ "$ARCHIVO" = "$ARCHIVO_OBJETIVO" ]; then
        TIMESTAMP=$(date '+%Y%m%d_%H%M%S')
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] Archivo detectado: $ARCHIVO"
        
        # --- GESTIÓN DE RESPALDOS ---
        # Si existe 'app' o 'assets', se resguardan en backups/backup_YYYYMMDD_HHMMSS/
        if [ -d "$RUTA_DESTINO/app" ] || [ -d "$RUTA_DESTINO/assets" ]; then
            DESTINO_BACKUP="$CARPETA_BACKUPS/backup_$TIMESTAMP"
            mkdir -p "$DESTINO_BACKUP"
            
            if [ -d "$RUTA_DESTINO/app" ]; then
                mv "$RUTA_DESTINO/app" "$DESTINO_BACKUP/"
                echo "-> Respaldo de 'app' guardado en: $DESTINO_BACKUP/app"
            fi
            
            if [ -d "$RUTA_DESTINO/assets" ]; then
                mv "$RUTA_DESTINO/assets" "$DESTINO_BACKUP/"
                echo "-> Respaldo de 'assets' guardado en: $DESTINO_BACKUP/assets"
            fi
        fi
        
        # --- EXTRACCIÓN Y COPIA ---
        TEMP_DIR=$(mktemp -d)
        unzip -q "$CARPETA_DESCARGAS/$ARCHIVO_OBJETIVO" -d "$TEMP_DIR"
        
        DIR_APP=$(find "$TEMP_DIR" -type d -name "app" | head -n 1)
        DIR_ASSETS=$(find "$TEMP_DIR" -type d -name "assets" | head -n 1)
        
        if [ -n "$DIR_APP" ]; then
            cp -r "$DIR_APP" "$RUTA_DESTINO/"
            echo "-> Nueva versión de 'app' copiada en $RUTA_DESTINO"
        fi
        
        if [ -n "$DIR_ASSETS" ]; then
            cp -r "$DIR_ASSETS" "$RUTA_DESTINO/"
            echo "-> Nueva versión de 'assets' copiada en $RUTA_DESTINO"
        fi
        
        # Limpieza
        rm -rf "$TEMP_DIR"
        
        # Descomenta la siguiente línea si deseas borrar el .zip tras procesarlo
        rm "$CARPETA_DESCARGAS/$ARCHIVO_OBJETIVO"
    fi
done
