# EducarOCR

Aplicación Android para extraer texto de pantallas bloqueadas (certificados de arranque expirados) y preparar datos útiles (Unique Hardware ID, Boot Tic) mediante OCR.

---

## Descripción

EducarOCR es una aplicación móvil desarrollada para capturar o seleccionar una imagen de la pantalla de un equipo bloqueado por vencimiento del certificado de inicio y extraer los datos necesarios para generar/obtener códigos de desbloqueo. El objetivo principal es facilitar el trabajo del personal técnico al recuperar el "Unique Hardware ID" y el "Boot Tick" automáticamente desde la imagen.

Este repo contiene el módulo Android principal (`app/`) con la UI y la lógica OCR.

---

## Características principales

- Selección o captura de imagen.
- Recorte básico de imagen antes de procesar.
- OCR para extraer el texto de la pantalla bloqueada.
- Lógica de parseo para obtener `Unique Hardware ID` y `Boot Tick` y formatearlos.
- Pantallas para previsualizar imagen y resultados, copiar al portapapeles.

---

## Estructura del proyecto (resumen)

- `app/`  — Módulo principal de la aplicación
  - `src/main/java/...` — Código fuente (Kotlin)
  - `src/main/res/` — Recursos (layouts/compose resources, drawables, mipmap, strings, colors)
  - `src/main/AndroidManifest.xml`
  - `build.gradle.kts` — Configuración del módulo
- `build.gradle.kts` — Configuración del proyecto (raíz)
- `gradle/` — Wrapper y versiones
- `README.md` — 

> Nota: revisa `app/build.gradle.kts` para ver las dependencias exactas utilizadas (Compose, ML Kit u otra librería OCR, Coil/Glide, Coroutines, etc.).

---

## Dependencias claves

- Kotlin (versión "2.0.21").
- AndroidX / Jetpack (Core, AppCompat, Lifecycle).
- Jetpack Compose (la UI usa Compose).
- Librería OCR (ML Kit Text Recognition) — Antes Tesseract (deprecada)
- Coil o Glide para carga de imágenes (opcional).
- Coroutines para tareas asíncronas (opcional).

---

## Cómo ejecutar (desarrollador)

1. Abre el proyecto en Android Studio.
2. Espera a que Gradle sincronice. Si Android Studio no muestra "Sync Project with Gradle Files", abre el archivo `build.gradle.kts` del módulo `app` y Android Studio normalmente propondrá sincronizar; también puedes usar el menú `File > Sync Project with Gradle Files`.
3. Si falta algún SDK/Build Tools, Android Studio pedirá instalarlos: acepta e instala.
4. Conecta tu dispositivo por USB con Depuración USB activada (Developer options) o inicia un emulador.
5. Ejecuta la app seleccionando el módulo `app` y pulsando Run.

Consejos rápidos para probar en dispositivo real:
- Habilita `Desarrollador > Depuración USB` y acepta el diálogo de autorización cuando conectes el dispositivo.
- Si no aparece el dispositivo, ejecuta `adb devices` en la terminal para verificar la conexión.

---

## Notas sobre recursos e iconos

- Los nombres de recursos deben contener solo letras minúsculas a-z, números 0-9 y guion bajo (`_`). Ejemplo válido: `educar_ocr.png`.
- Coloca los drawables en `app/src/main/res/drawable/` y los iconos adaptativos en `mipmap-anydpi-v26/` o usa el asistente de "Image Asset" de Android Studio para generar `ic_launcher` correctamente.
- Si ves errores del tipo: `resource drawable/educar_ocr not found` o `is not a valid file-based resource name character`, revisa que el nombre del archivo sea `educar_ocr.png` (todo en minúsculas) y que las referencias en XML coincidan.

---

## Problemas conocidos y recomendaciones OCR

Observaciones registradas por el equipo:
- La librería OCR puede unir o separar líneas de forma diferente a Tesseract. Esto provoca que campos como `Unique Hardware ID` y `Boot Tick` aparezcan desordenados o en líneas separadas.
- En algunos casos el texto aparece en español (p. ej. `ID único de hardware`) con acentos o espacios extraños, lo que dificulta el emparejado exacto por palabra clave.

Sugerencias para mejorar la extracción (resumido, acciones posibles):
1. Normalizar el texto OCR antes de parsear: convertir a ASCII básico, eliminar caracteres no imprimibles, normalizar espacios y guiones.
2. Usar expresiones regulares robustas para capturar los valores:
   - Para `Unique Hardware ID`: buscar patrones hexadecimales del tipo `([0-9A-Fa-f]{2}(-| |:)){5}[0-9A-Fa-f]{2}` y luego eliminar separadores.
   - Para `Boot Tick`: buscar cerca de la ocurrencia de `Boot` o variantes (`boot`, `Boot Tick`, `Marca de arranque`, `Marca de arranque :`) y extraer el primer grupo de bytes hex (o decimal) de longitud esperada; si hay ambigüedad, preferir el grupo con contenido no nulo (p. ej. `17` en vez de `00`).
3. En vez de depender de la posición por línea, buscar por contexto semántico (palabra clave + valor más cercano) en el texto completo.
4. Añadir heurísticas para casos en que el OCR invierta el orden (p. ej. cuando los campos están en español y el hardware ID aparece antes de la etiqueta). Una heurística: si se detectan 12 bytes hex consecutivos, interpretarlos como `Unique Hardware ID` independientemente de la posición relativa de la etiqueta.

Estos puntos ya están reflejados en la lógica actual del proyecto, pero es útil mantenerlos en la documentación y como tareas futuras.

---

## Pruebas y validación

- Prueba manual: usa imágenes de ejemplo (casos reales) y verifica que el `Unique Hardware ID` y `Boot Tick` coincidan con lo esperado.
- Añadir tests unitarios para la función de parseo (string -> datos). Recomendado: convertir la lógica de extracción en una clase/util con pruebas JUnit que cubran los diferentes casos.

---

## Estilo y refactor sugerido

El proyecto inicialmente tenía mucha lógica en `MainActivity`. 
Para mejorar mantenibilidad se refactorizó parcialmente, pero se recomienda un refactor adicional:

- Mover la lógica de OCR y parseo a una clase `OcrParser` en `com.example.educarocr.ocr`.
- Mantener la UI en `ui.screens` (o `ui.*`) y organizar las composables por pantalla.
- Extraer utilidades (formatos, regex) a `util/`.
- Seguir principios de arquitectura MVVM o similar para separar lógica de UI y negocio.
- Añadir tests unitarios para la lógica de parseo.
- Usar ViewModels para manejar estados y lógica de UI.
- Considerar usar módulos separados si el proyecto crece (p. ej. `ocr`, `ui`, `data`).
- Documentar funciones y clases clave con KDoc.

---

## Contribución

1. Crea una rama `feature/<nombre>` desde `dev`.
2. Añade cambios y tests.
3. Envía un PR hacia `dev` con descripción de cambios y pruebas.

---

## Licencia

MIT License

Copyright (c) 2025 Sergio Mamani

---
