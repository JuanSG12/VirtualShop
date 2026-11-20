# -*- coding: utf-8 -*-
"""Inserta comentarios KDoc en funciones/clases que no tienen (se ejecutó una vez; es idempotente)."""
import re, glob, os
ROOT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "java", "com", "tramas", "app")

DEFAULT = {
    "onCreateView": "Infla el layout de la pantalla con ViewBinding.",
    "onTabUnselected": "No requiere acción al deseleccionar una pestaña.",
    "onTabReselected": "No requiere acción al volver a tocar la pestaña activa.",
}
BY_FILE = {
    "TramasApp.kt:onCreate": "Se ejecuta al abrir la app: instala los servicios (Firebase).",
    "AdminProductsFragment.kt:onViewCreated": "Configura el adaptador, las pestañas de moderación y escucha los productos de Firestore.",
    "AdminProductsFragment.kt:render": "Muestra solo los productos del estado de la pestaña seleccionada.",
    "AdminDashboardFragment.kt:onViewCreated": "Conecta los botones y escucha en tiempo real usuarios, productos y órdenes.",
    "AdminDashboardFragment.kt:kpi": "Pinta una tarjeta de indicador (ícono, valor y etiqueta).",
    "AdminSalesFragment.kt:onViewCreated": "Crea los chips de periodo, el adaptador de transacciones y escucha las órdenes.",
    "UserFormFragment.kt:onViewCreated": "Prepara el formulario en modo crear o editar según el argumento userId.",
    "AdminUsersFragment.kt:onViewCreated": "Configura el listado, los filtros por rol y escucha los usuarios de Firestore.",
    "AdminUsersFragment.kt:render": "Aplica búsqueda y filtro de rol sobre la lista de usuarios.",
    "SellerOrdersFragment.kt:onViewCreated": "Configura pestañas y adaptador, y escucha los pedidos del vendedor.",
    "SellerOrdersFragment.kt:render": "Muestra los pedidos de la pestaña seleccionada.",
    "ProductFormFragment.kt:onViewCreated": "Prepara el formulario (crear/editar), carga el producto existente y conecta foto, GPS y guardado.",
    "SellerProfileFragment.kt:onViewCreated": "Llena el perfil con los datos del vendedor y conecta logo, GPS, radio y guardado.",
    "SellerProductsFragment.kt:onViewCreated": "Configura el listado de productos propios (editar/eliminar), el botón de crear y la búsqueda.",
    "SellerProductsFragment.kt:stat": "Pinta una tarjeta de indicador del panel del vendedor.",
    "SplashActivity.kt:onCreate": "Anima el logo y, tras 2 s, abre el login o directamente la interfaz del rol si hay sesión.",
    "ForgotPasswordActivity.kt:onCreate": "Valida el correo y pide a Firebase el envío del enlace de recuperación.",
    "LoginActivity.kt:onCreate": "Conecta los botones de ingreso, huella, registro y recuperación; avisa si falta configurar Firebase.",
    "LoginActivity.kt:setLoading": "Bloquea los botones y cambia el texto mientras se autentica.",
    "RegisterActivity.kt:onCreate": "Valida el formulario y registra la cuenta (Authentication + perfil en Firestore).",
    "LocationHelper.kt:fetch": "Obtiene la ubicación actual con el Fused Location Provider y la convierte en dirección.",
    "BiometricHelper.kt:onAuthenticationSucceeded": "La huella fue reconocida: entrega el Cipher desbloqueado al llamador.",
    "BiometricHelper.kt:onAuthenticationError": "Error o cancelación del diálogo de huella; el cancelar del usuario no se informa.",
    "BarChartView.kt:onDraw": "Dibuja el eje, las barras (la mayor en ocre) y las etiquetas de cada día.",
    "Utils.kt:Holder": "Contenedor de la vista de una fila (guarda su binding).",
    "EpaycoCheckoutActivity.kt:onCreate": "Configura el WebView y carga el checkout de ePayco.",
    "EpaycoCheckoutActivity.kt:shouldOverrideUrlLoading": "Intercepta la URL de respuesta de ePayco para capturar el ref_payco y cerrar la pantalla.",
    "ProductDetailFragment.kt:onViewCreated": "Carga el producto por su id y conecta el selector de cantidad y el botón de agregar.",
    "BuyerProfileFragment.kt:onViewCreated": "Llena el perfil con los datos del usuario y conecta foto, GPS, guardado, huella y cuenta.",
    "BuyerProfileFragment.kt:enableFingerprint": "Verifica la contraseña (inicio de sesión) y activa la huella protegiendo la contraseña con el Keystore.",
    "CartFragment.kt:onViewCreated": "Configura la lista del carrito y escucha los cambios en Firestore para actualizar totales.",
    "CartFragment.kt:line": "Pinta una fila del resumen (etiqueta y valor en pesos).",
    "CatalogFragment.kt:onViewCreated": "Configura la grilla, los chips de categoría, la búsqueda y escucha el catálogo.",
    "CheckoutFragment.kt:onViewCreated": "Escucha el carrito para el resumen y conecta ubicación y botón de pago.",
    "CheckoutFragment.kt:validate": "Valida dirección, carrito no vacío y aceptación de términos.",
    "CheckoutFragment.kt:showProcessing": "Muestra el diálogo 'Procesando tu pago'.",
    "CheckoutFragment.kt:hideProcessing": "Oculta el diálogo de procesamiento.",
    "CheckoutFragment.kt:line": "Pinta una fila del resumen (etiqueta y valor en pesos).",
    "BuyerOrdersFragment.kt:onViewCreated": "Configura el historial y escucha las órdenes del comprador en Firestore.",
    "HomeFragment.kt:onViewCreated": "Conecta buscador, banner, categorías y escucha el catálogo para las dos listas.",
    "HomeFragment.kt:productAdapter": "Crea un adaptador de tarjetas de producto de ancho fijo para las listas horizontales.",
    "HomeFragment.kt:addToCart": "Agrega un producto al carrito en Firestore y confirma con un mensaje.",
    "RoleHostActivity.kt:onCreate": "Arma toolbar, menú lateral, barra inferior y navegación según el rol; observa el carrito.",
    "RoleHostActivity.kt:handleOnBackPressed": "Atrás: cierra el menú lateral si está abierto o retrocede en la navegación.",
    "Pricing.kt:subtotal": "Suma precio por cantidad de todas las líneas.",
    "Pricing.kt:iva": "IVA del 19 % sobre el subtotal.",
    "Pricing.kt:shipping": "Envío fijo (0 si el carrito está vacío).",
    "Pricing.kt:total": "Subtotal + IVA + envío.",
    "Pricing.kt:count": "Cantidad total de unidades en el carrito.",
    "Services.kt:signOut": "Cierra la sesión de Firebase en este dispositivo.",
    "Services.kt:get": "Lee un documento por id (null si no existe).",
    "Services.kt:observeBySeller": "Productos de un vendedor, en tiempo real.",
    "Services.kt:delete": "Elimina el documento.",
    "Services.kt:setStatus": "Cambia el estado de moderación del producto.",
    "Services.kt:observe": "Líneas del carrito del usuario, en tiempo real.",
    "Services.kt:add": "Agrega un producto al carrito (suma la cantidad si ya estaba, sin pasar del stock).",
    "Services.kt:setQty": "Cambia la cantidad de una línea (entre 1 y el stock).",
    "Services.kt:remove": "Quita una línea del carrito.",
    "Services.kt:clear": "Vacía el carrito.",
    "Services.kt:observeForBuyer": "Órdenes de un comprador, en tiempo real.",
    "Services.kt:observeForSeller": "Órdenes que incluyen productos de un vendedor, en tiempo real.",
    "Services.kt:observeAll": "Todas las órdenes, en tiempo real (reportes del administrador).",
    "Services.kt:updateStatus": "Cambia el estado de la orden (preparando, enviado, entregado...).",
    "Services.kt:upload": "Sube los bytes de una imagen a la ruta indicada y devuelve su URL.",
    "Mappers.kt:long": "Convierte un valor de Firestore a Long (0 si falta).",
    "Mappers.kt:int": "Convierte un valor de Firestore a Int (0 si falta).",
    "Mappers.kt:dbl": "Convierte un valor de Firestore a Double (null si falta).",
    "Mappers.kt:toUser": "Documento de Firestore -> User.",
    "Mappers.kt:toProduct": "Documento de Firestore -> Product.",
    "Mappers.kt:toCartItem": "Documento de Firestore -> CartItem.",
    "Mappers.kt:toOrder": "Documento de Firestore -> Order (con sus líneas).",
    "FirebaseServices.kt:signIn": "Inicia sesión en Firebase Authentication y valida que el perfil exista y esté activo.",
    "FirebaseServices.kt:register": "Crea la cuenta en Authentication y su perfil en Firestore; envía correo de verificación.",
    "FirebaseServices.kt:loadCurrentProfile": "Lee el perfil del usuario con sesión persistente; cierra la sesión si fue eliminado o suspendido.",
    "FirebaseServices.kt:sendPasswordReset": "Pide a Firebase enviar el correo de restablecimiento de contraseña.",
    "FirebaseServices.kt:deleteOwnAccount": "Borra el perfil de Firestore y la cuenta de Authentication.",
    "FirebaseServices.kt:save": "Guarda/actualiza el documento (merge para no borrar campos desconocidos).",
    "FirebaseServices.kt:createWithAuth": "Crea la cuenta con una segunda instancia de Firebase para no cerrar la sesión del administrador, y escribe el perfil.",
    "FirebaseServices.kt:delete": "Elimina el documento de Firestore.",
    "FirebaseServices.kt:observeBySeller": "Consulta productos por vendedor (un solo filtro: sin índice compuesto).",
    "FirebaseServices.kt:setStatus": "Actualiza solo el campo status del producto.",
    "FirebaseServices.kt:decrementStock": "Descuenta el stock de cada producto comprado con un incremento atómico.",
    "FirebaseServices.kt:col": "Subcolección del carrito de un usuario.",
    "FirebaseServices.kt:add": "Agrega al carrito sumando cantidad y respetando el stock.",
    "FirebaseServices.kt:setQty": "Actualiza la cantidad limitándola entre 1 y el stock guardado.",
    "FirebaseServices.kt:remove": "Elimina una línea del carrito.",
    "FirebaseServices.kt:clear": "Vacía el carrito en un solo lote (batch).",
    "FirebaseServices.kt:create": "Crea la orden con un id generado por Firestore y lo devuelve.",
    "FirebaseServices.kt:observeForSeller": "Órdenes cuyo arreglo sellerIds contiene al vendedor.",
    "FirebaseServices.kt:updateStatus": "Actualiza solo el estado de la orden.",
    "FirebaseServices.kt:setPayment": "Guarda referencia, estado de la pasarela, modo y nuevo estado de la orden.",
    "Epayco.kt:PaymentResult": "Resultado de la verificación: referencia, estado y mensaje de ePayco.",
}

def desc(fname, name):
    return BY_FILE.get(f"{fname}:{name}") or DEFAULT.get(name)

count = 0
for f in glob.glob(ROOT + "/**/*.kt", recursive=True):
    fname = os.path.basename(f)
    lines = open(f, encoding="utf-8").read().split("\n")
    out = []
    for i, l in enumerate(lines):
        t = l.strip()
        m = re.match(r'(?:(?:private|override|protected|internal|public|open|abstract|suspend|inline|data|enum|object|companion)\s+)*(fun|class|object|interface)\s+(?:[A-Za-z<>?.]+\.)?([A-Za-z_]+)', t)
        if m and not t.startswith(("//", "*", "/*")):
            j = len(out) - 1
            while j >= 0 and out[j].strip().startswith("@"): j -= 1
            prev = out[j].strip() if j >= 0 else ""
            if not (prev.endswith("*/") or prev.startswith("//")):
                d = desc(fname, m.group(2))
                if d:
                    indent = l[: len(l) - len(l.lstrip())]
                    insert_at = j + 1
                    out.insert(insert_at, f"{indent}/** {d} */")
                    count += 1
        out.append(l)
    open(f, "w", encoding="utf-8").write("\n".join(out))
print("comentarios agregados:", count)
