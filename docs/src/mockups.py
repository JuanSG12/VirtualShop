# -*- coding: utf-8 -*-
"""Mockups de VirtualShop dibujados con reportlab (coordenadas lógicas 300x600, y desde arriba)."""
from reportlab.lib.colors import Color, HexColor, white
from reportlab.platypus import Flowable

W, H = 300, 600
BUYER, SELLER, ADMIN = HexColor("#1F6F5C"), HexColor("#C2572B"), HexColor("#2D3E73")
BG, INK, GREY = HexColor("#F7F4EF"), HexColor("#222222"), HexColor("#8A8F98")
LINE, CARD, RED = HexColor("#E3DED4"), white, HexColor("#C0392B")
OK, WARN = HexColor("#2E9E6B"), HexColor("#E0A030")


class Ctx:
    """Envoltura del canvas con coordenadas top-down."""

    def __init__(self, canv, color):
        self.c, self.color = canv, color

    def rect(self, x, y, w, h, fill=CARD, stroke=None, r=0, sw=0.8):
        c = self.c
        if fill:
            c.setFillColor(fill)
        if stroke:
            c.setStrokeColor(stroke)
            c.setLineWidth(sw)
        if r:
            c.roundRect(x, H - y - h, w, h, r, fill=1 if fill else 0, stroke=1 if stroke else 0)
        else:
            c.rect(x, H - y - h, w, h, fill=1 if fill else 0, stroke=1 if stroke else 0)

    def circle(self, x, y, rad, fill=CARD, stroke=None, sw=0.8):
        c = self.c
        if stroke:
            c.setStrokeColor(stroke)
            c.setLineWidth(sw)
        if fill:
            c.setFillColor(fill)
        c.circle(x, H - y, rad, fill=1 if fill else 0, stroke=1 if stroke else 0)

    def line(self, x1, y1, x2, y2, col=LINE, sw=0.8):
        self.c.setStrokeColor(col)
        self.c.setLineWidth(sw)
        self.c.line(x1, H - y1, x2, H - y2)

    def text(self, x, y, s, size=9, col=INK, bold=False, anchor="l"):
        c = self.c
        c.setFillColor(col)
        c.setFont("Helvetica-Bold" if bold else "Helvetica", size)
        y = H - y
        if anchor == "c":
            c.drawCentredString(x, y, s)
        elif anchor == "r":
            c.drawRightString(x, y, s)
        else:
            c.drawString(x, y, s)

    # ---- componentes ----
    def status_bar(self):
        self.rect(0, 0, W, 22, self.color)
        self.text(14, 15, "9:41", 8, white, True)
        self.rect(262, 8, 20, 9, None, white, 2)
        self.rect(264, 10, 13, 5, white)

    def appbar(self, title, back=False):
        self.status_bar()
        self.rect(0, 22, W, 40, self.color)
        if back:
            self.text(14, 48, "<", 16, white, True)
        self.text(36 if back else 16, 47, title, 14, white, True)

    def nav(self, items, active):
        self.rect(0, 552, W, 48, white)
        self.line(0, 552, W, 552)
        n = len(items)
        for i, it in enumerate(items):
            cx = W / n * (i + .5)
            on = i == active
            self.circle(cx, 570, 7, self.color if on else LINE)
            self.text(cx, 590, it, 7.5, self.color if on else GREY, on, "c")

    def btn(self, x, y, w, label, fill=None, h=32, outline=False, tcol=None):
        fill = fill or self.color
        if outline:
            self.rect(x, y, w, h, white, fill, 8, 1.2)
            self.text(x + w / 2, y + h / 2 + 3.5, label, 10, tcol or fill, True, "c")
        else:
            self.rect(x, y, w, h, fill, None, 8)
            self.text(x + w / 2, y + h / 2 + 3.5, label, 10, tcol or white, True, "c")

    def field(self, x, y, w, label, value="", h=34):
        self.rect(x, y, w, h, white, LINE, 6, 1)
        self.text(x + 8, y + 11, label, 6.5, GREY)
        self.text(x + 8, y + 26, value, 9.5, INK)

    def img(self, x, y, w, h, tint="#D9CDB8"):
        self.rect(x, y, w, h, HexColor(tint), None, 6)
        p = self.c.beginPath()
        p.moveTo(x + w * .15, H - (y + h * .8))
        p.lineTo(x + w * .42, H - (y + h * .35))
        p.lineTo(x + w * .62, H - (y + h * .65))
        p.lineTo(x + w * .75, H - (y + h * .5))
        p.lineTo(x + w * .88, H - (y + h * .8))
        p.close()
        self.c.setFillColor(Color(1, 1, 1, alpha=.55))
        self.c.drawPath(p, fill=1, stroke=0)

    def chip(self, x, y, label, fill, tcol=white, w=None, h=15):
        w = w or (len(label) * 4.6 + 12)
        self.rect(x, y, w, h, fill, None, 7)
        self.text(x + w / 2, y + 10.5, label, 7, tcol, True, "c")
        return w

    def card(self, x, y, w, h):
        self.rect(x, y, w, h, CARD, LINE, 8, 0.8)

    def product(self, x, y, name, price, tint):
        self.card(x, y, 130, 132)
        self.img(x + 6, y + 6, 118, 70, tint)
        self.text(x + 8, y + 92, name, 8.5, INK, True)
        self.text(x + 8, y + 107, price, 9, self.color, True)
        self.text(x + 8, y + 123, "* 4,8  ·  2,4 km", 6.5, GREY)

    def pin(self, x, y):
        self.circle(x, y, 7, RED)
        self.circle(x, y, 2.5, white)

    def map(self, x, y, w, h):
        self.rect(x, y, w, h, HexColor("#DCE8DD"), LINE, 6)
        for i in range(1, 4):
            self.line(x, y + h * i / 4, x + w, y + h * i / 4 + (6 if i % 2 else -6), HexColor("#FFFFFF"), 3)
            self.line(x + w * i / 4, y, x + w * i / 4 + 10, y + h, HexColor("#FFFFFF"), 3)
        self.pin(x + w * .55, y + h * .5)

    def fab(self, label="+"):
        self.circle(262, 520, 20, self.color)
        self.text(262, 527, label, 20, white, True, "c")

    def lines(self, x, y, w, n=3, gap=9):
        for i in range(n):
            self.rect(x, y + i * gap, w * (0.65 if i == n - 1 else 1), 4, LINE, None, 2)

    def tabs(self, y, labels, active):
        self.rect(0, y, W, 28, white)
        n = len(labels)
        for i, t in enumerate(labels):
            cx = W / n * (i + .5)
            self.text(cx, y + 18, t, 8.5, self.color if i == active else GREY, i == active, "c")
        self.rect(W / n * active + 12, y + 25, W / n - 24, 3, self.color)

    def kpi(self, x, y, label, value):
        self.card(x, y, 130, 54)
        self.text(x + 10, y + 18, label, 7.5, GREY)
        self.text(x + 10, y + 42, value, 15, self.color, True)

    def bars(self, x, y, w, h, vals):
        n = len(vals)
        bw = w / n * .6
        for i, v in enumerate(vals):
            bh = h * v
            self.rect(x + i * w / n + (w / n - bw) / 2, y + h - bh, bw, bh, self.color, None, 2)
        self.line(x, y + h, x + w, y + h, GREY, .6)

    def list_row(self, y, title, sub, chip=None, chipc=OK, avatar=True, h=48):
        self.rect(10, y, 280, h, white, LINE, 8)
        if avatar:
            self.circle(34, y + h / 2, 14, HexColor("#D9CDB8"))
        self.text(56, y + h / 2 - 2, title, 9, INK, True)
        self.text(56, y + h / 2 + 11, sub, 7, GREY)
        if chip:
            self.chip(288 - len(chip) * 4.6 - 14, y + h / 2 - 8, chip, chipc)


# ------------------------------------------------------------------ AUTENTICACIÓN
def s_login(c):
    c.rect(0, 0, W, H, BG)
    c.status_bar()
    c.circle(150, 92, 34, c.color)
    c.text(150, 98, "VS", 20, white, True, "c")
    c.text(150, 150, "VirtualShop", 18, INK, True, "c")
    c.text(150, 165, "Artesanías y productos de Colombia", 8, GREY, anchor="c")
    c.field(24, 190, 252, "Correo electrónico", "juan@correo.com")
    c.field(24, 232, 252, "Contraseña", "••••••••")
    c.text(276, 282, "¿Olvidaste tu contraseña?", 7.5, c.color, anchor="r")
    c.btn(24, 296, 252, "Iniciar sesión")
    c.circle(150, 372, 28, white, c.color, 1.6)
    for r in (7, 13, 19):
        c.circle(150, 372, r, None, c.color, 1.2)
    c.text(150, 416, "Ingresar con huella", 8.5, c.color, True, "c")
    c.line(24, 440, 120, 440)
    c.text(150, 443, "o", 8, GREY, anchor="c")
    c.line(180, 440, 276, 440)
    c.btn(24, 456, 252, "Continuar con Google", c.color, outline=True)
    c.text(150, 520, "¿No tienes cuenta?  Regístrate", 8.5, INK, anchor="c")


def s_register(c):
    c.rect(0, 0, W, H, BG)
    c.appbar("Crear cuenta", True)
    c.field(16, 76, 268, "Nombre completo", "Juan Pérez")
    c.field(16, 116, 268, "Correo electrónico", "juan@correo.com")
    c.field(16, 156, 268, "Teléfono", "+57 300 000 0000")
    c.field(16, 196, 268, "Contraseña (mín. 8 caracteres)", "••••••••")
    c.text(16, 250, "Quiero registrarme como:", 8.5, INK, True)
    c.rect(16, 258, 130, 30, c.color, None, 8)
    c.text(81, 277, "Comprador", 9, white, True, "c")
    c.rect(154, 258, 130, 30, white, c.color, 8, 1.2)
    c.text(219, 277, "Vendedor", 9, c.color, True, "c")
    for i, t in enumerate(("Acepto la política de tratamiento de datos", "(Ley 1581 de 2012) y autorizo su uso.", "Acepto los términos y condiciones.")):
        if i != 1:
            c.rect(16, 310 + i * 36, 12, 12, c.color, None, 3)
            c.text(34, 320 + i * 36, t, 7.5)
        else:
            c.text(34, 320 + 12, t, 7.5)
    c.btn(16, 400, 268, "Registrarme")
    c.text(150, 456, "Te enviaremos un enlace para verificar tu correo.", 7.5, GREY, anchor="c")


def s_biometric(c):
    s_login(c)
    c.rect(0, 0, W, H, Color(0, 0, 0, alpha=.55))
    c.rect(0, 370, W, 230, white, None, 0)
    c.rect(0, 370, W, 8, white, None, 14)
    c.text(150, 408, "Confirma tu identidad", 12, INK, True, "c")
    c.text(150, 424, "Usa tu huella para ingresar a VirtualShop", 8, GREY, anchor="c")
    c.circle(150, 480, 26, HexColor("#E7F2EE"), c.color, 1.6)
    for r in (6, 12, 18):
        c.circle(150, 480, r, None, c.color, 1.2)
    c.text(150, 525, "Toca el sensor de huella", 8, GREY, anchor="c")
    c.text(60, 575, "USAR CONTRASEÑA", 8, c.color, True, "c")
    c.text(240, 575, "CANCELAR", 8, c.color, True, "c")


# ------------------------------------------------------------------ COMPRADOR
B_NAV = ("Inicio", "Buscar", "Carrito", "Perfil")


def b_home(c):
    c.rect(0, 0, W, H, BG)
    c.appbar("VirtualShop")
    c.rect(12, 70, 276, 28, white, None, 14)
    c.text(26, 88, "Buscar artesanías, café, tejidos…", 8.5, GREY)
    x = 12
    for i, t in enumerate(("Todo", "Tejidos", "Cerámica", "Café", "Joyería")):
        w = c.chip(x, 108, t, c.color if i == 0 else white, white if i == 0 else INK)
        x += w + 5
    c.rect(12, 132, 276, 62, HexColor("#E6B98F"), None, 10)
    c.text(24, 156, "Cerca de ti", 12, INK, True)
    c.text(24, 172, "Talleres a menos de 5 km", 8.5, INK)
    c.text(12, 214, "Destacados", 11, INK, True)
    c.product(12, 222, "Mochila wayúu", "$ 185.000", "#CFA87A")
    c.product(158, 222, "Taza cerámica", "$ 42.000", "#C9B7A3")
    c.product(12, 362, "Café de origen", "$ 28.500", "#B79B80")
    c.product(158, 362, "Collar en plata", "$ 96.000", "#BBC4C9")
    c.nav(B_NAV, 0)


def b_detail(c):
    c.rect(0, 0, W, H, BG)
    c.appbar("Detalle del producto", True)
    c.img(0, 62, W, 190, "#CFA87A")
    for i in range(3):
        c.circle(130 + i * 20, 244, 3.5, white if i == 0 else HexColor("#FFFFFF88"))
    c.text(16, 280, "Mochila wayúu tejida a mano", 13, INK, True)
    c.text(16, 304, "$ 185.000", 16, c.color, True)
    c.text(16, 322, "* * * * *  4,8 (36 reseñas)", 8, WARN)
    c.card(12, 336, 276, 46)
    c.circle(36, 359, 14, HexColor("#D9CDB8"))
    c.text(58, 356, "Taller Kai", 9.5, INK, True)
    c.text(58, 369, "Riohacha · a 3,2 km de ti", 7.5, GREY)
    c.pin(268, 359)
    c.text(16, 404, "Descripción", 9.5, INK, True)
    c.lines(16, 414, 268, 4, 10)
    c.text(16, 470, "Cantidad", 9, INK, True)
    c.rect(80, 458, 90, 24, white, LINE, 12)
    c.text(94, 475, "-", 12, c.color, True)
    c.text(125, 474, "1", 10, INK, True, "c")
    c.text(156, 475, "+", 12, c.color, True)
    c.text(284, 474, "Stock: 12", 8, GREY, anchor="r")
    c.btn(16, 498, 268, "Agregar al carrito", h=38)


def b_cart(c):
    c.rect(0, 0, W, H, BG)
    c.appbar("Mi carrito")
    for i, (n, p, q) in enumerate((("Mochila wayúu", "$ 185.000", 1), ("Taza cerámica", "$ 84.000", 2), ("Café de origen", "$ 28.500", 1))):
        y = 72 + i * 76
        c.card(10, y, 280, 68)
        c.img(16, y + 8, 52, 52, ("#CFA87A", "#C9B7A3", "#B79B80")[i])
        c.text(76, y + 22, n, 9.5, INK, True)
        c.text(76, y + 36, p, 9, c.color, True)
        c.rect(76, y + 42, 66, 20, BG, LINE, 10)
        c.text(86, y + 56, "-", 11, c.color, True)
        c.text(109, y + 55, str(q), 9, INK, True, "c")
        c.text(130, y + 56, "+", 11, c.color, True)
        c.text(278, y + 20, "x", 11, RED, True, "r")
    c.card(10, 306, 280, 118)
    for i, (a, b) in enumerate((("Subtotal", "$ 297.500"), ("IVA (19%)", "$ 56.525"), ("Envío", "$ 12.000"))):
        c.text(22, 328 + i * 18, a, 8.5, GREY)
        c.text(278, 328 + i * 18, b, 8.5, INK, anchor="r")
    c.line(22, 386, 278, 386)
    c.text(22, 408, "Total", 11, INK, True)
    c.text(278, 408, "$ 366.025", 12, c.color, True, "r")
    c.btn(10, 444, 280, "Ir a pagar", h=38)
    c.text(150, 506, "Vaciar carrito", 8.5, RED, True, "c")
    c.nav(B_NAV, 2)


def b_checkout(c):
    c.rect(0, 0, W, H, BG)
    c.appbar("Pagar pedido", True)
    c.text(14, 82, "Dirección de entrega", 9.5, INK, True)
    c.map(12, 90, 276, 100)
    c.text(14, 206, "Cra 7 # 45-12, Bogotá", 8.5, INK)
    c.btn(170, 194, 118, "Usar mi ubicación", h=22, outline=True)
    c.text(14, 240, "Método de pago", 9.5, INK, True)
    for i, (t, on) in enumerate((("Tarjeta crédito / débito", True), ("PSE – Débito bancario", False), ("Nequi", False))):
        y = 248 + i * 40
        c.card(12, y, 276, 34)
        c.circle(30, y + 17, 7, white, c.color, 1.4)
        if on:
            c.circle(30, y + 17, 3.5, c.color)
        c.text(46, y + 21, t, 9, INK)
    c.field(12, 376, 276, "Número de tarjeta (Wompi)", "•••• •••• •••• 4242")
    c.card(12, 420, 276, 40)
    c.text(22, 444, "Total a pagar", 9, GREY)
    c.text(278, 445, "$ 366.025", 12, c.color, True, "r")
    c.btn(12, 470, 276, "Pagar con Wompi", h=38)
    c.text(150, 524, "Pago seguro procesado por Wompi.", 7, GREY, anchor="c")
    c.text(150, 534, "VirtualShop no almacena datos de tu tarjeta.", 7, GREY, anchor="c")


def b_orders(c):
    c.rect(0, 0, W, H, BG)
    c.appbar("Mis pedidos")
    data = (("Pedido #1042", "12 oct · 3 productos", "Aprobado", OK), ("Pedido #1031", "08 oct · 1 producto", "En camino", WARN),
            ("Pedido #1007", "01 oct · 2 productos", "Entregado", c.color), ("Pedido #0994", "27 sep · 1 producto", "Rechazado", RED))
    for i, (a, b, s, col) in enumerate(data):
        y = 74 + i * 84
        c.card(10, y, 280, 76)
        c.text(20, y + 22, a, 10, INK, True)
        c.text(20, y + 37, b, 7.5, GREY)
        c.chip(290 - len(s) * 4.6 - 22, y + 12, s, col)
        c.text(20, y + 62, "Ver recibo", 8, c.color, True)
        c.text(280, y + 62, "$ 366.025", 9, INK, True, "r")
    c.nav(B_NAV, 3)


def b_profile(c):
    c.rect(0, 0, W, H, BG)
    c.appbar("Mi perfil")
    c.circle(150, 112, 34, HexColor("#D9CDB8"))
    c.circle(176, 138, 11, c.color, white, 1.5)
    c.text(176, 142, "o", 8, white, True, "c")
    c.text(150, 168, "Juan Pérez", 13, INK, True, "c")
    c.text(150, 182, "juan@correo.com · Comprador", 8, GREY, anchor="c")
    for i, t in enumerate(("Datos personales", "Mis direcciones", "Privacidad y consentimientos", "Ingreso con huella")):
        y = 202 + i * 40
        c.card(12, y, 276, 34)
        c.text(24, y + 21, t, 9.5, INK)
        if i == 3:
            c.rect(246, y + 9, 30, 16, c.color, None, 8)
            c.circle(268, y + 17, 6, white)
        else:
            c.text(276, y + 21, ">", 10, GREY, True, "r")
    c.btn(12, 372, 276, "Editar perfil", h=34)
    c.btn(12, 414, 276, "Cerrar sesión", h=34, outline=True)
    c.text(150, 476, "Eliminar mi cuenta", 8.5, RED, True, "c")
    c.nav(B_NAV, 3)


# ------------------------------------------------------------------ VENDEDOR
V_NAV = ("Panel", "Productos", "Pedidos", "Tienda")


def v_panel(c):
    c.rect(0, 0, W, H, BG)
    c.appbar("Panel del vendedor")
    c.kpi(12, 74, "Ventas hoy", "$ 480.000")
    c.kpi(158, 74, "Pedidos nuevos", "6")
    c.kpi(12, 136, "Productos activos", "24")
    c.kpi(158, 136, "Stock bajo", "3")
    c.card(10, 200, 280, 150)
    c.text(20, 220, "Ventas de la semana", 9.5, INK, True)
    c.bars(26, 232, 248, 90, (.4, .6, .5, .8, .55, .9, .7))
    for i, d in enumerate("LMXJVSD"):
        c.text(26 + 248 / 7 * (i + .5), 340, d, 7, GREY, anchor="c")
    c.text(14, 372, "Últimos pedidos", 10, INK, True)
    c.list_row(382, "#1042 · Ana Gómez", "3 productos · $ 366.025", "Nuevo", WARN)
    c.list_row(436, "#1039 · Luis Ríos", "1 producto · $ 96.000", "Pagado", OK)
    c.nav(V_NAV, 0)


def v_products(c):
    c.rect(0, 0, W, H, BG)
    c.appbar("Mis productos")
    c.rect(12, 70, 276, 28, white, None, 14)
    c.text(26, 88, "Buscar en mi catálogo", 8.5, GREY)
    items = (("Mochila wayúu", "$ 185.000 · Stock 12", "Activo", OK, "#CFA87A"), ("Taza cerámica", "$ 42.000 · Stock 3", "Stock bajo", WARN, "#C9B7A3"),
             ("Collar en plata", "$ 96.000 · Stock 8", "En revisión", GREY, "#BBC4C9"), ("Café de origen", "$ 28.500 · Stock 40", "Activo", OK, "#B79B80"))
    for i, (n, s, st, col, tint) in enumerate(items):
        y = 108 + i * 76
        c.card(10, y, 280, 68)
        c.img(16, y + 8, 52, 52, tint)
        c.text(76, y + 24, n, 9.5, INK, True)
        c.text(76, y + 38, s, 7.5, GREY)
        c.chip(76, y + 46, st, col)
        c.text(262, y + 28, "Editar", 7.5, c.color, True, "r")
        c.text(280, y + 52, "Eliminar", 7.5, RED, True, "r")
    c.fab()
    c.nav(V_NAV, 1)


def v_form(c):
    c.rect(0, 0, W, H, BG)
    c.appbar("Nuevo producto", True)
    c.text(14, 82, "Fotos (máx. 5)", 9, INK, True)
    c.rect(12, 90, 76, 76, white, c.color, 8, 1.4)
    c.text(50, 124, "Cámara", 8.5, c.color, True, "c")
    c.text(50, 136, "Tomar foto", 7, GREY, anchor="c")
    c.img(96, 90, 76, 76, "#CFA87A")
    c.img(180, 90, 76, 76, "#C9B7A3")
    c.field(12, 178, 276, "Nombre del producto", "Mochila wayúu tejida")
    c.field(12, 218, 134, "Categoría", "Tejidos  v")
    c.field(154, 218, 134, "SKU", "MOC-0012")
    c.field(12, 258, 134, "Precio (COP)", "185000")
    c.field(154, 258, 134, "Stock", "12")
    c.field(12, 298, 276, "Descripción", "Hecha a mano por artesanas…", 52)
    c.text(14, 372, "Ubicación del producto", 9, INK, True)
    c.map(12, 380, 276, 78)
    c.btn(12, 466, 136, "Cancelar", h=34, outline=True)
    c.btn(152, 466, 136, "Guardar", h=34)


def v_orders(c):
    c.rect(0, 0, W, H, BG)
    c.appbar("Pedidos")
    c.tabs(62, ("Nuevos", "En proceso", "Enviados"), 0)
    for i, (a, b, btn_) in enumerate((("#1042 · Ana Gómez", "3 productos · $ 366.025", "Preparar"), ("#1041 · Pedro Silva", "1 producto · $ 185.000", "Preparar"), ("#1040 · María Díaz", "2 productos · $ 70.500", "Preparar"))):
        y = 100 + i * 100
        c.card(10, y, 280, 92)
        c.text(20, y + 22, a, 10, INK, True)
        c.text(20, y + 37, b, 8, GREY)
        c.text(20, y + 52, "Entrega: Bogotá · 3,2 km", 7.5, GREY)
        c.chip(240, y + 12, "Pagado", OK)
        c.btn(180, y + 58, 100, btn_, h=26)
        c.text(20, y + 76, "Ver detalle", 8, c.color, True)
    c.nav(V_NAV, 2)


def v_order_detail(c):
    c.rect(0, 0, W, H, BG)
    c.appbar("Pedido #1042", True)
    steps = ("Pagado", "Preparando", "Enviado", "Entregado")
    c.line(40, 96, 260, 96, LINE, 3)
    c.line(40, 96, 113, 96, c.color, 3)
    for i, s in enumerate(steps):
        x = 40 + i * 73.3
        c.circle(x, 96, 8, c.color if i < 2 else white, c.color if i < 2 else LINE, 1.4)
        c.text(x, 120, s, 7, INK if i < 2 else GREY, i < 2, "c")
    c.text(14, 150, "Productos", 9.5, INK, True)
    for i, (n, q) in enumerate((("Mochila wayúu", "x1 · $ 185.000"), ("Taza cerámica", "x2 · $ 84.000"), ("Café de origen", "x1 · $ 28.500"))):
        y = 158 + i * 44
        c.card(10, y, 280, 38)
        c.img(16, y + 5, 28, 28)
        c.text(52, y + 17, n, 9, INK, True)
        c.text(52, y + 30, q, 7.5, GREY)
    c.text(14, 304, "Entrega", 9.5, INK, True)
    c.text(14, 318, "Ana Gómez · +57 311 000 0000", 8, GREY)
    c.map(12, 326, 276, 90)
    c.text(14, 434, "Total", 10, INK, True)
    c.text(286, 434, "$ 366.025", 12, c.color, True, "r")
    c.btn(12, 450, 276, "Marcar como enviado", h=36)
    c.text(150, 506, "Cancelar pedido", 8.5, RED, True, "c")


def v_store(c):
    c.rect(0, 0, W, H, BG)
    c.appbar("Perfil de tienda")
    c.circle(150, 112, 32, HexColor("#D9CDB8"))
    c.circle(174, 136, 10, c.color, white, 1.5)
    c.text(150, 164, "Toca para cambiar el logo", 7.5, GREY, anchor="c")
    c.field(12, 176, 276, "Nombre comercial", "Taller Kai")
    c.field(12, 216, 134, "NIT / Cédula", "900.123.456-7")
    c.field(154, 216, 134, "Teléfono", "+57 300 111 2233")
    c.text(14, 272, "Ubicación del taller", 9, INK, True)
    c.map(12, 280, 276, 110)
    c.btn(12, 398, 136, "Actualizar GPS", h=28, outline=True)
    c.field(154, 396, 134, "Radio de entrega", "25 km", 32)
    c.btn(12, 444, 276, "Guardar cambios", h=36)
    c.nav(V_NAV, 3)


# ------------------------------------------------------------------ ADMIN
A_NAV = ("Panel", "Usuarios", "Productos", "Reportes")


def a_panel(c):
    c.rect(0, 0, W, H, BG)
    c.appbar("Administración")
    c.kpi(12, 74, "Usuarios", "1.248")
    c.kpi(158, 74, "Productos", "3.560")
    c.kpi(12, 136, "Pedidos hoy", "87")
    c.kpi(158, 136, "Ventas del mes", "$ 42,3 M")
    c.card(10, 200, 280, 140)
    c.text(20, 220, "Transacciones (30 días)", 9.5, INK, True)
    pts = (.2, .35, .3, .5, .45, .7, .65, .85)
    for i in range(len(pts) - 1):
        c.line(26 + i * 35, 330 - pts[i] * 90, 26 + (i + 1) * 35, 330 - pts[i + 1] * 90, c.color, 2)
    c.card(10, 352, 280, 44)
    c.text(20, 370, "12 productos pendientes de moderación", 8.5, INK, True)
    c.text(20, 384, "Revisar ahora >", 8, c.color, True)
    c.card(10, 404, 280, 44)
    c.text(20, 422, "3 reportes (PQRS) sin responder", 8.5, INK, True)
    c.text(20, 436, "Ver reportes >", 8, c.color, True)
    c.nav(A_NAV, 0)


def a_users(c):
    c.rect(0, 0, W, H, BG)
    c.appbar("Usuarios")
    c.rect(12, 70, 276, 28, white, None, 14)
    c.text(26, 88, "Buscar por nombre o correo", 8.5, GREY)
    x = 12
    for i, t in enumerate(("Todos", "Compradores", "Vendedores", "Admin")):
        w = c.chip(x, 106, t, c.color if i == 0 else white, white if i == 0 else INK)
        x += w + 5
    rows = (("Ana Gómez", "ana@correo.com", "Comprador", c.color), ("Taller Kai", "kai@taller.co", "Vendedor", SELLER),
            ("Luis Ríos", "luis@correo.com", "Comprador", c.color), ("Sofía Mora", "sofia@vs.co", "Admin", GREY), ("Pedro Silva", "pedro@correo.com", "Suspendido", RED))
    for i, (n, e, r, col) in enumerate(rows):
        c.list_row(130 + i * 56, n, e, r, col)
    c.fab()
    c.nav(A_NAV, 1)


def a_user_edit(c):
    c.rect(0, 0, W, H, BG)
    c.appbar("Editar usuario", True)
    c.circle(150, 112, 32, HexColor("#D9CDB8"))
    c.text(150, 164, "ID: 8fK2…xQ1 · Alta: 03/09/2026", 7.5, GREY, anchor="c")
    c.field(12, 176, 276, "Nombre completo", "Taller Kai")
    c.field(12, 216, 276, "Correo (no editable)", "kai@taller.co")
    c.field(12, 256, 276, "Rol", "Vendedor  v")
    c.card(12, 300, 276, 36)
    c.text(24, 322, "Cuenta activa", 9.5, INK)
    c.rect(246, 310, 30, 16, c.color, None, 8)
    c.circle(268, 318, 6, white)
    c.card(12, 346, 276, 36)
    c.text(24, 368, "Último acceso: 12/10/2026 08:14", 8.5, GREY)
    c.btn(12, 398, 276, "Guardar cambios", h=36)
    c.btn(12, 442, 276, "Suspender usuario", h=34, outline=True)
    c.text(150, 500, "Eliminar usuario (baja lógica)", 8.5, RED, True, "c")


def a_products(c):
    c.rect(0, 0, W, H, BG)
    c.appbar("Moderación de productos")
    c.tabs(62, ("Pendientes (12)", "Aprobados", "Rechazados"), 0)
    for i, (n, v, t) in enumerate((("Collar en plata", "Joyas del Sur", "#BBC4C9"), ("Sombrero vueltiao", "Taller Zenú", "#CFA87A"), ("Café especial 500 g", "Finca La Aurora", "#B79B80"))):
        y = 100 + i * 100
        c.card(10, y, 280, 92)
        c.img(16, y + 8, 60, 60, t)
        c.text(86, y + 24, n, 9.5, INK, True)
        c.text(86, y + 38, "Vendedor: " + v, 7.5, GREY)
        c.text(86, y + 52, "Precio: $ 96.000 · 3 fotos", 7.5, GREY)
        c.btn(86, y + 62, 94, "Aprobar", OK, h=24)
        c.btn(186, y + 62, 94, "Rechazar", RED, h=24, outline=True)
    c.nav(A_NAV, 2)


def a_reports(c):
    c.rect(0, 0, W, H, BG)
    c.appbar("Pagos y reportes")
    c.field(12, 72, 134, "Desde", "01/10/2026")
    c.field(154, 72, 134, "Hasta", "13/10/2026")
    c.card(10, 114, 280, 120)
    c.text(20, 134, "Ventas por categoría", 9.5, INK, True)
    c.bars(26, 144, 248, 70, (.9, .6, .45, .3, .2))
    for i, d in enumerate(("Tejidos", "Café", "Cerám.", "Joyas", "Otros")):
        c.text(26 + 248 / 5 * (i + .5), 226, d, 6.5, GREY, anchor="c")
    for i, (r, m, s, col) in enumerate((("VS-1042-A", "$ 366.025", "Aprobado", OK), ("VS-1041-B", "$ 185.000", "Aprobado", OK), ("VS-1040-C", "$ 70.500", "Pendiente", WARN), ("VS-0994-D", "$ 96.000", "Rechazado", RED))):
        c.list_row(246 + i * 52, r, m, s, col, False, 44)
    c.btn(12, 462, 276, "Exportar CSV", h=34, outline=True)
    c.nav(A_NAV, 3)


def a_categories(c):
    c.rect(0, 0, W, H, BG)
    c.appbar("Categorías", True)
    for i, (n, k) in enumerate((("Tejidos", "428 productos"), ("Cerámica", "310 productos"), ("Café y gastronomía", "512 productos"), ("Joyería", "287 productos"), ("Madera y talla", "199 productos"))):
        y = 74 + i * 56
        c.card(10, y, 280, 48)
        c.text(20, y + 22, n, 10, INK, True)
        c.text(20, y + 36, k, 7.5, GREY)
        c.text(222, y + 28, "Editar", 7.5, c.color, True, "r")
        c.text(280, y + 28, "Eliminar", 7.5, RED, True, "r")
    c.field(12, 362, 276, "Nueva categoría", "Instrumentos musicales")
    c.btn(12, 404, 276, "Agregar categoría", h=34)
    c.text(150, 460, "Al eliminar, los productos pasan a “Sin categoría”.", 7.5, GREY, anchor="c")


class Phone(Flowable):
    """Pantalla de teléfono con leyenda."""

    def __init__(self, draw, color, caption, scale=0.47):
        super().__init__()
        self.draw_fn, self.color, self.caption, self.s = draw, color, caption, scale
        self.width, self.height = W * scale, H * scale + 26

    def wrap(self, aw, ah):
        return self.width, self.height

    def draw(self):
        c = self.canv
        s = self.s
        c.saveState()
        c.translate(0, 26)
        c.scale(s, s)
        p = c.beginPath()
        p.roundRect(0, 0, W, H, 26)
        c.saveState()
        c.clipPath(p, stroke=0, fill=0)
        self.draw_fn(Ctx(c, self.color))
        c.restoreState()
        c.setStrokeColor(HexColor("#1B1B1B"))
        c.setLineWidth(5)
        c.roundRect(0, 0, W, H, 26, fill=0, stroke=1)
        c.restoreState()
        c.setFillColor(INK)
        c.setFont("Helvetica-Bold", 7.5)
        c.drawCentredString(self.width / 2, 8, self.caption)


SCREENS = {
    "auth": (BUYER, [(s_login, "A1. Inicio de sesión"), (s_register, "A2. Registro con rol"), (s_biometric, "A3. Ingreso con huella")]),
    "buyer": (BUYER, [(b_home, "C1. Inicio / catálogo"), (b_detail, "C2. Detalle de producto"), (b_cart, "C3. Carrito (CRUD)"),
                      (b_checkout, "C4. Pasarela de pagos"), (b_orders, "C5. Mis pedidos"), (b_profile, "C6. Perfil (CRUD)")]),
    "seller": (SELLER, [(v_panel, "V1. Panel del vendedor"), (v_products, "V2. Mis productos"), (v_form, "V3. Crear / editar producto"),
                        (v_orders, "V4. Gestión de pedidos"), (v_order_detail, "V5. Detalle de pedido"), (v_store, "V6. Perfil de tienda")]),
    "admin": (ADMIN, [(a_panel, "D1. Panel de administración"), (a_users, "D2. Administración de usuarios"), (a_user_edit, "D3. Editar usuario"),
                      (a_products, "D4. Moderación de productos"), (a_reports, "D5. Pagos y reportes"), (a_categories, "D6. Categorías")]),
}


def phones(key):
    color, items = SCREENS[key]
    return [Phone(f, color, cap) for f, cap in items]
