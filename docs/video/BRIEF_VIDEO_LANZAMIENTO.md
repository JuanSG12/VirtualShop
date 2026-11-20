# Brief: video de lanzamiento de Tramas (máx. 1 minuto)

> Para la sesión de Claude Code que tiene Remotion y los agentes de video. Todo lo necesario está aquí.

## Requisitos de la actividad (obligatorios)
- **Máximo 1 minuto** (objetivo: 55–58 s).
- Formato **tipo lanzamiento de producto**: fluido, atractivo, muestra las funcionalidades más relevantes.
- **Capturas reales del aplicativo en un dispositivo físico. NO se acepta grabación de emulador.** Todo lo que se vea de la app debe ser grabación del celular real (puede ir dentro de un marco de teléfono, pero el contenido es la grabación real).

## Especificaciones técnicas
- Vertical **1080 × 1920**, 30 fps, MP4 (H.264) + audio AAC.
- Remotion: composición de **1800 frames** (60 s a 30 fps); usar `<OffthreadVideo>` para los clips y `<Sequence>` por escena.
- Marca en `docs/video/assets/` (`logo.png`, `brand.json`): colores teal `#0F5C54`, terracota `#B5472A`, índigo `#27386B`, ocre `#E9A23B`, fondo `#FAF7F2`. Tipografía Roboto/Inter. Elemento gráfico: franja de triángulos tipo tejido (ocre/blanco) como separador.
- Idioma: español. Tono: cálido, cercano, orgullo por lo artesanal y lo local.

## Guion (storyboard)
| Tiempo | Escena | Imagen | Texto en pantalla (copy) |
|---|---|---|---|
| 0:00–0:04 | **Gancho** | Fondo teal con degradado; el logo se teje (líneas que se cruzan) y aparece el nombre | **Tramas** · "Lo hecho a mano, cerca de ti" |
| 0:04–0:10 | **Promesa** | Marco de teléfono con `clip01` (login) y entrada con huella | "Entra en un toque. Con tu huella." |
| 0:10–0:22 | **Comprador** | `clip02` catálogo → detalle → carrito; `clip03` ubicación + pago ePayco aprobado | "Descubre productos de artesanos cerca de ti" → "Pide con tu ubicación" → "Paga seguro con ePayco" |
| 0:22–0:33 | **Vendedor** | `clip04` nuevo producto tomando la foto con la cámara; `clip05` pedidos | "Vende lo que haces. Una foto y listo." → "Gestiona tus pedidos" |
| 0:33–0:43 | **Administrador** | `clip06` dashboard en tiempo real, usuarios, aprobación de productos | "Todo bajo control, en tiempo real" |
| 0:43–0:51 | **Tecnología** | Fondo claro; íconos que entran en cascada: Firebase, Firestore, Storage, Google Ubicación, Huella, ePayco | "Firebase · Firestore · Storage · Ubicación Google · Huella · ePayco" |
| 0:51–0:58 | **Cierre** | Logo + franja tejida; datos del proyecto | **Tramas** · "Tienda virtual Android" · *[Nombre del estudiante]* · *Fundación Universitaria Compensar* · (opcional) QR al repositorio `https://github.com/JuanSG12/Tramas` |

Transiciones: cortes con barrido lateral (como un telar), entrada de cada pantalla con ligero zoom; subtítulos grandes (mín. 64 px), contraste alto, máximo 2 líneas. Sincronizar los cortes con el pulso de la música.
Música: instrumental alegre/folk-electrónica, libre de derechos (YouTube Audio Library o similar), volumen bajo; sin locución obligatoria.

## Tomas que debe grabar el estudiante (celular físico)
Grabar con el **grabador de pantalla del celular** (o `scrcpy --record` / `adb shell screenrecord`), en **vertical**, con el celular al 100 % de batería, sin notificaciones (modo No molestar) y con datos de ejemplo bonitos (productos con foto real). Cada clip: 6–12 s, sin pausas largas.

| Clip | Qué grabar |
|---|---|
| `clip01_huella` | Pantalla de login → toca **Ingresar con huella** → apoya el dedo → entra a la app |
| `clip02_catalogo` | Inicio del comprador → catálogo (scroll suave) → abrir un producto → **Agregar al carrito** → carrito |
| `clip03_pago` | **Ir a pagar** → **Usar mi ubicación** (dirección se completa) → **Pagar** → ePayco → **¡Pago aprobado!** |
| `clip04_vendedor` | Vendedor: **Nuevo producto** → tocar la foto → **Tomar foto** de un producto real → llenar nombre/precio → **Guardar** |
| `clip05_pedidos` | Vendedor: **Pedidos recibidos** → tocar **Preparar** y ver cómo cambia el estado |
| `clip06_admin` | Administrador: dashboard (gráfico) → usuarios → **Pendientes** → **Aprobar** un producto |

Entregar los clips en una carpeta `public/clips/` del proyecto Remotion con esos nombres.

## Criterios de calidad
- Duración total ≤ 60 s (verificar el MP4 final).
- Todo clip de la app es grabación real de dispositivo físico (el marco de teléfono es decorativo).
- Texto legible en pantalla pequeña; nada cortado por la zona segura (dejar 120 px arriba y 200 px abajo).
- Exportar también un `Tramas_lanzamiento.mp4` ligero (< 50 MB) para subir a la plataforma.
