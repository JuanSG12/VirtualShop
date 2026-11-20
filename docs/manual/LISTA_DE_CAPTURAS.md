# Capturas reales que necesita el manual de usuario

El manual (`docs/Manual_de_Usuario_Tramas.pdf`) debe llevar **capturas originales tomadas en tu celular físico**. Mientras no las pongas, el PDF usa capturas de borrador (marcadas "borrador") o un recuadro vacío ("pendiente").

## Cómo hacerlo
1. Con la app configurada (ver `docs/CONFIGURACION.md`) toma cada captura en el celular (botones *Encender + Bajar volumen*).
2. Pásalas al computador y renómbralas **exactamente** como en la tabla (`.png` o `.jpg`).
3. Guárdalas en `docs/manual/capturas/`.
4. Regenera el manual: `python3 docs/src/build_manual.py` (al final imprime cuáles aún faltan).

## Lista (26 capturas)
| Archivo | Qué debe mostrar |
|---|---|
| `01_login` | Pantalla de inicio de sesión |
| `02_registro` | Formulario de registro |
| `03_recuperar` | Recuperar contraseña (con el mensaje de éxito si puedes) |
| `04_activar_huella` | Diálogo "¿Activar ingreso con huella?" |
| `05_huella` | Diálogo de huella del sistema |
| `06_menu_lateral` | Menú lateral abierto |
| `b01_inicio` | Inicio del comprador |
| `b02_catalogo` | Catálogo con productos |
| `b03_detalle` | Detalle de un producto |
| `b04_carrito` | Carrito con productos y resumen |
| `b05_pago` | Pantalla de pago (dirección) |
| `b06_ubicacion` | Pago con la dirección completada con "Usar mi ubicación" |
| `b07_epayco` | Pasarela de ePayco abierta |
| `b08_pago_aprobado` | Diálogo "¡Pago aprobado!" |
| `b09_pedidos` | Mis pedidos |
| `b10_perfil` | Mi perfil |
| `v01_productos` | Mis productos (vendedor) |
| `v02_formulario` | Nuevo producto con foto |
| `v03_foto_opciones` | Diálogo "Tomar foto / Elegir de la galería" |
| `v04_pedidos` | Pedidos recibidos |
| `v05_perfil` | Perfil de vendedor |
| `a01_dashboard` | Dashboard del administrador |
| `a02_usuarios` | Gestión de usuarios |
| `a03_usuario_form` | Crear / editar usuario |
| `a04_productos` | Moderación de productos |
| `a05_reportes` | Reporte de ventas |

Consejo: crea una cuenta de cada rol (comprador, vendedor, administrador) y haz todas las capturas en una sola sesión.
