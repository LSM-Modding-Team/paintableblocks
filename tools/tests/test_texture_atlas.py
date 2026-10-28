"""Regression tests for the missing-material bug. Python only; no game/build execution."""
from pathlib import Path
import json,sys,tempfile,unittest
sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
from texture_atlas import TextureAtlases

class TextureAtlasTests(unittest.TestCase):
    def setUp(self):self.temp=tempfile.TemporaryDirectory();self.assets=Path(self.temp.name)/'assets'
    def tearDown(self):self.temp.cleanup()
    def png(self,path):
        p=self.assets/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(b'fixture: atlas discovery only');return p
    def test_existing_material_file_is_not_an_automatic_sprite(self):
        self.png('paintableblocks/textures/material/iron.png')
        self.assertIsNone(TextureAtlases(self.assets).resolve('paintableblocks:material/iron'))
    def test_block_subdirectory_is_loaded_under_the_same_namespace(self):
        file=self.png('paintableblocks/textures/block/material/iron.png');atlases=TextureAtlases(self.assets)
        self.assertEqual(atlases.resolve('paintableblocks:block/material/iron'),('blocks',file))
        self.assertIsNone(atlases.resolve('minecraft:block/material/iron'))
    def test_a_png_cannot_be_found_using_an_unregistered_alias(self):
        self.png('paintableblocks/textures/block/material/iron.png')
        self.assertIsNone(TextureAtlases(self.assets).resolve('paintableblocks:material/iron'))
    def test_item_directory_is_not_the_block_atlas(self):
        file=self.png('paintableblocks/textures/item/brush.png');atlases=TextureAtlases(self.assets)
        self.assertEqual(atlases.resolve('paintableblocks:item/brush'),('items',file))
        self.assertIsNone(atlases.resolve('paintableblocks:item/brush','blocks'))
    def test_explicit_directory_source_maps_prefix_to_real_directory(self):
        file=self.png('paintableblocks/textures/shared/iron.png');p=self.assets/'minecraft/atlases/blocks.json';p.parent.mkdir(parents=True)
        p.write_text(json.dumps({'sources':[{'type':'minecraft:directory','source':'shared','prefix':'palette/'}]}))
        self.assertEqual(TextureAtlases(self.assets).resolve('paintableblocks:palette/iron'),('blocks',file))
        self.assertIsNone(TextureAtlases(self.assets).resolve('paintableblocks:shared/iron'))
    def test_single_source_alias_uses_the_resource_file(self):
        file=self.png('paintableblocks/textures/shared/iron.png');p=self.assets/'minecraft/atlases/blocks.json';p.parent.mkdir(parents=True)
        p.write_text(json.dumps({'sources':[{'type':'minecraft:single','resource':'paintableblocks:shared/iron','sprite':'paintableblocks:steel'}]}))
        self.assertEqual(TextureAtlases(self.assets).resolve('paintableblocks:steel'),('blocks',file))

if __name__=='__main__':unittest.main()
