package edu.cornell.cis3152.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import edu.cornell.gdiac.util.ScreenListener;
import edu.cornell.gdiac.util.XBoxController;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;


public class LevelSelectScene implements Screen {
    private static final int DOORS_VISIBLE = 5;
    private static final int TOTAL_LEVELS = 15;
    private static final int MAX_BUTTONS = DOORS_VISIBLE+2;

    private Stage stage;
    private Skin skin;
    private Texture bgTexture;

    private Array<DoorEntry> doorEntries;

    private int chapterNumber = 0;

    private float doorWidth, doorHeight, spacing;
    private int scrollIndex = 0;
    private int currentIndex = 0;
    private int selectedLevel = 1;

    private InputController inputController = InputController.getInstance();
    private ScreenListener listener;
    private boolean prevButtonA = false;
    private float joystickCooldown = 0f;
    private Texture chapTitleTexture, chap1TitleTexture, chap3TitleTexture, chap4TitleTexture;
    private Image chapLabel;
    private ParticleEngine particleEngine;
    private boolean doorDisplayDirty = true;
    private BitmapFont unicaFont;
    private Array<Label> doorLabels = new Array<>();
    private ArrowEntry leftArrowEntry, rightArrowEntry;
    private SoundEngine soundEngine;


    private class Entry{
        public Image image;
        public TextureRegionDrawable defaultDrawable;
        public TextureRegionDrawable hoverDrawable;
    }

    private class DoorEntry extends Entry{
        public float targetX;
        public int levelIndex;

        public DoorEntry(Image door, TextureRegionDrawable defaultDrawable, TextureRegionDrawable hoverDrawable, int levelIndex) {
            this.image = door;
            this.defaultDrawable = defaultDrawable;
            this.hoverDrawable = hoverDrawable;
            this.levelIndex = levelIndex;
            this.targetX = door.getX();
        }
    }

    private class ArrowEntry extends Entry{
        public int direction;
        public ArrowEntry(Image arrow, TextureRegionDrawable defaultDrawable, TextureRegionDrawable hoverDrawable,int direction) {
            this.image = arrow;
            this.defaultDrawable = defaultDrawable;
            this.hoverDrawable = hoverDrawable;
            this.direction = direction;
        }
    }

    public LevelSelectScene(SoundEngine soundEngine) {
        this.soundEngine = soundEngine;
        stage = new Stage(new ScreenViewport());
        skin = new Skin();
        bgTexture = new Texture(Gdx.files.internal("ui/plain_back.png"));
        Image bgImage = new Image(bgTexture);
        bgImage.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.addActor(bgImage);
        createBasicUI();
    }
    private Texture loadDoorTexture(String statePrefix, boolean hover) {
        String suffix = hover ? "Hover" : "";
        String path = String.format("ui/doors/%sDoor%s.png", statePrefix, suffix);
        return new Texture(Gdx.files.internal(path));
    }

    private void createBasicUI() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        doorWidth = screenWidth * 0.18f;
        doorHeight = screenHeight * 0.35f;
        spacing = screenWidth * 0.001f;

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ui/Unica_One/UnicaOne-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = (int)(doorHeight * 0.2f);
        parameter.color = com.badlogic.gdx.graphics.Color.WHITE;

        unicaFont = generator.generateFont(parameter);
        generator.dispose();


        Texture chamberLabelTexture = new Texture(Gdx.files.internal("ui/chamber_sel.png"));
        Image chamberLabel = new Image(chamberLabelTexture);
        chamberLabel.setSize(screenWidth * 0.5f, screenHeight * 0.1f);
        chamberLabel.setPosition(screenWidth * 0.25f, screenHeight * 0.85f);
        stage.addActor(chamberLabel);

        chapTitleTexture = new Texture(Gdx.files.internal("ui/chap1_title.png"));
        chap1TitleTexture = new Texture(Gdx.files.internal("ui/chap_title.png"));
        chap3TitleTexture = new Texture(Gdx.files.internal("ui/chap3_title.png"));
        chap4TitleTexture = new Texture(Gdx.files.internal("ui/chap4_title.png"));
        chapLabel = new Image(chapTitleTexture);
        chapLabel.setSize(screenWidth * 0.2f, screenHeight * 0.05f);
        float verticalSpacing = screenHeight * 0.057f;
        chapLabel.setPosition((screenWidth - screenWidth * 0.2f) / 2f,
            chamberLabel.getY() - screenHeight * 0.05f - verticalSpacing);
        stage.addActor(chapLabel);


        doorEntries = new Array<>(TOTAL_LEVELS);
        SavedDataHandler handler = new SavedDataHandler();
        boolean foundFirstIncomplete = false;

        for (int i = 0; i < TOTAL_LEVELS; i++) {
            int levelNumber = i + 1;
            String completed = handler.getDataVal("level" + levelNumber);

            Texture baseTex, hoverTex;

//            if ("true".equals(completed)) {
                baseTex = loadDoorTexture("complete", false);
                hoverTex = loadDoorTexture("complete", true);
//            } else if (!foundFirstIncomplete) {
//                baseTex = loadDoorTexture("incomplete", false);
//                hoverTex = loadDoorTexture("incomplete", true);
//                foundFirstIncomplete = true;
//            } else {
//                baseTex = loadDoorTexture("locked", false);
//                hoverTex = baseTex;
//            }

            Image door = new Image(baseTex);
            door.setSize(doorWidth, doorHeight);

            Label.LabelStyle labelStyle = new Label.LabelStyle(unicaFont, com.badlogic.gdx.graphics.Color.WHITE);
            Label label = new Label(String.valueOf(levelNumber), labelStyle);
            label.setTouchable(Touchable.disabled);
            label.setFontScale(1.0f);
            label.setSize(doorWidth, doorHeight);
            label.setAlignment(com.badlogic.gdx.utils.Align.center);
            label.setPosition(door.getX(), door.getY());

            final int labelIndex = i;

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
                    doorLabels.get(labelIndex).setColor(com.badlogic.gdx.graphics.Color.ORANGE);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                    ((Image)event.getListenerActor()).setDrawable(entry.defaultDrawable);
                    doorLabels.get(labelIndex).setColor(com.badlogic.gdx.graphics.Color.WHITE);

                }
            });

            doorEntries.add(entry);
            doorLabels.add(label);
            stage.addActor(door);
            stage.addActor(label);
        }

        addArrowButtons();
        addBackSetting();
        updateDoorDisplay();
    }

    // ... rest of code unchanged ...


    private void addArrowButtons() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        // Left arrow textures and image
        Texture l_arrow_text = new Texture(Gdx.files.internal("ui/l_arrow.png"));
        Texture l_arrow_hov_text = new Texture(Gdx.files.internal("ui/l_arrow_click.png"));
        Image l_arrow = new Image(l_arrow_text);
        l_arrow.setSize(screenWidth * 0.05f, screenHeight * 0.05f);
        l_arrow.setPosition(screenWidth * 0.01f, screenHeight * 0.30f);
        stage.addActor(l_arrow);
        leftArrowEntry = new ArrowEntry(l_arrow,
            new TextureRegionDrawable(l_arrow_text), new TextureRegionDrawable(l_arrow_hov_text),-1);


        // Right arrow textures and image
        Texture r_arrow_text = new Texture(Gdx.files.internal("ui/r_arrow.png"));
        Texture r_arrow_hov_text = new Texture(Gdx.files.internal("ui/r_arrow_click.png"));
        Image r_arrow = new Image(r_arrow_text);
        r_arrow.setSize(screenWidth * 0.05f, screenHeight * 0.05f);
        r_arrow.setPosition(screenWidth * 0.94f, screenHeight * 0.30f);
        stage.addActor(r_arrow);
        rightArrowEntry = new ArrowEntry(r_arrow,new TextureRegionDrawable(r_arrow_text),
            new TextureRegionDrawable(r_arrow_hov_text),1);

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
                    if (scrollIndex >= 15){
                        chapLabel.setDrawable(new TextureRegionDrawable(new TextureRegion(chap4TitleTexture)));
                    }
                    else if (scrollIndex >= 10) {
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
                    if (scrollIndex >= 15) {
                        chapLabel.setDrawable(new TextureRegionDrawable(new TextureRegion(chap4TitleTexture)));
                    } else if (scrollIndex >= 10) {
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

        Texture backButtText = new Texture(Gdx.files.internal("ui/back_butt.png"));
        Texture settButtText = new Texture(Gdx.files.internal("ui/setting.png"));
        Image backButt = new Image(backButtText);
        backButt.setSize(screenWidth * 0.075f, screenHeight * 0.075f);
        backButt.setPosition(screenWidth * 0.01f, screenHeight * 0.9f);
        stage.addActor(backButt);

        Image settButt = new Image(settButtText);
        settButt.setSize(screenWidth * 0.075f, screenHeight * 0.075f);
        settButt.setPosition(screenWidth * 0.915f, screenHeight * 0.9f);
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
                entry.image.setY(yPos);
                entry.image.setVisible(true);
            } else {
                entry.targetX = 0;
                entry.image.setVisible(false);
            }
        }
    }

    private void updateDoorAnimation(float delta) {
        float lerpSpeed = 10f;
        for (int i = 0; i < doorEntries.size; i++) {
            DoorEntry entry = doorEntries.get(i);
            float currentX = entry.image.getX();
            float newX = currentX + (entry.targetX - currentX) * lerpSpeed * delta;
            if (i == 7) {


            }
            entry.image.setX(newX);

            if (i < doorLabels.size) {
                Label label = doorLabels.get(i);
                float labelX = newX + doorWidth / 2f - label.getPrefWidth() / 2f;
                float labelY = entry.image.getY() + doorHeight / 2f - label.getPrefHeight() / 2f;
                label.setPosition(newX, entry.image.getY());
                label.setSize(doorWidth, doorHeight);  // ensure it resizes with the door
                label.setVisible(entry.image.isVisible());
            }
        }
    }

    private Entry controllerButtons(){
        Entry entry;
        if (currentIndex%MAX_BUTTONS==0){
            entry = leftArrowEntry;
        }else if (currentIndex%MAX_BUTTONS==(MAX_BUTTONS-1)){
            entry = rightArrowEntry;
        }else{
            entry = doorEntries.get(5*chapterNumber+currentIndex-1);
        }
        return entry;
    }

    private void controllerSelect() {
        if (inputController.isUsingController()) {
            XBoxController xbox = inputController.xbox;
            boolean start = xbox.getA();
            Entry currentEntry = controllerButtons();
            currentEntry.image.setDrawable(currentEntry.hoverDrawable);
            if (start && !prevButtonA) {
                int selectionIndex = scrollIndex + currentIndex;
                if (selectionIndex >= 0 && selectionIndex < doorEntries.size) {

                    InputEvent downEvent = new InputEvent();
                    downEvent.setType(InputEvent.Type.touchDown);
                    downEvent.setStage(stage);
                    downEvent.setTarget(currentEntry.image);
                    downEvent.setButton(0);
                    currentEntry.image.fire(downEvent);
                    if (currentEntry.equals(leftArrowEntry)) {
                        chapterNumber = Math.max(0,chapterNumber-1);
                    }
                    if (currentEntry.equals(rightArrowEntry)) {
                        chapterNumber = Math.min(2,chapterNumber+1);
                    }
                }
            }
            prevButtonA = start;

            boolean moved = false;
            float horizontal = xbox.getLeftX();
            if (joystickCooldown <= 0) {
                if (horizontal < -0.4f && !moved) {
//                    particleEngine.dispose();
                    currentEntry.image.setDrawable(currentEntry.defaultDrawable);
                    currentIndex = Math.max(currentIndex - 1, 0);  // Prevent going negative
                    moved = true;
                    joystickCooldown = 0.25f;
                } else if (horizontal > 0.4f && !moved) {
                    currentEntry.image.setDrawable(currentEntry.defaultDrawable);
                    currentIndex = Math.min(currentIndex + 1, MAX_BUTTONS-1);  // Prevent going off right
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
        soundEngine.tendToMusicLoop();
        if (doorDisplayDirty) {
            updateDoorDisplay();
            doorDisplayDirty = false;
        }
        updateDoorAnimation(delta);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        controllerSelect();
        stage.act(delta);
        updateDoorAnimation(delta);
        stage.draw();
        if(inputController.xbox.getB()){listener.exitScreen(LevelSelectScene.this, 0);}

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
