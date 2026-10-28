"""Regenerate catalog identities and their name translations; no builds or visual assets."""
from pathlib import Path
import csv
import io
import json

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'docs/obsidian/PaintableBlocks/Pinturas/vanilla-paints-26.2-id.csv'
ASSETS = ROOT / 'src/main/resources/assets/paintableblocks'


def entries():
    data = SOURCE.read_bytes()
    try:
        text = data.decode('utf-8-sig')
    except UnicodeDecodeError:
        text = data.decode('cp1252')
    rows = list(csv.DictReader(io.StringIO(text), delimiter=';'))
    result = [dict(paintId=int(r['paint_id']), blockId=r['block_id'], nameEs=r['name_es'], nameEn=r['name_en']) for r in rows]
    if len(result) != 337 or [r['paintId'] for r in result] != list(range(1, 338)):
        raise ValueError('The approved catalog must contain IDs 1..337 exactly')
    if len({r['blockId'] for r in result}) != 337:
        raise ValueError('Duplicate catalog block')
    return result


def main():
    rows = entries()
    target = ASSETS / 'catalog/paints.json'
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(rows, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    for lang, field in [('en_us', 'nameEn'), ('es_es', 'nameEs')]:
        path = ASSETS / f'lang/{lang}.json'
        translations = json.loads(path.read_text(encoding='utf-8'))
        translations.update({f"paint.paintableblocks.{r['paintId']}": r[field] for r in rows})
        path.write_text(json.dumps(translations, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print('337 catalog identities and translations generated; no Java or texture generation.')


if __name__ == '__main__':
    main()
