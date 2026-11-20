# -*- coding: utf-8 -*-
"""Genera docs/Manual_de_Usuario_Tramas.pdf  ->  python3 docs/src/build_manual.py

Usa las capturas REALES del celular que estén en docs/manual/capturas/ (ver docs/manual/LISTA_DE_CAPTURAS.md).
Si falta alguna, usa la captura de pruebas (docs/capturas) marcada como "borrador", o un recuadro vacío si tampoco existe.
"""
import os
import sys
import tempfile

sys.path.insert(0, os.path.dirname(__file__))
import build_pdf as bp
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.units import cm
from reportlab.platypus import Image, KeepTogether, NextPageTemplate, PageBreak, Paragraph, Spacer, Table, TableStyle
from reportlab.platypus.tableofcontents import TableOfContents

bp.COVER_TITLE = "Manual de usuario"
HERE = os.path.dirname(__file__)
OUT = os.path.abspath(os.path.join(HERE, "..", "Manual_de_Usuario_Tramas.pdf"))
REAL = os.path.abspath(os.path.join(HERE, "..", "manual", "capturas"))
DRAFT = os.path.abspath(os.path.join(HERE, "..", "capturas"))
TMP = tempfile.mkdtemp()
P, S, E = bp.P, bp.S, bp.E

# clave -> (leyenda, captura de borrador en docs/capturas o None)
SHOTS = {
    "01_login": ("Inicio de sesión", "01_login"),
    "02_registro": ("Crear cuenta", "02_register"),
    "03_recuperar": ("Recuperar contraseña", "03_forgot"),
    "04_activar_huella": ("Activar la huella", None),
    "05_huella": ("Huella del sistema", None),
    "06_menu_lateral": ("Menú lateral", None),
    "b01_inicio": ("Inicio del comprador", "buyer_00_start"),
    "b02_catalogo": ("Catálogo", "buyer_01_catalogFragment"),
    "b03_detalle": ("Detalle de producto", "buyer_02_productDetailFragment"),
    "b04_carrito": ("Carrito", "buyer_03_cartFragment"),
    "b05_pago": ("Pago: dirección", "buyer_04_checkoutFragment"),
    "b06_ubicacion": ("Ubicación capturada", None),
    "b07_epayco": ("Pasarela ePayco", None),
    "b08_pago_aprobado": ("Pago aprobado", None),
    "b09_pedidos": ("Mis pedidos", "buyer_05_buyerOrdersFragment"),
    "b10_perfil": ("Mi perfil", "buyer_06_buyerProfileFragment"),
    "v01_productos": ("Mis productos", "seller_00_start"),
    "v02_formulario": ("Nuevo producto", "seller_01_productFormFragment"),
    "v03_foto_opciones": ("Cámara o galería", None),
    "v04_pedidos": ("Pedidos recibidos", "seller_02_sellerOrdersFragment"),
    "v05_perfil": ("Perfil de vendedor", "seller_03_sellerProfileFragment"),
    "a01_dashboard": ("Dashboard", "admin_00_start"),
    "a02_usuarios": ("Gestión de usuarios", "admin_01_adminUsersFragment"),
    "a03_usuario_form": ("Crear / editar usuario", "admin_02_userFormFragment"),
    "a04_productos": ("Moderación de productos", "admin_03_adminProductsFragment"),
    "a05_reportes": ("Reporte de ventas", "admin_04_adminSalesFragment"),
}
missing_real = []


def find(folder, name):
    for ext in (".png", ".jpg", ".jpeg"):
        f = os.path.join(folder, name + ext)
        if os.path.exists(f):
            return f
    return None


def shots(keys, per_row=3):
    """Fila(s) de capturas con leyenda. Prefiere la captura real del celular."""
    from PIL import Image as PI
    cells = []
    w = (4.5 * cm) if per_row == 1 else 17 * cm / per_row - 14
    for k in keys:
        cap, draft = SHOTS[k]
        path, note = find(REAL, k), ""
        if path is None:
            missing_real.append(k)
            path = find(DRAFT, draft) if draft else None
            note = " (borrador)" if path else " (pendiente)"
        if path:
            im = PI.open(path).convert("RGB")
            im.thumbnail((600, 1300))
            out = os.path.join(TMP, k + ".jpg")
            im.save(out, quality=88)
            flow = Image(out, width=w, height=w * im.height / im.width)
        else:
            flow = Table([[""]], colWidths=[w], rowHeights=[w * 2.1])
            flow.setStyle(TableStyle([("BOX", (0, 0), (-1, -1), 1, bp.HexColor("#BBBBBB")), ("BACKGROUND", (0, 0), (-1, -1), bp.HexColor("#F3F3F3"))]))
        cells.append([flow, Paragraph(E(cap + note), ParagraphStyle("c", parent=S["cap"], fontSize=7.5, leading=9, spaceBefore=2, spaceAfter=0))])
    rows = []
    for i in range(0, len(cells), per_row):
        row = cells[i:i + per_row]
        while len(row) < per_row:
            row.append(["", ""])
        t = Table([[c[0] for c in row], [c[1] for c in row]], colWidths=[(5.2 * cm) if per_row == 1 else 17 * cm / per_row] * per_row)
        t.setStyle(TableStyle([("ALIGN", (0, 0), (-1, -1), "CENTER"), ("VALIGN", (0, 0), (-1, -1), "TOP")]))
        rows += [t, Spacer(1, 8)]
    return rows


def steps(items):
    """Lista numerada de pasos."""
    return [Paragraph(t, S["bullet"], bulletText=f"{i}.") for i, t in enumerate(items, 1)]


def section(title, intro, step_list, keys, note=None):
    """Subsección: título, descripción, pasos y capturas.
    Con una sola captura va a la derecha del texto (compacto); con varias, en una fila debajo."""
    text = [P(intro)] + steps(step_list)
    if note:
        text.append(P("<i>" + note + "</i>"))
    if len(keys) == 1:
        shot_cell = shots(keys, per_row=1)[0]
        # una captura sola: ancho reducido para que quepa junto al texto
        side = Table([[text, shot_cell]], colWidths=[11.8 * cm, 5.2 * cm])
        side.setStyle(TableStyle([("VALIGN", (0, 0), (-1, -1), "TOP"), ("LEFTPADDING", (0, 0), (-1, -1), 0)]))
        return [KeepTogether([P(title, "H2"), side])]
    sh = shots(keys)
    return [KeepTogether([P(title, "H2")] + text + [Spacer(1, 4)] + sh[:2])] + sh[2:]


def build():
    toc = TableOfContents()
    toc.levelStyles = [ParagraphStyle("t0", fontName="Helvetica-Bold", fontSize=10.5, leading=16, spaceBefore=5),
                       ParagraphStyle("t1", fontName="Helvetica", fontSize=9.5, leading=14, leftIndent=16)]
    st = [NextPageTemplate("main"), PageBreak(), P("Tabla de contenido", "TOCH"), toc, PageBreak()]

    st += [P("1. Introducción", "H1"),
           P("<b>Tramas</b> es una tienda virtual para Android donde se compran y venden artesanías y productos regionales de Colombia. "
             "Este manual explica, paso a paso y con capturas de la aplicación en funcionamiento, cómo usarla según tu rol."),
           P("La aplicación tiene tres roles; el rol lo define tu cuenta y determina qué pantallas ves:"),
           bp.table(["Rol", "¿Qué puede hacer?"], [
               ("Comprador", "Explorar el catálogo, ver productos, armar el carrito, pagar con ePayco, consultar sus pedidos y administrar su perfil."),
               ("Vendedor", "Publicar y administrar sus productos con fotos, ver los pedidos que recibe y avanzar su estado, y administrar el perfil de su tienda."),
               ("Administrador", "Ver el panel de indicadores, gestionar usuarios, aprobar o rechazar productos y consultar el reporte de ventas."),
           ], [3.2 * cm, 13.8 * cm]),
           P("2. Requisitos e instalación", "H1")]
    st += steps([
        "Necesitas un celular con <b>Android 8.0 o superior</b> y conexión a internet. Para usar la huella, el celular debe tener una huella registrada (Ajustes > Seguridad).",
        "Instala el archivo <b>APK</b> de Tramas: ábrelo desde el celular y, si Android lo pide, permite instalar aplicaciones de esta fuente.",
        "Abre <b>Tramas</b>. Verás la pantalla de bienvenida y después el inicio de sesión.",
        "La primera vez que uses una función como <b>Usar mi ubicación</b>, Android pedirá permiso de ubicación: toca <i>Permitir mientras se usa la app</i>.",
    ])

    st += [P("3. Funciones comunes a todos los roles", "H1")]
    st += section("3.1 Crear una cuenta", "Cualquier persona puede registrarse como Comprador o Vendedor. (La cuenta de Administrador la crea otro administrador.)", [
        "En el inicio de sesión toca <b>Regístrate</b>.",
        "Escribe tu nombre, correo, teléfono y una contraseña de mínimo 8 caracteres.",
        "Elige <b>Comprador</b> o <b>Vendedor</b>.",
        "Marca la aceptación de la política de tratamiento de datos y toca <b>Crear mi cuenta</b>. Entrarás directamente a tu interfaz.",
    ], ["02_registro"])
    st += section("3.2 Iniciar sesión", "Escribe tu correo y contraseña y toca <b>Iniciar sesión</b>. La app te lleva a la interfaz de tu rol (Comprador, Vendedor o Administrador).", [
        "Escribe tu correo y tu contraseña.",
        "Toca <b>Iniciar sesión</b>.",
        "Si los datos son incorrectos, la app te lo indica. Si tu cuenta está suspendida, un administrador debe reactivarla.",
    ], ["01_login"])
    st += section("3.3 Ingreso con huella", "Después de iniciar sesión con tu contraseña puedes activar la huella para entrar más rápido. Tu contraseña se guarda cifrada y solo se abre con tu huella.", [
        "Inicia sesión con tu correo y contraseña. La app pregunta <b>¿Activar ingreso con huella?</b>; toca <b>Activar</b> y apoya el dedo en el sensor.",
        "La próxima vez, en el inicio de sesión toca <b>Ingresar con huella</b> y apoya el dedo.",
        "Para desactivarla (rol Comprador) ve a <b>Mi perfil</b> y apaga el interruptor <i>Ingreso con huella dactilar</i>. Si cambias las huellas del celular, deberás activarla de nuevo.",
    ], ["04_activar_huella", "05_huella"])
    st += section("3.4 Recuperar la contraseña", "Si olvidaste tu contraseña, Tramas te envía un enlace por correo para crear una nueva.", [
        "En el inicio de sesión toca <b>¿Olvidaste tu contraseña?</b>.",
        "Escribe tu correo y toca <b>Enviar enlace</b>.",
        "Abre el correo, toca el enlace y crea tu nueva contraseña. Luego inicia sesión normalmente.",
    ], ["03_recuperar"])
    st += section("3.5 Navegación y cierre de sesión", "Todas las pantallas usan la misma navegación.", [
        "<b>Barra inferior:</b> cambia entre las secciones principales de tu rol.",
        "<b>Menú lateral:</b> toca el ícono de tres rayas (arriba a la izquierda) para ver todas las secciones y tu nombre.",
        "<b>Cerrar sesión:</b> en el menú lateral toca <b>Cerrar sesión</b> y confirma.",
    ], ["06_menu_lateral"])

    st += [PageBreak(), P("4. Rol Comprador", "H1")]
    st += section("4.1 Inicio", "Muestra un saludo, el buscador, el banner <b>Cerca de ti</b>, las categorías y los productos destacados y cercanos.", [
        "Toca el buscador o una categoría para ir al catálogo.",
        "Toca <b>Explorar ahora</b> para permitir el uso de tu ubicación; así los productos se ordenan del más cercano al más lejano.",
        "Toca un producto para ver su detalle, o el botón <b>+</b> para agregarlo al carrito.",
    ], ["b01_inicio"])
    st += section("4.2 Catálogo", "Todos los productos aprobados. Se actualiza solo cuando los vendedores publican.", [
        "Escribe en el buscador para filtrar por nombre.",
        "Toca una categoría (Tejidos, Cerámica, Café, Joyería, Sombreros) para filtrar.",
        "Cada tarjeta muestra la distancia, el vendedor y el precio.",
    ], ["b02_catalogo"])
    st += section("4.3 Detalle del producto", "Muestra la foto, el precio, el vendedor, la descripción y las unidades disponibles.", [
        "Usa <b>+</b> y <b>−</b> para elegir la cantidad (no puede superar el stock).",
        "Toca <b>Agregar al carrito</b>. El ícono del carrito en la barra inferior muestra cuántos productos llevas.",
    ], ["b03_detalle"])
    st += section("4.4 Carrito de compras", "Aquí puedes revisar y modificar lo que vas a comprar. Tu carrito se guarda en tu cuenta.", [
        "Cambia la cantidad con <b>+</b> y <b>−</b>, o quita un producto con el ícono de la caneca.",
        "Revisa el resumen: subtotal, IVA (19 %), envío y total.",
        "Toca <b>Vaciar carrito</b> para quitar todo, o <b>Ir a pagar</b> para continuar.",
    ], ["b04_carrito"])
    st += section("4.5 Pagar", "El pago tiene tres pasos: dirección de entrega, pago con ePayco y confirmación.", [
        "Escribe tu <b>dirección de entrega</b>, o toca <b>Usar mi ubicación</b> para que la app la complete con tu posición actual.",
        "Revisa el resumen, acepta los términos y toca <b>Pagar</b>.",
        "Se abre la pasarela segura de <b>ePayco</b>: elige el medio de pago (tarjeta, PSE u otros) y completa el pago allí. Tramas no ve los datos de tu tarjeta.",
        "Al volver, la app confirma el estado de la transacción. Si fue aprobada verás <b>¡Pago aprobado!</b>, tu carrito queda vacío y el pedido aparece en <b>Mis pedidos</b>.",
    ], ["b05_pago", "b06_ubicacion", "b07_epayco", "b08_pago_aprobado"],
        note="Si el pago queda pendiente o es rechazado, la app te lo informa y puedes intentar de nuevo desde el carrito.")
    st += section("4.6 Mis pedidos", "Historial de tus compras con su estado: Pendiente de pago, Pagado, Preparando, Enviado o Entregado. Cambia solo cuando el vendedor avanza el pedido.", [
        "Abre el menú lateral y toca <b>Mis pedidos</b>.",
        "Revisa el número, la fecha, el total y el estado de cada pedido.",
    ], ["b09_pedidos"])
    st += section("4.7 Mi perfil", "Aquí administras tus datos.", [
        "Toca el ícono de cámara para cambiar tu foto: elige <b>Tomar foto</b> o <b>Elegir de la galería</b>.",
        "Edita tu nombre, teléfono y dirección; toca <b>Usar mi ubicación actual</b> para completar la dirección y <b>Guardar cambios</b>.",
        "Activa o desactiva el ingreso con huella y las notificaciones.",
        "<b>Cerrar sesión</b> sale de tu cuenta. <b>Eliminar mi cuenta</b> borra tu perfil y tus datos de forma permanente.",
    ], ["b10_perfil"])

    st += [PageBreak(), P("5. Rol Vendedor", "H1")]
    st += section("5.1 Mis productos", "Tu catálogo. Arriba ves cuántos productos están activos, con stock bajo o en revisión.", [
        "Usa el buscador para encontrar un producto.",
        "Toca el lápiz para editar o la caneca para eliminar (pide confirmación).",
        "Un producto nuevo queda <b>En revisión</b> hasta que un administrador lo apruebe; solo entonces los compradores lo ven.",
    ], ["v01_productos"])
    st += section("5.2 Crear o editar un producto", "Toca el botón <b>Nuevo producto</b>.", [
        "Toca el recuadro de la foto y elige <b>Tomar foto</b> (cámara) o <b>Elegir de la galería</b>.",
        "Escribe el nombre, elige la categoría, el precio en pesos, el stock y la descripción.",
        "Toca <b>Usar GPS</b> para asociar la ubicación del producto (opcional).",
        "Toca <b>Guardar</b>. La foto se sube y el producto queda en revisión.",
    ], ["v02_formulario", "v03_foto_opciones"])
    st += section("5.3 Pedidos recibidos", "Los pedidos pagados que incluyen tus productos, organizados en pestañas.", [
        "<b>Nuevos:</b> pedidos pagados. Toca <b>Preparar</b> cuando empieces a alistarlos.",
        "<b>En proceso:</b> toca <b>Marcar enviado</b> cuando salga el paquete.",
        "<b>Enviados:</b> toca <b>Marcar entregado</b> al confirmar la entrega. El comprador ve cada cambio al instante.",
    ], ["v04_pedidos"])
    st += section("5.4 Perfil de vendedor", "Datos de tu tienda.", [
        "Toca la cámara para cambiar el logo.",
        "Completa nombre comercial, NIT o cédula, teléfono y oficio artesanal.",
        "Toca <b>Actualizar GPS</b> para guardar la ubicación de tu taller y ajusta el <b>radio de entrega</b> con el control deslizante.",
        "Toca <b>Guardar cambios</b>.",
    ], ["v05_perfil"])

    st += [PageBreak(), P("6. Rol Administrador", "H1")]
    st += section("6.1 Dashboard", "Resumen de la plataforma en tiempo real.", [
        "Revisa las tarjetas: usuarios, productos, pedidos de hoy y ventas del mes.",
        "El gráfico muestra las ventas de los últimos 7 días.",
        "Las alertas te llevan a los productos pendientes de moderación o a los usuarios suspendidos.",
        "<b>Cargar productos de ejemplo</b> crea productos aprobados para probar el catálogo.",
    ], ["a01_dashboard"])
    st += section("6.2 Gestión de usuarios", "Crear, consultar, editar y eliminar usuarios de cualquier rol.", [
        "Busca por nombre o correo, o filtra por rol con los chips.",
        "Toca <b>Nuevo usuario</b>: escribe nombre, correo, teléfono, contraseña inicial y elige el rol; luego <b>Guardar usuario</b>.",
        "Toca un usuario (o el lápiz) para editar su nombre, teléfono, rol o estado. Apaga <b>Cuenta activa</b> para suspenderlo.",
        "Toca la caneca para eliminar: el usuario pierde el acceso. No puedes eliminarte ni cambiar tu propio rol.",
    ], ["a02_usuarios", "a03_usuario_form"])
    st += section("6.3 Gestión de productos", "Moderación de lo que publican los vendedores.", [
        "Pestaña <b>Pendientes</b>: toca <b>Aprobar</b> para publicarlo en el catálogo o <b>Rechazar</b>.",
        "Pestañas <b>Aprobados</b> y <b>Rechazados</b>: puedes cambiar la decisión. Mantén presionado un producto para eliminarlo.",
    ], ["a04_productos"])
    st += section("6.4 Reporte de ventas", "Ventas de las órdenes pagadas.", [
        "Elige el periodo: últimos 7 días, 30 días o este año.",
        "Consulta el total, las ventas por categoría y las transacciones recientes.",
        "Toca <b>Exportar reporte (CSV)</b> para copiar los datos y pegarlos en una hoja de cálculo.",
    ], ["a05_reportes"])

    st += [PageBreak(), P("7. Preguntas frecuentes", "H1"),
           bp.table(["Pregunta", "Respuesta"], [
               ("No puedo iniciar sesión.", "Revisa el correo y la contraseña. Si aparece que la cuenta está suspendida o eliminada, contacta a un administrador."),
               ("La huella no funciona.", "Verifica que el celular tenga una huella registrada. Si la cambiaste, inicia sesión con contraseña y activa la huella de nuevo."),
               ("No aparece ningún producto en el catálogo.", "Los productos nuevos deben ser aprobados por un administrador. Si eres administrador, usa <i>Cargar productos de ejemplo</i> en el dashboard."),
               ("La ubicación no se obtiene.", "Activa el GPS del celular y permite el permiso de ubicación a Tramas (Ajustes > Aplicaciones > Tramas > Permisos)."),
               ("El pago quedó pendiente.", "ePayco aún está confirmando la transacción. Revisa más tarde el estado en <i>Mis pedidos</i>."),
               ("¿Mis datos están protegidos?", "Sí. Los datos de pago los procesa ePayco, tu contraseña nunca se guarda en texto plano y puedes eliminar tu cuenta cuando quieras."),
           ], [5.0 * cm, 12.0 * cm])]
    return st


if __name__ == "__main__":
    bp.Doc(OUT).multiBuild(build())
    print("OK ->", OUT)
    if missing_real:
        print("Capturas reales que faltan (se usó borrador o recuadro):", ", ".join(sorted(set(missing_real))))
