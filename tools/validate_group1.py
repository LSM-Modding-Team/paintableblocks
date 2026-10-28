"""Static group-1 data/resource checks. Never invokes Java, Gradle, a compiler or the game."""
from pathlib import Path
import argparse
import hashlib
import json
import re
import tomllib
import zipfile
from generate_catalog import entries

ROOT = Path(__file__).resolve().parents[1]
RESOURCES = ROOT / 'src/main/resources'
JAVA = ROOT / 'src/main/java/net/nicomar/paintableblocks'
CLIENT_SHA1 = '2dc72797acbc1b63fc16a11c4ac393605f453754'


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--minecraft-jar', type=Path, help='Optional official 26.2 client JAR, read as ZIP only')
    parser.add_argument('--scope-check', action='store_true', help='Also enforce the boundary of this group-1 delivery')
    args = parser.parse_args()
    rows = entries()
    actual = json.loads((RESOURCES / 'assets/paintableblocks/catalog/paints.json').read_text(encoding='utf-8'))
    assert actual == rows, 'Runtime catalog differs from approved CSV'
    for lang, field in [('en_us', 'nameEn'), ('es_es', 'nameEs')]:
        translations = json.loads((RESOURCES / f'assets/paintableblocks/lang/{lang}.json').read_text(encoding='utf-8'))
        for row in rows:
            assert translations[f"paint.paintableblocks.{row['paintId']}"] == row[field]
    properties = dict(line.split('=', 1) for line in (ROOT / 'gradle.properties').read_text().splitlines() if '=' in line and not line.startswith('#'))
    metadata_text = (RESOURCES / 'META-INF/neoforge.mods.toml').read_text(encoding='utf-8')
    metadata = tomllib.loads(re.sub(r'\$\{([^}]+)\}', lambda m: properties[m[1]], metadata_text))
    assert properties['minecraft_version'] == '26.2' and properties['neo_version'] == '26.2.0.88'
    assert metadata['mods'][0]['modId'] == 'paintableblocks'
    assert metadata['features']['paintableblocks']['javaVersion'] == '[25,)'
    resources = {str(p.relative_to(RESOURCES)): p for p in RESOURCES.rglob('*') if p.is_file()}
    docs = {p.stem for p in (ROOT / 'docs/obsidian/PaintableBlocks').rglob('*.md')}
    parsed = {key: json.loads(path.read_text(encoding='utf-8')) for key, path in resources.items() if key.endswith('.json')}
    names = ['paintable'] + [f'paintable_{n}x{n}' for n in [2, 4, 8, 16]]
    for name in names:
        assert parsed[f'assets/paintableblocks/blockstates/{name}.json']['variants']['']['type'] == 'paintableblocks:design'
        assert parsed[f'assets/paintableblocks/items/{name}.json']['model']['type'] == 'paintableblocks:design'
    mineable = parsed['data/minecraft/tags/block/mineable/pickaxe.json']
    assert mineable['replace'] is False and set(mineable['values']) == {f'paintableblocks:{n}' for n in names} | {f'paintableblocks:{n}_paint_bucket' for n in ['copper','gold','diamond','netherite']} | {f'paintableblocks:{n}' for n in ['workbench','cutter','assembler','paint_mill','paint_press']}
    for path in JAVA.rglob('*.java'):
        if 'client' not in path.relative_to(JAVA).parts:
            assert not re.search(r'import\s+net\.minecraft\.client\.', path.read_text()), f'Client import in common code: {path}'
    if args.scope_check:
        # These other gameplay fronts must remain untouched, rather than acquire partial placeholders.
        for relative in ['item/HammerItem.java', 'item/MalletItem.java', 'item/ChiselItem.java', 'item/BrushItem.java',
                         'item/RotationWrenchItem.java', 'production/WorkstationOperations.java', 'production/PaintMachineOperations.java',
                         'block/WorkstationBlock.java', 'block/PaintMachineBlock.java', 'block/PaintBucketBlock.java']:
            assert (JAVA / relative).stat().st_size == 0, f'Unexpected work outside group 1: {relative}'
        assert not any(p.suffix == '.png' for p in resources.values()), 'This delivery must not silently recreate the original unpainted PNG'
    native = None
    if args.minecraft_jar:
        assert hashlib.sha1(args.minecraft_jar.read_bytes()).hexdigest() == CLIENT_SHA1, 'Not the inspected official Minecraft 26.2 JAR'
        native = zipfile.ZipFile(args.minecraft_jar)
        native_paths = set(native.namelist())
        for row in rows:
            assert 'assets/minecraft/blockstates/' + row['blockId'].split(':')[1] + '.json' in native_paths
        for path in JAVA.rglob('*.java'):
            for imported in re.findall(r'import\s+(net\.minecraft\.[\w.]+);', path.read_text()):
                assert imported.replace('.', '/') + '.class' in native_paths, f'Unknown vanilla import: {imported}'
    def require_model(identifier):
        namespace, name = identifier.split(':', 1)
        path = f'assets/{namespace}/models/{name}.json'
        assert path in resources or (namespace == 'minecraft' and (native is None or path in native_paths)), path
    def inspect(value):
        if isinstance(value, dict):
            for key, item in value.items():
                if key in ('parent', 'base_model', 'cap_model') and isinstance(item, str):
                    require_model(item)
                if key == 'model' and isinstance(item, str):
                    require_model(item)
                if key == 'textures':
                    for texture in item.values():
                        if texture.startswith('#'):
                            continue
                        namespace, name = texture.split(':', 1)
                        target = f'assets/{namespace}/textures/{name}.png'
                        assert target in resources or (namespace == 'minecraft' and (native is None or target in native_paths)), target
                inspect(item)
        elif isinstance(value, list):
            for item in value:
                inspect(item)
    for resource,document in parsed.items():
        if resource.startswith("assets/"):
            inspect(document)
    for path in (ROOT / 'docs/obsidian/PaintableBlocks').rglob('*.md'):
        for target in re.findall(r'\[\[([^\]]+)\]\]', path.read_text()):
            assert target.split('|')[0].endswith('.csv') or target.split('|')[0] in docs, (path, target)
    for path in [JAVA / 'design/BlockDesign.java', JAVA / 'client/model/DesignMesh.java', JAVA / 'blockentity/PaintableBlockEntity.java']:
        assert path.stat().st_size > 0
    if native:
        native.close()
    print(json.dumps({'catalog_entries': len(rows), 'localized_catalog_names': 674, 'block_variants': len(names),
                      'json_files_checked': len(parsed), 'vanilla_jar_checked': bool(args.minecraft_jar),
                      'common_client_separation': 'passed', 'delivery_scope_checked': args.scope_check, 'java_compilation_or_execution': False}, ensure_ascii=False))
    print('Static checks only. Java contract harness and Minecraft behavior were NOT executed.')


if __name__ == '__main__':
    main()
