# -*- coding: utf-8 -*-
"""Convierte marcadores "@@Texto" en recursos @string/clave y los registra en values/strings.xml.
Uso: python3 tools/extract_strings.py   (se puede ejecutar varias veces; es idempotente)"""
import glob, os, re, unicodedata
from xml.sax.saxutils import escape
RES = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res")
STR = os.path.join(RES, "values", "strings.xml")


def slug(t):
    s = unicodedata.normalize("NFD", t).encode("ascii", "ignore").decode().lower()
    s = re.sub(r"[^a-z0-9]+", "_", s).strip("_")[:38] or "txt"
    return s if not s[0].isdigit() else "n_" + s


existing = {}
if os.path.exists(STR):
    for k, v in re.findall(r'<string name="([^"]+)">(.*?)</string>', open(STR, encoding="utf-8").read(), re.S):
        existing[k] = v
by_text = {v: k for k, v in existing.items()}
pat = re.compile(r'"@@([^"]*)"')
for f in glob.glob(RES + "/**/*.xml", recursive=True):
    if f == STR:
        continue
    src = open(f, encoding="utf-8").read()
    if "@@" not in src:
        continue

    def rep(m):
        text = m.group(1).replace("&amp;", "&")
        esc = escape(text).replace("'", "\\'")
        if esc in by_text:
            return f'"@string/{by_text[esc]}"'
        k, i = slug(text), 2
        while k in existing:
            k = f"{slug(text)}_{i}"
            i += 1
        existing[k] = esc
        by_text[esc] = k
        return f'"@string/{k}"'

    open(f, "w", encoding="utf-8").write(pat.sub(rep, src))
with open(STR, "w", encoding="utf-8") as fh:
    fh.write('<?xml version="1.0" encoding="utf-8"?>\n<resources>\n')
    for k in sorted(existing):
        fh.write(f'    <string name="{k}">{existing[k]}</string>\n')
    fh.write("</resources>\n")
print(len(existing), "strings")
