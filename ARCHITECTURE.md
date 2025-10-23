# EducarOCR - Arquitectura del Proyecto

## 📁 Estructura del Proyecto

El proyecto ahora sigue una **arquitectura limpia y profesional** basada en **MVVM + Clean Architecture**.

```
app/src/main/java/com/example/educarocr/
├── MainActivity.kt                          # ✅ Solo navegación (62 líneas)
├── ui/
│   ├── screens/
│   │   ├── MainScreen.kt                   # Pantalla principal (captura)
│   │   ├── PreviewScreen.kt                # Vista previa y recorte
│   │   └── OCRResultScreen.kt              # Resultados del OCR
│   ├── viewmodel/
│   │   └── OCRViewModel.kt                 # ViewModel (opcional, para futuro)
│   └── theme/
│       └── EducarOCRTheme.kt              # Tema de la app
├── domain/
│   ├── model/
│   │   └── OCRResult.kt                    # Modelo de datos
│   └── usecase/
│       └── NormalizeOCRUseCase.kt          # Lógica de normalización
├── data/
│   ├── repository/
│   │   └── OCRRepository.kt                # Repositorio de datos
│   └── datasource/
│       └── MLKitOCRDataSource.kt           # Integración con ML Kit
└── utils/
    └── BitmapUtils.kt                       # Utilidades de imagen
```

## 🎯 Ventajas de la Nueva Arquitectura

### ✅ **Separación de responsabilidades**
- **MainActivity**: Solo maneja navegación entre pantallas
- **Screens**: Solo UI y eventos de usuario
- **UseCases**: Lógica de negocio aislada y testeable
- **Repository**: Manejo de datos centralizado
- **Utils**: Funciones reutilizables

### ✅ **Mantenibilidad**
- Cada archivo tiene una única responsabilidad
- Fácil encontrar y modificar código específico
- Menos conflictos al trabajar en equipo

### ✅ **Testabilidad**
- Los UseCases pueden testearse sin UI
- Los Repositories pueden mockearse
- Las Utils son funciones puras

### ✅ **Escalabilidad**
- Fácil agregar nuevas funcionalidades
- Puedes agregar más pantallas sin tocar el código existente
- Fácil migrar a Jetpack Navigation o Compose Navigation

## 📊 Comparación: Antes vs Después

| Aspecto | Antes | Después |
|---------|-------|---------|
| MainActivity | ~650 líneas | 62 líneas |
| Archivos | 1 archivo gigante | 11 archivos organizados |
| Testeable | ❌ Difícil | ✅ Fácil |
| Mantenible | ❌ Difícil | ✅ Fácil |
| Profesional | ❌ No | ✅ Sí |

## 🔄 Flujo de Datos

```
Usuario → MainScreen 
          ↓
      PreviewScreen
          ↓
   OCRRepository → MLKitOCRDataSource
          ↓
      OCRResultScreen
          ↓
   NormalizeOCRUseCase
          ↓
    Datos procesados
```

## 🚀 Cómo Agregar Nuevas Funcionalidades

### 1. **Agregar una nueva pantalla**
Crea un nuevo archivo en `ui/screens/`:
```kotlin
@Composable
fun MiNuevaPantalla(onBack: () -> Unit) {
    // Tu código aquí
}
```

### 2. **Agregar lógica de negocio**
Crea un nuevo UseCase en `domain/usecase/`:
```kotlin
class MiNuevoUseCase {
    fun execute(input: String): Result {
        // Tu lógica aquí
    }
}
```

### 3. **Agregar una nueva fuente de datos**
Crea un nuevo DataSource en `data/datasource/`:
```kotlin
class MiNuevoDataSource {
    suspend fun getData(): Result<Data> {
        // Tu código aquí
    }
}
```

## 🎨 Icono de la Aplicación

El icono se ha configurado correctamente en:
- `res/drawable/educarocr.png` - Tu icono personalizado
- `res/mipmap-anydpi-v26/ic_launcher.xml` - Icono adaptativo (Android 8+)
- `res/values/ic_launcher_background.xml` - Color de fondo blanco

## 📝 Notas Técnicas

### Dependencias agregadas:
- `lifecycle-viewmodel-compose` - Para ViewModels en Compose
- `lifecycle-runtime-compose` - Para ciclo de vida en Compose
- `kotlinx-coroutines-android` - Para operaciones asíncronas
- `kotlinx-coroutines-play-services` - Para integración con ML Kit

### Patrones utilizados:
- **MVVM**: Separación entre Vista y Lógica
- **Clean Architecture**: Capas bien definidas
- **Repository Pattern**: Abstracción de fuentes de datos
- **Use Case Pattern**: Encapsulación de lógica de negocio
- **Sealed Classes**: Para estados de UI

## 🔧 Próximos Pasos Recomendados

1. **Migrar a Jetpack Navigation**: Para navegación más robusta
2. **Agregar pruebas unitarias**: Para UseCases y Repository
3. **Implementar Room Database**: Para guardar historial de OCR
4. **Agregar Dependency Injection**: Con Hilt o Koin
5. **Implementar manejo de estados**: Con StateFlow completo

## 📚 Recursos

- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [MVVM Pattern](https://developer.android.com/topic/architecture)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [ML Kit Text Recognition](https://developers.google.com/ml-kit/vision/text-recognition)

---

**Autor**: Refactorizado a arquitectura profesional  
**Fecha**: 2025-10-22  
**Versión**: 2.0

