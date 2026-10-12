#!/usr/bin/env bash
# measure-device.sh -- dump the running app's on-screen element geometry from a
# uiautomator hierarchy dump, normalised to the prototype's 390x808 logical space.
#
# The emulator is configured as 1080x2238 @ 443dpi (=> exactly 390x808dp), so the
# uiautomator pixel bounds map 1:1 onto prototype CSS pixels after dividing by
# 1080/390.
#
# Usage: MSYS_NO_PATHCONV=1 bash design/mockup/tools/measure-device.sh [out.json]
set -euo pipefail

OUT="${1:-.tmp-shots/device-ui.json}"
FACTOR=$(awk 'BEGIN{printf "%.6f", 390/1080}')

MSYS_NO_PATHCONV=1 adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1
MSYS_NO_PATHCONV=1 adb pull /sdcard/ui.xml "$OUT.xml" >/dev/null 2>&1

python - "$OUT.xml" "$OUT" "$FACTOR" <<'PY'
import sys, json, re, xml.etree.ElementTree as ET
src, dst, factor = sys.argv[1], sys.argv[2], float(sys.argv[3])
tree = ET.parse(src)
root = tree.getroot()
out = []
for node in root.iter('node'):
    b = node.get('bounds') or ''
    m = re.match(r'\[(-?\d+),(-?\d+)\]\[(-?\d+),(-?\d+)\]', b)
    if not m:
        continue
    x1, y1, x2, y2 = (int(v) for v in m.groups())
    w, h = x2 - x1, y2 - y1
    if w <= 0 or h <= 0:
        continue
    out.append({
        'cls': (node.get('class') or '').split('.')[-1],
        'text': (node.get('text') or '')[:24],
        'desc': (node.get('content-desc') or '')[:24],
        'res': node.get('resource-id') or '',
        'x': round(x1 * factor, 2), 'y': round(y1 * factor, 2),
        'w': round(w * factor, 2), 'h': round(h * factor, 2),
        'clickable': node.get('clickable') == 'true',
    })
json.dump({'elements': out}, open(dst, 'w', encoding='utf-8'), ensure_ascii=False, indent=2)
print(f'wrote {dst} ({len(out)} nodes)')
PY