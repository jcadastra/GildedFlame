package edu.cornell.cis3152.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import edu.cornell.gdiac.util.ScreenListener;
import edu.cornell.gdiac.util.XBoxController;

public class LevelSelectScene implements Screen {
    private static final int DOORS_VISIBLE = 5;
    private static final int TOTAL_LEVELS = 15;

    private Stage stage;
    private Skin skin;
    private Texture bgTexture;

    private Array<DoorEntry> doorEntries;

    private float doorWidth, doorHeight, spacing;
    private int scrollIndex = 0;
    private int currentIndex = 0;
    private int selectedLevel = 1;

    private InputController inputController = InputController.getInstance();
    private ScreenListener listener;
    private boolean prevButtonA = true;
    private float joystickCooldown = 0f;
    private Texture chapTitleTexture, chap1TitleTexture, chap3TitleTexture;
    private Image chapLabel;
    private ParticleEngine particleEngine;
    private boolean doorDisplayDirty = true;


    private class DoorEntry {
        public Image door;
        public float targetX;
        public TextureRegionDrawable defaultDrawable;
        public TextureRegionDrawable hoverDrawable;
        public int levelIndex;

        public DoorEntry(Image door, TextureRegionDrawable defaultDrawable, TextureRegionDrawable hoverDrawable, int levelIndex) {
            this.door = door;
            this.defaultDrawable = defaultDrawable;
            this.hoverDrawable = hoverDrawable;
            this.levelIndex = levelIndex;
            this.targetX = door.getX();
        }
    }

    public LevelSelectScene() {
        stage = new Stage(new ScreenViewport());
        skin = new Skin();
        bgTexture = new Texture(Gdx.files.internal("ui/plain_back.png"));
        Image bgImage = new Image(bgTexture);
        bgImage.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.addActor(bgImage);
        createBasicUI();
    }
    private Texture loadDoorTexture(String statePrefix, int levelNumber, boolean hover) {
        String suffix = hover ? "Hover" : "";
        int cappedLevel = ((levelNumber - 1) % 5) + 1;
        String path = String.format("ui/doors/%sDoor%s%d.png", statePrefix, suffix, cappedLevel);
        return new Texture(Gdx.files.internal(path));
    }

    private void createBasicUI() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        doorWidth = screenWidth * 0.2f;
        doorHeight = screenHeight * 0.35f;
        spacing = screenWidth * 0.001f;

        Texture chamberLabelTexture = new Texture(Gdx.files.internal("ui/chamber_sel.png"));
        Image chamberLabel = new Image(chamberLabelTexture);
        chamberLabel.setSize(screenWidth * 0.5f, screenHeight * 0.1f);
        chamberLabel.setPosition(screenWidth * 0.25f, screenHeight * 0.85f);
        stage.addActor(chamberLabel);

        chapTitleTexture = new Texture(Gdx.files.internal("ui/chap1_title.png"));
        chap1TitleTexture = new Texture(Gdx.files.internal("ui/chap_title.png"));
        chap3TitleTexture = new Texture(Gdx.files.internal("ui/chap3_title.png"));
        chapLabel = new Image(chapTitleTexture);
        chapLabel.setSize(screenWidth * 0.2f, screenHeight * 0.05f);
        float verticalSpacing = screenHeight * 0.02f;
        chapLabel.setPosition((screenWidth - screenWidth * 0.2f) / 2f,
            chamberLabel.getY() - screenHeight * 0.05f - verticalSpacing);
        stage.addActor(chapLabel);


        doorEntries = new Array<>(TOTAL_LEVELS);
        SavedDataHandler handler = new SavedDataHandler();
        boolean foundFirstIncomplete = false;

        for (int i = 0; i < TOTAL_LEVELS; i++) {
            int levelNumber = i + 1;
            String key = "level" + levelNumber;
            //String completed = handler.getDataVal(key);

            Texture baseTex, hoverTex;
            String completed = handler.getDataVal("level" + levelNumber);

            if ("true".equals(completed)) {
                baseTex = loadDoorTexture("complete", levelNumber, false);
                hoverTex = loadDoorTexture("complete", levelNumber, true);
            } else if (!foundFirstIncomplete) {
                baseTex = loadDoorTexture("incomplete", levelNumber, false);
                hoverTex = loadDoorTexture("incomplete", levelNumber, true);
                foundFirstIncomplete = true;
            } else {
                baseTex = loadDoorTexture("locked", levelNumber, false);
                hoverTex = loadDoorTexture("locked", levelNumber, true);
            }

            Image door = new Image(baseTex);
            door.setSize(doorWidth, doorHeight);

            final DoorEntry entry = new DoorEntry(
                door,
                new TextureRegionDrawable(new TextureRegion(baseTex)),
                new TextureRegionDrawable(new TextureRegion(hoverTex)),
                levelNumber
            );

            door.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        selectedLevel = entry.levelIndex;
                        if (listener != null) {
                            listener.exitScreen(LevelSelectScene.this, 1);
                        }

                    return true;
                }

                @Override
                public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                        ((Image)event.getListenerActor()).setDrawable(entry.hoverDrawable);

                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                    ((Image)event.getListenerActor()).setDrawable(entry.defaultDrawable);
                }
            });

            doorEntries.add(entry);
            stage.addActor(door);
        }


        addArrowButtons();
        addBackSetting();
        updateDoorDisplay();
    }

    private void addArrowButtons() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        // Left arrow textures and image
        Texture l_arrow_text = new Texture(Gdx.files.internal("ui/l_arrow.png"));
        Texture l_arrow_hov_text = new Texture(Gdx.files.internal("ui/l_arrow_click.png"));
        Image l_arrow = new Image(l_arrow_text);
        l_arrow.setSize(screenWidth * 0.05f, screenHeight * 0.05f);
        l_arrow.setPosition(screenWidth * 0.01f, screenHeight * 0.8f);
        stage.addActor(l_arrow);

        // Right arrow textures and image
        Texture r_arrow_text = new Texture(Gdx.files.internal("ui/r_arrow.png"));
        Texture r_arrow_hov_text = new Texture(Gdx.files.internal("ui/r_arrow_click.png"));
        Image r_arrow = new Image(r_arrow_text);
        r_arrow.setSize(screenWidth * 0.05f, screenHeight * 0.05f);
        r_arrow.setPosition(screenWidth * 0.94f, screenHeight * 0.8f);
        stage.addActor(r_arrow);

        // Create drawables for hover effects
        final TextureRegionDrawable l_arrow_drawable = new TextureRegionDrawable(new TextureRegion(l_arrow_text));
        final TextureRegionDrawable l_arrow_hover_drawable = new TextureRegionDrawable(new TextureRegion(l_arrow_hov_text));
        final TextureRegionDrawable r_arrow_drawable = new TextureRegionDrawable(new TextureRegion(r_arrow_text));
        final TextureRegionDrawable r_arrow_hover_drawable = new TextureRegionDrawable(new TextureRegion(r_arrow_hov_text));

        // Left arrow listener
        l_arrow.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (scrollIndex >= DOORS_VISIBLE) { // Change: scroll back by 5
                    scrollIndex -= DOORS_VISIBLE;
                    int maxVisible = Math.min(DOORS_VISIBLE, doorEntries.size - scrollIndex);
                    currentIndex = Math.min(currentIndex, maxVisible - 1);
                    doorDisplayDirty = true;
                    //updateDoorDisplay();

                    // Title update
                    int firstVisible = scrollIndex;
                    int lastVisible = scrollIndex + DOORS_VISIBLE - 1;
                    if (scrollIndex >= 10) {
                        chapLabel.setDrawable(new TextureRegionDrawable(new TextureRegion(chap3TitleTexture)));
                    } else if (scrollIndex >= 5) {
                        chapLabel.setDrawable(new TextureRegionDrawable(new TextureRegion(chap1TitleTexture)));
                    } else {
                        chapLabel.setDrawable(new TextureRegionDrawable(new TextureRegion(chapTitleTexture)));
                    }
                }
                return true;
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                ((Image) event.getListenerActor()).setDrawable(l_arrow_hover_drawable);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                ((Image) event.getListenerActor()).setDrawable(l_arrow_drawable);
            }
        });

        // Right arrow listener
        r_arrow.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (scrollIndex + DOORS_VISIBLE < doorEntries.size) { // Change: scroll forward by 5
                    scrollIndex += DOORS_VISIBLE;
                    int maxVisible = Math.min(DOORS_VISIBLE, doorEntries.size - scrollIndex);
                    currentIndex = Math.min(currentIndex, maxVisible - 1);
                    doorDisplayDirty = true;

                    //updateDoorDisplay();

                    // Title update
                    int firstVisible = scrollIndex;
                    int lastVisible = scrollIndex + DOORS_VISIBLE - 1;
                    if (scrollIndex >= 10) {
                        chapLabel.setDrawable(new TextureRegionDrawable(new TextureRegion(chap3TitleTexture)));
                    } else if (scrollIndex >= 5) {
                        chapLabel.setDrawable(new TextureRegionDrawable(new TextureRegion(chap1TitleTexture)));
                    } else {
                        chapLabel.setDrawable(new TextureRegionDrawable(new TextureRegion(chapTitleTexture)));
                    }
                }
                return true;
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                ((Image) event.getListenerActor()).setDrawable(r_arrow_hover_drawable);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                ((Image) event.getListenerActor()).setDrawable(r_arrow_drawable);
            }
        });
    }
    private void addBackSetting() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        // Left arrow textures and image
        Texture backButtText = new Texture(Gdx.files.internal("ui/back_butt.png"));
        Texture settButtText = new Texture(Gdx.files.internal("ui/setting.png"));
        Image backButt = new Image(backButtText);
        backButt.setSize(screenWidth * 0.05f, screenHeight * 0.05f);
        backButt.setPosition(screenWidth * 0.01f, screenHeight * 0.9f);
        stage.addActor(backButt);

        // Right arrow textures and image
        Image settButt = new Image(settButtText);
        settButt.setSize(screenWidth * 0.05f, screenHeight * 0.05f);
        settButt.setPosition(screenWidth * 0.94f, screenHeight * 0.9f);
        stage.addActor(settButt);

        // Create drawables for hover effects

        // Left arrow listener
        backButt.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (listener != null) {
                    listener.exitScreen(LevelSelectScene.this, 0);
                }
                return true;
            }
        });
    }


    private void updateDoorDisplay() {
        float screenWidth = Gdx.graphics.getWidth();
        float yPos = Gdx.graphics.getHeight() * 0.185f;

        int doorsToShow = Math.min(DOORS_VISIBLE, doorEntries.size - scrollIndex);
        float totalWidth = doorsToShow * doorWidth + (doorsToShow - 1) * spacing;
        float startX = (screenWidth - totalWidth) / 2f;

        for (int i = 0; i < doorEntries.size; i++) {
            DoorEntry entry = doorEntries.get(i);

            if (i >= scrollIndex && i < scrollIndex + doorsToShow) {
                int visibleIndex = i - scrollIndex;
                entry.targetX = startX + visibleIndex * (doorWidth + spacing);
                entry.door.setY(yPos);
                entry.door.setVisible(true);
            } else {
                // Move it far offscreen to force an animated entrance later
                entry.door.setX(-1000);
                entry.door.setVisible(false);
            }
        }
    }

    private void updateDoorAnimation(float delta) {
        float lerpSpeed = 10f;
        for (int i = 0; i < doorEntries.size; i++) {
            DoorEntry entry = doorEntries.get(i);
            float currentX = entry.door.getX();
            float newX = currentX + (entry.targetX - currentX) * lerpSpeed * delta;
            entry.door.setX(newX);
            // Remove visibility handling here
        }
    }
    private void controllerSelect() {
        if (inputController.isUsingController()) {
            XBoxController xbox = inputController.xbox;
            boolean start = xbox.getA();
            DoorEntry currentEntry = doorEntries.get(currentIndex);
            currentEntry.door.setDrawable(currentEntry.hoverDrawable);
            if (start && !prevButtonA) {
                int selectionIndex = scrollIndex + currentIndex;
                if (selectionIndex >= 0 && selectionIndex < doorEntries.size) {

                    InputEvent downEvent = new InputEvent();
                    downEvent.setType(InputEvent.Type.touchDown);
                    downEvent.setStage(stage);
                    downEvent.setTarget(currentEntry.door);
                    downEvent.setButton(0);
                    currentEntry.door.fire(downEvent);
                }
            }
            prevButtonA = start;

            boolean moved = false;
            float horizontal = xbox.getLeftX();
            if (joystickCooldown <= 0) {
                if (horizontal < -0.4f && !moved) {
//                    particleEngine.dispose();
                    currentEntry.door.setDrawable(currentEntry.defaultDrawable);
                    currentIndex = Math.max(currentIndex - 1, 0);  // Prevent going negative
                    moved = true;
                    joystickCooldown = 0.25f;
                } else if (horizontal > 0.4f && !moved) {
                    currentEntry.door.setDrawable(currentEntry.defaultDrawable);
                    int maxVisible = Math.min(DOORS_VISIBLE, doorEntries.size - scrollIndex);
                    currentIndex = Math.min(currentIndex + 1, maxVisible - 1);  // Prevent going off right
                    moved = true;
                    joystickCooldown = 0.25f;
                }
                if (Math.abs(horizontal) <= 0.01f) {
                    moved = false;
                }
            }
            joystickCooldown -= Gdx.graphics.getDeltaTime();
        }
    }

    @Override
    public void render(float delta) {
        if (doorDisplayDirty) {
            updateDoorDisplay();
            doorDisplayDirty = false;
        }
        updateDoorAnimation(delta);
        Gdx.gl.glClearColor(0.9f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        controllerSelect();
        stage.act(delta);
        updateDoorAnimation(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public int getSelectedLevel() {
        return selectedLevel;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }
}
