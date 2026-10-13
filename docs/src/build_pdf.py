# -*- coding: utf-8 -*-
"""Genera docs/Planeacion_Tramas.pdf  ->  python3 docs/src/build_pdf.py"""
import os
import sys
from xml.sax.saxutils import escape

sys.path.insert(0, os.path.dirname(__file__))

from reportlab.graphics.shapes import Drawing, Line, Polygon, Rect, String
from reportlab.lib import colors
from reportlab.lib.colors import HexColor, white
from reportlab.lib.enums import TA_CENTER, TA_JUSTIFY
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.units import cm
from reportlab.platypus import (BaseDocTemplate, Frame, KeepTogether, NextPageTemplate, PageBreak, PageTemplate,
                                Paragraph, Spacer, Table, TableStyle)
from reportlab.platypus.tableofcontents import TableOfContents

import content as C
import mockups as M

OUT = os.path.join(os.path.dirname(__file__), "..", "Planeacion_Tramas.pdf")

# ------------------------------------------------------------------ datos de portada (editar)
INSTITUCION = "Fundación Universitaria Compensar"
FACULTAD = "Facultad de Ingeniería"
ASIGNATURA = "Desarrollo de aplicaciones móviles nativas (Android)"
PROYECTO = "Tramas"
SUBTITULO = "Planeación de tienda virtual Android"
INTEGRANTES = ["[Nombre completo – integrante 1]", "[Nombre completo – integrante 2]", "[Nombre completo – integrante 3]"]
DOCENTE = "[Nombre del docente]"
FECHA = "13 de octubre de 2026"

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
                         subject="Planeación de tienda virtual Android", **kw)
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
    c.drawCentredString(w / 2, h - 222, "Planeación")
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
    c.drawString(2 * cm, 1.15 * cm, f"{PROYECTO} · Planeación · {INSTITUCION}")
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
    d = Drawing(480, 300)
    dark = HexColor("#555555")

    def box(x, y, w, h, label, col, tcol=white, fs=7.5, sub=None):
        d.add(Rect(x, y, w, h, rx=5, ry=5, fillColor=col, strokeColor=col))
        d.add(String(x + w / 2, y + h / 2 + (1 if sub else -3), label, textAnchor="middle", fontName="Helvetica-Bold", fontSize=fs, fillColor=tcol))
        if sub:
            d.add(String(x + w / 2, y + h / 2 - 9, sub, textAnchor="middle", fontName="Helvetica", fontSize=6, fillColor=tcol))

    box(0, 262, 340, 30, ":app  (Application, MainActivity, grafo de navegación, Hilt)", DARK)
    feats = [(":feature-auth", M.BUYER), (":feature-catalog", M.BUYER), (":feature-cart", M.BUYER), (":feature-payments", M.BUYER),
             (":feature-profile", M.BUYER), (":feature-seller", M.SELLER), (":feature-admin", M.ADMIN)]
    for i, (n, col) in enumerate(feats):
        box((i % 4) * 85, 210 - (i // 4) * 38, 80, 32, n, col, fs=6.8)
    d.add(String(0, 296, "", fontSize=1))
    box(0, 128, 340, 34, ":core-domain  (entidades, casos de uso, interfaces: PaymentGateway, AuthProvider, FileStorage)", HexColor("#3B7A8F"), fs=6.8)
    box(0, 84, 110, 34, ":core-data", HexColor("#7A5C3E"), sub="Repositorios, Room, Retrofit")
    box(115, 84, 110, 34, ":core-security", HexColor("#7A5C3E"), sub="Keystore, Biometric")
    box(230, 84, 110, 34, ":core-ui", HexColor("#7A5C3E"), sub="Tema M3, componentes")
    d.add(Rect(0, 8, 340, 66, rx=5, ry=5, fillColor=HexColor("#F3F3F3"), strokeColor=HexColor("#BBBBBB")))
    d.add(String(8, 62, "CAPAS TRANSVERSALES", fontName="Helvetica-Bold", fontSize=6.5, fillColor=dark))
    for i, t in enumerate(("CameraX", "FusedLocation", "WorkManager", "FCM (push)")):
        d.add(Rect(8 + i * 82, 20, 76, 28, rx=4, ry=4, fillColor=white, strokeColor=HexColor("#BBBBBB")))
        d.add(String(46 + i * 82, 31, t, textAnchor="middle", fontName="Helvetica", fontSize=7, fillColor=dark))
    # APIs externas
    d.add(Rect(360, 8, 120, 284, rx=6, ry=6, fillColor=HexColor("#FFF6EC"), strokeColor=HexColor("#E0A030")))
    d.add(String(420, 276, "APIS EXTERNAS", textAnchor="middle", fontName="Helvetica-Bold", fontSize=7, fillColor=HexColor("#8A5A00")))
    ext = [("Firebase Auth /", "Google Sign-In"), ("Cloud Firestore", "(datos)"), ("Cloud Storage", "(imágenes)"), ("Cloud Functions", "(lógica segura)"),
           ("Wompi", "(pagos: tarjeta, PSE, Nequi)"), ("Google Maps / Geocoder", "(ubicación)")]
    for i, (a, b) in enumerate(ext):
        y = 232 - i * 40
        d.add(Rect(368, y, 104, 32, rx=4, ry=4, fillColor=white, strokeColor=HexColor("#E0A030")))
        d.add(String(420, y + 19, a, textAnchor="middle", fontName="Helvetica-Bold", fontSize=6.8, fillColor=DARK))
        d.add(String(420, y + 8, b, textAnchor="middle", fontName="Helvetica", fontSize=6, fillColor=dark))
    d.add(Line(340, 100, 360, 100, strokeColor=HexColor("#E0A030"), strokeWidth=1.2))
    d.add(Line(340, 145, 360, 145, strokeColor=HexColor("#E0A030"), strokeWidth=1.2, strokeDashArray=[2, 2]))
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
              P("Este documento presenta la <b>planeación</b> de <b>Tramas</b>, una tienda virtual para dispositivos Android desarrollada de forma nativa (Kotlin). "
                "Reúne el levantamiento de requerimientos funcionales y no funcionales, el marco normativo del comercio electrónico en Colombia, "
                "una propuesta de arquitectura modular y los mockups de la aplicación diferenciados por rol de usuario: Vendedor, Comprador y Administrador."),
              P("Fue construido con la finalidad de <b>establecer las bases lógicas del proyecto antes de escribir código</b>: acordar qué debe hacer la aplicación, "
                "bajo qué restricciones legales y de calidad debe operar y cómo se relacionan los actores entre sí. Con esto se reducen reprocesos, se facilita "
                "la estimación del esfuerzo y se dispone de un documento de referencia común para el equipo de desarrollo, el diseño y la validación."),
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
    story += [P("2.5 Arquitectura modular propuesta", "H2"),
              P("La aplicación se plantea con <b>MVVM + Clean Architecture</b> y un proyecto Gradle <b>multi-módulo</b>. Cada funcionalidad vive en su propio módulo "
                "(<i>feature</i>) y depende únicamente de <i>core-domain</i>, que define casos de uso e interfaces; los proveedores externos (autenticación, almacenamiento, pagos) "
                "se implementan en <i>core-data</i>, de modo que pueden sustituirse sin modificar la interfaz. Esto facilita el trabajo en paralelo, las pruebas y la escalabilidad."),
              KeepTogether([arch_diagram(), P("Figura 1. Arquitectura modular de Tramas y servicios externos.", "cap")])]
    story += [table(["Componente", "Tecnología / API", "Uso en Tramas"], [
        ("Lenguaje y UI", "Kotlin, Jetpack Compose, Material 3, Navigation", "Interfaz declarativa y navegación por rol."),
        ("Autenticación", "Firebase Authentication, Google Sign-In (OAuth 2.0), BiometricPrompt", "Registro, login, custom claims por rol y desbloqueo con huella."),
        ("Almacenamiento", "Cloud Firestore, Cloud Storage, Room (caché local)", "Usuarios, productos, carritos y pedidos; imágenes; uso sin conexión."),
        ("Pagos", "Wompi (tarjeta, PSE, Nequi) con Cloud Functions y webhooks", "Cobro tokenizado y confirmación de transacciones."),
        ("Cámara y ubicación", "CameraX, FusedLocationProvider, Geocoder", "Fotos de productos y perfil; cercanía y direcciones."),
        ("Otros", "Hilt, Coil, Paging 3, WorkManager, FCM, Crashlytics", "Inyección de dependencias, imágenes, listas, tareas en segundo plano, notificaciones y monitoreo."),
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
              P("Los mockups representan la interfaz propuesta con una identidad visual cercana a la implementación final: Material Design 3, barra superior de color por rol "
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

    # 6. Referencias
    story += [Spacer(1, 14), P("6. Referencias", "H1")]
    story += [Paragraph(E(r), S["ref"]) for r in C.REFERENCIAS]
    return story


if __name__ == "__main__":
    out = os.path.abspath(OUT)
    Doc(out).multiBuild(build())
    print("OK ->", out)
