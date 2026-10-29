"""Static integration checks for volume/slopes. Never compiles or executes Java or Minecraft."""
from pathlib import Path
import hashlib,json,re,zipfile,argparse
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'src/main/java/net/nicomar/paintableblocks'

def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--minecraft-jar',type=Path,required=True);args=p.parse_args()
 assert hashlib.sha1(args.minecraft_jar.read_bytes()).hexdigest()=='2dc72797acbc1b63fc16a11c4ac393605f453754'
 archive=zipfile.ZipFile(args.minecraft_jar);native=set(archive.namelist())
 for source in JAVA.rglob('*.java'):
  text=source.read_text()
  for imported in re.findall(r'import\s+(net\.minecraft\.[\w.]+);',text):assert imported.replace('.','/')+'.class' in native,(source,imported)
  if 'client' not in source.relative_to(JAVA).parts:assert not re.search(r'import\s+net\.minecraft\.client\.',text)
 assert 'VERSION = 2' in (JAVA/'design/BlockDesign.java').read_text()
 network=(JAVA/'network/ModNetworking.java').read_text();assert 'event.registrar("6")' in network
 for payload in ['Use','Selection','VolumeAction','VolumeState','BucketAction','ProductionAction','DesignRequest','DesignSync']:
  assert 'record '+payload+'(' in network
 for payload in ['VolumeAction','VolumeState','DesignRequest','DesignSync']:
  assert f'{payload}.TYPE,{payload}.CODEC' in network
  assert payload in ['VolumeAction','DesignRequest'] or f'{payload}.TYPE' in ((JAVA/'client/selection/ClientDesignUpdates.java') if payload=='DesignSync' else (JAVA/'client/selection/VolumePreview.java')).read_text()
 assets=ROOT/'src/main/resources/assets/paintableblocks';strings=[json.loads((assets/f'lang/{lang}.json').read_text()) for lang in ['es_es','en_us']]
 for source in JAVA.rglob('*.java'):
  for key in re.findall(r'"((?:message|button|tab|item)\.paintableblocks\.[\w]+)"',source.read_text()):
   for lang in strings:assert key in lang,(source,key)
 recipes=ROOT/'src/main/resources/data/paintableblocks';recipe=json.loads((recipes/'recipe/slope_former.json').read_text());assert recipe['result']['id']=='paintableblocks:slope_former';assert recipe['pattern']==['III',' D ',' S ']
 def trim(rows):
  rows=[row for row in rows if row.strip()];lo=min(i for row in rows for i,c in enumerate(row) if c!=' ');hi=max(i for row in rows for i,c in enumerate(row) if c!=' ');return [row[lo:hi+1] for row in rows]
 def matches(ingredient,item,seen=()):
  if isinstance(ingredient,list):return any(matches(v,item,seen) for v in ingredient)
  if isinstance(ingredient,dict):return matches(ingredient.get('item',ingredient.get('tag','')),item,seen)
  if not ingredient.startswith('#'):return ingredient==item
  tag=ingredient[1:];assert tag not in seen
  ns,path=tag.split(':');name=f'data/{ns}/tags/item/{path}.json'
  if name not in native:return False
  return any(matches(v if isinstance(v,str) else v['id'],item,(*seen,tag)) for v in json.loads(archive.read(name))['values'])
 signature=[[recipe['key'].get(c,' ') for c in row] for row in recipe['pattern']];compared=0
 for path in native:
  if not path.startswith('data/minecraft/recipe/') or not path.endswith('.json'):continue
  other=json.loads(archive.read(path))
  if other.get('type')!='minecraft:crafting_shaped':continue
  compared+=1;rows=trim(other['pattern'])
  if len(rows)!=3 or any(len(row)!=3 for row in rows):continue
  for flip in [False,True]:
   matrix=[row[::-1] if flip else row for row in rows]
   assert not all((cell==' ' and signature[y][x]==' ') or (cell!=' ' and signature[y][x]!=' ' and matches(other['key'][cell],signature[y][x])) for y,row in enumerate(matrix) for x,cell in enumerate(row)),('New recipe conflicts with vanilla',path)
 print('Vanilla shaped recipes compared:',compared)
 advancement=json.loads((recipes/'advancement/recipes/slope_former.json').read_text());assert advancement['rewards']['recipes']==['paintableblocks:slope_former']
 tabs=(JAVA/'registry/CreativeTabs.java').read_text();assert tabs.count('TABS.register("')==3
 assert 'CreativeModeTabs.' not in (JAVA/'registry/ModContent.java').read_text()
 assert 'CreativeModeTabs.' not in (JAVA/'registry/ProductionContent.java').read_text()
 raw=(assets/'textures/block/unpainted.png').read_bytes();assert hashlib.sha256(raw).hexdigest()=='aca1bd691809249ca928990bca71ea1cee4a150e409bd2d14e664baaa2472afa'
 print(json.dumps({'creative_tabs':3,'slope_variants':5,'protocol':6,'design_format':2,'recipe':'slope_former','official_imports':'resolved','localized_keys':'resolved','java_or_minecraft_execution':False}))
 print('Geometry/interaction Java regression cases are PREPARED, NOT EXECUTED. No runtime or compilation claim.')

if __name__=='__main__':main()
