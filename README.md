# Tramas — Tienda virtual Android (frontend)

Marketplace móvil de artesanías y productos regionales de Colombia. Este repositorio contiene:

| Carpeta | Contenido |
|---|---|
| `app/` | Proyecto Android Studio (Kotlin, XML, ViewBinding, Navigation, Material 3) |
| `docs/` | Planeación del proyecto (PDF) y script que lo genera |
| `release/` | APK de validación (`Tramas-debug.apk`) |
| `tools/` | Scripts auxiliares (generan íconos/ilustraciones vectoriales y extraen strings) |

## Cómo abrirlo
1. Android Studio Koala o superior, JDK 17+.
2. `File > Open` → carpeta raíz del repositorio. Gradle sincroniza solo.
3. Ejecutar la configuración `app` en un emulador o dispositivo con Android 8.0 (API 26) o superior.
4. APK por línea de comandos: `./gradlew assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`.

## Probar los tres roles
En la pantalla de inicio de sesión escribe cualquier correo válido y una contraseña de 6+ caracteres, y elige el rol en **"Probar la app como"** (Comprador, Vendedor o Administrador). También puedes entrar con **huella**: si el equipo no tiene huella registrada, la app ofrece continuar en modo demostración.

## Pantallas implementadas
**Transversal:** splash, inicio de sesión, autenticación con biometría (BiometricPrompt), registro, recuperar contraseña, menú lateral (Drawer) + Bottom Navigation por rol.

**Comprador:** inicio, catálogo (búsqueda + filtro por categoría), detalle de producto, carrito (CRUD), pasarela de pagos (dirección con geolocalización, tarjeta/PSE/Nequi, confirmación), mis pedidos, perfil (foto con cámara).

**Vendedor:** panel de productos (CRUD completo con foto por cámara y GPS), historial de pedidos recibidos (por estado), perfil de vendedor.

**Administrador:** dashboard (KPIs y gráfico propio), gestión de usuarios (listar, crear, editar, eliminar), gestión/moderación de productos, reporte de ventas.

## Arquitectura de la interfaz
- `ui/host/RoleHostActivity`: contenedor único (Toolbar + Drawer + BottomNavigation + NavHost). Cada rol define solo su **tema**, **grafo de navegación** (`res/navigation`) y **menús** (`res/menu`).
- Un fragment por pantalla con **ViewBinding**; listas con `RecyclerView` y un `BindingAdapter` genérico.
- `data/Repo`: repositorio **en memoria** con datos de ejemplo y CRUD real; sustituye al backend en esta fase (Firebase y Wompi quedan para la siguiente).

## Recursos (values, drawables, estilos)
- `values/colors.xml`: paleta propia (teal, terracota, índigo, ocre). **Cada rol tiene su tema** (`Theme.Tramas.Buyer / Seller / Admin`) en `values/themes.xml`.
- `values/styles.xml`: estilos de botones, campos de texto, tarjetas, chips, toolbar, bottom nav, tipografías y formas.
- `values/strings.xml` y `strings_app.xml`: todos los textos en español (con plurales).
- `drawable/`: íconos vectoriales, ilustraciones de productos (`art_*`), fondos con gradiente, patrón tejido y logo.
- `mipmap-anydpi-v26/`: ícono adaptativo.

## Regenerar recursos
```
python3 tools/gen_icons.py       # íconos ic_*.xml
python3 tools/gen_drawables.py   # ilustraciones art_*.xml, logo y patrón
python3 tools/extract_strings.py # convierte "@@Texto" de los layouts en @string/…
```

## Capturas
Generadas automáticamente con Robolectric (`ScreensTest`), una por pantalla: carpeta [`docs/capturas`](docs/capturas).
Para regenerarlas: `./gradlew testDebugUnitTest -PshotsDir=$PWD/docs/capturas`.
