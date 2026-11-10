# Guion del video: Interfaces de Tramas (máx. 5 min)

Lo que va entre **[corchetes]** es lo que se muestra en pantalla. Lo que va entre comillas es lo que se dice. Reemplaza *[tu nombre]*.

## 1. Presentación (0:00 – 0:25)
**[Android Studio con el proyecto abierto, panel izquierdo visible]**
"Hola, soy *[tu nombre]*. En este video explico las interfaces gráficas de mi tienda virtual Android, que se llama Tramas. Es un marketplace de artesanías y productos regionales de Colombia, con tres roles: comprador, vendedor y administrador. Voy a explicar el código XML, los layouts, los estilos, los drawables y los values, y al final muestro la app funcionando."

## 2. Estructura del proyecto (0:25 – 1:05)
**[Abrir `app/src/main/res` con layout, values, drawable, navigation y menu. Luego `ui/host/RoleHostActivity.kt`]**
"Dentro de la carpeta res tengo todos los recursos. En layout están las pantallas, cada una en un archivo XML. En navigation hay un grafo de navegación por cada rol, y en menu están los menús: el lateral y el de la barra inferior.
Los tres roles comparten una sola activity contenedora, RoleHostActivity. Ella trae la barra superior, el menú lateral, la barra inferior y el contenedor de fragmentos. Cada rol solo cambia tres cosas: su tema de colores, su grafo de navegación y sus menús. Así no repito código."

## 3. Values: colores, temas y estilos (1:05 – 2:00)
**[Abrir `values/colors.xml`, `themes.xml`, `styles.xml`]**
"En colors.xml definí una paleta propia: verde azulado, terracota, índigo y ocre. No uso los colores por defecto de Android.
En themes.xml hay un tema base llamado Theme.Tramas y tres temas hijos, uno por rol. Cada uno solo cambia el color primario: verde para el comprador, terracota para el vendedor e índigo para el administrador. En RoleHostActivity aplico el tema con setTheme antes de crear la pantalla.
En styles.xml están los estilos reutilizables: botones, campos de texto, tarjetas, chips, la barra superior y la inferior, y las tipografías. Así todos los componentes se ven iguales sin repetir atributos. También uso strings.xml para todos los textos en español, con plurales."

## 4. Drawables (2:00 – 2:35)
**[Abrir la carpeta `drawable`, luego `art_mochila.xml`, `bg_header_primary.xml` y `bg_selectable_option.xml`]**
"En drawable tengo íconos vectoriales y también las ilustraciones de los productos, como esta mochila, dibujadas con paths en XML. Eso las hace livianas y nítidas en cualquier pantalla.
Hay fondos con degradado, como bg_header_primary, que toma los colores del tema, por eso cambia según el rol. Y bg_selectable_option es un selector: se ve distinto cuando la opción de pago está seleccionada o no."

## 5. Pantallas transversales: splash, login y biometría (2:35 – 3:25)
**[Abrir `activity_splash.xml`, `activity_login.xml` y `LoginActivity.kt`]**
"El splash usa un ConstraintLayout con el logo, el nombre y un patrón tejido. La animación de entrada está en SplashActivity.
El login es un ScrollView con una tarjeta Material sobre un encabezado con degradado. Usa TextInputLayout para el correo y la contraseña, con íconos y validación de errores. Hay un ChipGroup para elegir el rol en modo demostración.
En LoginActivity está la huella con BiometricPrompt, que es el diálogo del sistema. Si el equipo no tiene huella registrada, la app ofrece continuar en modo demostración."

## 6. Comprador y vendedor (3:25 – 4:10)
**[Abrir `fragment_home.xml`, `item_product_card.xml`, `fragment_checkout.xml` y `fragment_product_form.xml`]**
"El inicio del comprador es un NestedScrollView con un buscador, un banner y listas horizontales de productos. Cada producto usa el layout item_product_card dentro de un RecyclerView. Para no escribir un adaptador por cada lista, creé un BindingAdapter genérico con ViewBinding.
En el pago uso un RadioGroup, y cada opción tiene el estilo propio Tramas.PayOption, que parece una tarjeta seleccionable.
En el rol vendedor, el formulario de producto tiene una foto tomada con la cámara, una lista desplegable de categorías, validaciones y un botón para ubicar el producto con el GPS."

## 7. Administrador (4:10 – 4:40)
**[Abrir `fragment_admin_dashboard.xml`, `BarChartView.kt` y `fragment_admin_users.xml`]**
"El dashboard del administrador muestra tarjetas con indicadores y un gráfico de barras. Ese gráfico es una vista personalizada, BarChartView, que dibujo con Canvas usando el color del tema.
La gestión de usuarios es un listado con búsqueda y filtro por rol, y un formulario para crear, editar y eliminar. Así queda el CRUD completo."

## 8. Demostración y cierre (4:40 – 5:00)
**[Grabación de pantalla del celular: login, huella, un recorrido rápido por cada rol]**
"Así se ve la aplicación funcionando en el celular, con los tres roles. El APK de validación y el código están en el repositorio de GitHub. Gracias."

## Consejos
- Lee el guion dos veces antes de grabar. Si te pasas de 5 minutos, acorta las secciones 5 y 6.
- Deja abierto el archivo que mencionas y señálalo con el cursor; no hace falta explicar cada línea.
