"""Static group-2 checks only. Reads text/PNG; never builds or starts Java/Minecraft."""
from pathlib import Path
import hashlib
import json
import re
import struct

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'src/main/java/net/nicomar/paintableblocks'
ASSETS = ROOT / 'src/main/resources/assets/paintableblocks'
TOOLS = ['tongs', 'gloves', 'hammer', 'mallet', 'chisel', 'face_scraper', 'cleaning_sponge', 'rotation_wrench']
MATERIALS = ['cobblestone', 'copper', 'iron', 'gold', 'diamond', 'netherite']
TOOLS += [f'{m}_{tool}' for m in MATERIALS for tool in ['brush', 'scraper']]
TOOLS += [f'{m}_paint_bucket' for m in ['copper', 'gold', 'diamond', 'netherite']]


def main():
    png = ASSETS / 'textures/block/unpainted.png'
    raw = png.read_bytes()
    assert hashlib.sha256(raw).hexdigest() == 'aca1bd691809249ca928990bca71ea1cee4a150e409bd2d14e664baaa2472afa'
    assert raw[:8] == b'\x89PNG\r\n\x1a\n' and struct.unpack('>II', raw[16:24]) == (16, 16)
    assert json.loads((ASSETS / 'models/block/unpainted.json').read_text())['textures']['all'] == 'paintableblocks:block/unpainted'
    for path in ASSETS.rglob('*'):
        if path.is_file() and any(tool in path.stem for tool in TOOLS):
            assert not any(part in ['items', 'models', 'textures'] for part in path.relative_to(ASSETS).parts), f'Tool visual asset forbidden: {path}'
    assert '.random' not in (JAVA / 'operation/CollectionPolicy.java').read_text()
    for kind in ['extract_paint', 'upgrade_tools']:
        recipe = ROOT / f'src/main/resources/data/paintableblocks/recipe/{kind}.json'
        assert json.loads(recipe.read_text())['type'] == f'paintableblocks:{kind}'
    for lang in ['es_es', 'en_us']:
        strings = json.loads((ASSETS / f'lang/{lang}.json').read_text())
        for name in TOOLS:
            prefix = 'block' if name.endswith('_paint_bucket') else 'item'
            assert f'{prefix}.paintableblocks.{name}' in strings
    for path in JAVA.rglob('*.java'):
        if 'client' not in path.relative_to(JAVA).parts:
            assert not re.search(r'import\s+net\.minecraft\.client\.', path.read_text())
    print(json.dumps({'original_png': '16x16, exact SHA256', 'tools_without_visual_assets': len(TOOLS),
                      'recipe_definitions': 2, 'group3_boundary': 'superseded by group3', 'java_compilation_or_execution': False}))
    print('Static checks only; Java contract cases are prepared but not executed.')


if __name__ == '__main__':
    main()
