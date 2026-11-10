# Guion del video (máx. 5 min) — Interfaces de Tramas

Graba pantalla de Android Studio + emulador. Habla en primera persona y señala el código mientras lo explicas.

**0:00 – 0:30 · Introducción**
Nombre del proyecto (Tramas), qué es y los tres roles. Abre el árbol del proyecto y muestra `ui/`, `res/layout`, `res/values`, `res/drawable`, `res/navigation`, `res/menu`.

**0:30 – 1:20 · Values y estilos** (`res/values`)
- `colors.xml`: paleta propia (teal, terracota, índigo, ocre) — no usa los colores por defecto.
- `themes.xml`: tema base `Theme.Tramas` y un tema por rol (`.Buyer`, `.Seller`, `.Admin`) que cambia `colorPrimary`. Mostrar cómo `RoleHostActivity` hace `setTheme(themeRes)`.
- `styles.xml`: `Widget.Tramas.Button`, `TextInput`, `Card`, `Chip`, `BottomNav`, `TextAppearance.Tramas.*`.
- `dimens.xml` y `strings.xml` (textos y plurales en español).

**1:20 – 2:00 · Drawables**
- `ic_*.xml` (vectores), `art_*.xml` (ilustraciones de producto hechas con paths), `bg_header_primary.xml` (gradient con atributos del tema), `bg_selectable_option.xml` (selector de estados), `ic_launcher` adaptativo.

**2:00 – 3:00 · Transversal: splash, login, biometría, navegación**
- `activity_splash.xml` (ConstraintLayout) + animación en `SplashActivity`.
- `activity_login.xml`: ScrollView > FrameLayout, MaterialCardView, TextInputLayout, ChipGroup. En `LoginActivity`: validación y `BiometricPrompt`.
- `activity_host.xml`: DrawerLayout + AppBar + FragmentContainerView + BottomNavigationView. Mostrar `nav_buyer.xml`, `bottom_buyer.xml`, `drawer_buyer.xml` y cómo se enlazan por **id**.

**3:00 – 4:00 · Comprador y Vendedor**
- Comprador: `fragment_home` (NestedScrollView, RecyclerView horizontal), `item_product_card`, `fragment_checkout` (RadioGroup con estilo propio `Tramas.PayOption`).
- Vendedor: `fragment_product_form` (cámara, dropdown, validaciones) y `fragment_seller_products` (CRUD con `item_seller_product`).
- Explicar `BindingAdapter` genérico y ViewBinding.

**4:00 – 4:40 · Administrador**
- `fragment_admin_dashboard` + `BarChartView` (View personalizada con Canvas), `fragment_admin_users` / `fragment_user_form` (CRUD).

**4:40 – 5:00 · Demostración rápida y cierre**
Ejecuta la app en los tres roles (login → cada rol) y muestra el APK generado y el repositorio en GitHub.
