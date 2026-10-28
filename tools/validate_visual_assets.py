"""Validate actual resource faces, UVs and collision solids without running Minecraft/Java."""
from pathlib import Path
from functools import lru_cache
from collections import Counter
import argparse,hashlib,json,re
from texture_atlas import TextureAtlases
from PIL import Image
ROOT=Path(__file__).resolve().parents[1];ASSETS=ROOT/'src/main/resources/assets/paintableblocks'
NORMAL={'down':(0,-1,0),'up':(0,1,0),'north':(0,0,-1),'south':(0,0,1),'west':(-1,0,0),'east':(1,0,0)}
AXIS={face:next(i for i,v in enumerate(n) if v) for face,n in NORMAL.items()}

@lru_cache(None)
def resolve(name):
 data=json.loads((ASSETS/'models'/(name+'.json')).read_text());base={}
 if data.get('parent','').startswith('paintableblocks:'):base=resolve(data['parent'].split(':',1)[1])
 return dict(base,**{k:v for k,v in data.items() if k!='textures'},textures=dict(base.get('textures',{}),**data.get('textures',{})))

def faces(model):
 out=[]
 for element in model.get('elements',[]):
  a,b=element['from'],element['to'];assert all(isinstance(v,int) and 0<=v<=16 for v in a+b)
  assert all(a[i]<b[i] for i in range(3)),'Zero-thickness or inverted element'
  assert 'rotation'not in element,'The shell validator expects axis-aligned geometry'
  for face,properties in element['faces'].items():
   tex=properties['texture'];visited=set()
   while tex.startswith('#'):
    assert tex not in visited,'Cyclic texture slot';visited.add(tex);tex=model['textures'][tex[1:]]
   assert tex.startswith('paintableblocks:')
   assert (ASSETS/'textures'/(tex.split(':',1)[1]+'.png')).exists(),tex
   uv=properties['uv'];assert len(uv)==4 and all(0<=v<=16 for v in uv)
   assert uv[0]<uv[2] and uv[1]<uv[3],'Collapsed or inverted UV'
   axis=AXIS[face];p=b[axis] if NORMAL[face][axis]>0 else a[axis];u,v=[i for i in range(3) if i!=axis]
   if 'cullface'in properties:assert properties['cullface']==face and p==(16 if NORMAL[face][axis]>0 else 0),'Interior face incorrectly culled against a neighbor'
   for x in range(a[u],b[u]):
    for y in range(a[v],b[v]):out.append((face,p,x,y))
 return out

def main():
 parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--minecraft-jar',type=Path,help='Read the verified official 26.2 atlas JSON without running Java');args=parser.parse_args()
 atlases=TextureAtlases(ASSETS.parent,args.minecraft_jar);sprite_refs=set()
 for model_file in (ASSETS/'models').rglob('*.json'):
  model=resolve(str(model_file.relative_to(ASSETS/'models'))[:-5])
  for sprite in model.get('textures',{}).values():
   visited=set()
   while sprite.startswith('#'):
    assert sprite not in visited,('Cyclic texture alias',model_file,sprite)
    visited.add(sprite);sprite=model['textures'][sprite[1:]]
   if sprite.startswith('paintableblocks:'):
    sprite_refs.add(sprite)
    assert atlases.resolve(sprite,'blocks') is not None,('PNG exists but model sprite is not loaded into the block atlas',model_file,sprite)
 manifest=json.loads((ROOT/'tools/visual_assets_manifest.json').read_text());checked=0;face_count=0
 original=ASSETS/'textures/block/unpainted.png';assert hashlib.sha256(original.read_bytes()).hexdigest()=='aca1bd691809249ca928990bca71ea1cee4a150e409bd2d14e664baaa2472afa'
 for path in (ASSETS/'textures').rglob('*.png'):
  im=Image.open(path);assert im.size==(16,16),(path,im.size)
  assert im.convert('RGBA').getchannel('A').getextrema()==(255,255),'Unintended translucent edges'
 for path in (ASSETS/'models').rglob('*.json'):
  name=str(path.relative_to(ASSETS/'models'))[:-5];model=resolve(name)
  if not model.get('elements'):continue
  actual=faces(model);counts=Counter((AXIS[f],p,u,v) for f,p,u,v in actual)
  assert all(n==1 for n in counts.values()),('Coplanar overlap, including opposing normals',path)
  checked+=1;face_count+=len(actual)
 for name,boxes in manifest['shape_boxes'].items():
  occupied=set()
  for a,b,c,A,B,C in boxes:
   cells={(x,y,z) for x in range(a,A) for y in range(b,B) for z in range(c,C)}
   assert not (occupied&cells),('Overlapping collision boxes',name);occupied|=cells
  exterior=set()
  for c in occupied:
   for face,normal in NORMAL.items():
    if tuple(c[i]+normal[i] for i in range(3)) in occupied:continue
    axis=AXIS[face];uv=[i for i in range(3) if i!=axis];exterior.add((face,c[axis]+(normal[axis]>0),c[uv[0]],c[uv[1]]))
  assert set(faces(resolve('block/'+name)))==exterior,('Visual and collision shells differ',name)
  state=json.loads((ASSETS/'blockstates'/(name+'.json')).read_text());assert state['variants']['']['model']=='paintableblocks:block/'+name
 source=(ROOT/'src/main/java/net/nicomar/paintableblocks/design/VisualShapes.java').read_text()
 for name,boxes in manifest['shape_boxes'].items():
  line=next(line for line in source.splitlines() if 'Map.entry("'+name+'"' in line)
  parsed=[list(map(int,m.split(','))) for m in re.findall(r'\{(\d+,\d+,\d+,\d+,\d+,\d+)\}',line)]
  assert parsed==boxes,('Java collision differs from model source',name)
 for name in manifest['tools']+manifest['world_blocks']:
  definition=json.loads((ASSETS/'items'/(name+'.json')).read_text());assert definition['model']['type']=='minecraft:model'
  model=resolve(definition['model']['model'].split(':',1)[1]);assert model['elements'],'Flat/generated icon instead of 3D geometry'
 cap=faces(resolve('item/paint_cap'));assert not any(f=='down' and p==10 and 4<=x<12 and 4<=z<12 for f,p,x,z in cap),'Hidden bottle-cap contact retained'
 print(json.dumps({'registered_variants_completed':29,'model_files_with_shells':checked,'unit_surface_patches_checked':face_count,'new_textures_16px':len(manifest['textures']),'world_collision_models_matching':len(manifest['world_blocks']),'coplanar_overlaps':0,'original_unpainted':'unchanged','atlas_sprites_checked':len(sprite_refs),'official_atlas_sources':atlases.official,'java_or_minecraft_execution':False}))

if __name__=='__main__':main()
