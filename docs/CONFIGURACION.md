# Configuración de Tramas (Firebase + ePayco)

La app necesita **tu propio proyecto de Firebase** y, para pagos reales de prueba, una cuenta de **ePayco**. Sigue los pasos en orden; toma unos 15 minutos.

## 1. Proyecto de Firebase
1. Entra a <https://console.firebase.google.com> con tu cuenta de Google y pulsa **Agregar proyecto** (nombre: `Tramas`; Google Analytics puede ir desactivado).
2. En la página del proyecto pulsa el ícono de **Android** y registra la app:
   - **Nombre del paquete:** `com.tramas.app`
   - Pulsa *Registrar app* y **descarga `google-services.json`**.
3. Copia `google-services.json` dentro de la carpeta **`app/`** del proyecto (junto a `app/build.gradle.kts`).

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

## 6. Pagos con ePayco
1. Crea una cuenta en <https://dashboard.epayco.co> (puedes usar el modo **pruebas**).
2. `Integraciones > Llaves API` y copia la **llave pública** (`PUBLIC_KEY`).
3. En la raíz del proyecto crea (o edita) el archivo **`local.properties`** y agrega:
   ```
   EPAYCO_PUBLIC_KEY=tu_llave_publica
   EPAYCO_TEST=true
   ```
   Este archivo **no se sube a Git** (está en `.gitignore`).
4. Compila de nuevo. En pruebas usa las **tarjetas de prueba** que publica ePayco en su documentación (sección "Datos de prueba").
5. Sin llave pública, la app registra un pago **SIMULADO** (queda marcado como `SIMULADO` en la orden). Para la entrega final configura la llave.

## 7. Ejecutar en un celular físico
1. En el celular: `Ajustes > Acerca del teléfono` → toca 7 veces *Número de compilación* → `Opciones de desarrollador` → activa **Depuración USB**.
2. Conéctalo por USB y acepta la huella digital de depuración.
3. En Android Studio elige tu celular y pulsa ▶ (o instala `release/Tramas-debug.apk`).
4. **Huella:** registra una huella en `Ajustes > Seguridad`. Inicia sesión con tu correo y, cuando la app lo pregunte, pulsa **Activar**. La próxima vez usa **"Ingresar con huella"**.

## Solución de problemas
| Síntoma | Causa / solución |
|---|---|
| Al abrir la app sale "Firebase no está configurado" | Falta `app/google-services.json` (o está en otra carpeta). Cópialo y compila de nuevo. |
| "No tienes permiso para esta acción" | Las reglas de Firestore no están publicadas (paso 3.2) o el usuario no tiene el rol adecuado. |
| El inicio de sesión falla con "correo o contraseña incorrectos" | Verifica que el método Correo/contraseña esté habilitado (paso 2). |
| Error de ruta con caracteres no ASCII en Windows | Mueve el proyecto a una ruta sin tildes (por ejemplo `C:\Tramas`). |
| La huella no aparece disponible | El teléfono no tiene huella registrada o no es de clase "fuerte". |
