# Reglas del Proyecto — Buscaminas Del Rancho

Estas reglas aplican a todos los agentes y flujos de trabajo en este repositorio:

1. **OpenSpec Actualizado**:
   - Antes de iniciar tareas o tocar código, asegurar que OpenSpec está al día (`npm install -g @fission-ai/openspec@latest`) y sincronizar el repositorio con `openspec update`.
2. **Rama `development` como Entorno de Trabajo**:
   - Nunca tocar código directamente en `master`. Si hay cambios sin commitear en `master` o cualquier otra rama, commitearlos antes de cambiar a `development`.
   - Si la rama `development` no existe, debe crearse localmente y subirse al repositorio remoto (`git checkout -b development && git push -u origin development`).
3. **Auditoría Obligatoria de `.gitignore` y Seguridad**:
   - Antes de realizar cualquier commit, verificar el archivo `.gitignore` y asegurar que no haya claves (`*.jks`, `*.keystore`), scripts con credenciales (`keystore_env.sh`), archivos de entorno (`.env*`), propiedades locales (`keystore.properties`, `local.properties`), credenciales de Firebase o binarios pesados en seguimiento de Git.
   - Si se detecta un archivo sensible desprotegido, agregarlo a `.gitignore`, desindexarlo con `git rm --cached` y proveer un `.example` seguro.
4. **Mantenimiento y Auditoría de Scripts Shell**:
   - `build_debug.sh`, `build_release.sh` y `run_emulator.sh` deben estar adaptados al proyecto (rutas, dependencies, package `com.nov0caina.buscaminas.estilo.sinaloa`).
   - Los scripts deben soportar paso de comandos o argumentos adicionales (`--extra-args` o passthrough) hacia Gradle.
5. **Compilación Exclusiva con Scripts**:
   - Siempre compilar usando `./build_debug.sh` o `./build_release.sh`. Si se necesita un flag o tarea extra, adaptar el script para soportarlo.
6. **Cierre de Cambios y Push Remoto**:
   - Tras archivar un cambio con OpenSpec (`openspec archive`), agrupar los cambios en commits semánticos y hacer `git push origin development`.
