"""Read-only production data checks. Does not invoke Java, Gradle or Minecraft."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'
NAMES=['workbench','cutter','assembler','paint_mill','paint_press']

def main():
    for name in NAMES:
        model=json.loads((RES/f'assets/paintableblocks/models/block/{name}.json').read_text())
        assert model['parent']=='minecraft:block/block' and model['elements']
        assert all((RES/('assets/paintableblocks/textures/'+value.split(':',1)[1]+'.png')).exists() for value in model['textures'].values() if not value.startswith('#'))
        state=json.loads((RES/f'assets/paintableblocks/blockstates/{name}.json').read_text())
        assert state['variants']['']['model']==f'paintableblocks:block/{name}'
        item=json.loads((RES/f'assets/paintableblocks/items/{name}.json').read_text())
        assert item['model']['model']==f'paintableblocks:block/{name}'
    recipes={p.stem:json.loads(p.read_text()) for p in (RES/'data/paintableblocks/recipe').glob('*.json')}
    signatures=set()
    for name,recipe in recipes.items():
        if recipe['type']=='minecraft:crafting_shapeless':
            assert 1<=len(recipe['ingredients'])<=9
            key=tuple(sorted(recipe['ingredients']))
            assert key not in signatures,('Ambiguous generated shapeless recipe',name)
            signatures.add(key)
        elif recipe['type']=='minecraft:crafting_shaped':
            assert 1<=len(recipe['pattern'])<=3 and all(1<=len(row)<=3 for row in recipe['pattern'])
            assert set(''.join(recipe['pattern']))-{' '}==set(recipe['key'])
        if recipe['type'].startswith('minecraft:'):
            assert (RES/f'data/paintableblocks/advancement/recipes/{name}.json').exists()
    assert recipes['paintable_crafting']['type']=='paintableblocks:paintable_crafting'
    assert recipes['progression']['type']=='paintableblocks:progression'
    for name in ['hammer','mallet','chisel','face_scraper','cleaning_sponge','tongs','gloves','rotation_wrench','copper_paint_bucket','workbench','paint_mill']:
        assert name in recipes
    for tier in ['cobblestone','copper','iron','gold','diamond']:
        assert tier+'_brush' in recipes and tier+'_scraper' in recipes
    for lang in ['en_us','es_es']:
        strings=json.loads((RES/f'assets/paintableblocks/lang/{lang}.json').read_text())
        for name in NAMES:assert 'block.paintableblocks.'+name in strings
    print(json.dumps({'production_blocks':5,'textured_production_models':5,'recipe_files':len(recipes),'java_or_game_execution':False}))

if __name__=='__main__':main()
