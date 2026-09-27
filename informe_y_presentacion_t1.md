# 📘 Entregables Académicos T1 - EcoPoints
**Curso:** Desarrollo de Aplicaciones Móviles (NRC 19586)  
**Docente:** Ing. Gerardo Sarmiento Quistán  
**Facultad:** Ingeniería | **Carrera:** Ingeniería de Sistemas Computacionales  
**Semestre:** 2026-2  

---

# PARTE 1: Contenido para la Presentación en Diapositivas (PPTX)
*(Estructurado para una exposición grupal de 20 minutos)*

### Diapositiva 1: Carátula Oficial
* **Título del Proyecto:** EcoPoints: Aplicación Móvil para la Gamificación del Cuidado Ambiental y Reciclaje Urbano
* **Nombre de la App:** EcoPoints
* **Logo:** *(Insertar el logo oficial `ic_logo_ecopoints.png`)*
* **Curso y NRC:** Desarrollo de Aplicaciones Móviles - NRC 19586
* **Docente:** Ing. Gerardo Sarmiento Quistán
* **Integrantes del Grupo:**
  1. [Nombre del Alumno 1] - Código [XXXXX]
  2. [Nombre del Alumno 2] - Código [XXXXX]
  3. [Nombre del Alumno 3] - Código [XXXXX]
  4. [Nombre del Alumno 4] - Código [XXXXX]
  5. [Nombre del Alumno 5] - Código [XXXXX]
* **Fecha:** Septiembre de 2026 - Lima, Perú

---

### Diapositiva 2: Identidad y Concepto de la Aplicación
* **Slogan:** *"Pequeñas acciones, grandes cambios"*
* **Problema Identificado:** Falta de incentivos y hábitos estructurados para el reciclaje y las prácticas sustentables en los ciudadanos.
* **Solución Propuesta:** Una aplicación móvil nativa con mecánicas de **gamificación** (mascota virtual guardiana, puntos acumulables, medallas y retos diarios) que premia las acciones ecológicas cotidianas.
* **Mascotas Guías Disponibles:** Koala 🐨, Panda 🐼, Zorro 🦊, Gato 🐱 y Búho 🦉.

---

### Diapositiva 3: Objetivos del Proyecto
* **Objetivo General:**
  * Desarrollar una aplicación móvil nativa en Android Studio utilizando Kotlin y Jetpack Compose que promueva hábitos ecológicos en los usuarios mediante gamificación y localización de centros de acopio.
* **Objetivos Específicos:**
  1. Diseñar e implementar interfaces de usuario modernas, intuitivas y accesibles con Jetpack Compose Material 3.
  2. Implementar una arquitectura modular basada en múltiples actividades comunicadas fluidamente mediante `Intents` explícitos y transferencia de parámetros con `Bundle/Extras`.
  3. Gestionar la persistencia de datos local eficiente mediante `SharedPreferences` para la sesión, progreso de retos y atributos de la mascota.
  4. Ofrecer una experiencia interactiva que simule el cuidado y desarrollo de un guardián ambiental virtual.

---

### Diapositiva 4: Alcances y Limitaciones
* **Alcances del Proyecto (Para la T1):**
  * Cumplimiento del 75%+ de los requerimientos funcionales previstos.
  * Módulo de autenticación (Login y Registro personalizado con selección y nombramiento de mascota).
  * Módulo de Bienvenida con adjudicación de bono inicial (+50 EcoPoints) e insignia *"Amigo de la Naturaleza"*.
  * Dashboard Principal con saldo en tiempo real, interacción con mascota (alimentar y jugar) y lista interactiva de retos ecológicos.
  * Módulo de configuración de preferencias de usuario y comunidad.
  * Persistencia 100% funcional en almacenamiento local.
* **Limitaciones:**
  * En esta primera entrega (T1), el almacenamiento es puramente local (`SharedPreferences`), sin sincronización con bases de datos en la nube (Firebase/API REST prevista para entregas posteriores).
  * El módulo de mapa se encuentra en fase de previsualización estática de puntos de acopio cercanos.

---

### Diapositiva 5: Requerimientos del Sistema (Product Backlog)
| ID | Historia de Usuario / Requerimiento | Prioridad | Estimación | Estado T1 |
| :---: | :--- | :---: | :---: | :---: |
| **US-01** | Como usuario, quiero registrarme eligiendo una mascota guardián y un nombre para ella. | Alta | 5 pts | ✅ 100% |
| **US-02** | Como usuario, quiero iniciar sesión con mis credenciales para acceder a mi perfil. | Alta | 3 pts | ✅ 100% |
| **US-03** | Como usuario nuevo, quiero recibir un bono de bienvenida (+50 pts) y una medalla inicial. | Media | 2 pts | ✅ 100% |
| **US-04** | Como usuario, quiero visualizar mi saldo de EcoPoints y mi nivel actual. | Alta | 3 pts | ✅ 100% |
| **US-05** | Como usuario, quiero alimentar y jugar con mi mascota consumiendo EcoPoints. | Alta | 5 pts | ✅ 100% |
| **US-06** | Como usuario, quiero completar retos diarios para ganar puntos y aumentar mi nivel. | Alta | 5 pts | ✅ 100% |
| **US-07** | Como usuario, quiero configurar mis notificaciones, ranking y unidades de mapa. | Media | 3 pts | ✅ 100% |
| **US-08** | Como usuario, quiero ubicar puntos de reciclaje en un mapa interactivo con GPS. | Media | 8 pts | ⏳ T2 / Final |

---

### Diapositiva 6: Arquitectura y Comunicación entre Actividades (`Intents`)
* **Modelo Multiactividad:** La aplicación se descompuso en actividades especializadas:
  * `MainActivity`: Actividad de arranque y autenticación.
  * `RegisterActivity`: Gestión del formulario de registro y selección de compañero.
  * `WelcomeActivity`: Presentación de la mascota y entrega de recompensas.
  * `HomeActivity`: Tablero central interactivo y gamificación.
  * `SettingsActivity`: Preferencias de sistema.
* **Flujo de `Intents`** (claves centralizadas en `IntentExtras.kt`):
  * `MainActivity` ➔ `Intent(EMAIL)` ➔ `RegisterActivity` (el correo ya escrito llega precargado)
  * `RegisterActivity` ➔ `Intent(EMAIL)` ➔ `MainActivity` (al volver al login se devuelve el correo)
  * `RegisterActivity` ➔ `Intent(USER_NAME, PET_LEVEL, SELECTED_PET, PET_NAME, IS_NEW_USER)` ➔ `WelcomeActivity`
  * `MainActivity` ➔ `Intent(USER_NAME, PET_LEVEL, IS_NEW_USER)` ➔ `WelcomeActivity` / `HomeActivity`
  * `WelcomeActivity` ➔ `Intent(USER_NAME, PET_LEVEL)` ➔ `HomeActivity`
  * `WelcomeActivity` / `HomeActivity` ➔ `Intent(USER_NAME)` ➔ `SettingsActivity`
  * `SettingsActivity` ➔ **resultado** `setResult(MAP_UNIT, RANKING_VISIBLE, NOTIFICATIONS_ENABLED)` ➔ `HomeActivity` (`registerForActivityResult`)
  * `HomeActivity` / `WelcomeActivity` ➔ `Intent(FLAG_ACTIVITY_CLEAR_TASK, LOGGED_OUT)` ➔ `MainActivity` (Cierre de sesión seguro).

---

### Diapositiva 7: Persistencia de Datos con `SharedPreferences`
* **Implementación:** Clase centralizada `PreferencesManager.kt` que encapsula el acceso seguro a `SharedPreferences` en modo privado (`Context.MODE_PRIVATE`).
* **Datos Persistidos:**
  * Sesión del usuario: `user_logged_in`, `user_name`, `user_email`.
  * Economía del juego: `ecopoints_balance` (gastable) y `ecopoints_historical` (nivel permanente).
  * Estadísticas de la mascota: `pet_level`, `pet_hunger` (0-100), `pet_happiness` (0-100), `last_fed_timestamp`.
  * Retos: `user_challenges` (lista JSON con los retos elegidos por el usuario, su plazo de 1/3/7/14 días y su estado), `daily_challenge_progress`.
  * Configuración: `notifications_enabled`, `ranking_visible`, `map_distance_unit`.

---

### Diapositiva 8: Demostración en Vivo del Aplicativo
*(Momento para compartir pantalla en el emulador de Android Studio)*
* 1. Pantalla de inicio de sesión con validaciones.
* 2. Proceso de registro y selección dinámica de mascota.
* 3. Pantalla de bienvenida con bono otorgado.
* 4. Interacción en el Dashboard: Marcar un reto diario ➔ Aumento automático del saldo de EcoPoints en vivo.
* 5. Alimentar a la mascota ➔ Descuento de puntos y recuperación de energía y felicidad.
* 6. Ajustes y persistencia tras reinicio de la app.

---

### Diapositiva 9: Autoevaluación del Equipo
*(Requisito explícito de la rúbrica)*
| Integrante | Rol en el Proyecto | Participación | Autoevaluación (%) |
| :--- | :--- | :---: | :---: |
| [Alumno 1 - Líder] | Arquitectura de Actividades e Intents | 100% | 100% |
| [Alumno 2] | UI/UX en Jetpack Compose y Animaciones | 100% | 100% |
| [Alumno 3] | Lógica de Gamificación y SharedPreferences | 100% | 100% |
| [Alumno 4] | Módulo de Retos Diarios y EcoMapa | 100% | 100% |
| [Alumno 5] | Documentación técnica, informe y diapositivas | 100% | 100% |

---

### Diapositiva 10: Conclusiones
* Se logró una aplicación móvil nativa robusta que cumple con el 75%+ de los requerimientos para la entrega T1.
* Se implementó con éxito el modelo de comunicación entre actividades mediante `Intents` y la persistencia estructurada mediante `SharedPreferences`.
* El uso de Jetpack Compose permitió crear una interfaz dinámica y moderna con identidad visual propia.

---
---

# PARTE 2: Estructura del Informe Técnico (Hasta Cap. IV)

```text
================================================================================
INFORME TÉCNICO DE INGENIERÍA - EVALUACIÓN T1
CURSO: DESARROLLO DE APLICACIONES MÓVILES (NRC 19586)
DOCENTE: ING. GERARDO SARMIENTO QUISTÁN
================================================================================
```

## CAPÍTULO I: INTRODUCCIÓN Y GENERALIDADES DEL PROYECTO

### 1.1. Título del Proyecto
**EcoPoints:** Plataforma móvil gamificada para el fomento de prácticas ecológicas y gestión de reciclaje urbano en entornos comunitarios.

### 1.2. Descripción y Justificación del Proyecto
En la actualidad, uno de los mayores desafíos ambientales en las áreas urbanas radica en la baja tasa de segregación de residuos y la falta de constancia ciudadana en hábitos sustentables. Aunque existen programas de reciclaje, los usuarios suelen carecer de retroalimentación inmediata o motivación intrínseca para mantener conductas ecológicas cotidianas.

**EcoPoints** aborda esta problemática aplicando técnicas de **gamificación** (economía de puntos virtuales, adopción de una mascota virtual interactiva, insignias por logros y retos periódicos) combinadas con la tecnología móvil nativa de Android, transformando una obligación cívica en una experiencia divertida, medible y gratificante.

### 1.3. Objetivos del Proyecto
* **Objetivo General:**
  * Desarrollar una solución móvil nativa para la plataforma Android que incentive la reducción de la huella de carbono individual mediante retos diarios y el cuidado de una mascota ecológica virtual.
* **Objetivos Específicos:**
  * Construir interfaces gráficas responsivas y declarativas basadas en Jetpack Compose y Material Design 3.
  * Establecer un flujo de navegación inter-actividades utilizando `Intents` explícitos y transporte de datos mediante `Bundle/Extras`.
  * Gestionar el almacenamiento persistente local de configuraciones, estado de autenticación y puntuaciones mediante la API de `SharedPreferences`.
  * Prototipar el módulo de geolocalización básica para futuros servicios de mapeo de centros de acopio.

### 1.4. Alcances y Limitaciones
* **Alcances:** Para la evaluación T1 se abarca el flujo completo de usuario desde el registro, selección de guardián ambiental, bienvenida con recompensa inicial, panel de administración de retos y alimentación de la mascota, hasta el módulo de ajustes de sistema.
* **Limitaciones:** La versión actual no requiere conexión a internet permanente ni sincronización remota con servicios en la nube (previstos para la evaluación T2/Final).

---

## CAPÍTULO II: REQUERIMIENTOS DEL SISTEMA Y PRODUCT BACKLOG

### 2.1. Requerimientos Funcionales (RF)
* **RF-01 (Autenticación y Sesión):** El sistema debe permitir a los usuarios iniciar sesión con correo y contraseña (mínimo 8 caracteres) y mantener la sesión abierta mediante persistencia local.
* **RF-02 (Registro con Mascota Guía):** El sistema debe permitir al usuario registrar su nombre, correo, elegir una especie de mascota (Koala, Panda, Zorro, Gato, Búho) y asignarle un nombre personalizado.
* **RF-03 (Bono de Bienvenida):** Al completar el registro, el sistema debe acreditar automáticamente 50 EcoPoints y desbloquear la insignia de nivel 1 ("Amigo de la Naturaleza").
* **RF-04 (Dashboard de Gamificación):** La pantalla principal debe mostrar en tiempo real el saldo de EcoPoints, el nivel y el estado vital de la mascota (Hambre y Felicidad de 0 a 100%).
* **RF-05 (Interacción con Mascota):** El usuario podrá gastar 10 EcoPoints para alimentar a su mascota (recuperando 20% de energía y 10% de felicidad) o interactuar con ella para aumentar su felicidad.
* **RF-06 (Retos Ecológicos):** El usuario elige qué retos sustentables quiere cumplir (movilidad en bicicleta, reciclaje de botellas, o uno propio), define su plazo (1, 3, 7 o 14 días), puede editarlos y eliminarlos, y al marcarlos como cumplidos suma EcoPoints a su cuenta instantáneamente (más plazo, más recompensa). Marcar un reto se puede deshacer mientras los puntos no se hayan gastado.
* **RF-07 (Configuración y Ajustes):** El sistema debe permitir configurar la visibilidad en el ranking, recordatorios de retos y unidades de medición métricas/imperiales.

### 2.2. Requerimientos No Funcionales (RNF)
* **RNF-01 (Rendimiento):** Las transiciones entre actividades y el refresco del estado visual deben ejecutarse a 60 fps sin bloqueos en el hilo principal (`Main Thread`).
* **RNF-02 (Compatibilidad):** El aplicativo debe operar en dispositivos con Android 7.0 (API 24) o superior, con optimización para pantallas modernas (API 34+).
* **RNF-03 (Usabilidad y Accesibilidad):** Interfaz limpia, con contraste de color verde ecológico (`#2E7D32`), tipografía clara y retroalimentación mediante `Toast` y componentes Material 3.
* **RNF-04 (Mantenibilidad):** Separación modular del código en paquetes organizados (`data`, `ui`, `theme`, actividades individuales).

### 2.3. Product Backlog
Se organizó el trabajo bajo el marco de trabajo ágil Scrum:
1. **Épica 1: Identidad y Onboarding:** Historias de usuario US-01, US-02 y US-03 (Completadas al 100%).
2. **Épica 2: Gamificación y Core Loop:** Historias US-04, US-05 y US-06 (Completadas al 100%).
3. **Épica 3: Ajustes y Preferencias:** Historia US-07 (Completada al 100%).
4. **Épica 4: EcoMapa y Geolocalización:** Historia US-08 (Fase de previsualización para la T2).

---

## CAPÍTULO III: ARQUITECTURA Y DISEÑO DE LA APLICACIÓN

### 3.1. Arquitectura de Navegación y Comunicación entre Actividades (`Intents`)
El proyecto cumple estrictamente con el principio de navegación desacoplada mediante actividades independientes comunicadas por `Intents`:
* **`MainActivity`:** Punto de entrada registrado como `LAUNCHER`; el inicio de sesión es siempre la pantalla inicial. Si hay una sesión guardada en `SharedPreferences`, ofrece el botón "Continuar como…" que envía un `Intent` hacia `HomeActivity`; además valida el formato del correo y ofrece enlace a `RegisterActivity`, a la que le pasa el correo ya escrito.
* **`RegisterActivity`:** Captura las preferencias del nuevo usuario y empaqueta en el `Intent` los datos seleccionados (`EXTRA_USER_NAME`, `EXTRA_PET_LEVEL`, `EXTRA_SELECTED_PET`) despachándolos a `WelcomeActivity`.
* **`WelcomeActivity`:** Desempaqueta los extras del `Intent` o de `PreferencesManager` y despliega la animación de bienvenida. Conecta hacia `HomeActivity` o `SettingsActivity`.
* **`HomeActivity`:** Núcleo de la experiencia donde convergen el saldo, la mascota y los retos.
* **`SettingsActivity`:** Control de preferencias del sistema. Recibe el nombre del usuario por `Intent` y devuelve a la actividad que la abrió el resultado de los ajustes (unidad de mapa, ranking y notificaciones).

### 3.2. Diseño de Interfaces (UI/UX) con Jetpack Compose
* **Paradigma Declarativo:** La interfaz no utiliza layouts XML tradicionales, sino funciones `@Composable` modulares y reactivas, facilitando la recomposición instantánea de las barras de progreso y el contador de puntos.
* **Sistema de Diseño (Design System):** Centralizado en `Color.kt` y `Theme.kt`, basado en una paleta cromática ecológica:
  * `EcoGreen` (`#2E7D32`): Identidad primaria y acciones positivas.
  * `EcoGreenDark` (`#1B5E20`): Tipografías de encabezados y bordes destacados.
  * `EcoGreenLight` (`#E9F7EF`): Fondos de tarjetas y contenedores suaves.
  * `EcoGold` (`#B8860B`): Insignias y logros de nivel.

### 3.3. Modelo de Persistencia Local (`SharedPreferences`)
La clase `PreferencesManager` provee métodos públicos tipados para garantizar consistencia:
* Separación de saldo: El método `earnEcoPoints(amount)` suma tanto al saldo disponible (`ecopoints_balance`) como al acumulador de nivel (`ecopoints_historical`).
* El método `spendEcoPoints(amount)` descuenta únicamente del saldo disponible, impidiendo que el saldo caiga por debajo de cero y protegiendo el nivel histórico alcanzado por el usuario.

---

## CAPÍTULO IV: HERRAMIENTAS DE INGENIERÍA Y TECNOLOGÍAS UTILIZADAS

### 4.1. Entorno de Desarrollo y Plataforma
* **IDE Oficial:** Android Studio (Ladybug / Koala Feature Drop).
* **Lenguaje de Programación:** Kotlin versión 2.2.10.
* **Build System:** Gradle 9.5.0 con Android Gradle Plugin (AGP) 9.3.3 y Kotlin DSL (`build.gradle.kts` con Version Catalogs `libs.versions.toml`).
* **Java Development Kit (JDK):** JetBrains Runtime (JBR) versión 17/21.

### 4.2. Librerías y Dependencias Principales
* `androidx.compose.material3:material3`: Componentes visuales basados en Material 3 (Cards, Scaffold, TopAppBar, Buttons, Sliders).
* `androidx.activity:activity-compose:1.13.0`: Puente de integración entre el ciclo de vida de `ComponentActivity` y el árbol de composición de Compose.
* `androidx.core:core-ktx:1.19.0`: Extensiones idiomáticas de Kotlin para las APIs de Android.

### 4.3. Entorno de Pruebas y Emulación
* **Dispositivo de Pruebas Virtual (AVD):** Google Pixel 8.
* **Nivel de API:** Android 14.0 (UpsideDownCake - API 34).
* **Aceleración Gráfica:** Host GPU Acceleration (OpenGL ES / Vulkan habilitado sobre tarjeta dedicada NVIDIA GeForce RTX 5070).
* **Almacenamiento del AVD:** Despliegue en unidad secundaria de alto rendimiento (`D:\Android_AVD`) vinculado mediante NTFS Junction Directory para máxima eficiencia de lectura/escritura.
