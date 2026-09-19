from pathlib import Path

FILES = {
    Path("app/src/main/res/values/strings.xml"): {
        "world_local_timezone": "Local timezone",
        "world_section_your_time": "YOUR TIME",
        "world_section_spotlight": "Spotlight cities",
        "world_section_spotlight_subtitle": "A quick view of the cities that matter most.",
        "world_section_map": "World map",
        "world_map_hint": "Drag the red meridian to explore the UTC offset.",
        "world_map_open_water": "Open water",
        "world_section_saved": "Saved cities",
        "world_section_saved_subtitle": "%1$d more cities in your clock list",
    },
    Path("app/src/main/res/values-id/strings.xml"): {
        "world_local_timezone": "Zona waktu lokal",
        "world_section_your_time": "WAKTU ANDA",
        "world_section_spotlight": "Kota pilihan",
        "world_section_spotlight_subtitle": "Tampilan cepat untuk kota yang paling penting bagi Anda.",
        "world_section_map": "Peta dunia",
        "world_map_hint": "Geser garis meridian merah untuk menjelajahi offset UTC.",
        "world_map_open_water": "Perairan terbuka",
        "world_section_saved": "Kota tersimpan",
        "world_section_saved_subtitle": "%1$d kota lain di daftar jam Anda",
    },
}

MARKER = '    <string name="world_screen_subtitle">'


def escape(value: str) -> str:
    return (value.replace('&', '&amp;')
                 .replace('<', '&lt;')
                 .replace('>', '&gt;')
                 .replace('"', '&quot;'))

for path, entries in FILES.items():
    text = path.read_text(encoding="utf-8")
    missing = [name for name in entries if f'name="{name}"' not in text]
    if not missing:
        continue
    anchor = '    </string>'
    marker_index = text.find(MARKER)
    if marker_index < 0:
        raise SystemExit(f"missing world_screen_subtitle marker in {path}")
    insert_at = text.find(anchor, marker_index) + len(anchor)
    block = ''.join(f'\n    <string name="{name}">{escape(entries[name])}</string>' for name in missing)
    text = text[:insert_at] + block + text[insert_at:]
    path.write_text(text, encoding="utf-8")
    print(f"updated {path}: {', '.join(missing)}")
