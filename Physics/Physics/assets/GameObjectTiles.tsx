<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.10.2" name="GameObjects" tilewidth="934" tileheight="934" tilecount="8" columns="0">
 <grid orientation="orthogonal" width="1" height="1"/>
 <tile id="0">
  <image width="934" height="934" source="environment/Rope/ropeEnd.png"/>
 </tile>
 <tile id="1">
  <properties>
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
  <image width="200" height="114" source="environment/platform.png"/>
 </tile>
 <tile id="13">
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
</tileset>
