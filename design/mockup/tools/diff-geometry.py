#!/usr/bin/env python
"""diff-geometry.py -- match prototype DOM elements against device uiautomator
elements by their text/content-desc and report geometric deltas.

Both sides are normalised to the prototype's 390x808 logical space, so a delta
of d means the app is off by d density-independent pixels.

Usage:
  python design/mockup/tools/diff-geometry.py <proto.json> <device.json> [--tol 1.0]
"""
import json
import sys
import argparse


def load(path):
    with open(path, encoding='utf-8') as fh:
        return json.load(fh)


def norm_text(value):
    return ''.join((value or '').split()).strip()


def index_by_text(elements, text_key, extra_key=None):
    out = {}
    for el in elements:
        key = norm_text(el.get(text_key))
        if not key:
            key = norm_text(el.get(extra_key)) if extra_key else ''
        if not key:
            continue
        out.setdefault(key, []).append(el)
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('proto')
    ap.add_argument('device')
    ap.add_argument('--tol', type=float, default=1.0)
    ap.add_argument('--csv', action='store_true')
    args = ap.parse_args()

    proto = load(args.proto)
    device = load(args.device)

    proto_els = [
        e for e in proto['elements']
        if e.get('text') and e.get('w', 0) > 0
    ]
    dev_els = device['elements']

    proto_by_text = index_by_text(proto_els, 'text')
    dev_by_text = {}
    for el in dev_els:
        for key in (norm_text(el.get('text')), norm_text(el.get('desc'))):
            if key:
                dev_by_text.setdefault(key, []).append(el)

    rows = []
    matched = 0
    for key, plist in proto_by_text.items():
        dlist = dev_by_text.get(key)
        if not dlist:
            rows.append((key, 'prod-only', None, None, None, None, None))
            continue
        p = plist[0]
        d = dlist[0]
        matched += 1
        dx = d['x'] - p['x']
        dy = d['y'] - p['y']
        dw = d['w'] - p['w']
        dh = d['h'] - p['h']
        flag = '' if max(abs(dx), abs(dy), abs(dw), abs(dh)) <= args.tol else 'DIFF'
        rows.append((key, flag, p, d, (dx, dy, dw, dh), p['fontSize'], d['h']))

    print(f'proto elements: {len(proto_els)}   matched by text: {matched}   '
          f'tolerance: +/-{args.tol}dp')
    print()
    header = f"{'text':22} {'flag':9} {'proto x/y/w/h':32} {'dx/dy/dw/dh':30} fs"
    print(header)
    print('-' * len(header))
    for key, flag, p, d, delta, fs, dh in rows:
        if p is None:
            print(f'{key[:22]:22} {flag:9} {"missing on device":32}')
            continue
        pb = f"{p['x']:.1f},{p['y']:.1f},{p['w']:.1f},{p['h']:.1f}"
        db = f"{delta[0]:+.1f},{delta[1]:+.1f},{delta[2]:+.1f},{delta[3]:+.1f}"
        print(f'{key[:22]:22} {flag:9} {pb:32} {db:30} {fs}')

    diffs = [r for r in rows if r[1] == 'DIFF']
    print()
    print(f'differences (> {args.tol}dp): {len(diffs)}')
    for key, flag, p, d, delta, fs, dh in diffs:
        print(f'  {key[:26]:28} d(x,y,w,h)={delta[0]:+.1f},{delta[1]:+.1f},'
              f'{delta[2]:+.1f},{delta[3]:+.1f}')


if __name__ == '__main__':
    main()