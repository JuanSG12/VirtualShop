# Configuración de Tramas (Firebase + ePayco)

La app necesita **tu propio proyecto de Firebase** y, para pagos reales de prueba, una cuenta de **ePayco**. Sigue los pasos en orden; toma unos 15 minutos.

## 1. Proyecto de Firebase
1. Entra a <https://console.firebase.google.com> con tu cuenta de Google y pulsa **Agregar proyecto** (nombre: `Tramas`; Google Analytics puede ir desactivado).
2. En la página del proyecto pulsa el ícono de **Android** y registra la app:
   - **Nombre del paquete:** `com.tramas.app`
   - Pulsa *Registrar app* y **descarga `google-services.json`**.
3. Copia `google-services.json` dentro de la carpeta **`app/`** del proyecto (junto a `app/build.gradle.kts`) y en Android Studio usa `File > Sync Project with Gradle Files`.

## 2. Authentication
`Compilación (Build) > Authentication > Comenzar > Método de acceso > Correo electrónico/contraseña > Habilitar > Guardar`.

## 3. Firestore (base de datos)
1. `Build > Firestore Database > Crear base de datos`. Elige una ubicación cercana (por ejemplo `nam5` o `southamerica-east1`) y **modo producción**.
2. Pestaña **Reglas**: borra lo que haya, pega el contenido de [`firebase/firestore.rules`](../firebase/firestore.rules) y pulsa **Publicar**.

## 4. Storage (fotos)
1. `Build > Storage > Comenzar`. Firebase puede pedir cambiar al plan **Blaze** (tiene capa gratuita; exige tarjeta). Si no puedes, **sáltate este paso**: la app guarda las fotos reducidas dentro de Firestore automáticamente (plan B) y todo sigue funcionando.
2. Si lo activaste, pestaña **Reglas**: pega [`firebase/storage.rules`](../firebase/storage.rules) y **Publicar**.

## 5. Crear el primer administrador
El rol ADMIN no se puede elegir al registrarse (por seguridad). Para crear el primero:
1. Ejecuta la app y **regístrate** con tu correo (como Comprador).
2. En la consola: `Firestore Database > Datos > users` → abre el documento cuyo id es tu UID (el que coincide con tu correo).
3. Edita el campo **`role`** y cambia `BUYER` por `ADMIN`. Cierra sesión en la app y vuelve a entrar: verás el panel de administración.
4. Desde ahí puedes crear más usuarios (vendedores, otros administradores) y pulsar **"Cargar productos de ejemplo"** en el dashboard.

## 6. Pagos con ePayco (hoy Davipay)
ePayco pertenece ahora a Davivienda y su panel/documentación se encuentran en `dashboard.davipay.com` / `docs.davipay.com` (el panel antiguo era `dashboard.epayco.co`).
1. Crea o abre tu cuenta del comercio y copia las **llaves** (en el panel: configuración / integraciones / llaves API): la **llave pública** (`PUBLIC_KEY`) y, si tu cuenta usa el checkout nuevo (v2), también la **llave privada** (`PRIVATE_KEY`).
2. Agrégalas a `local.properties` (en la raíz del proyecto; **no se sube a Git**):
   ```
   EPAYCO_PUBLIC_KEY=tu_llave_publica
   EPAYCO_PRIVATE_KEY=tu_llave_privada_de_PRUEBAS   # solo si tu cuenta usa checkout v2
   EPAYCO_TEST=true
   ```
3. La app elige sola el modo: con llave privada crea una **sesión de pago** (checkout v2); con solo la pública usa el checkout clásico; sin llaves, registra un pago **SIMULADO** marcado en la orden.
4. **Seguridad:** una llave privada dentro de una app es extraíble. Usa **solo la llave de pruebas**, no subas a Git un APK compilado con ella y regenera las llaves cuando termine el curso. En producción la sesión de pago debe crearla un servidor (por ejemplo una función en la nube).
5. Tarjetas de prueba (documentación de ePayco, sección "Medios de pago en pruebas"): Visa `4575 6231 8229 0326`, vence `12/2027`, CVV `123` (aprobada); Visa `4151 6115 2758 3283` (fondos insuficientes, rechazada); Mastercard `5170 3944 9037 9427` (fallida); Amex `3731 1885 6457 642` (pendiente).

## 7. Ejecutar en un celular físico
1. En el celular: `Ajustes > Acerca del teléfono` → toca 7 veces *Número de compilación* → `Opciones de desarrollador` → activa **Depuración USB**.
2. Conéctalo por USB y acepta la huella digital de depuración.
3. En Android Studio elige tu celular y pulsa ▶ (o instala `release/Tramas-debug.apk`).
4. **Huella:** registra una huella en `Ajustes > Seguridad`. Inicia sesión con tu correo y, cuando la app lo pregunte, pulsa **Activar**. La próxima vez usa **"Ingresar con huella"**.

## 8. Para la entrega (que el docente pueda ejecutar la app)
1. **Sube tu `app/google-services.json` al repositorio.** Es lo que conecta la app con *tu* proyecto de Firebase; sin él, quien clone el repo verá el aviso "Firebase no está configurado". (Contiene identificadores del proyecto, no contraseñas; en la consola de Google Cloud puedes restringir la API key a tu paquete y a tu huella SHA-1.)
2. Crea **tres usuarios de prueba** (uno por rol) y anótalos en la tabla de usuarios de prueba del `README.md`. El administrador se crea con el paso 5 de esta guía.
3. Deja las reglas de Firestore/Storage publicadas (pasos 3 y 4) y, si usas ePayco, la llave pública en `local.properties` (no se sube; en el README indica que se configura según el paso 6).
4. Verifica en una ventana de incógnito que el repositorio es público y que contiene `app/google-services.json`.

## Solución de problemas
| Síntoma | Causa / solución |
|---|---|
| Al abrir la app sale "Firebase no está configurado" | Falta `app/google-services.json` (o está en otra carpeta). Cópialo y compila de nuevo. |
| "No tienes permiso para esta acción" | Las reglas de Firestore no están publicadas (paso 3.2) o el usuario no tiene el rol adecuado. |
| El inicio de sesión falla con "correo o contraseña incorrectos" | Verifica que el método Correo/contraseña esté habilitado (paso 2). |
| "No se pudo abrir la pasarela de pago" | Las llaves de `local.properties` son incorrectas, o la cuenta usa checkout v2 y falta `EPAYCO_PRIVATE_KEY`. |
| Error de ruta con caracteres no ASCII en Windows | Mueve el proyecto a una ruta sin tildes (por ejemplo `C:\Tramas`). |
| La huella no aparece disponible | El teléfono no tiene huella registrada o no es de clase "fuerte". |
