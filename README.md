# 💊 Pastillero Fácil

> Aplicación Android accesible para adultos mayores que facilita el recordatorio y registro puntual de la toma de medicamentos, con soporte de voz e integración de IA.

---

## 📋 Descripción

**Pastillero Fácil** es una aplicación Android nativa diseñada especialmente para **adultos mayores**. Su objetivo principal es ayudar a llevar un control riguroso y sencillo de los medicamentos del día a día, enviando alertas puntuales y permitiendo confirmar cada toma desde la notificación misma o desde la app.

La aplicación combina una interfaz de alta legibilidad, soporte de entrada por voz en español y notificaciones inteligentes con modo insistente para garantizar que el usuario no olvide ninguna dosis.

---

## ✨ Funcionalidades principales

| Funcionalidad | Descripción |
|---|---|
| 🏠 **Pantalla Hoy** | Muestra todas las tomas programadas para el día actual con su estado (pendiente / confirmada) |
| 💊 **Gestión de medicamentos** | Agregar, listar y eliminar medicamentos con nombre, frecuencia y hora de inicio |
| 🗓️ **Historial** | Registro completo de todas las tomas pasadas ordenadas por fecha |
| 👤 **Perfil de usuario** | Nombre, foto de perfil y configuración del modo de notificaciones |
| 🔔 **Notificaciones exactas** | Alarmas precisas con vibración y sonido, incluso con el teléfono en reposo |
| ⚠️ **Modo insistente** | Reintento automático de notificaciones cada 10 minutos si la toma no se confirma |
| 🎤 **Entrada por voz** | Dictar medicamentos en español ("Paracetamol cada 8 horas") |
| 📲 **Acción directa en notificación** | Botón "¡YA LA TOMÉ!" directamente en la notificación del sistema |
| 🔁 **Persistencia tras reinicio** | BootReceiver reprograma alarmas automáticamente tras reiniciar el dispositivo |

---

## 🏗️ Arquitectura del proyecto

El proyecto sigue el patrón **MVVM (Model-View-ViewModel)** con arquitectura en capas:

```
com.example
├── MainActivity.kt                  # Punto de entrada, permisos y tema
│
├── data/                            # Capa de datos (Room)
│   ├── AppDatabase.kt               # Singleton de la base de datos Room
│   ├── AppDao.kt                    # DAO con queries de Room
│   ├── MedicationEntity.kt          # Entidad "medicamentos"
│   ├── TomaRecordEntity.kt          # Entidad "registros_toma"
│   ├── UserProfile.kt               # Entidad "user_profile"
│   └── MedicationRepository.kt     # Repositorio con toda la lógica de negocio
│
├── notification/                    # Sistema de notificaciones y alarmas
│   ├── AlarmScheduler.kt            # Programación de alarmas exactas (AlarmManager)
│   └── NotificationHelper.kt        # Construcción y publicación de notificaciones
│
├── receiver/                        # BroadcastReceivers de Android
│   ├── AlarmReceiver.kt             # Recibe el disparo de la alarma y muestra notificación
│   ├── NotificationActionReceiver.kt # Confirma toma desde la notificación
│   └── BootReceiver.kt              # Reprograma alarmas tras reinicio del dispositivo
│
├── voice/                           # Entrada por voz
│   └── VoiceInputHelper.kt          # Intent de reconocimiento y parser de texto en español
│
└── ui/                              # Capa de presentación (Jetpack Compose)
    ├── PastilleroFacilApp.kt        # Scaffold principal: TopBar, BottomNav, FAB y navegación
    ├── MainViewModel.kt             # ViewModel principal con StateFlow
    ├── components/
    │   └── SeniorComponents.kt      # Componentes reutilizables de alto contraste
    ├── screens/
    │   ├── TodayTomasScreen.kt      # Pantalla "Hoy" con lista de tomas del día
    │   ├── MedicationsListScreen.kt # Lista y gestión de medicamentos activos
    │   ├── HistoryScreen.kt         # Historial de tomas por fecha
    │   ├── ProfileSettingsScreen.kt # Configuración de perfil y notificaciones
    │   ├── AddMedicationDialog.kt   # Diálogo para agregar medicamento (manual + voz)
    │   └── UserRegistrationDialog.kt # Diálogo de registro inicial de nombre y foto
    └── theme/
        ├── Color.kt                 # Paleta de colores "Natural Tones" (alto contraste)
        ├── Theme.kt                 # Tema Material3 de la aplicación
        └── Type.kt                  # Tipografía personalizada
```

---

## 🗄️ Base de datos (Room)

La base de datos local se llama `pastillero_facil_db` y contiene **3 tablas**:

### `user_profile`
| Campo | Tipo | Descripción |
|---|---|---|
| `id` | Int (PK) | Siempre = 1 (perfil único) |
| `nombre` | String | Nombre del usuario |
| `fotoPerfil` | String? | URI de la foto (opcional) |
| `modoInsistente` | Boolean | Activa reintento automático de notificaciones |

### `medicamentos`
| Campo | Tipo | Descripción |
|---|---|---|
| `id` | Long (PK auto) | ID único |
| `nombre` | String | Nombre del medicamento |
| `frecuenciaHoras` | Int | Frecuencia de toma (ej. 8 = cada 8 horas) |
| `horaInicio` | String | Hora de la primera toma (formato HH:mm) |
| `horariosCalculados` | String | Lista separada por comas ("08:00,16:00,00:00") |
| `activo` | Boolean | Si el medicamento está activo |
| `fechaCreacion` | Long | Timestamp de creación |

### `registros_toma`
| Campo | Tipo | Descripción |
|---|---|---|
| `id` | Long (PK auto) | ID único |
| `medicamentoId` | Long | Referencia al medicamento |
| `medicamentoNombre` | String | Nombre (desnormalizado para historial) |
| `fechaHoraProgramada` | Long | Timestamp de la toma programada |
| `horarioTexto` | String | Texto legible (ej. "08:00") |
| `confirmado` | Boolean | Si el usuario confirmó la toma |
| `fechaHoraConfirmacion` | Long? | Timestamp de confirmación |
| `fechaKey` | String | Clave de fecha "yyyy-MM-dd" para agrupación |

---

## 🔔 Sistema de notificaciones y alarmas

### Flujo de una alarma

```
App lanza → AlarmScheduler.scheduleMedicationToma()
    ↓ (hora exacta)
AlarmManager dispara → AlarmReceiver.onReceive()
    ↓
NotificationHelper.showMedicationNotification()
    ↓ (muestra notificación con botón)
Usuario pulsa "¡YA LA TOMÉ!" → NotificationActionReceiver.onReceive()
    ↓
Repository.confirmToma() → cancela alarma de reintento
```

### Modo insistente
Si el usuario **no confirma** la toma, tras 10 minutos se lanza una alarma de reintento con un título de aviso distinto:
- Normal: *"💊 ¡Hora de su medicamento!"*
- Insistente: *"⚠️ ¡Aviso importante! No olvide su medicina"*

### Recuperación tras reinicio
`BootReceiver` escucha `BOOT_COMPLETED` y reprograma todas las alarmas pendientes (tomas del día no confirmadas y en el futuro).

---

## 🎤 Entrada por voz

`VoiceInputHelper` crea un Intent para el reconocedor de voz del sistema en español (`es-ES`) y parsea frases como:

| Frase hablada | Resultado |
|---|---|
| "Paracetamol cada 8 horas" | nombre=Paracetamol, freq=8h |
| "Tomar Losartán cada doce horas" | nombre=Losartán, freq=12h |
| "Aspirina una vez al día" | nombre=Aspirina, freq=24h |
| "Enalapril cada 6 horas" | nombre=Enalapril, freq=6h |

---

## 🎨 Diseño y accesibilidad

La paleta de colores **"Natural Tones"** está diseñada para alto contraste y legibilidad en adultos mayores:

- **Primario**: Azul cobalto `#005FB0`
- **Fondo**: Blanco cálido `#FDFBFF`
- **Texto principal**: Gris carbón `#1B1B1F`
- **Éxito (confirmado)**: Verde bosque `#006E1C`
- **Alerta**: Rojo carmesí `#BA1A1A`
- **Advertencia**: Naranja `#C04B00`

La tipografía y los tamaños de fuente están calibrados para facilitar la lectura (mínimo 15sp en etiquetas secundarias, 26sp en el título principal).

---

## 🧭 Navegación

La app utiliza una **barra de navegación inferior** con 4 pestañas gestionadas por `selectedTab` en el `MainViewModel`:

| Tab | Ícono | Pantalla |
|---|---|---|
| 0 - Hoy | Today | TodayTomasScreen |
| 1 - Medicinas | Medication | MedicationsListScreen |
| 2 - Historial | History | HistoryScreen |
| 3 - Perfil | Person | ProfileSettingsScreen |

El **FAB** (botón flotante "Agregar Medicina") aparece en las pestañas Hoy y Medicinas.

---

## 🛠️ Stack tecnológico

| Tecnología | Versión | Uso |
|---|---|---|
| **Kotlin** | 2.2.10 | Lenguaje principal |
| **Android Gradle Plugin** | 9.1.1 | Build system |
| **Jetpack Compose** | BOM 2024.09.00 | UI declarativa |
| **Material3** | (via BOM) | Sistema de diseño |
| **Room** | 2.7.0 | Base de datos local |
| **KSP** | 2.3.5 | Procesador de anotaciones |
| **Kotlin Coroutines** | 1.10.2 | Asincronía |
| **Firebase AI (Gemini)** | BOM 34.17.0 | IA generativa integrada |
| **Firebase App Check** | (via BOM) | Seguridad de la API |
| **Coil** | 2.7.0 | Carga de imágenes |
| **Retrofit + Moshi** | 2.12.0 / 1.15.2 | HTTP client + JSON |
| **OkHttp** | 4.10.0 | HTTP networking |
| **Robolectric + Roborazzi** | 4.16.1 / 1.59.0 | Tests unitarios y screenshots |
| **Secrets Gradle Plugin** | 2.0.1 | Manejo seguro de API keys |

---

## 📱 Requisitos

- **Android mínimo**: API 24 (Android 7.0 Nougat)
- **Android objetivo**: API 36
- **Permisos requeridos**:
  - `POST_NOTIFICATIONS` — Para enviar recordatorios
  - `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` — Para alarmas precisas
  - `RECORD_AUDIO` — Para entrada por voz
  - `RECEIVE_BOOT_COMPLETED` — Para reprogramar alarmas tras reinicio
  - `VIBRATE` — Para vibración en notificaciones

---

## ⚙️ Configuración y variables de entorno

El proyecto usa el plugin **Secrets Gradle** para gestionar claves de forma segura:

```bash
# .env (en la raíz del proyecto, NO subir a git)
GEMINI_API_KEY=tu_clave_aqui
```

Copia `.env.example` a `.env` y configura tu `GEMINI_API_KEY` de Google AI Studio.

Para la firma de release se usan variables de entorno:
```bash
KEYSTORE_PATH=/ruta/a/mi-keystore.jks
STORE_PASSWORD=contraseña_store
KEY_PASSWORD=contraseña_key
```

---

## 🔥 Integración con Firebase

El proyecto incluye soporte para **Firebase AI (Gemini)** a través de la dependencia `firebase-ai`. La configuración de Firebase requiere el archivo `google-services.json` en `app/`. Si no está presente, el build continúa con una advertencia.

Dependencias de Firebase disponibles:
- ✅ **Firebase AI** — IA generativa con Gemini
- ✅ **Firebase App Check** (reCAPTCHA + debug)
- ⬜ Firebase Firestore (comentado, disponible para activar)
- ⬜ Firebase Auth + Google Sign-In (comentado, disponible para activar)

---

## 🏗️ Compilar y ejecutar

### Debug
```bash
./gradlew assembleDebug
```

### Release
```bash
./gradlew assembleRelease
```

### Tests unitarios
```bash
./gradlew test
```

### Tests instrumentados
```bash
./gradlew connectedAndroidTest
```

### Screenshots (Roborazzi)
```bash
./gradlew recordRoborazziDebug
```

---

## 📁 Estructura del proyecto

```
Pastillero-facil/
├── .env.example                      # Plantilla de variables de entorno
├── .gitignore
├── build.gradle.kts                  # Configuración raíz del proyecto
├── gradle.properties                 # Propiedades de Gradle (paralelismo, caché, etc.)
├── metadata.json                     # Metadata del proyecto para AI Studio
├── settings.gradle.kts               # Configuración de módulos
├── gradle/
│   ├── libs.versions.toml            # Catálogo de versiones de dependencias
│   └── wrapper/
├── public/                           # Recursos públicos (AI Studio)
└── app/
    ├── build.gradle.kts              # Configuración del módulo app
    ├── proguard-rules.pro            # Reglas de ofuscación R8
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/example/    # Código fuente Kotlin
        │   └── res/                 # Recursos Android
        ├── test/                    # Tests unitarios (Robolectric)
        └── androidTest/             # Tests de instrumentación (Espresso)
```

---

## 🤝 Créditos

Proyecto desarrollado con asistencia de **Google AI Studio** y las capacidades de **Gemini API**.

---

## 📄 Licencia

Proyecto educativo de uso personal. Todos los derechos reservados © 2026 sanchezluys.
