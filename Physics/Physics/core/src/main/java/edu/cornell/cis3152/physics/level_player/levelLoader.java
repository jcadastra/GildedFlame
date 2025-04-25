//package edu.cornell.cis3152.physics.level_player;
//
//import com.badlogic.gdx.graphics.Texture;
//import com.badlogic.gdx.graphics.Texture.TextureWrap;
//import com.badlogic.gdx.math.Interpolation;
//import com.badlogic.gdx.math.Vector2;
//import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
//import com.badlogic.gdx.utils.JsonValue;
//import edu.cornell.cis3152.physics.ParticleEngine;
//import edu.cornell.cis3152.physics.level_player.enemies.Moth;
//import edu.cornell.cis3152.physics.level_player.enemies.Totem;
//import edu.cornell.cis3152.physics.level_player.enviromentals.Button;
//import edu.cornell.cis3152.physics.level_player.enviromentals.Fire;
//import edu.cornell.cis3152.physics.level_player.enviromentals.FloatingLight;
//import edu.cornell.cis3152.physics.level_player.enviromentals.GameObject;
//import edu.cornell.cis3152.physics.level_player.enviromentals.Lighting;
//import edu.cornell.cis3152.physics.level_player.enviromentals.ObstacleMaterial;
//import edu.cornell.cis3152.physics.level_player.enviromentals.Rope;
//import edu.cornell.cis3152.physics.level_player.enviromentals.Rune;
//import edu.cornell.cis3152.physics.level_player.enviromentals.Surface;
//import edu.cornell.cis3152.physics.level_player.player.Avatar;
//import edu.cornell.cis3152.physics.level_player.player.Torch;
//import edu.cornell.cis3152.physics.level_player.utils.Event;
//import edu.cornell.cis3152.physics.level_player.utils.EventAction;
//import edu.cornell.gdiac.physics2.ObstacleSprite;
//import edu.cornell.gdiac.physics2.WheelObstacle;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.function.Function;
//
//public class levelLoader {
//    public levelLoader() {}
//
//
//    private List<float[]> extractSurfaces(int[] data, int cols, int rows) {
//        List<float[]> surfaces = new ArrayList<>();
//        for (int y = 0; y < rows; y++) {
//            for (int x = 0; x < cols; x++) {
//                int yy = rows - y;
//                int index = y * cols + x;
//                int tileId = data[index];
//                if (tileId != 0) { // Skip empty tiles
//                    float[] polygon = new float[]{
//                        x, yy,                         // top-left
//                        x, yy - 1,                // bottom-left
//                        x + 1, yy - 1,        // bottom-right
//                        x + 1, yy
//                    };
//                    surfaces.add(polygon);
//                }
//            }
//        }
//        return surfaces;
//    }
//
//    public void loadLevel(String levelName, String levelInfoName, float phyiscsUnits) {
//        this.levelName = levelName;
//
////        weatherMachine = new MarthasWeatherMachine(phyiscsUnits,10);
//        JsonValue levelData = directory.getEntry(levelName,JsonValue.class);
//        JsonValue levelInfo = directory.getEntry(levelInfoName, JsonValue.class);
//
//        // Create ground pieces
//        Texture texture;
//        enemies = new ArrayList<>();
//
//        JsonValue layers = levelData.get("layers");
//        for (JsonValue layer : layers) {
//            String layerType = layer.getString("type");
//            String layerName = layer.getString("name");
//
//            if (layerType.equals("tilelayer")) {
//                int width = layer.getInt("width");
//                int height = layer.getInt("height");
//                System.out.println(width + "x" + height);
//                JsonValue data = layer.get("data");
//
//                JsonValue settings = levelInfo.get("walls");
//                int[] tileData = new int[width * height];
//                for (int i = 0; i < data.size; i++) {
//                    tileData[i] = data.getInt(i);
//                }
//
//                List<float[]> polygons = extractSurfaces(tileData, width, height);
//
//                for (float[] points : polygons) {
////                    System.out.println(Arrays.toString(points));
//                    // Determine bounds of the polygon in tile coordinates
//                    int minX = (int) points[0];
//                    int maxY = (int) points[1];
//                    int maxX = (int) points[4];
//                    int minY = (int) points[5];
//                    int x = minX;
//                    int y = height - maxY;
//                    int index = y * width + x;
//                    int tileId = tileData[index];
//                    String name;
//                    if (tileId == 2 || tileId == 3 || tileId == 4) {
//                        name = "platform";
//                    } else if (tileId == 8 || tileId == 9 || tileId == 10) {
//                        name = "platform";
//                    } else {
//                        name = "wall";
//                    }
//
//                    float tileunits = units/300f;
//                    Surface tile = new Surface(points, units, name, settings);
//                    Texture textur = directory.getEntry("stoneTile"+(tileId), Texture.class);
//                    textur.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
//
//                    tile.setTexture(textur);
//
//
//                    addSprite(tile);
//                }
//            } else if (layerType.equals("objectgroup")) {
//                Map<String, JsonValue> ropeAnchors = new HashMap<>();
//                for (JsonValue object : layer.get("objects")) {
//                    String objName = object.getString("name", "unnamed");
//                    float x = object.getFloat("x") / levelData.getInt("tilewidth");
//                    float y = (18 * 300 - object.getFloat("y")) / levelData.getInt("tileheight");
//                    float[] pos = new float[]{x, y};
//
//                    if (objName.contains("player")) {
//                        Texture playerTexture = directory.getEntry("platform-player",
//                            Texture.class);
//                        avatar = new Avatar(directory, units, levelInfo.get("traci"));
//                        avatar.setTexture(playerTexture);
//                        avatar.getObstacle().setPosition(pos[0], pos[1]);
//                        //System.out.println("position" + pos[0] + " " + pos[1]);
//                        addSprite(avatar);
//                        avatar.createSensor();
//                        if (lightController != null) {
//                            lightController.attachPlayerLight(avatar);
//                        }
//                    } else if (objName.contains("torch")) {
//                        Lighting l = new Lighting(units, levelInfo.get("light"));
//                        addSprite(l);
//                        l.createSensor();
//                        torchFire = new Fire(units, new Vector2(10, 10));
//                        addSprite(torchFire);
//                        lightController = new LightController(torchFire.getObstacle().getPosition(),
//                            world, camera, bounds, units, cameraZoomLevel);
//                        lightController.attachTorchLight(torchFire);
//                        lightController.resetCamera(camera.position.x, camera.position.y);
//                        lightController.attachPlayerLight(avatar);
//
//                        particleEngine = new ParticleEngine(torchFire, units);
//                        particleEngine.newFires(fireController);
//
//                        texture = directory.getEntry("platform-torch", Texture.class);
//                        torch = new Torch(units, constants.get("torch"));
//                        torch.getObstacle().setPosition(pos[0], pos[1]);
//                        torch.setTexture(texture);
//                        torch.setMaterial(new ObstacleMaterial("torch", null));
//                        addSprite(torch);
//                        l.getObstacle().setPosition(torch.getObstacle().getPosition());
//                        torchFire.getObstacle().setPosition(new Vector2(
//                            torch.getObstacle().getPosition().cpy().add(0, torch.getHeight() / 5)));
//                        fireController.forceAddTorchFire(torchFire, torch, new Vector2(
//                            torch.getObstacle().getPosition().cpy().add(0, torch.getHeight() / 4)));
//                        activeLightJoint = world.createJoint(torch.attachObj(l));
//                        activeFireJoint = world.createJoint(torch.attachObj(torchFire));
//
//                        //floating lights
//                        floatingLights = new FloatingLight[3];
//                        Vector2 goalPos = goalDoor.getObstacle().getPosition();
//                        FloatingLight goalLight = new FloatingLight(units, goalPos, 1, goalPos);
//                        floatingLights[0] = goalLight;
//                        Vector2 centerPos = new Vector2(bounds.width / 2, bounds.height / 2);
//                        FloatingLight centerLight = new FloatingLight(units, centerPos, 1,
//                            centerPos);
//                        floatingLights[1] = centerLight;
//                        Vector2 edgePos1 = new Vector2(bounds.width - 5, 5);
//                        Vector2 edgePos2 = new Vector2(5, 5);
//                        FloatingLight edgeLight = new FloatingLight(units, edgePos1, 1, edgePos2);
//                        floatingLights[2] = edgeLight;
//                        for (int i = 0; i < floatingLights.length; i++) {
//                            addSprite(floatingLights[i]);
//                        }
//                        for (FloatingLight floatingLight : floatingLights) {
//                            lightController.attachAmbientLight(floatingLight);
//                        }
//                    } else if (objName.contains("moth")) {
//                        texture = directory.getEntry("platform-moth01", Texture.class);
//                        Vector2 position = new Vector2(pos[0], pos[1]);
//                        Moth moth = new Moth(1, units,
//                            levelInfo.get("enemies").get("moths").get("instances").get(0),
//                            directory, position);
//                        moth.setTexture(texture);
//                        addSprite(moth);
//                        moth.createSensor();
//                        moth.setTorchFire(this.torchFire);
//                        enemies.add(moth);
//                    } else if (objName.contains("totem")) {
//                        texture = directory.getEntry("platform-totem01", Texture.class);
//                        Vector2 position = new Vector2(pos[0], pos[1]);
//                        Totem totem = new Totem(1, units,
//                            levelInfo.get("enemies").get("totems").get("instances").get(0),
//                            directory, position);
//                        totem.setTexture(texture);
//                        addSprite(totem);
//                        totem.createSensor();
//                        enemies.add(totem);
//                    } else if (objName.contains("goaldoor")) {
//                        texture = directory.getEntry("shared-goal", Texture.class);
//                        System.out.println("goal door texture: " + texture);
//                        float size = 1f;
//
//                        GameObject goalDoor = new GameObject(x, y, size * 1.47f, size, units, true);
//                        goalDoor.getObstacle().setSensor(true);
//                        goalDoor.getObstacle().setBodyType(BodyType.StaticBody);
//                        goalDoor.getObstacle().setName("goalDoor");
//                        goalDoor.setTexture(texture);
//                        goalDoor.getObstacle().setPhysicsUnits(units);
//                        this.goalDoor = goalDoor;
//                        addSprite(goalDoor);
//                    } else if (objName.contains("platform")) {
//                        GameObject platform = new GameObject(x,y,width,height, units, true);
//                        platform.getObstacle().setBodyType(BodyType.KinematicBody);
//                        platform.getObstacle().setName(objName);
////                        goalDoor.setTexture();
//                        platform.getObstacle().setPhysicsUnits(units);
//                        platform.setMaterial(new ObstacleMaterial("platform", null));
//                        addSprite(platform);
//                    } else if (objName.contains("burnable")) {
//                        GameObject box = new GameObject(x,y,width,height, units, true);
//                        box.getObstacle().setBodyType(BodyType.DynamicBody);
//                        box.getObstacle().setName(objName);
////                        goalDoor.setTexture();
//                        box.getObstacle().setPhysicsUnits(units);
//                        if (!objName.contains("non")) {
//                            box.setMaterial(new ObstacleMaterial("wood", null));
//                        } else {
//                            box.setMaterial(new ObstacleMaterial("stone", null));
//                        }
//                        addSprite(box);
//                    } else if (objName.contains("rune")) {
//                        boolean hasMoveEvent = false;
//                        boolean hasRotateEvent = false;
//                        Vector2 platformStartPos = null;
//                        Vector2 platformEndPos = null;
//                        float startDegree = Integer.MAX_VALUE;
//                        float endDegree = 0f;
//                        float duration = 1f;
//                        String targetName = "";
//                        float[] thresholds = null;
//
//                        JsonValue runeProperties = object.get("properties");
//                        for (JsonValue prop : runeProperties) {
//                            String propName = prop.getString("name");
//                            String value = prop.getString("value");
//                            switch (propName) {
//
//                                case "rotateEvent":
//                                    hasRotateEvent = Boolean.parseBoolean(value);
//                                    break;
//                                case "moveEvent":
//                                    hasMoveEvent = Boolean.parseBoolean(value);
//                                    break;
//                                case "startPos":
//                                    platformStartPos = new Vector2(Float.parseFloat(value.split(",")[0]), Float.parseFloat(value.split(",")[1]));
//                                    break;
//                                case "endPos":
//                                    platformEndPos = new Vector2(Float.parseFloat(value.split(",")[0]), Float.parseFloat(value.split(",")[1]));
//                                    break;
//                                case "startDegree":
//                                    startDegree = Float.parseFloat(value);
//                                    break;
//                                case "endDegree":
//                                    endDegree = Float.parseFloat(value);
//                                    break;
//                                case "time":
//                                    duration = Float.parseFloat(value);
//                                    break;
//                                case "platformName":
//                                    targetName = value;
//                                    break;
//                                case "thresholds":
//                                    String[] tokens = value.split(",");
//                                    thresholds = new float[tokens.length];
//                                    for (int i = 0; i < tokens.length; i++) {
//                                        thresholds[i] = Float.parseFloat(tokens[i].trim());
//                                    }
//                                    break;
//                            }
//                        }
//
//                        String finalTargetName = targetName;
//                        ObstacleSprite target = sprites.stream().filter(sprite -> sprite.getName().equals(finalTargetName)).findFirst().orElse(null);
//
//                        Rune rune = new Rune(x,y, units, thresholds);
//                        addSprite(rune);
//                        runeSet.add(rune);
//
//                        if (hasMoveEvent) {
//                            if (platformStartPos == null) {
//                                platformStartPos = new Vector2(target.getObstacle().getPosition());
//                            }
//                            EventAction<Vector2> moveAction = new EventAction<>(target, "move",
//                                platformStartPos, platformEndPos);
//                            rune.registerEventAction(moveAction);
//                        }
//
//                        if (hasRotateEvent) {
//                            if (startDegree == Integer.MAX_VALUE) {
//                                startDegree = target.getObstacle().getAngle();
//                            } else {
//                                startDegree = (float) Math.toRadians(startDegree);
//                            }
//                            float toRad = (float) Math.toRadians(endDegree);
//
//                            EventAction<Float> rotateAction = new EventAction<>(target, "rotate",
//                                startDegree, toRad);
//                            rune.registerEventAction(rotateAction);
//                        }
//                    } else if (objName.contains("button")) {
//                        float rotationRad = 0f;
//                        boolean latch = false;
//                        boolean doubleSided = false;
//                        boolean hasMoveEvent = false;
//                        boolean hasRotateEvent = false;
//                        Vector2 platformStartPos = null;
//                        Vector2 platformEndPos = null;
//                        float startDegree = Integer.MAX_VALUE;
//                        float endDegree = 0f;
//                        float duration = 1f;
//                        String targetName = "";
//                        String interpolation = "";
//
//                        JsonValue properties = object.get("properties");
//                        for (JsonValue prop : properties) {
//                            String propName = prop.getString("name");
//                            String value = prop.getString("value");
//                            switch (propName) {
//                                case "latch":
//                                    latch = Boolean.parseBoolean(value);
//                                    break;
//                                case "doublesided":
//                                    doubleSided = Boolean.parseBoolean(value);
//                                    break;
//                                case "rotationRadiance":
//                                    rotationRad = Float.parseFloat(value);
//                                    break;
//                                case "rotateEvent":
//                                    hasRotateEvent = Boolean.parseBoolean(value);
//                                    break;
//                                case "moveEvent":
//                                    hasMoveEvent = Boolean.parseBoolean(value);
//                                    break;
//                                case "startPos":
//                                    platformStartPos = new Vector2(Float.parseFloat(value.split(",")[0]), Float.parseFloat(value.split(",")[1]));
//                                    break;
//                                case "endPos":
//                                    platformEndPos = new Vector2(Float.parseFloat(value.split(",")[0]), Float.parseFloat(value.split(",")[1]));
//                                    break;
//                                case "startDegree":
//                                    startDegree = Float.parseFloat(value);
//                                    break;
//                                case "endDegree":
//                                    endDegree = Float.parseFloat(value);
//                                    break;
//                                case "time":
//                                    duration = Float.parseFloat(value);
//                                    break;
//                                case "platformName":
//                                    targetName = value;
//                                    break;
//                                case "interpolation":
//                                    interpolation = value.toLowerCase();
//                                    break;
//                            }
//                        }
//
//                        String finalTargetName = targetName;
//                        ObstacleSprite target = sprites.stream().filter(sprite -> sprite.getName().equals(finalTargetName)).findFirst().orElse(null);
//
//                        Vector2 buttonPosition = new Vector2(x,y);
//                        Button button = new Button(buttonPosition,(float) Math.toRadians(rotationRad), doubleSided, latch, units);
//                        addSpriteGroup(button);
//
//                        Function<Float, Float> movementFunc = Interpolation.smoother::apply;
//                        switch (interpolation) {
//                            case "linear":
//                                movementFunc = Interpolation.linear::apply;
//                                break;
//                            case "swing":
//                                movementFunc = Interpolation.swing::apply;
//                                break;
//                        }
//
//                        if (hasMoveEvent) {
//                            if (platformStartPos == null) {
//                                platformStartPos = new Vector2(target.getObstacle().getPosition());
//                            }
//                            EventAction<Vector2> moveAction = new EventAction<>(target, "move",
//                                platformStartPos, platformEndPos, duration, movementFunc);
//
//                            Event<Integer, Vector2> moveEvent = new Event<>(button,button::getState,
//                                state -> state == 1, moveAction);
//                            eventHandler.registerEvent(moveEvent);
//                        }
//
//                        if (hasRotateEvent) {
//                            if (startDegree == Integer.MAX_VALUE) {
//                                startDegree = target.getObstacle().getAngle();
//                            } else {
//                                startDegree = (float) Math.toRadians(startDegree);
//                            }
//                            float toRad = (float) Math.toRadians(endDegree);
//
//                            EventAction<Float> rotateAction = new EventAction<>(target, "rotate",
//                                startDegree, toRad, duration, movementFunc);
//
//                            Event<Integer, Float> rotateEvent = new Event<>(button,
//                                button::getState,
//                                state -> state == 1, rotateAction);
//                            eventHandler.registerEvent(rotateEvent);
//                        }
//                    } else {
//                        if (objName.matches("\\d+")) {
//                            ropeAnchors.put(objName, object);
//                        } else {
//                            Texture temp = directory.getEntry(objName, Texture.class);
//                            if (temp != null) {
//
//                                float width = object.getFloat("width") / levelData.getInt("tilewidth");
//                                float height = object.getFloat("height") / levelData.getInt("tileheight");
//
//                                GameObject decoration = new GameObject(x,y,width,height, units, true);
//                                decoration.getObstacle().setSensor(true);  // set as sensor
//                                decoration.getObstacle().setBodyType(BodyType.StaticBody);
//                                decoration.setTexture(temp);
//                                decoration.getObstacle().setName(objName);
//
//                                addSprite(decoration);
//                            } else {
//                                System.out.println("Unknown object: " + objName);
//                            }
//                        }
//                    }
//                }
//
//                int rcnt = ropeAnchors.size()/2;
//                for (int i = 0; i < rcnt; i++) {
//                    String startName = String.valueOf(2 * i + 1);
//                    String endName = String.valueOf(2 * i + 2);
//
//                    JsonValue start = ropeAnchors.get(startName);
//                    JsonValue end = ropeAnchors.get(endName);
//
//                    if (start != null && end != null) {
//                        float x1 = start.getFloat("x") / levelData.getInt("tilewidth");
//                        float y1 = (18 * 300 - start.getFloat("y")) / levelData.getInt("tileheight");
//                        float x2 = end.getFloat("x") / levelData.getInt("tilewidth");
//                        float y2 = (18 * 300 - end.getFloat("y")) / levelData.getInt("tileheight");
//
//                        int depth = 10, piecelen = 10, thickness = 14;
//                        JsonValue props = end.get("properties");
//                        if (props != null) {
//                            for (JsonValue prop : props) {
//                                String pname = prop.getString("name");
//                                String val = prop.getString("value");
//                                switch (pname) {
//                                    case "depth":
//                                        depth = Integer.parseInt(val);
//                                        break;
//                                }
//                            }
//                        }
//                        texture = directory.getEntry( "platform-rope-end", Texture.class );
//                        Texture middle_texture = directory.getEntry( "platform-rope-mid", Texture.class );
//                        Rope rope = new Rope(new Vector2(x1, y1), new Vector2(x2, y2), depth, thickness, piecelen, units, levelInfo.get("ropes").get(0));
//                        rope.setTextures(texture, middle_texture);
//                        //temp = rope;
//                        addSpriteGroup(rope);
//                    }
//
//                }
//            }
//        }
//
//        torchArc = new ArrayList<>();
//        for (int i = 0; i < dotTorchArcCount / deltaTorchArc; i++) {
//            WheelObstacle temp = new WheelObstacle(-1,-1, 0.4f/32f * units);
//            temp.setBodyType(BodyType.StaticBody);
//            ObstacleSprite tracker = new ObstacleSprite(temp);
//            tracker.getObstacle().setPhysicsUnits(phyiscsUnits);
//            tracker.getObstacle().setName("trajectoryPoint");
//            tracker.getObstacle().setSensor(true);
//            tracker.getObstacle().setPhysicsUnits(units);
//            addSprite(tracker);
//            torchArc.add(tracker);
//        }
//    }
//}
