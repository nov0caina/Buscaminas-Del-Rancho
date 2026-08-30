# Guía de Publicación en Google Play Store: Buscaminas Del Rancho

Esta guía explica paso a paso cómo preparar, empaquetar y publicar tu juego **Buscaminas Del Rancho** en la Google Play Store.

## 1. Preparación del Proyecto

Antes de empaquetar tu aplicación, debes verificar la configuración de tu proyecto en el archivo `app/build.gradle.kts`.

### Verificación de Versión e ID
Actualmente, tu configuración es la siguiente:
- **Application ID:** `com.nov0caina.buscaminas.estilo.sinaloa` (Este será el identificador único de tu app en la tienda, ej: `play.google.com/store/apps/details?id=com.nov0caina.buscaminas.estilo.sinaloa`).
- **Version Code:** `1` (Número entero interno. **Deberás incrementarlo** cada vez que subas una nueva actualización).
- **Version Name:** `1.0` (La versión que verán los usuarios).

*Asegúrate de cambiar estos valores en el futuro cuando lances actualizaciones.*

### Iconos y Recursos
Asegúrate de que tu aplicación cuenta con un ícono (launcher icon) adecuado dentro de la carpeta `res/mipmap`. Este ícono es el que se mostrará en los dispositivos de los usuarios.

---

## 2. Configuración de la Clave de Firma (Keystore)

Google Play requiere que las aplicaciones estén firmadas digitalmente. Tu archivo `app/build.gradle.kts` ya está configurado para buscar un archivo de firma en la ruta del proyecto o a través de variables de entorno:

```kotlin
signingConfigs {
  create("release") {
    val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
    storeFile = file(keystorePath)
    storePassword = System.getenv("STORE_PASSWORD")
    keyAlias = "upload" // Importante: el alias está definido como "upload"
    keyPassword = System.getenv("KEY_PASSWORD")
  }
}
```

### Si aún no tienes la clave (`my-upload-key.jks`):
Abre tu terminal en la raíz del proyecto y ejecuta el siguiente comando para generarla. **Guarda muy bien las contraseñas que elijas, si pierdes este archivo no podrás actualizar tu app en el futuro.**

```bash
keytool -genkey -v -keystore my-upload-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

### Configuración de Contraseñas
Dado que el `build.gradle.kts` usa variables de entorno para las contraseñas, antes de compilar debes exportarlas en tu terminal (reemplaza `tu_contraseña` por la que usaste al crear la clave):

```bash
export STORE_PASSWORD="tu_contraseña"
export KEY_PASSWORD="tu_contraseña"
```

*(Opcional: puedes crear un script en tu proyecto para automatizar este paso o agregarlo a tu entorno, pero no lo subas a Git).*

---

## 3. Generación del Android App Bundle (AAB)

Google Play ya no acepta archivos `.apk` para nuevas aplicaciones; debes subir un archivo **`.aab` (Android App Bundle)**.

1. Abre tu terminal en la raíz del proyecto (`/home/nov0caina/Documents/VSC_Env/Workspace/buscaminas-del-rancho`).
2. Ejecuta el comando para construir la versión de lanzamiento:

```bash
./gradlew bundleRelease
```

3. Si todo sale bien, encontrarás tu archivo generado en:
   `app/build/outputs/bundle/release/app-release.aab`

---

## 4. Configuración en Google Play Console

Dado que ya tienes tu cuenta de desarrollador, ingresa a [Google Play Console](https://play.google.com/console).

### A. Crear la aplicación
1. Haz clic en **Crear aplicación**.
2. **Nombre de la aplicación:** Buscaminas Del Rancho (o el nombre público que decidas).
3. **Idioma predeterminado:** Español.
4. **Tipo de aplicación:** Juego.
5. **Gratuita / De pago:** Selecciona según corresponda.
6. Acepta las declaraciones de políticas y leyes de EE. UU., y haz clic en **Crear aplicación**.

### B. Tareas de Configuración (Panel de Control)
En el panel de control de tu aplicación, verás una sección llamada **"Configura tu aplicación"**. Debes completar todos los formularios requeridos:
- **Acceso a la aplicación:** Indica si todas las funciones están disponibles sin inicio de sesión especial.
- **Anuncios:** Declara si tu juego tiene anuncios.
- **Clasificación de contenido:** Completa un cuestionario para que le asignen una clasificación de edad (ej. PEGI 3, ESRB E).
- **Público objetivo y contenido:** Define si el juego está dirigido a niños o adultos.
- **Aplicación de noticias:** Indica que NO es una app de noticias.
- **Seguridad de los datos:** Debes llenar un cuestionario sobre qué datos recopilas de los usuarios (si usas Firebase Analytics, Crashlytics o AdMob, debes declararlo aquí).
- **Categoría y datos de contacto:** Selecciona la categoría "Juegos > Puzles/Mesa" y proporciona tu email o web de soporte.
- **Configurar la ficha de Play Store:** Aquí deberás subir:
  - Nombre, Descripción breve y Descripción completa.
  - **Ícono de la aplicación** (512x512 píxeles, formato PNG/JPEG).
  - **Gráfico de funciones** (1024x500 píxeles).
  - **Capturas de pantalla del teléfono** (Al menos 2 capturas del juego funcionando).

---

## 5. Subida del Archivo AAB y Lanzamiento

Se recomienda siempre usar una **Prueba Interna** o **Prueba Cerrada** antes del lanzamiento a producción, pero los pasos son los mismos para Producción.

1. En el menú de la izquierda, ve a **Producción** (o **Pruebas > Pruebas internas**).
2. Selecciona la pestaña **Versiones** y haz clic en **Crear nueva versión**.
3. **App Signing de Play:** Se te pedirá que aceptes usar la firma de aplicaciones de Play (Play App Signing). **Acepta**, es obligatorio y recomendado.
4. En la sección "Paquetes de aplicaciones", haz clic en **Subir** y selecciona el archivo `app-release.aab` que generaste en el paso 3.
5. Google procesará el archivo. Si el `applicationId` y la versión son correctos, verás los detalles cargados.
6. Agrega un **Nombre de la versión** (ej. "1.0 - Lanzamiento inicial") y las **Notas de la versión** (lo que es nuevo en la app).
7. Haz clic en **Guardar** (o Siguiente) y luego en **Revisar versión**.

### 6. Enviar a Revisión
Si completaste todos los formularios del paso 4B y subiste tu archivo `.aab` correctamente, el botón **"Iniciar lanzamiento a Producción"** (o a pruebas) estará habilitado.
Haz clic en él para enviar la aplicación.

*Nota: La primera vez que publicas una aplicación, Google puede tardar desde unas pocas horas hasta 7 días en revisarla y aprobarla.*

---

## Consejos Finales para "Buscaminas Del Rancho"
- **Política de Privacidad:** Google Play exige una URL de Política de Privacidad si tu app se dirige a menores o recopila datos (como identificadores de anuncios). Puedes crear una gratis en sitios como *freeprivacypolicy.com* o alojarla en un Google Docs público.
- **Actualizaciones:** Cuando quieras subir una versión nueva con correcciones o nuevos niveles, recuerda cambiar el `versionCode` a `2` (y así sucesivamente) en `build.gradle.kts`, generar el `.aab` nuevamente y repetir el paso 5.
