# RideAdvisor

Asistente para conductores de plataformas de transporte (Uber, inDrive, DiDi) que
detecta solicitudes de viaje mediante notificaciones del sistema, las evalúa contra
tus filtros (precio mínimo, distancia máxima, apps permitidas) y te avisa con un
panel flotante cuando una solicitud vale la pena. **No pulsa nada dentro de esas
apps**: solo lee y analiza sus notificaciones. La decisión final siempre es tuya.

## Cómo abrir el proyecto

1. Abre Android Studio (Iguana o más reciente recomendado).
2. `File → Open` y selecciona la carpeta `RideAdvisor/`.
3. Deja que Gradle sincronice (usa AGP 8.3.2, Kotlin 1.9.24, compileSdk 34, minSdk 24).
4. Ejecuta en un dispositivo físico o en un emulador/BlueStacks 5.

## Primer uso

Al abrir la app por primera vez, un asistente de 6 pasos te guía para:
1. Activar el acceso a notificaciones (Notification Listener).
2. Activar "Mostrar sobre otras apps" (overlay).
3. Configurar el precio mínimo.
4. Configurar la distancia máxima.
5. Elegir qué apps quieres monitorear (Uber / inDrive / DiDi).
6. Activar RideAdvisor.

## Estructura del proyecto

```
app/src/main/java/com/hassiel/rideadvisor/
├── data/            Modelos (RideRequest, RideResult, UserPreferences, HistoryEntry)
├── db/               Room: entidad, DAO y base de datos del historial
├── repository/       PreferencesRepository (DataStore) y RideRepository (Room)
├── filter/            RideFilterEngine + criterios de filtrado individuales
├── service/           RideNotificationService (Notification Listener) y OverlayService
├── utils/             NotificationParser, PermissionManager, Constants
└── ui/                 Splash, Onboarding, MainActivity + Fragments (Dashboard, Radar, Settings, History)
```

## Notas técnicas

- **Notification Listener**: en algunos fabricantes (Xiaomi, Huawei, etc.) hay que
  además desactivar la optimización de batería para que el listener no se
  desconecte en segundo plano.
- **BlueStacks 5**: el permiso de "Notification Listener" se activa igual que en un
  dispositivo real, desde Ajustes → Apps → Acceso especial → Acceso a notificaciones.
- **Parser de notificaciones**: `NotificationParser` es tolerante a formatos con o
  sin símbolo de moneda, con coma o punto decimal, y a distancias en km o millas.
  Si un dato no está presente en el texto, se devuelve `null` (nunca se inventa).
- **Ampliar filtros**: para agregar un nuevo criterio (precio/km, ganancia estimada,
  horarios, etc.) solo hay que crear una clase que implemente `FilterCriterion` en
  `filter/FilterCriteria.kt` y añadirla en `RideFilterEngine.buildCriteria()`.
- **Ampliar apps soportadas**: agrega el paquete Android correspondiente en
  `Constants.SUPPORTED_PACKAGES`.

## Advertencia de uso

Revisa los términos de servicio de las plataformas (Uber, inDrive, DiDi) en tu país
antes de usar herramientas de este tipo mientras trabajas como conductor.
