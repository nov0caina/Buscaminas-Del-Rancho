# Guía de Gestión de Versiones y Canales — Google Play Console
**Buscaminas Del Rancho** (`com.nov0caina.buscaminas.estilo.sinaloa`)

---

## 1. Arquitectura de Canales (Tracks) en Play Console

Google Play Console organiza la distribución de la aplicación en diferentes canales de prueba y publicación. Para *Buscaminas Del Rancho*, la jerarquía y propósito de cada canal es la siguiente:

| Canal | Audiencia | Propósito | Estado Actual |
| :--- | :--- | :--- | :--- |
| **Prueba Interna** | Desarrollador y equipo cercano (hasta 100 testers) | Validar builds rápidamente sin revisión de Google. | Build 12 (`1.0.0`) activo. |
| **Prueba Cerrada (Alpha)** | Grupo selecto de testers registrados (13 testers vía correo) | **Canal crítico.** Cumplir el requisito obligatorio de Google Play de 14 días continuos de pruebas antes de solicitar acceso a Producción. | Build 12 (`1.0.0`) activo. |
| **Acceso Anticipado / Prueba Abierta** | Cualquier usuario en Play Store (178 países) | Permitir que usuarios descubran la app antes del lanzamiento oficial. | Build 12 (`1.0.0`) activo. |
| **Producción** | Todo el público en Google Play Store | Lanzamiento oficial una vez aprobada la revisión de 14 días de prueba cerrada. | Pendiente de promoción tras el periodo de 14 días. |

> [!IMPORTANT]
> **Requisito de 14 días de Google Play:**  
> Para cuentas personales de desarrollador creadas después de noviembre de 2023, Google exige que al menos **12 testers** permanezcan registrados en la **Prueba Cerrada** durante **14 días consecutivos** interactuando con la app antes de solicitar la publicación en Producción.

---

## 2. Reglas de Versionado en el Código (`app/build.gradle.kts`)

El archivo [app/build.gradle.kts](file:///home/nov0caina/Documents/VSC_Env/Workspace/buscaminas-del-rancho/app/build.gradle.kts) define dos parámetros fundamentales dentro de `defaultConfig`:

```kotlin
defaultConfig {
    applicationId = "com.nov0caina.buscaminas.estilo.sinaloa"
    minSdk = 24
    targetSdk = 36

    // REGLA: Entero incremental monotónico. Google Play NUNCA permite subir un código igual o menor.
    // La versión actual en producción/pruebas es 12.
    // La siguiente versión a compilar DEBE tener versionCode >= 13.
    versionCode = 12

    // Versión semántica visible para los usuarios finales (Mayor.Menor.Parche)
    versionName = "1.0.0"
}
```

### Reglas obligatorias para próximas versiones:
1. **`versionCode` estrictamente incremental:**  
   * Google Play rechazará automáticamente cualquier archivo `.aab` cuyo `versionCode` sea menor o igual a **12**.
   * El siguiente build generado con `./build_release.sh` debe configurarse con `versionCode = 13`.
2. **`versionName` semántico:**  
   * Incrementa el parche para correcciones de errores menores (ejemplo: `1.0.1`).
   * Incrementa la versión menor para nuevas funciones o modos de juego (ejemplo: `1.1.0`).

---

## 3. Convención de Nomenclatura en Play Console

Al crear una nueva versión en cualquier canal de Google Play Console, se solicita un campo llamado **Nombre de la versión** (*Release name*).

Para mantener consistencia y evitar confusiones:
* **Formato oficial estándar:** `<versionName> (Build <versionCode>)`
* **Ejemplos correctos:**
  * `1.0.0 (Build 12)`
  * `1.0.1 (Build 13)`
  * `1.1.0 (Build 14)`
* **Evitar formatos ambiguos** como solo `12 (1.0.0)` o simplemente el número de compilación sin la versión semántica.

---

## 4. Gestión de Borradores y Bundles Históricos

### A. Borradores Huérfanos (*Draft Releases*)
* **¿Qué son?** Si entras a un canal y pulsas *"Crear nueva versión"* pero no la completas ni lanzas, Google Play guarda un **Borrador** con versión `-`.
* **Cómo resolverlo:** Entra al canal respectivo (ej. *Pruebas ➔ Prueba cerrada*), selecciona la versión en borrador, entra en *"Editar versión"* y pulsa el botón de opciones `⋮` ➔ **Descartar versión / Eliminar borrador**.
* **Efecto:** La fila huérfana desaparece y la consola muestra limpiamente solo la versión activa.

### B. App Bundles Inactivos en el Historial
* En la sección *Últimos app bundles* del explorador de artefactos, verás versiones anteriores como el código 8, 9 u 11 con estado **Inactivo**.
* **Comportamiento esperado:** Google Play Console **no permite borrar** artefactos históricos por motivos de auditoría y rollback. El estado *Inactivo* garantiza que ningún usuario los descargará jamás.

---

## 5. Procedimiento para Publicar una Nueva Versión

Cuando se necesite lanzar una actualización:

1. **Actualizar el código:**  
   En `app/build.gradle.kts`, incrementar `versionCode` (mínimo 13) y opcionalmente `versionName`.
2. **Compilar el Release AAB:**  
   Ejecutar exclusivamente el script oficial del proyecto:
   ```bash
   ./build_release.sh
   ```
   *(El bundle firmado se generará en `google_play_assets/` o en `app/build/outputs/bundle/release/`).*
3. **Subir a Play Console:**  
   * Ve a **Pruebas ➔ Prueba cerrada (Alpha)**.
   * Haz clic en **Crear nueva versión**.
   * Arrastra el archivo `.aab` generado.
   * Asigna el nombre de versión con el formato estándar: `X.Y.Z (Build N)`.
   * Agrega las notas de versión (qué cambió o se corrigió).
   * Revisa y confirma el lanzamiento al 100%.
