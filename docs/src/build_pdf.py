# -*- coding: utf-8 -*-
"""Genera docs/Documento_Tecnico_Definitivo_Tramas.pdf  ->  python3 docs/src/build_pdf.py"""
import os
import sys
import tempfile
from xml.sax.saxutils import escape

sys.path.insert(0, os.path.dirname(__file__))

from reportlab.graphics.shapes import Drawing, Line, Polygon, Rect, String
from reportlab.lib import colors
from reportlab.lib.colors import HexColor, white
from reportlab.lib.enums import TA_CENTER, TA_JUSTIFY
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.units import cm
from reportlab.platypus import (CondPageBreak, Image, BaseDocTemplate, Frame, KeepTogether, NextPageTemplate, PageBreak, PageTemplate,
                                Paragraph, Spacer, Table, TableStyle)
from reportlab.platypus.tableofcontents import TableOfContents

import content as C

C.apply_overrides()
import mockups as M

OUT = os.path.join(os.path.dirname(__file__), "..", "Documento_Tecnico_Definitivo_Tramas.pdf")

# ------------------------------------------------------------------ datos de portada (editar)
INSTITUCION = "Fundación Universitaria Compensar"
FACULTAD = "Facultad de Ingeniería"
ASIGNATURA = "Desarrollo de aplicaciones móviles nativas (Android)"
PROYECTO = "Tramas"
SUBTITULO = "Documento técnico definitivo"
INTEGRANTES = ["[Nombre completo – integrante 1]", "[Nombre completo – integrante 2]", "[Nombre completo – integrante 3]"]
DOCENTE = "[Nombre del docente]"
FECHA = "13 de octubre de 2026"

COVER_TITLE = "Documento técnico definitivo"
PRIMARY, DARK, SOFT = HexColor("#1F6F5C"), HexColor("#16324A"), HexColor("#EEF4F1")

# ------------------------------------------------------------------ estilos
base = dict(fontName="Helvetica", fontSize=10, leading=14.5, textColor=HexColor("#222222"))
S = {
    "body": ParagraphStyle("body", alignment=TA_JUSTIFY, spaceAfter=7, **base),
    "bullet": ParagraphStyle("bullet", leftIndent=14, bulletIndent=3, spaceAfter=3, **base),
    "H1": ParagraphStyle("H1", fontName="Helvetica-Bold", fontSize=19, leading=23, textColor=DARK, spaceBefore=4, spaceAfter=10),
    "H2": ParagraphStyle("H2", fontName="Helvetica-Bold", fontSize=13, leading=17, textColor=PRIMARY, spaceBefore=12, spaceAfter=6),
    "H3": ParagraphStyle("H3", fontName="Helvetica-Bold", fontSize=10.5, leading=14, textColor=DARK, spaceBefore=8, spaceAfter=4),
    "TOCH": ParagraphStyle("TOCH", fontName="Helvetica-Bold", fontSize=19, leading=23, textColor=DARK, spaceAfter=14),
    "cell": ParagraphStyle("cell", fontName="Helvetica", fontSize=7.6, leading=9.6, textColor=HexColor("#222222")),
    "cellb": ParagraphStyle("cellb", fontName="Helvetica-Bold", fontSize=7.6, leading=9.6, textColor=HexColor("#222222")),
    "head": ParagraphStyle("head", fontName="Helvetica-Bold", fontSize=8, leading=10, textColor=white),
    "cap": ParagraphStyle("cap", fontName="Helvetica-Oblique", fontSize=8.5, leading=11, textColor=HexColor("#555555"), alignment=TA_CENTER, spaceBefore=3, spaceAfter=10),
    "ref": ParagraphStyle("ref", leftIndent=22, firstLineIndent=-22, spaceAfter=5, **{**base, "fontSize": 9.5, "leading": 13}),
}
P = lambda t, s="body": Paragraph(t, S[s])
E = escape


def bullets(items):
    return [Paragraph(t, S["bullet"], bulletText="•") for t in items]


# ------------------------------------------------------------------ documento con TOC automática
class Doc(BaseDocTemplate):
    def __init__(self, fn, **kw):
        super().__init__(fn, pagesize=A4, title=f"{PROYECTO} – {SUBTITULO}", author=", ".join(INTEGRANTES),
                         subject="Documento técnico definitivo de tienda virtual Android", **kw)
        w, h = A4
        fr = Frame(2 * cm, 2 * cm, w - 4 * cm, h - 4 * cm, id="f")
        self.addPageTemplates([PageTemplate(id="cover", frames=[Frame(0, 0, w, h, id="c")], onPage=cover_page),
                               PageTemplate(id="main", frames=[fr], onPage=footer)])
        self._n = 0

    def beforeDocument(self):
        self._n = 0

    def afterFlowable(self, fl):
        if isinstance(fl, Paragraph) and fl.style.name in ("H1", "H2"):
            text = fl.getPlainText()
            key = f"h{self._n}"
            self._n += 1
            level = 0 if fl.style.name == "H1" else 1
            self.canv.bookmarkPage(key)
            self.canv.addOutlineEntry(text, key, level, closed=0)
            self.notify("TOCEntry", (level, text, self.page, key))


def cover_page(c, doc):
    w, h = A4
    c.setFillColor(PRIMARY)
    c.rect(0, h * .45, w, h * .55, fill=1, stroke=0)
    c.setFillColor(HexColor("#17564A"))
    c.rect(0, h * .45, w, 8, fill=1, stroke=0)
    # motivo: tres teléfonos
    for i, col in enumerate((M.BUYER, M.SELLER, M.ADMIN)):
        x = w / 2 - 105 + i * 75
        c.setFillColor(white)
        c.roundRect(x, h * .45 + 30 - i * 0, 60, 105, 8, fill=1, stroke=0)
        c.setFillColor(col)
        c.roundRect(x + 4, h * .45 + 118 - 14, 52, 11, 3, fill=1, stroke=0)
        c.rect(x + 4, h * .45 + 34, 52, 10, fill=1, stroke=0)
        c.setFillColor(HexColor("#E3DED4"))
        for k in range(3):
            c.roundRect(x + 8, h * .45 + 62 + k * 15, 44, 9, 2, fill=1, stroke=0)
    c.setFillColor(white)
    c.setFont("Helvetica", 11)
    c.drawCentredString(w / 2, h - 70, INSTITUCION.upper())
    c.setFont("Helvetica", 10)
    c.drawCentredString(w / 2, h - 86, FACULTAD)
    c.setFont("Helvetica-Bold", 46)
    c.drawCentredString(w / 2, h - 190, PROYECTO)
    c.setFont("Helvetica", 17)
    c.drawCentredString(w / 2, h - 222, COVER_TITLE)
    c.setFont("Helvetica-Oblique", 11)
    c.drawCentredString(w / 2, h - 244, "Lo hecho a mano, cerca de ti")
    c.setFillColor(HexColor("#222222"))
    y = h * .45 - 50
    c.setFont("Helvetica-Bold", 10)
    c.drawCentredString(w / 2, y, "ASIGNATURA")
    c.setFont("Helvetica", 11)
    c.drawCentredString(w / 2, y - 16, ASIGNATURA)
    y -= 52
    c.setFont("Helvetica-Bold", 10)
    c.drawCentredString(w / 2, y, "INTEGRANTES")
    c.setFont("Helvetica", 11)
    for i, n in enumerate(INTEGRANTES):
        c.drawCentredString(w / 2, y - 16 - i * 15, n)
    y -= 16 + len(INTEGRANTES) * 15 + 22
    c.setFont("Helvetica-Bold", 10)
    c.drawCentredString(w / 2, y, "DOCENTE")
    c.setFont("Helvetica", 11)
    c.drawCentredString(w / 2, y - 16, DOCENTE)
    c.setFont("Helvetica", 11)
    c.drawCentredString(w / 2, 70, "Bogotá D.C., " + FECHA)


def footer(c, doc):
    w, h = A4
    c.setStrokeColor(HexColor("#CCCCCC"))
    c.line(2 * cm, 1.6 * cm, w - 2 * cm, 1.6 * cm)
    c.setFont("Helvetica", 8)
    c.setFillColor(HexColor("#666666"))
    c.drawString(2 * cm, 1.15 * cm, f"{PROYECTO} · {COVER_TITLE} · {INSTITUCION}")
    c.drawRightString(w - 2 * cm, 1.15 * cm, f"Página {doc.page}")


# ------------------------------------------------------------------ tablas
def table(headers, rows, widths, first_bold=True):
    data = [[Paragraph(h, S["head"]) for h in headers]]
    for r in rows:
        data.append([Paragraph(E(str(v)), S["cellb"] if (i == 0 and first_bold) or v in ("Alta", "Media", "Baja") and i == 2 else S["cell"])
                     for i, v in enumerate(r)])
    t = Table(data, colWidths=widths, repeatRows=1)
    st = [("BACKGROUND", (0, 0), (-1, 0), PRIMARY), ("VALIGN", (0, 0), (-1, -1), "TOP"),
          ("GRID", (0, 0), (-1, -1), .4, HexColor("#B9C7C2")), ("TOPPADDING", (0, 0), (-1, -1), 4),
          ("BOTTOMPADDING", (0, 0), (-1, -1), 4), ("LEFTPADDING", (0, 0), (-1, -1), 4), ("RIGHTPADDING", (0, 0), (-1, -1), 4)]
    for i in range(1, len(data)):
        if i % 2 == 0:
            st.append(("BACKGROUND", (0, i), (-1, i), SOFT))
    t.setStyle(TableStyle(st))
    return t


def req_tables(groups):
    out = []
    for name, rows in groups.items():
        out.append(Paragraph(E(name), S["H3"]))
        out.append(table(["No. Requerimiento", "Descripción técnica", "Prioridad", "Observaciones para el desarrollador (datos, metadatos y seguridad)"],
                         rows, [2.3 * cm, 4.9 * cm, 1.8 * cm, 8.0 * cm]))
        out.append(Spacer(1, 6))
    return out



REPO_URL = "https://github.com/JuanSG12/Tramas"
SHOTS = os.path.join(os.path.dirname(__file__), "..", "capturas")
_tmp = tempfile.mkdtemp()


def shot_rows(items, per_row=4, title=None):
    """Filas de capturas reales (docs/capturas) con leyenda; se reducen a JPEG para no inflar el PDF."""
    from PIL import Image as PI
    cells = []
    for fn, cap in items:
        im = PI.open(os.path.join(SHOTS, fn + ".png")).convert("RGB")
        im = im.resize((540, int(540 * im.height / im.width)))
        path = os.path.join(_tmp, fn + ".jpg")
        im.save(path, quality=88)
        w = 17 * cm / per_row - 10
        cells.append([Image(path, width=w, height=w * im.height / im.width),
                      Paragraph(E(cap), ParagraphStyle("shotcap", parent=S["cap"], fontSize=7.5, leading=9, spaceBefore=2, spaceAfter=0))])
    out = []
    for i in range(0, len(cells), per_row):
        row = cells[i:i + per_row]
        while len(row) < per_row:
            row.append("")
        t = Table([[c[0] if c else "" for c in row], [c[1] if c else "" for c in row]], colWidths=[17 * cm / per_row] * per_row)
        t.setStyle(TableStyle([("ALIGN", (0, 0), (-1, -1), "CENTER"), ("VALIGN", (0, 0), (-1, -1), "TOP"),
                               ("LEFTPADDING", (0, 0), (-1, -1), 3), ("RIGHTPADDING", (0, 0), (-1, -1), 3)]))
        out += [t, Spacer(1, 8)]
    if title:
        out = [KeepTogether([P(title, "H3")] + out[:2])] + out[2:]
    return out

# ------------------------------------------------------------------ diagramas
def flow_diagram():
    d = Drawing(480, 330)
    grey = HexColor("#555555")
    lanes = [
        ("Comprador", M.BUYER, ["Inicio", "Detalle", "Carrito", "Pago", "Pedidos"], 205, "Perfil (CRUD)"),
        ("Vendedor", M.SELLER, ["Panel", "Productos", "Form. producto", "Pedidos", "Detalle pedido"], 120, "Perfil de tienda"),
        ("Administrador", M.ADMIN, ["Panel", "Usuarios", "Editar usuario", "Moderación", "Reportes"], 35, "Categorías"),
    ]

    def box(x, y, w, h, label, col):
        d.add(Rect(x, y, w, h, rx=5, ry=5, fillColor=col, strokeColor=col))
        d.add(String(x + w / 2, y + h / 2 - 2.5, label, textAnchor="middle", fontName="Helvetica-Bold", fontSize=7, fillColor=white))

    def harrow(x1, x2, y):
        d.add(Line(x1, y, x2, y, strokeColor=grey, strokeWidth=1))
        d.add(Polygon([x2, y, x2 - 4, y + 2.5, x2 - 4, y - 2.5], fillColor=grey, strokeColor=grey))

    box(10, 290, 70, 26, "Splash", grey)
    harrow(80, 110, 303)
    box(110, 290, 80, 26, "Login / Registro", M.BUYER)
    harrow(190, 225, 303)
    box(225, 290, 90, 26, "Huella (biométrico)", grey)
    harrow(315, 345, 303)
    box(345, 290, 125, 26, "Redirección según rol", DARK)
    d.add(String(407, 280, "rol = Comprador | Vendedor | Administrador", textAnchor="middle", fontName="Helvetica-Oblique", fontSize=6, fillColor=grey))
    trunk_x, trunk_y_end = 466, 72
    d.add(Line(trunk_x, 290, trunk_x, trunk_y_end, strokeColor=grey, strokeWidth=1, strokeDashArray=[3, 2]))
    for name, col, steps, y, extra in lanes:
        d.add(Rect(2, y - 14, 476, 58, rx=6, ry=6, fillColor=HexColor("#F7F7F5"), strokeColor=HexColor("#DDDDDD")))
        d.add(String(8, y - 8, name.upper(), fontName="Helvetica-Bold", fontSize=7, fillColor=col))
        d.add(String(455, y - 8, "Menú inferior: " + extra, textAnchor="end", fontName="Helvetica-Oblique", fontSize=6.5, fillColor=col))
        for i, s in enumerate(steps):
            x = 10 + i * 92
            box(x, y + 2, 74, 24, s, col)
            if i:
                harrow(x - 18, x, y + 14)
        d.add(Line(trunk_x, y + 37, 47, y + 37, strokeColor=grey, strokeWidth=1, strokeDashArray=[3, 2]))
        d.add(Line(47, y + 37, 47, y + 28, strokeColor=grey, strokeWidth=1))
        d.add(Polygon([47, y + 27, 44.5, y + 31, 49.5, y + 31], fillColor=grey, strokeColor=grey))
    return d


def arch_diagram():
    """Diagrama de la arquitectura implementada: UI -> contratos -> Firebase / servicios del dispositivo -> APIs externas."""
    d = Drawing(480, 300)
    dark = HexColor("#555555")

    def box(x, y, w, h, label, col, tcol=white, fs=7.5, sub=None):
        d.add(Rect(x, y, w, h, rx=5, ry=5, fillColor=col, strokeColor=col))
        d.add(String(x + w / 2, y + h / 2 + (2 if sub else -3), label, textAnchor="middle", fontName="Helvetica-Bold", fontSize=fs, fillColor=tcol))
        if sub:
            d.add(String(x + w / 2, y + h / 2 - 8, sub, textAnchor="middle", fontName="Helvetica", fontSize=6, fillColor=tcol))

    box(0, 262, 340, 30, "UI: Activities y Fragments (XML + ViewBinding) con Navigation Component", DARK, fs=7)
    for i, (n, col) in enumerate((("Comprador", M.BUYER), ("Vendedor", M.SELLER), ("Administrador", M.ADMIN))):
        box(i * 113, 222, 114 if i == 2 else 110, 30, n, col, sub="tema, grafo y menús propios")
    box(0, 180, 340, 32, "RoleHostActivity: Toolbar + Drawer + Bottom Navigation + NavHost", HexColor("#3B7A8F"), fs=7)
    box(0, 120, 340, 50, "Contratos (data/Services.kt)", HexColor("#6B4E9B"), sub="AuthRepository · UserRepository · ProductRepository · CartRepository · OrderRepository · ImageStorage", fs=8)
    box(0, 76, 340, 34, "Implementaciones Firebase (data/firebase)", HexColor("#7A5C3E"), sub="repositorios con Flow (listeners en tiempo real) y corrutinas", fs=8)
    d.add(Rect(0, 4, 340, 62, rx=5, ry=5, fillColor=HexColor("#F3F3F3"), strokeColor=HexColor("#BBBBBB")))
    d.add(String(8, 54, "SERVICIOS DEL DISPOSITIVO", fontName="Helvetica-Bold", fontSize=6.5, fillColor=dark))
    for i, t in enumerate(("BiometricVault", "LocationHelper", "ImagePicker", "Epayco (WebView)")):
        d.add(Rect(8 + i * 82, 12, 76, 28, rx=4, ry=4, fillColor=white, strokeColor=HexColor("#BBBBBB")))
        d.add(String(46 + i * 82, 23, t, textAnchor="middle", fontName="Helvetica", fontSize=6.8, fillColor=dark))
    d.add(Rect(360, 4, 120, 288, rx=6, ry=6, fillColor=HexColor("#FFF6EC"), strokeColor=HexColor("#E0A030")))
    d.add(String(420, 278, "SERVICIOS EXTERNOS", textAnchor="middle", fontName="Helvetica-Bold", fontSize=7, fillColor=HexColor("#8A5A00")))
    ext = [("Firebase Authentication", "(correo y contraseña)"), ("Cloud Firestore", "(usuarios, productos, órdenes)"),
           ("Cloud Storage", "(fotos)"), ("Google Location + Geocoder", "(ubicación y dirección)"),
           ("ePayco", "(checkout y validación)"), ("Android Keystore", "(huella y cifrado)")]
    for i, (a_, b_) in enumerate(ext):
        y = 232 - i * 41
        d.add(Rect(368, y, 104, 33, rx=4, ry=4, fillColor=white, strokeColor=HexColor("#E0A030")))
        d.add(String(420, y + 20, a_, textAnchor="middle", fontName="Helvetica-Bold", fontSize=6.5, fillColor=DARK))
        d.add(String(420, y + 9, b_, textAnchor="middle", fontName="Helvetica", fontSize=6, fillColor=dark))
    for y in (93, 40):
        d.add(Line(340, y, 360, y, strokeColor=HexColor("#E0A030"), strokeWidth=1.2))
    return d


def phone_rows(key, per_row=3):
    ph = M.phones(key)
    rows = [ph[i:i + per_row] for i in range(0, len(ph), per_row)]
    out = []
    for r in rows:
        r = r + [""] * (per_row - len(r))
        t = Table([r], colWidths=[17 * cm / per_row] * per_row)
        t.setStyle(TableStyle([("ALIGN", (0, 0), (-1, -1), "CENTER"), ("VALIGN", (0, 0), (-1, -1), "TOP"),
                               ("LEFTPADDING", (0, 0), (-1, -1), 4), ("RIGHTPADDING", (0, 0), (-1, -1), 4)]))
        out.append(t)
        out.append(Spacer(1, 8))
    return out


# ------------------------------------------------------------------ contenido
def build():
    story = [NextPageTemplate("main"), PageBreak()]
    toc = TableOfContents()
    toc.dotsMinLevel = 0
    toc.levelStyles = [
        ParagraphStyle("t0", fontName="Helvetica-Bold", fontSize=10.5, leading=16, leftIndent=0, spaceBefore=5),
        ParagraphStyle("t1", fontName="Helvetica", fontSize=9.5, leading=14, leftIndent=16),
    ]
    story += [P("Tabla de contenido", "TOCH"), toc, PageBreak()]

    # 1. Introducción
    story += [P("1. Introducción", "H1"),
              P("Este documento es la <b>documentación técnica definitiva</b> de <b>Tramas</b>, una tienda virtual para dispositivos Android desarrollada de forma nativa (Kotlin). "
                "Reúne el levantamiento de requerimientos funcionales y no funcionales, el marco normativo del comercio electrónico en Colombia, el diseño de las interfaces "
                "diferenciadas por rol (Vendedor, Comprador y Administrador) y la descripción de lo que realmente se construyó: arquitectura, modelo de datos, seguridad, "
                "integraciones (Firebase, geolocalización de Google y ePayco), pruebas y despliegue."),
              P("Fue construido con la finalidad de <b>dejar un documento coherente con el aplicativo entregado</b>: que cualquier persona pueda entender qué hace la aplicación, "
                "bajo qué restricciones legales y de calidad opera, cómo está organizado el código y cómo se configura y se prueba. Las secciones 1 a 5 conservan la planeación "
                "(requerimientos y diseño) y la sección 6 documenta la implementación final y cómo cada requerimiento quedó cubierto."),
              P("El documento cubre las secciones mínimas exigidas para la aplicación: <i>Inicio, Autenticación, Administración de Usuarios, Administración de Productos, "
                "CRUD de Usuario, CRUD de Productos, CRUD de Carrito, Pasarela de Pagos y CRUD de Perfil</i>, e incorpora el uso de cámara, geolocalización, "
                "ingreso con huella y APIs externas para autenticación, almacenamiento y pagos."),
              P("<b>Convenciones.</b> Los requerimientos se identifican como RF-XXX-NN (funcionales) y RNF-XXX-NN (no funcionales). La prioridad <b>Alta</b> corresponde al producto mínimo viable (MVP), "
                "<b>Media</b> a funcionalidades necesarias para la primera versión pública y <b>Baja</b> a mejoras que pueden diferirse.")]

    # 2. Enfoque
    story += [P("2. Enfoque", "H1"), P("2.1 ¿De qué trata el proyecto?", "H2"),
              P("Tramas es un <b>marketplace móvil de artesanías y productos regionales de Colombia</b> (tejidos, cerámica, café de origen, joyería, talla en madera). "
                "Conecta a artesanos y pequeños productores (vendedores) con compradores que buscan productos auténticos y cercanos, bajo la supervisión de un administrador "
                "que gestiona usuarios, moderación de productos, categorías y reportes. El comprador explora el catálogo, ve qué tan cerca está el taller, agrega productos al carrito "
                "y paga en la misma aplicación; el vendedor publica productos con fotos tomadas desde su celular y gestiona sus pedidos."),
              P("2.2 Enfoque temático y comercial", "H2"),
              P("El enfoque es <b>comercio local y de origen</b>: modelo de comisión por venta, con el pago procesado por una pasarela colombiana. La propuesta de valor es "
                "(i) vender sin necesidad de montar una tienda propia, (ii) mostrar el origen y la ubicación del productor mediante geolocalización y "
                "(iii) pagar con los medios habituales en el país (tarjetas, PSE y Nequi)."),
              P("2.3 ¿Por qué este nicho?", "H2")]
    story += bullets([
        "<b>Necesidad real:</b> muchos artesanos y productores venden por redes sociales o de forma presencial, sin catálogo estructurado, inventario ni pagos integrados.",
        "<b>Aprovecha las capacidades del dispositivo:</b> la cámara (fotos del producto), el GPS (cercanía y entrega) y la huella (acceso rápido y seguro) tienen un sentido claro en el negocio, no son adornos técnicos.",
        "<b>Alcance acotado y viable:</b> un catálogo de productos físicos con inventario simple permite cubrir todos los CRUD y el flujo completo de compra dentro de un proyecto académico.",
        "<b>Contexto regulatorio claro:</b> la venta de bienes a distancia está regulada (Estatuto del Consumidor, protección de datos), lo que permite analizar el marco legal con casos concretos.",
        "<b>Enfoque cultural y de impacto:</b> visibiliza oficios tradicionales y favorece la economía local.",
    ])
    story += [P("2.4 Actores y roles", "H2")]
    story += [table(["Rol", "Objetivo", "Funciones principales"], [
        ("Comprador", "Encontrar y comprar productos de forma segura.", "Registrarse, explorar y buscar, ver detalle, gestionar carrito, pagar, consultar pedidos, administrar su perfil."),
        ("Vendedor", "Publicar y vender sus productos.", "CRUD de productos con cámara y ubicación, control de stock, gestionar pedidos, perfil de tienda, reportes de ventas."),
        ("Administrador", "Garantizar la calidad y el cumplimiento de la plataforma.", "CRUD de usuarios, suspender cuentas, moderar productos, gestionar categorías, auditoría, pagos y reportes."),
    ], [2.6 * cm, 5.2 * cm, 9.2 * cm]), Spacer(1, 8)]
    story += [P("2.5 Arquitectura de la aplicación", "H2"),
              P("La aplicación usa una <b>arquitectura por capas</b> con contratos entre la interfaz y los datos. Las pantallas (Activities y Fragments con layouts XML y ViewBinding) "
                "solo conocen las <b>interfaces</b> de <i>data/Services.kt</i>; las implementaciones de Firebase viven en <i>data/firebase</i> y se instalan al abrir la app "
                "(clase <i>TramasApp</i>). Un único contenedor, <i>RoleHostActivity</i>, sirve a los tres roles: cada uno aporta su tema, su grafo de navegación y sus menús. "
                "Esto permite cambiar de proveedor sin tocar la interfaz y probar las pantallas con un backend en memoria."),
              KeepTogether([arch_diagram(), P("Figura 1. Arquitectura implementada de Tramas y servicios externos.", "cap")])]
    story += [table(["Componente", "Tecnología / API", "Uso en Tramas"], [
        ("Lenguaje y UI", "Kotlin, layouts XML, Material 3, ViewBinding, Navigation Component", "Pantallas por rol, navegación con menú lateral y barra inferior."),
        ("Autenticación", "Firebase Authentication (correo y contraseña), BiometricPrompt + Android Keystore", "Registro, login, recuperación de contraseña y huella (la contraseña se guarda cifrada con una llave protegida por la huella)."),
        ("Base de datos", "Cloud Firestore", "Usuarios, productos, carritos y órdenes en tiempo real, con reglas de seguridad por rol."),
        ("Imágenes", "Firebase Storage, Coil", "Fotos de productos y de perfil (cámara o galería); plan B dentro de Firestore si Storage no está activo."),
        ("Pagos", "ePayco (Checkout estándar y servicio de validación)", "Cobro con tarjeta, PSE u otros medios y confirmación del estado de la transacción."),
        ("Ubicación", "Fused Location Provider (Google Play Services) y Geocoder", "Ubicación del usuario asociada a la dirección de entrega y cálculo de cercanía."),
        ("Calidad", "JUnit, Robolectric, GitHub Actions", "Pruebas de precios y de pantallas; compilación y APK en cada push."),
    ], [3.0 * cm, 6.4 * cm, 7.6 * cm])]

    # 3. Normatividad
    story += [P("3. Normatividad legal", "H1"),
              P("La operación de una tienda virtual en Colombia implica obligaciones en protección de datos personales, protección del consumidor, comercio electrónico, "
                "tributación y seguridad informática. La siguiente tabla identifica las normas aplicables y cómo influyen en las decisiones de diseño y desarrollo. "
                "<i>Nota: la vigencia y los textos deben confirmarse en las fuentes oficiales antes de la puesta en producción; el análisis es de carácter académico y no reemplaza asesoría jurídica.</i>"),
              table(["Ley / Norma", "Descripción", "Influencia y aplicación en el desarrollo"], C.NORMAS, [4.0 * cm, 5.6 * cm, 7.4 * cm]),
              PageBreak()]

    # 4. Requerimientos
    story += [P("4. Requerimientos", "H1"),
              P("Los requerimientos se organizan por módulo y se expresan con la estructura <i>[No. Requerimiento – Descripción técnica – Prioridad – Observaciones para el desarrollador]</i>. "
                "La columna de observaciones indica los datos y metadatos involucrados y las características de seguridad que deben implementarse."),
              P("4.1 Requerimientos funcionales", "H2")]
    story += req_tables(C.RF)
    story += [PageBreak(), P("4.2 Requerimientos no funcionales", "H2"),
              P("Se agrupan en rendimiento, seguridad, usabilidad y escalabilidad (incluye compatibilidad, mantenibilidad y disponibilidad).")]
    story += req_tables(C.RNF)
    story += [PageBreak()]

    # 5. Mockup
    story += [P("5. Mockup", "H1"),
              P("Los mockups representan el diseño inicial de la interfaz (la versión implementada se documenta en la sección 6), con una identidad visual cercana a la implementación final: Material Design 3, barra superior de color por rol "
                "(<font color='#1F6F5C'><b>verde</b></font> Comprador, <font color='#C2572B'><b>terracota</b></font> Vendedor, <font color='#2D3E73'><b>azul</b></font> Administrador), "
                "tarjetas con esquinas redondeadas, navegación inferior de cuatro destinos y acciones primarias en botones de ancho completo. "
                "Los precios y datos son de ejemplo."),
              P("5.1 Lógica de navegación", "H2"),
              P("Todos los roles comparten el flujo de autenticación. Tras iniciar sesión (con contraseña, Google o huella), la app lee el rol del token y carga el grafo de navegación correspondiente. "
                "La autorización se valida además en el servidor."),
              KeepTogether([flow_diagram(), P("Figura 2. Mapa de navegación por rol.", "cap")])]
    sections = [
        ("auth", "5.2 Autenticación (común a los tres roles)", "Pantallas compartidas: inicio de sesión (contraseña, Google o huella), registro con selección de rol y confirmación biométrica."),
        ("buyer", "5.3 Rol Comprador", "Flujo principal: explorar catálogo → ver detalle → carrito (CRUD) → pasarela de pagos → seguimiento de pedidos; más el CRUD de perfil con foto por cámara y dirección por geolocalización."),
        ("seller", "5.4 Rol Vendedor", "Flujo principal: panel de ventas → CRUD de productos (cámara y ubicación) → gestión de pedidos hasta el despacho → perfil de tienda."),
        ("admin", "5.5 Rol Administrador", "Flujo principal: panel de indicadores → administración y CRUD de usuarios → moderación de productos → pagos y reportes → gestión de categorías."),
    ]
    for key, title, intro in sections:
        rows = phone_rows(key)
        story += [KeepTogether([P(title, "H2"), P(intro)] + rows[:2])] + rows[2:]
    story += [P("5.6 Correspondencia con las secciones mínimas", "H2")]
    story += [table(["Sección mínima requerida", "Pantallas", "Requerimientos"], [
        ("Inicio", "A1, C1, V1, D1", "RF-HOM-01 a 04"),
        ("Autenticación", "A1, A2, A3", "RF-AUT-01 a 07"),
        ("Administración de Usuarios / CRUD de Usuario", "D2, D3", "RF-USR-01 a 06"),
        ("Administración de Productos / CRUD de Productos", "V2, V3, D4, D6, C2", "RF-PRD-01 a 09"),
        ("CRUD de Carrito", "C3", "RF-CAR-01 a 06"),
        ("Pasarela de Pagos", "C4, C5, V4, V5, D5", "RF-PAG-01 a 08"),
        ("CRUD de Perfil", "C6, V6", "RF-PER-01 a 07"),
    ], [6.2 * cm, 5.0 * cm, 5.8 * cm])]


    # 6. Implementación del aplicativo
    story += [PageBreak(), P("6. Implementación del aplicativo", "H1"),
              P("Esta sección documenta lo que se construyó y lo contrasta con la planeación. "
                f"El código fuente completo y comentado está en el repositorio público <b>{REPO_URL}</b>; la guía para configurarlo (Firebase y ePayco) está en <i>docs/CONFIGURACION.md</i>."),
              P("6.1 Arquitectura implementada", "H2"),
              P("La Figura 1 (sección 2.5) resume las capas. El paquete <i>com.tramas.app</i> se organiza así:"),
              table(["Paquete", "Responsabilidad"], [
                  ("ui/auth", "Splash, login (correo y huella), registro y recuperación de contraseña."),
                  ("ui/host", "RoleHostActivity: contenedor común (Toolbar, menú lateral, barra inferior y NavHost) y las tres activities de rol."),
                  ("ui/buyer, ui/seller, ui/admin", "Un Fragment por pantalla de cada rol (inicio, catálogo, detalle, carrito, pago, pedidos, perfil, productos, usuarios, reportes...)."),
                  ("ui/common", "Utilidades de interfaz: BindingAdapter genérico, ImagePicker (cámara/galería), LocationHelper, BiometricHelper, carga de imágenes y gráfico BarChartView."),
                  ("ui/payment", "EpaycoCheckoutActivity: WebView con el checkout de ePayco."),
                  ("data", "Modelos, contratos (Services.kt), reglas de precio (Pricing) y sesión."),
                  ("data/firebase", "Implementaciones de los contratos con Firebase Authentication, Firestore y Storage."),
                  ("data/payment", "Configuración de ePayco y verificación de la transacción."),
                  ("security", "BiometricVault: cifrado de la contraseña con una llave del Keystore protegida por la huella."),
              ], [4.2 * cm, 12.8 * cm]),
              P("6.2 Modelo de datos en Firestore", "H2"),
              table(["Colección", "Campos principales"], [
                  ("users/{uid}", "name, email, phone, role (BUYER | SELLER | ADMIN), active, address, lat, lng, photoUrl, nit, craft, radiusKm"),
                  ("users/{uid}/cart/{productId}", "name, price, qty, stock, category, sellerId, sellerName, imageUrl (copia del producto para mostrar el carrito sin consultas extra)"),
                  ("products/{id}", "name, description, price (COP, entero), stock, category, sellerId, sellerName, status (PENDING | ACTIVE | REJECTED), lat, lng, imageUrl, createdAt"),
                  ("orders/{id}", "buyerId, buyerName, buyerEmail, items[] (productId, name, price, qty, category, sellerId), sellerIds[], subtotal, iva, shipping, total, "
                                  "status, address, lat, lng, paymentRef, paymentState, paymentMode (EPAYCO | SIMULADO), createdAt"),
              ], [4.2 * cm, 12.8 * cm]),
              P("Las consultas usan un solo filtro (por estado, vendedor o comprador) y el orden se aplica en el cliente, de modo que Firestore no necesita índices compuestos.")]

    story += [P("6.3 Seguridad y control de acceso", "H2"),
              P("El rol se guarda en <i>users/{uid}.role</i> y se hace cumplir en el servidor con las reglas de <i>firebase/firestore.rules</i>; la interfaz solo cambia lo que se muestra. "
                "Resumen de las reglas:"),
              table(["Colección", "Lectura", "Escritura"], [
                  ("users", "El propio usuario o un administrador.", "Cada usuario crea su perfil solo como Comprador o Vendedor y no puede cambiar su rol ni su estado; el administrador gestiona todos."),
                  ("users/…/cart", "Solo su dueño.", "Solo su dueño."),
                  ("products", "Cualquier usuario autenticado.", "El vendedor crea productos propios en estado PENDING y los edita sin cambiar el estado; el administrador modera; cualquier comprador solo puede descontar stock."),
                  ("orders", "Comprador, vendedores involucrados y administrador.", "El comprador la crea y registra el pago; el vendedor y el administrador actualizan el estado."),
                  ("Storage: products/, users/{uid}/", "Usuarios autenticados.", "Solo imágenes de menos de 3 MB; las de perfil, únicamente su dueño."),
              ], [3.6 * cm, 5.2 * cm, 8.2 * cm]),
              P("Además: la contraseña nunca se guarda en el dispositivo en texto plano (solo cifrada y desbloqueable con la huella), los datos de tarjeta los captura ePayco y las llaves "
                "(ePayco) se leen de <i>local.properties</i>, que no se sube a Git.")]

    story += [P("6.4 Flujos principales", "H2"), P("Inicio de sesión con huella", "H3")]
    story += bullets([
        "Tras iniciar sesión con correo y contraseña, la app ofrece <b>activar la huella</b>. Si el usuario acepta, el sistema pide la huella y con la llave que esta desbloquea se cifra y guarda la contraseña (AES-GCM, Android Keystore).",
        "En los siguientes ingresos, <b>Ingresar con huella</b> pide la huella, descifra la contraseña y autentica contra Firebase. La huella nunca sale del sistema operativo.",
        "Si el usuario registra o borra huellas en el teléfono, la llave se invalida y la app pide volver a iniciar sesión con contraseña.",
    ])
    story += [P("Compra y pago", "H3")]
    story += bullets([
        "El comprador agrega productos al <b>carrito</b> (Firestore); el resumen calcula subtotal, IVA 19 % y envío.",
        "En el <b>pago</b> escribe la dirección o usa <b>Usar mi ubicación</b>: se obtiene la ubicación con el Fused Location Provider, se convierte en dirección con Geocoder y se guarda junto a la orden.",
        "Al pagar se crea la <b>orden en estado Pendiente de pago</b> y se abre el checkout de ePayco; el comprador elige el medio de pago y paga en la página de ePayco.",
        "Al volver, la app <b>consulta a ePayco el estado real</b> de la transacción. Si fue aprobada: la orden pasa a <b>Pagado</b>, se descuenta el inventario y se vacía el carrito. Si está pendiente o fue rechazada, se registra y se informa.",
        "La orden aparece en el <b>historial del comprador</b> y en los <b>pedidos del vendedor</b>, quien la avanza a Preparando, Enviado y Entregado; el administrador la ve en los reportes.",
        "Si no hay llave de ePayco configurada, el pago se registra como <b>SIMULADO</b> (marcado en la orden) para poder probar el flujo.",
    ])

    story += [P("6.5 Servicios de Google y de terceros", "H2"),
              table(["Servicio", "Uso", "Clases"], [
                  ("Firebase Authentication", "Registro, inicio y cierre de sesión, recuperación de contraseña, eliminación de cuenta.", "FirebaseAuthRepo"),
                  ("Cloud Firestore", "Persistencia de usuarios, productos, carritos y órdenes con actualización en tiempo real.", "FirestoreUserRepo, FirestoreProductRepo, FirestoreCartRepo, FirestoreOrderRepo"),
                  ("Firebase Storage", "Fotos de productos y de perfil.", "FirebaseImageStorage"),
                  ("Fused Location Provider + Geocoder", "Ubicación del usuario y dirección de entrega.", "LocationHelper"),
                  ("BiometricPrompt + Keystore", "Ingreso con huella.", "BiometricVault, BiometricHelper"),
                  ("ePayco", "Cobro y verificación de la transacción.", "EpaycoCheckoutActivity, EpaycoApi"),
              ], [4.2 * cm, 7.0 * cm, 5.8 * cm]),
              P("6.6 Interfaces implementadas", "H2"),
              P("Las capturas siguientes se generaron con las pruebas automáticas sobre un backend en memoria; las capturas del aplicativo en un dispositivo físico, con datos reales, están en el <b>Manual de usuario</b>.")]
    story += shot_rows(title="Transversal (todos los roles)", items=[
        ("00_splash", "Splash"), ("01_login", "Inicio de sesión y huella"), ("02_register", "Registro"), ("03_forgot", "Recuperar contraseña")])
    story += shot_rows(title="Comprador", items=[
        ("buyer_00_start", "Inicio"), ("buyer_01_catalogFragment", "Catálogo"), ("buyer_02_productDetailFragment", "Detalle de producto"),
        ("buyer_03_cartFragment", "Carrito"), ("buyer_04_checkoutFragment", "Pasarela de pagos"), ("buyer_05_buyerOrdersFragment", "Mis pedidos"),
        ("buyer_06_buyerProfileFragment", "Perfil")])
    story += shot_rows(title="Vendedor", items=[
        ("seller_00_start", "Panel de productos"), ("seller_01_productFormFragment", "Crear / editar producto"),
        ("seller_02_sellerOrdersFragment", "Pedidos recibidos"), ("seller_03_sellerProfileFragment", "Perfil de vendedor")])
    story += shot_rows(title="Administrador", items=[
        ("admin_00_start", "Dashboard"), ("admin_01_adminUsersFragment", "Gestión de usuarios"), ("admin_02_userFormFragment", "Editar usuario"),
        ("admin_03_adminProductsFragment", "Gestión de productos"), ("admin_04_adminSalesFragment", "Reporte de ventas")])

    story += [CondPageBreak(9 * cm), P("6.7 Trazabilidad: requerimientos y código", "H2"),
              table(["Requerimientos", "Implementación", "Estado"], [
                  ("RF-AUT-01, 02, 05, 06, 07", "RegisterActivity, LoginActivity, ForgotPasswordActivity, RoleHostActivity.returnToLogin, firestore.rules", "Implementado"),
                  ("RF-AUT-04 (huella)", "BiometricVault, BiometricHelper, LoginActivity, BuyerProfileFragment", "Implementado"),
                  ("RF-AUT-03 (Google)", "—", "Pendiente"),
                  ("RF-HOM-01, 02, 04", "HomeFragment, CatalogFragment, RoleHostActivity", "Implementado"),
                  ("RF-HOM-03 (destacados editables)", "Los destacados se calculan (más recientes); no son editables por el administrador", "Parcial"),
                  ("RF-USR-01 a 06", "AdminUsersFragment, UserFormFragment, FirestoreUserRepo", "Implementado"),
                  ("RF-PER-01 a 04, 06, 07", "BuyerProfileFragment, SellerProfileFragment, ImagePicker, LocationHelper", "Implementado"),
                  ("RF-PER-05 (consentimientos)", "Aceptación en el registro y preferencias de notificación; sin historial de cambios", "Parcial"),
                  ("RF-PRD-01 a 07", "ProductFormFragment, SellerProductsFragment, AdminProductsFragment, FirestoreProductRepo", "Implementado"),
                  ("RF-PRD-08 (categorías)", "Lista fija en Categories.all; sin CRUD", "Parcial"),
                  ("RF-PRD-09 (reseñas)", "—", "Pendiente"),
                  ("RF-CAR-01 a 05", "CartFragment, FirestoreCartRepo, Pricing", "Implementado"),
                  ("RF-CAR-06 (revalidar precios)", "El stock se limita al agregar; el precio se toma del carrito", "Parcial"),
                  ("RF-PAG-01 a 04", "CheckoutFragment, EpaycoCheckoutActivity, EpaycoApi", "Implementado"),
                  ("RF-PAG-05, 07, 08", "BuyerOrdersFragment, SellerOrdersFragment, AdminSalesFragment (reporte CSV al portapapeles)", "Implementado"),
                  ("RF-PAG-05 (comprobante PDF), 06 (retracto), RF-PAG-07 (push)", "—", "Pendiente"),
                  ("RF-ADM-01", "AdminDashboardFragment (indicadores y gráfico en tiempo real)", "Implementado"),
                  ("RF-ADM-02, 03 (auditoría, PQRS)", "—", "Pendiente"),
              ], [4.6 * cm, 9.6 * cm, 2.8 * cm]),
              P("6.8 Pruebas, versionamiento y despliegue", "H2")]
    story += bullets([
        "<b>Pruebas unitarias (JUnit):</b> reglas de precio del carrito (IVA 19 % y envío).",
        "<b>Pruebas de pantallas (Robolectric):</b> infla las 20 pantallas de los tres roles con un backend en memoria y genera sus capturas; otra prueba comprueba que, sin Firebase configurado, la app abre el login y avisa en vez de cerrarse.",
        "<b>Versionamiento con Git y GitHub:</b> el código está en " + REPO_URL + ". El README explica la estructura y la puesta en marcha.",
        "<b>Despliegue continuo:</b> el flujo de GitHub Actions (<i>.github/workflows/android.yml</i>) compila, ejecuta las pruebas y publica el APK de depuración como artefacto en cada push.",
        "<b>Comentarios en el código:</b> todas las clases y los métodos relevantes llevan comentarios KDoc que explican su propósito y su lógica.",
    ])
    story += [P("6.9 Ajustes respecto a la planeación y trabajo futuro", "H2"),
              table(["Elemento", "Planeación", "Resultado final"], [
                  ("Interfaz", "Jetpack Compose", "Vistas XML con ViewBinding y Material 3 (alineado con el contenido del curso)."),
                  ("Arquitectura", "Multi-módulo con MVVM y Clean Architecture", "Capas ui / data con contratos intercambiables; módulos Gradle por funcionalidad quedan como evolución."),
                  ("Pasarela de pagos", "Wompi con Cloud Functions y webhooks", "ePayco (checkout estándar) con verificación de la transacción desde la app; el webhook en servidor queda como mejora."),
                  ("Cámara", "CameraX", "Cámara del sistema (TakePicture + FileProvider) o galería, con reducción de la imagen antes de subirla."),
                  ("Autenticación", "Correo, Google y huella", "Correo y huella; Google queda pendiente."),
                  ("Roles", "Custom claims de Firebase", "Campo role en Firestore validado por reglas de seguridad."),
                  ("Eliminar usuario", "Baja lógica y anonimización", "Se elimina el perfil y se bloquea el acceso; borrar la cuenta de Authentication requiere Admin SDK."),
              ], [3.2 * cm, 5.4 * cm, 8.4 * cm]),
              P("<b>Mejoras futuras:</b> inicio con Google, notificaciones push (FCM), webhook de ePayco en un servidor (Cloud Functions), CRUD de categorías, reseñas, auditoría, retracto y comprobante en PDF, "
                "mapa interactivo (Google Maps SDK) y modularización por funcionalidad.")]

    # 7. Referencias
    story += [PageBreak(), P("7. Referencias", "H1")]
    story += [Paragraph(E(r), S["ref"]) for r in C.REFERENCIAS]
    return story


if __name__ == "__main__":
    out = os.path.abspath(OUT)
    Doc(out).multiBuild(build())
    print("OK ->", out)
