"""Software inspection of resource-model faces. Does not run Minecraft or Java."""
from pathlib import Path
from functools import lru_cache
import json,math,textwrap
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(__file__).resolve().parents[1];ASSETS=ROOT/'src/main/resources/assets/paintableblocks'
NORMAL={'up':(0,1,0),'down':(0,-1,0),'east':(1,0,0),'west':(-1,0,0),'south':(0,0,1),'north':(0,0,-1)}
@lru_cache(None)
def resolve(name):
 p=ASSETS/'models'/(name+'.json');obj=json.loads(p.read_text());base={}
 if obj.get('parent','').startswith('paintableblocks:'):base=resolve(obj['parent'].split(':')[1])
 return dict(base,**{k:v for k,v in obj.items() if k!='textures'},textures=dict(base.get('textures',{}),**obj.get('textures',{})))
@lru_cache(None)
def texture(name):return Image.open(ASSETS/'textures'/(name.split(':')[1]+'.png')).convert('RGB')
def image_for(name):
 m=resolve(name);faces=[]
 for e in m.get('elements',[]):
  lo=e['from'];hi=e['to']
  for face,f in e['faces'].items():
   if sum(NORMAL[face])<=0:continue
   tex=f['texture']
   while tex.startswith('#'):tex=m['textures'][tex[1:]]
   im=texture(tex);axis=next(i for i,v in enumerate(NORMAL[face]) if v);uv=[i for i in range(3) if i!=axis];plane=hi[axis]
   for a in range(lo[uv[0]],hi[uv[0]]):
    for b in range(lo[uv[1]],hi[uv[1]]):
     c=[0,0,0];c[axis]=plane;c[uv[0]]=a+.5;c[uv[1]]=b+.5;x,y,z=c
     u,v={'up':(x,z),'east':(16-z,16-y),'south':(x,16-y)}[face];color=im.getpixel((max(0,min(15,int(u))),max(0,min(15,int(v)))))
     light={'up':1.0,'east':.75,'south':.9}[face];color=tuple(int(t*light) for t in color)
     vertices=[]
     for A,B in [(a,b),(a+1,b),(a+1,b+1),(a,b+1)]:
      q=[0,0,0];q[axis]=plane;q[uv[0]]=A;q[uv[1]]=B;vertices.append(q)
     faces.append((sum(c),vertices,color))
 out=Image.new('RGB',(164,138),(31,38,44));draw=ImageDraw.Draw(out)
 for _,poly,color in sorted(faces,key=lambda f:f[0]):draw.polygon([(82+(p[0]-p[2])*3.8,73+((p[0]+p[2]-16)*.5-(p[1]-8))*3.8) for p in poly],fill=color)
 return out

def main():
 manifest=json.loads((ROOT/'tools/visual_assets_manifest.json').read_text());lang=json.loads((ASSETS/'lang/es_es.json').read_text());ids=manifest['tools']+[t+'_paint_bucket' for t in ['copper','gold','diamond','netherite']]+['workbench','cutter','assembler','paint_mill','paint_press']
 W=1068;H=1040;out=Image.new('RGB',(W,H),(23,29,35));d=ImageDraw.Draw(out)
 try:font=ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf',14);title=ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf',24)
 except OSError:font=ImageFont.load_default();title=font
 d.text((24,18),'PaintableBlocks · recursos 3D',font=title,fill=(227,221,202));d.text((24,54),'Vista técnica de los modelos JSON · texturas originales de 16×16 · no es una captura de Minecraft',font=font,fill=(157,179,182))
 for i,name in enumerate(ids):
  x=24+(i%6)*172;y=94+(i//6)*181;kind='block' if name in manifest['world_blocks'] else 'item';out.paste(image_for(kind+'/'+name),(x,y))
  label=lang.get(kind+'.paintableblocks.'+name,name)
  for line,part in enumerate(textwrap.wrap(label,width=22)):d.text((x+82,y+143+line*17),part,font=font,fill=(225,221,208),anchor='mt')
 d.text((24,H-26),'29 variantes · superficies exteriores sin caras duplicadas · colisión compartida con los modelos de mundo',font=font,fill=(157,179,182))
 path=ROOT/'docs/technical/visual-assets-preview.png';out.save(path,optimize=True);print(path)

if __name__=='__main__':main()
