"""Author original 16px pixel textures and voxel-shell JSON assets. No Java/Gradle/game invocation.

The unpainted original and vanilla paint textures are inputs owned by their respective authors;
this generator does not rewrite them. Geometry is the boundary of a labeled solid voxel union.
"""
from pathlib import Path
from collections import defaultdict
import hashlib,json,math
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/paintableblocks'
TIERS=['cobblestone','copper','iron','gold','diamond','netherite']
COLORS={'cobblestone':(119,126,128),'copper':(191,113,66),'iron':(171,190,195),'gold':(224,177,64),'diamond':(57,190,185),'netherite':(76,65,79),'wood':(139,92,52),'dark_wood':(83,54,37),'leather':(131,79,49),'dark_leather':(59,46,40),'bristles':(218,211,172),'sponge':(192,174,62),'machine':(70,86,91),'stone':(104,111,115)}
MODELS={};TEXTURES={};WORLDS={}
DIRS={'down':(0,-1,0),'up':(0,1,0),'north':(0,0,-1),'south':(0,0,1),'west':(-1,0,0),'east':(1,0,0)}
AXIS={'down':1,'up':1,'north':2,'south':2,'west':0,'east':0}

def write_json(path,data,compact_elements=False):
 path=ASSETS/path;path.parent.mkdir(parents=True,exist_ok=True)
 if compact_elements and 'elements'in data:
  header={k:v for k,v in data.items() if k!='elements'};text=json.dumps(header,indent=2,ensure_ascii=False).rstrip()[:-1].rstrip()+',\n  "elements": [\n'
  text+=',\n'.join('    '+json.dumps(e,separators=(',',':'),ensure_ascii=False) for e in data['elements'])+'\n  ]\n}\n'
 else:text=json.dumps(data,indent=2,ensure_ascii=False)+'\n'
 path.write_text(text)

def shade(color,delta):return tuple(max(0,min(255,c+delta)) for c in color)
def noise(x,y,seed):return ((x*37+y*71+seed*53)^(x*y*13+seed*17))%13-6

def texture(name,base,kind='metal',seed=0,decor=None):
 im=Image.new('RGB',(16,16));pixels=im.load()
 for y in range(16):
  for x in range(16):
   d=noise(x,y,seed)
   if kind=='wood':d+=-15 if x%5==0 else 5 if x%5==1 else 0;d+=-9 if (y+x//5*7)%13==0 else 0
   elif kind=='cloth':d+=3 if (x+y)%2==0 else -3
   elif kind=='stone':d+=-13 if (x+y//4*3)%7==0 or y%6==0 else 0
   elif kind=='bristle':d+=7 if x%3==0 else -7 if x%3==2 else 0
   elif kind=='metal':d+=8 if x in [1,2] else -9 if x==15 else 0
   pixels[x,y]=shade(base,d)
 if kind=='sponge':
  draw=ImageDraw.Draw(im)
  for x,y in [(2,2),(10,1),(6,5),(13,7),(2,10),(9,12),(14,14)]:
   draw.rectangle((x,y,x+1,y+1),fill=shade(base,-45));draw.point((x,y+2),fill=shade(base,22))
 if decor:decor(ImageDraw.Draw(im),base)
 p=ASSETS/'textures'/ (name+'.png');p.parent.mkdir(parents=True,exist_ok=True);im.save(p,optimize=True);TEXTURES[name]=im
 return 'paintableblocks:'+name

class Solid:
 def __init__(self):self.cells={}
 def box(self,x0,y0,z0,x1,y1,z1,material):
  assert 0<=x0<x1<=16 and 0<=y0<y1<=16 and 0<=z0<z1<=16
  for x in range(x0,x1):
   for y in range(y0,y1):
    for z in range(z0,z1):self.cells[x,y,z]=material
  return self
 def cut(self,x0,y0,z0,x1,y1,z1):
  for c in list(self.cells):
   if x0<=c[0]<x1 and y0<=c[1]<y1 and z0<=c[2]<z1:del self.cells[c]
  return self

def bounds_uv(face,lo,hi):
 x,y,z=lo;X,Y,Z=hi
 return {'down':[x,16-Z,X,16-z],'up':[x,z,X,Z],'north':[16-X,16-Y,16-x,16-y],'south':[x,16-Y,X,16-y],'west':[z,16-Y,Z,16-y],'east':[16-Z,16-Y,16-z,16-y]}[face]

def shell(solid,world=False,excluded=None):
 planes=defaultdict(set)
 for c,mat in solid.cells.items():
  for face,delta in DIRS.items():
   if tuple(c[i]+delta[i] for i in range(3)) in solid.cells:continue
   axis=AXIS[face];uv=[i for i in range(3) if i!=axis];plane=c[axis]+(delta[axis]>0)
   if excluded and excluded(c,face,plane):continue
   key=mat.get(face,mat['all']) if isinstance(mat,dict) else mat
   planes[face,int(plane),key].add((c[uv[0]],c[uv[1]]))
 elements=[]
 for (face,plane,mat),points in sorted(planes.items()):
  points=set(points);axis=AXIS[face];uv=[i for i in range(3) if i!=axis]
  while points:
   a,b=min(points);A=a+1
   while (A,b) in points:A+=1
   B=b+1
   while all((v,B) in points for v in range(a,A)):B+=1
   for u in range(a,A):
    for v in range(b,B):points.remove((u,v))
   lo=[0,0,0];hi=[0,0,0];lo[uv[0]]=a;hi[uv[0]]=A;lo[uv[1]]=b;hi[uv[1]]=B
   lo[axis]=plane-1 if DIRS[face][axis]>0 else plane;hi[axis]=lo[axis]+1
   f={'uv':bounds_uv(face,lo,hi),'texture':'#'+mat}
   if world and plane==(16 if DIRS[face][axis]>0 else 0):f['cullface']=face
   elements.append({'from':lo,'to':hi,'faces':{face:f}})
 return elements

def boxes(solid):
 todo=set(solid.cells);out=[]
 while todo:
  x,y,z=min(todo);X=x+1
  while (X,y,z) in todo:X+=1
  Y=y+1
  while all((a,Y,z) in todo for a in range(x,X)):Y+=1
  Z=z+1
  while all((a,b,Z) in todo for a in range(x,X) for b in range(y,Y)):Z+=1
  for a in range(x,X):
   for b in range(y,Y):
    for c in range(z,Z):todo.remove((a,b,c))
  out.append([x,y,z,X,Y,Z])
 return out

TOOL_DISPLAY={'gui':{'rotation':[18,-35,-18],'translation':[0,0,0],'scale':[.92,.92,.92]},'ground':{'rotation':[0,0,0],'translation':[0,2,0],'scale':[.5,.5,.5]},'fixed':{'rotation':[0,180,0],'translation':[0,0,0],'scale':[.8,.8,.8]},'thirdperson_righthand':{'rotation':[0,90,-35],'translation':[0,3,1],'scale':[.75,.75,.75]},'thirdperson_lefthand':{'rotation':[0,-90,35],'translation':[0,3,1],'scale':[.75,.75,.75]},'firstperson_righthand':{'rotation':[0,-90,20],'translation':[1,2,0],'scale':[.8,.8,.8]},'firstperson_lefthand':{'rotation':[0,90,-20],'translation':[1,2,0],'scale':[.8,.8,.8]}}

def model(name,solid,textures,world=False,display=None,excluded=None):
 elements=shell(solid,world,excluded);body={'parent':'minecraft:block/block','ambientocclusion':world,'gui_light':'side','textures':dict(textures,particle='#'+next(iter(textures))),'elements':elements}
 if display:body['display']=display
 write_json(Path('models')/(name+'.json'),body,True);MODELS[name]={'solid':solid,'textures':textures,'elements':elements}
 if world:WORLDS[name.split('/')[-1]]=solid
 return name

def item(name,modelpath):write_json(Path('items')/(name+'.json'),{'model':{'type':'minecraft:model','model':'paintableblocks:'+modelpath}})
def block(name,modelpath):write_json(Path('blockstates')/(name+'.json'),{'variants':{'':{'model':'paintableblocks:'+modelpath}}});item(name,modelpath)

def generate():
 common={}
 for i,(name,c) in enumerate(COLORS.items()):common[name]=texture('block/material/'+name,c,'wood' if 'wood'in name else 'stone' if name in ['cobblestone','stone'] else 'sponge' if name=='sponge' else 'bristle' if name=='bristles' else 'cloth' if 'leather'in name else 'metal',i)
 # Distinct tool silhouettes. Materials replace occupied cells; they are never layered as decals.
 tools={}
 tools['hammer']=Solid().box(7,1,7,9,11,9,'wood').box(6,8,6,10,11,10,'dark_leather').box(3,10,5,13,14,11,'iron').box(2,11,5,3,14,11,'iron').box(13,11,6,14,13,10,'iron')
 tools['mallet']=Solid().box(7,1,7,9,12,9,'dark_wood').box(3,10,4,13,15,12,'wood').box(5,10,4,6,15,12,'dark_wood').box(10,10,4,11,15,12,'dark_wood')
 tools['chisel']=Solid().box(7,7,7,9,14,9,'iron').box(6,12,7,10,15,9,'iron').box(7,15,7,9,16,9,'iron').box(6,2,6,10,7,10,'wood').box(6,1,6,10,2,10,'iron').box(6,7,6,10,8,10,'iron')
 tools['face_scraper']=Solid().box(7,1,7,9,9,9,'wood').box(6,7,6,10,10,10,'dark_leather').box(4,9,7,12,14,9,'iron').box(3,12,7,13,14,9,'iron').box(4,14,7,12,15,9,'iron')
 s=Solid().box(4,4,4,12,12,12,'sponge').box(5,3,5,11,13,11,'sponge').box(3,5,5,13,11,11,'sponge').box(5,5,3,11,11,13,'sponge');tools['cleaning_sponge']=s
 tools['tongs']=Solid().box(4,1,7,6,8,9,'dark_leather').box(10,1,7,12,8,9,'dark_leather').box(5,6,7,7,12,9,'iron').box(9,6,7,11,12,9,'iron').box(6,8,6,10,10,10,'copper').box(3,11,7,6,15,9,'iron').box(10,11,7,13,15,9,'iron').box(4,14,7,7,16,9,'iron').box(9,14,7,12,16,9,'iron')
 s=Solid()
 for x in [2,9]:
  s.box(x,4,6,x+5,10,10,'leather').box(x,2,6,x+5,4,10,'dark_leather')
  for k in [0,2,4]:s.box(x+k,10,7,x+k+1,14-(k==4),9,'leather').box(x+k,8,6,x+k+1,10,7,'iron')
 s.box(1,7,6,3,10,9,'leather').box(13,7,6,15,10,9,'leather');tools['gloves']=s
 tools['rotation_wrench']=Solid().box(7,3,7,9,11,9,'copper').box(5,0,7,11,4,9,'iron').cut(7,1,7,9,3,9).box(3,10,6,13,12,10,'iron').box(3,12,6,6,16,10,'iron').box(10,12,6,13,16,10,'iron')
 for name,s in tools.items():model('item/'+name,s,{k:common[k] for k in sorted(set(s.cells.values()))},display=TOOL_DISPLAY);item(name,'item/'+name)
 brush=Solid().box(7,0,7,9,9,9,'wood').box(6,1,7,10,3,9,'dark_leather').box(5,8,6,11,11,10,'tier').box(4,11,6,12,15,10,'bristles')
 for x in range(4,12,2):brush.box(x,15,6,x+1,16,10,'bristles')
 model('item/brush_base',brush,{'wood':common['wood'],'dark_leather':common['dark_leather'],'tier':common['iron'],'bristles':common['bristles']},display=TOOL_DISPLAY)
 scraper=Solid().box(7,0,7,9,10,9,'wood').box(6,7,6,10,11,10,'tier').box(3,10,7,13,14,9,'tier')
 for x in range(3,13,2):scraper.box(x,14,7,x+1,16,9,'tier')
 model('item/scraper_base',scraper,{'wood':common['wood'],'tier':common['iron']},display=TOOL_DISPLAY)
 for tier in TIERS:
  for kind in ['brush','scraper']:
   name=tier+'_'+kind;write_json(Path('models/item')/(name+'.json'),{'parent':'paintableblocks:item/'+kind+'_base','textures':{'tier':common[tier]}});item(name,'item/'+name)
 # Tiered, sealed paint cans. Capacity badges are pixels on the can wall, not overlapping planes.
 for i,tier in enumerate(['copper','gold','diamond','netherite']):
  def badge(draw,c,tier=tier,i=i):
   draw.rectangle((0,4,15,5),fill=shade(c,-35));draw.rectangle((4,7,11,12),fill=(221,214,187));draw.line((4,7,11,7),fill=(245,237,208))
   count=[1,4,8,8][i]
   for k in range(count):
    x=5+(k%4)*2;y=8+(k//4)*2;draw.point((x,y),fill=(56,65,68))
   if tier=='netherite':draw.line((5,11,10,11),fill=COLORS['gold'])
  texture('block/'+tier+'_paint_bucket',COLORS[tier],seed=50+i,decor=badge)
 can=Solid().box(3,0,3,13,11,13,'body').box(2,4,3,14,11,13,'body').box(3,4,2,13,11,14,'body').box(2,10,2,14,12,14,'tier').box(3,12,7,4,16,9,'iron').box(12,12,7,13,16,9,'iron').box(4,15,7,12,16,9,'iron')
 model('block/paint_bucket_base',can,{'body':'paintableblocks:block/copper_paint_bucket','tier':common['copper'],'iron':common['iron']},True)
 WORLDS.pop('paint_bucket_base')
 for tier in ['copper','gold','diamond','netherite']:
  name=tier+'_paint_bucket';write_json(Path('models/block')/(name+'.json'),{'parent':'paintableblocks:block/paint_bucket_base','textures':{'body':'paintableblocks:block/'+name,'tier':common[tier]}});block(name,'block/'+name);WORLDS[name]=can
 # Original machine face textures: end grain, framed panels, gauges and moving-part motifs are opaque pixels.
 def bench_top(d,c):
  for k in [0,4,8,12,15]:d.line((k,0,k,15),fill=shade(c,-25));d.line((0,k,15,k),fill=shade(c,-25))
  for x in [1,14]:
   for y in [1,14]:d.point((x,y),fill=COLORS['iron'])
 texture('block/workbench_top',COLORS['wood'],'wood',70,bench_top)
 def wood_side(d,c):
  d.rectangle((0,0,15,1),fill=shade(c,-30));d.rectangle((0,13,15,14),fill=shade(c,-25))
  for x in [1,14]:d.line((x,2,x,12),fill=shade(c,15))
 texture('block/workbench_side',COLORS['wood'],'wood',71,wood_side)
 for i,name in enumerate(['cutter','assembler','paint_mill','paint_press']):
  base=COLORS['stone'] if name=='paint_mill' else COLORS['machine']
  def panel(d,c,name=name):
   d.rectangle((1,6,14,14),outline=shade(c,-30));d.line((2,7,13,7),fill=shade(c,30))
   if name=='cutter':
    for x in range(3,11,2):d.line((x,10,x,13),fill=(28,39,43))
    d.rectangle((11,9,13,11),fill=COLORS['copper']);d.point((12,9),fill=(225,192,106))
   elif name=='assembler':
    for x,y in [(3,9),(6,9),(9,9),(3,12),(6,12),(9,12)]:d.rectangle((x,y,x+1,y+1),fill=COLORS['diamond'])
    d.line((12,9,12,13),fill=COLORS['copper'])
   elif name=='paint_mill':
    d.rectangle((4,9,11,13),fill=(35,40,41));d.rectangle((3,9,12,10),fill=COLORS['copper']);d.line((5,12,10,12),fill=(159,153,128))
   else:
    d.rectangle((3,11,12,13),fill=(31,42,46))
    for x in range(3,13,3):d.line((x,11,x+1,12),fill=COLORS['gold'])
  texture('block/'+name+'_side',base,'stone' if name=='paint_mill' else 'metal',80+i,panel)
  texture('block/'+name+'_top',shade(base,18),'metal',90+i,lambda d,c:d.rectangle((1,1,14,14),outline=shade(c,-30)))
 bench=Solid()
 for x in [1,12]:
  for z in [1,12]:bench.box(x,0,z,x+3,13,z+3,'dark_wood').box(x,0,z,x+3,2,z+3,'iron')
 bench.box(2,2,2,14,4,14,'wood').box(1,8,1,15,10,3,'wood').box(1,8,13,15,10,15,'wood').box(0,12,0,16,16,16,{'all':'bench_side','up':'bench_top'})
 for x in [0,14]:
  for z in [0,14]:bench.box(x,12,z,x+2,16,z+2,'iron')
 model('block/workbench',bench,{'wood':common['wood'],'dark_wood':common['dark_wood'],'iron':common['iron'],'bench_side':'paintableblocks:block/workbench_side','bench_top':'paintableblocks:block/workbench_top'},True);block('workbench','block/workbench')
 def body(name):return {'all':'body','up':'top'}
 cutter=Solid().box(0,0,0,16,10,16,body('cutter')).box(0,10,0,16,11,16,'iron')
 cutter.box(4,11,7,12,13,9,'iron').box(5,13,7,11,15,9,'iron').box(7,15,7,9,16,9,'iron')
 for x in [3,5,10,12]:cutter.box(x,11+(x in [5,10])*3,7,x+1,12+(x in [5,10])*3,9,'iron')
 cutter.box(1,11,2,3,12,14,'copper').box(13,11,2,15,12,14,'copper')
 assembler=Solid().box(0,0,0,16,12,16,body('assembler'))
 for x in [0,13]:
  for z in [0,13]:assembler.box(x,12,z,x+3,16,z+3,'copper')
 assembler.box(0,15,6,16,16,10,'iron').box(6,12,6,10,15,10,'diamond')
 mill=Solid().box(0,0,0,16,10,16,body('paint_mill')).box(2,10,2,14,14,14,'stone').cut(4,11,4,12,14,12).box(1,13,2,15,15,14,'copper').box(2,13,1,14,15,15,'copper').cut(3,13,3,13,15,13)
 mill.box(7,10,7,9,16,9,'iron').box(7,15,7,15,16,9,'dark_wood').box(13,13,7,15,15,9,'wood')
 press=Solid().box(0,0,0,16,4,16,body('paint_press')).box(1,4,2,15,6,14,'iron').box(1,4,4,4,16,12,'copper').box(12,4,4,15,16,12,'copper').box(1,13,4,15,16,12,'iron').box(4,9,4,12,11,12,'iron').box(7,11,7,9,13,9,'dark_wood')
 for name,s in [('cutter',cutter),('assembler',assembler),('paint_mill',mill),('paint_press',press)]:
  tex={'body':'paintableblocks:block/'+name+'_side','top':'paintableblocks:block/'+name+'_top'}
  for material in ['iron','copper','diamond','stone','dark_wood','wood']:tex[material]=common[material]
  model('block/'+name,s,tex,True);block(name,'block/'+name)
 # Bote cap: remove the hidden contact with the painted body (x/z 4..12, top y=10).
 cap=Solid().box(3,10,3,13,12,13,'iron').box(3,12,7,4,16,9,'iron').box(12,12,7,13,16,9,'iron').box(4,15,7,12,16,9,'iron')
 model('item/paint_cap',cap,{'iron':common['iron']},excluded=lambda c,f,p:f=='down' and p==10 and 4<=c[0]<12 and 4<=c[2]<12)
 # Persist collision geometry from exactly the same solids used to author the nine world models.
 shape_data={name:boxes(s) for name,s in WORLDS.items()}
 java='''package net.nicomar.paintableblocks.design;

import java.util.Map;
import net.minecraft.world.phys.shapes.*;

/** Generated by tools/generate_visual_assets.py; exact solid union of the resource models. */
public final class VisualShapes {
    private VisualShapes(){}
    private static VoxelShape make(int[][] boxes){VoxelShape shape=Shapes.empty();for(int[] b:boxes)shape=Shapes.joinUnoptimized(shape,Shapes.box(b[0]/16.0,b[1]/16.0,b[2]/16.0,b[3]/16.0,b[4]/16.0,b[5]/16.0),BooleanOp.OR);return shape.optimize();}
    private static final Map<String,VoxelShape> SHAPES=Map.ofEntries(
'''
 java+=',\n'.join('        Map.entry("'+name+'",make(new int[][]{'+','.join('{'+','.join(map(str,b))+'}' for b in bs)+'}))' for name,bs in sorted(shape_data.items()))
 java+='''
    );
    public static VoxelShape of(String name){var shape=SHAPES.get(name);if(shape==null)throw new IllegalArgumentException("Unknown visual shape: "+name);return shape;}
}
'''
 p=ROOT/'src/main/java/net/nicomar/paintableblocks/design/VisualShapes.java';p.parent.mkdir(parents=True,exist_ok=True);p.write_text(java)
 manifest={'schema':1,'texture_size':[16,16],'tools':list(tools)+[t+'_'+k for t in TIERS for k in ['brush','scraper']],'world_blocks':sorted(WORLDS),'textures':sorted(TEXTURES),'shape_boxes':shape_data,'generator':'tools/generate_visual_assets.py'}
 p=ROOT/'tools/visual_assets_manifest.json';p.write_text(json.dumps(manifest,indent=2)+'\n')
 print(json.dumps({'tool_items':20,'bucket_blocks':4,'production_blocks':5,'original_textures':len(TEXTURES),'shell_templates':len(MODELS),'shape_boxes':sum(map(len,shape_data.values()))}))
 return manifest

if __name__=='__main__':generate()
