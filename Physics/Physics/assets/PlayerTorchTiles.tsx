<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.10.2" name="PlayerTorchTiles" tilewidth="300" tileheight="600" tilecount="2" columns="0">
 <grid orientation="orthogonal" width="1" height="1"/>
 <tile id="2" type="torch">
  <properties>
   <property name="name" value="torch"/>
  </properties>
  <image width="216" height="437" source="platform/torch.png"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="26" y="15" width="98" height="271"/>
  </objectgroup>
 </tile>
 <tile id="3" type="player">
  <properties>
   <property name="name" value="player"/>
  </properties>
  <image width="300" height="600" source="tiled_images/player_resize.png"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="35" y="53" width="256" height="547"/>
  </objectgroup>
 </tile>
</tileset>
