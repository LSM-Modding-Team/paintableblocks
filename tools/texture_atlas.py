"""Resolve model sprites against Minecraft 26.2 atlas sources, rather than PNG existence alone."""
from pathlib import Path
import hashlib,json,zipfile

CLIENT_SHA1='2dc72797acbc1b63fc16a11c4ac393605f453754'
# Minimal directory rules from the official 26.2 atlas JSON. --minecraft-jar reads the full originals.
DEFAULTS={'blocks':[{'type':'minecraft:directory','source':'block','prefix':'block/'}],
          'items':[{'type':'minecraft:directory','source':'item','prefix':'item/'}]}

class TextureAtlases:
    def __init__(self,assets,minecraft_jar=None):
        self.assets=Path(assets);self.sources={name:list(entries) for name,entries in DEFAULTS.items()};self.official=False
        if minecraft_jar is not None:
            path=Path(minecraft_jar)
            assert hashlib.sha1(path.read_bytes()).hexdigest()==CLIENT_SHA1,'Not the inspected official Minecraft 26.2 client'
            with zipfile.ZipFile(path) as archive:
                self.sources={name:json.loads(archive.read(f'assets/minecraft/atlases/{name}.json'))['sources'] for name in DEFAULTS}
            self.official=True
        for name in self.sources:
            local=self.assets/'minecraft/atlases'/(name+'.json')
            if local.exists():self.sources[name].extend(json.loads(local.read_text())['sources'])

    def resolve(self,sprite,atlas=None):
        """Return (atlas, PNG) only when the atlas creates this exact namespaced sprite ID."""
        namespace,name=sprite.split(':',1)
        for atlas_name,sources in self.sources.items():
            if atlas is not None and atlas_name!=atlas:continue
            for entry in sources:
                kind=entry['type'].split(':')[-1]
                if kind=='directory':
                    prefix=entry.get('prefix','')
                    if not name.startswith(prefix):continue
                    source=entry['source'].strip('/');relative=name[len(prefix):]
                    path=self.assets/namespace/'textures'/source/(relative+'.png')
                    if path.is_file():return atlas_name,path
                elif kind=='single':
                    resource=entry['resource'];identifier=entry.get('sprite',resource)
                    if identifier!=sprite:continue
                    ns,resource_path=resource.split(':',1);path=self.assets/ns/'textures'/(resource_path+'.png')
                    if path.is_file():return atlas_name,path
        return None
