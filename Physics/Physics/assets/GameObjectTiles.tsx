<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.10.2" name="GameObjects" tilewidth="5000" tileheight="1250" tilecount="16" columns="0">
 <grid orientation="orthogonal" width="1" height="1"/>
 <tile id="0">
  <properties>
   <property name="dynamic1" type="bool" value="false"/>
   <property name="dynamic2" type="bool" value="false"/>
   <property name="end1Material" value="rope,iron"/>
   <property name="end2Material" value="rope,iron"/>
   <property name="middleMaterial" value="rope,iron"/>
   <property name="pin1" value=""/>
   <property name="pin2" value=""/>
  </properties>
  <image width="150" height="150" source="environment/Rope/chainEnd.png"/>
 </tile>
 <tile id="1">
  <properties>
   <property name="material" value="wood/stone/infinite"/>
   <property name="name" value="burnable"/>
  </properties>
  <image width="458" height="500" source="environment/barrel.png"/>
 </tile>
 <tile id="2">
  <properties>
   <property name="name" value="nonburnable"/>
  </properties>
  <image width="392" height="500" source="environment/statue.png"/>
 </tile>
 <tile id="3">
  <image width="300" height="300" source="environment/tablet.png"/>
 </tile>
 <tile id="4">
  <properties>
   <property name="name" value="goaldoor"/>
  </properties>
  <image width="346" height="235" source="environment/treasure.png"/>
 </tile>
 <tile id="5">
  <properties>
   <property name="name" value="infburnable"/>
  </properties>
  <image width="5000" height="1250" source="environment/platform.png"/>
 </tile>
 <tile id="14">
  <properties>
   <property name="doubleSided" type="bool" value="false"/>
   <property name="endDegree" type="float" value="0"/>
   <property name="endPos" value=""/>
   <property name="interpolation">smoother
linear
swing</property>
   <property name="latch" type="bool" value="false"/>
   <property name="moveEvent" type="bool" value="false"/>
   <property name="platformName" value=""/>
   <property name="rotateEvent" type="bool" value="false"/>
   <property name="startDegree" type="float" value="0"/>
   <property name="startPos" value=""/>
   <property name="time" type="float" value="0"/>
  </properties>
  <image width="211" height="120" source="environment/button.png"/>
 </tile>
 <tile id="15">
  <properties>
   <property name="dissipateTime" type="float" value="1"/>
   <property name="endDegree" type="float" value="0"/>
   <property name="endPos" value=""/>
   <property name="moveEvent" type="bool" value="false"/>
   <property name="platformName" value=""/>
   <property name="rotateEvent" type="bool" value="false"/>
   <property name="startDegree" type="float" value="0"/>
   <property name="startPos" value=""/>
   <property name="thresholds" value=""/>
   <property name="timeTo" type="float" value="1"/>
  </properties>
  <image width="400" height="520" source="environment/runeBase.png"/>
 </tile>
 <tile id="13" x="0" y="0" width="40" height="52">
  <properties>
   <property name="endDegree" type="float" value="0"/>
   <property name="endPos" value=""/>
   <property name="moveEvent" type="bool" value="false"/>
   <property name="platformName" value=""/>
   <property name="rotateEvent" type="bool" value="false"/>
   <property name="startDegree" type="float" value="0"/>
   <property name="startPos" value=""/>
   <property name="thresholds" value=""/>
  </properties>
  <image width="73" height="93" source="environment/rune.png"/>
 </tile>
 <tile id="16">
  <properties>
   <property name="rain" type="int" value="0"/>
  </properties>
  <image width="510" height="324" source="environment/rain.jpg"/>
 </tile>
 <tile id="17">
  <image width="300" height="300" source="environment/grate.png"/>
 </tile>
 <tile id="18">
  <properties>
   <property name="startOnFire" type="bool" value="false"/>
  </properties>
  <image width="323" height="242" source="environment/brazier.png"/>
 </tile>
 <tile id="23">
  <properties>
   <property name="flipHorizontally" type="bool" value="false"/>
   <property name="name" value="qb1"/>
  </properties>
  <image width="230" height="158" source="environment/quarterBlocks_0513/qb1.png"/>
 </tile>
 <tile id="24">
  <properties>
   <property name="flipHorizontally" type="bool" value="false"/>
   <property name="name" value="qb2"/>
  </properties>
  <image width="230" height="158" source="environment/quarterBlocks_0513/qb2.png"/>
 </tile>
 <tile id="25">
  <properties>
   <property name="flipHorizontally" type="bool" value="false"/>
   <property name="name" value="qb3"/>
  </properties>
  <image width="230" height="158" source="environment/quarterBlocks_0513/qb3.png"/>
 </tile>
 <tile id="26">
  <properties>
   <property name="flipHorizontally" type="bool" value="false"/>
   <property name="name" value="qb4"/>
  </properties>
  <image width="230" height="158" source="environment/quarterBlocks_0513/qb4.png"/>
 </tile>
</tileset>
