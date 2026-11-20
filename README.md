# Tramas — Tienda virtual Android

Marketplace móvil de artesanías y productos regionales de Colombia, con tres roles (Comprador, Vendedor, Administrador).
App nativa Android en **Kotlin + XML (ViewBinding)** con **Firebase** (Authentication, Firestore, Storage), **geolocalización de Google**, **huella dactilar** y pagos con **ePayco**.

| Carpeta | Contenido |
|---|---|
| `app/` | Proyecto Android Studio |
| `firebase/` | Reglas de seguridad de Firestore y Storage |
| `docs/` | Documentación técnica, manual de usuario, guion/brief del video y scripts que generan los PDF |
| `tools/` | Scripts auxiliares (íconos vectoriales, textos) |

## Puesta en marcha (resumen)
1. Abre la carpeta raíz en Android Studio (JDK 17+; la ruta no debe tener tildes en Windows).
2. Crea tu proyecto de Firebase y copia `google-services.json` a `app/`. **Guía paso a paso: [`docs/CONFIGURACION.md`](docs/CONFIGURACION.md)**.
3. (Pagos) agrega `EPAYCO_PUBLIC_KEY=...` a `local.properties`.
4. Ejecuta en un celular físico (huella y cámara requieren dispositivo real).

Sin `google-services.json` el proyecto compila, pero la app avisa que falta configurar Firebase.

## Usuarios de prueba
También puedes crear tu propia cuenta de Comprador o Vendedor desde la app ("Regístrate"). El administrador solo existe con la cuenta de abajo.
| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | admin@admin.com | *(completar contraseña)* |
| Vendedor | juanito@juan.com | *(completar contraseña)* |
| Comprador | jsan@jsanw.com | *(completar contraseña)* |

## Funcionalidades
| Componente | Dónde está en el código |
|---|---|
| Registro, login, cierre de sesión y recuperación de contraseña (3 roles) | `ui/auth`, `data/firebase/FirebaseServices.kt` (`FirebaseAuthRepo`) |
| Login con huella (BiometricPrompt + Keystore) | `security/BiometricVault.kt`, `ui/common/BiometricHelper.kt` |
| CRUD de usuarios (administrador) en Firestore | `ui/admin/AdminUsersFragment`, `UserFormFragment`, `FirestoreUserRepo` |
| CRUD de productos con foto (cámara/galería) en Storage | `ui/seller/ProductFormFragment`, `ui/common/ImagePicker`, `FirestoreProductRepo`, `FirebaseImageStorage` |
| Carrito y órdenes con historial en Firestore | `ui/buyer/CartFragment`, `CheckoutFragment`, `BuyerOrdersFragment`, `FirestoreCartRepo`, `FirestoreOrderRepo` |
| Geolocalización asociada a la dirección de entrega | `ui/common/LocationHelper` (Fused Location + Geocoder) |
| Pasarela de pagos ePayco con confirmación | `ui/payment/EpaycoCheckoutActivity`, `data/payment/Epayco.kt` |
| Moderación de productos y reporte de ventas | `ui/admin/AdminProductsFragment`, `AdminSalesFragment`, `AdminDashboardFragment` |

## Arquitectura
- **Capas:** `ui` (Activities/Fragments + XML) → `data` (contratos `Services.kt` + implementaciones Firebase en `data/firebase`) → Firebase.
- **Contratos e inyección simple:** las pantallas solo conocen las interfaces de `data/Services.kt`. En producción se instalan las implementaciones de Firebase (`TramasApp`); en las pruebas, un backend en memoria (`app/src/test/.../FakeBackend.kt`).
- **Tiempo real:** las listas escuchan Firestore con `Flow` (`snapshotListener`) y se actualizan solas.
- **Un contenedor para los tres roles:** `RoleHostActivity` (Toolbar + Drawer + Bottom Navigation + Navigation Component). Cada rol define su tema, su grafo (`res/navigation`) y sus menús (`res/menu`).
- **Seguridad:** el rol vive en `users/{uid}.role` y se aplica con las reglas de `firebase/firestore.rules` (no solo en la interfaz).

## Pruebas
`./gradlew testDebugUnitTest` — pruebas de precios (JUnit) y de las pantallas con Robolectric (infla las 20 pantallas y guarda capturas). `./gradlew assembleDebug` genera el APK en `app/build/outputs/apk/debug/`.
CI: `.github/workflows/android.yml` compila, prueba y publica el APK en cada push.

## Limitaciones conocidas
- La confirmación del pago la consulta la propia app a ePayco; en producción debe hacerla un servidor (webhook de confirmación).
- Eliminar un usuario borra su perfil de Firestore y bloquea su acceso, pero no su cuenta de Authentication (requiere Admin SDK / Cloud Functions).
- El cambio de estado de la orden por el comprador/vendedor se valida con reglas básicas; un backend con Cloud Functions sería más robusto.
