#!/usr/bin/env bash
# Side-by-side compare: prototype screen (410x880) vs device screenshot.
# Device (1080px wide @ density 421 => 410dp) is scaled to 410 wide so both
# images are 1px == 1dp and can be compared directly.
#
# Usage: design/mockup/tools/compare.sh <page> <device-screenshot> [dark|light]
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"
SHOTS="$ROOT/.tmp-shots"
PAGE="${1:?page}"
DEV="${2:?device screenshot path}"
THEME="${3:-dark}"

PROTO="$SHOTS/proto-$PAGE-$THEME.png"
OUT="$SHOTS/cmp-$PAGE-$THEME.png"
[ -f "$PROTO" ] || { echo "missing prototype shot: $PROTO (run capture-proto.cjs)"; exit 1; }

magick "$DEV" -resize 390x "$SHOTS/_dev.png"
HEIGHT=$(magick identify -format "%h" "$SHOTS/_dev.png")
magick "$PROTO" -crop "390x${HEIGHT}+0+0" +repage "$SHOTS/_proto.png" 2>/dev/null || magick "$PROTO" "$SHOTS/_proto.png"
magick montage "$SHOTS/_proto.png" "$SHOTS/_dev.png" \
  -tile 2x1 -geometry +6+6 -background '#555555' \
  -label '%f' "$OUT"
echo "wrote $OUT"