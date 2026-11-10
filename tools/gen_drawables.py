# -*- coding: utf-8 -*-
"""Genera los vectores de ilustración de productos y el patrón tejido (res/drawable)."""
import math, os
OUT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res", "drawable")
HEAD = '<?xml version="1.0" encoding="utf-8"?>\n<vector xmlns:android="http://schemas.android.com/apk/res/android"\n    android:width="{w}dp" android:height="{h}dp" android:viewportWidth="{w}" android:viewportHeight="{h}">\n'


def path(d, fill=None, stroke=None, sw=0, alpha=None, cap=None):
    a = ""
    if fill:
        a += f' android:fillColor="{fill}"'
    if stroke:
        a += f' android:strokeColor="{stroke}" android:strokeWidth="{sw}"'
        if cap:
            a += f' android:strokeLineCap="{cap}"'
    if alpha is not None:
        a += f' android:fillAlpha="{alpha}"'
    return f'    <path android:pathData="{d}"{a}/>\n'


def circle(cx, cy, r):
    return f"M{cx - r},{cy} a{r},{r} 0 1 0 {2 * r},0 a{r},{r} 0 1 0 {-2 * r},0 z"


def zig(x, y, w, h, n):
    step = w / n
    return "".join(f"M{x + i * step:.1f},{y + h} L{x + i * step + step / 2:.1f},{y} L{x + (i + 1) * step:.1f},{y + h} z " for i in range(n))


def save(name, body, w=160, h=160):
    with open(os.path.join(OUT, name + ".xml"), "w", encoding="utf-8") as f:
        f.write(HEAD.format(w=w, h=h) + body + "</vector>\n")


def base(bg):
    return path("M0,0h160v160h-160z", bg) + path(circle(124, 36, 52), "#FFFFFF", alpha=0.18)


# mochila
b = base("#E8C9A0")
b += path("M60,64 a20,20 0 1 1 40,0", stroke="#5B3A24", sw=5, cap="round")
b += path("M46,66 L114,66 L124,134 L36,134 Z", "#B5472A")
b += path(zig(41, 82, 78, 12, 8), "#F3D9A4") + path(zig(39, 102, 82, 12, 8), "#0F5C54") + path(zig(37, 120, 86, 10, 9), "#F3D9A4")
save("art_mochila", b)
# taza
b = base("#E7DDD0")
b += path("M50,56 h56 v52 a14,14 0 0 1 -14,14 h-28 a14,14 0 0 1 -14,-14 z", "#F6F1E8")
b += path("M106,74 h6 a12,12 0 0 1 0,26 h-6", stroke="#F6F1E8", sw=7)
b += path("M50,78 h56 v12 h-56 z", "#B5472A") + path(zig(50, 92, 56, 10, 7), "#0F5C54")
b += path("M62,42 q4,-8 0,-14 M78,42 q4,-8 0,-14 M94,42 q4,-8 0,-14", stroke="#FFFFFF", sw=3, cap="round")
save("art_taza", b)
# café
b = base("#D8C3AA")
b += path("M52,42 h56 v84 a6,6 0 0 1 -6,6 h-44 a6,6 0 0 1 -6,-6 z", "#5B3A24") + path("M52,42 h56 v16 h-56 z", "#3D2616")
b += path(circle(80, 92, 20), "#F3D9A4") + path(circle(80, 92, 9), "#5B3A24") + path("M80,84 q-5,8 0,16", stroke="#F3D9A4", sw=2)
save("art_cafe", b)
# collar
b = base("#CBD3D8")
beads = ""
for k in range(13):
    a = math.pi * (0.1 + 0.8 * k / 12)
    beads += path(circle(round(80 + 48 * math.cos(a + math.pi) * -1, 1), round(40 + 70 * math.sin(a), 1), 6), "#F8F8F8")
b += beads + path(circle(80, 112, 12), "#E9A23B") + path(circle(80, 112, 5), "#B97F1E")
save("art_collar", b)
# sombrero
b = base("#E9D8B4")
b += path("M26,104 a54,18 0 1 0 108,0 a54,18 0 1 0 -108,0 z", "#F2E3BE") + path("M54,104 v-30 a26,24 0 0 1 52,0 v30 z", "#F2E3BE")
b += path("M54,90 h52 v14 h-52 z", "#1F2328") + path(zig(54, 78, 52, 10, 8), "#1F2328")
save("art_sombrero", b)
# tejido
b = base("#F6DFB0")
cols = ["#B5472A", "#0F5C54", "#27386B", "#E9A23B", "#B5472A", "#0F5C54"]
for i, c in enumerate(cols):
    b += path(zig(20, 28 + i * 18, 120, 14, 10), c)
save("art_tejido", b)
# patrón zigzag (banners)
with open(os.path.join(OUT, "pattern_zigzag.xml"), "w", encoding="utf-8") as f:
    f.write(HEAD.format(w=240, h=12) + path(zig(0, 0, 240, 12, 30), "#FFFFFF", alpha=0.6) + "</vector>\n")
# logo (adaptive foreground y marca)
lg = path(circle(54, 54, 40), "#0F5C54")
for dy in (-14, 0, 14):
    lg += path(f"M30,{54 + dy} h48", stroke="#FFFFFF", sw=4, cap="round")
for dx in (-14, 0, 14):
    lg += path(f"M{54 + dx},30 v48", stroke="#E9A23B", sw=4, cap="round")
save("ic_logo", lg, 108, 108)
fg = ""
for dy in (-14, 0, 14):
    fg += path(f"M38,{54 + dy} h32", stroke="#FFFFFF", sw=4, cap="round")
for dx in (-14, 0, 14):
    fg += path(f"M{54 + dx},38 v32", stroke="#E9A23B", sw=4, cap="round")
save("ic_launcher_foreground", fg, 108, 108)
print("ok")
