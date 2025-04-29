package edu.cornell.cis3152.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
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
    private Stage stage;
    private Skin skin;
    private Texture bgTexture;

    private Array<DoorEntry> doorEntries;
    private Texture baseDoorTexture;

    private float doorWidth, doorHeight, spacing;
    private int scrollIndex = 0;
    private static final int DOORS_VISIBLE = 5;

    private int selectedLevel = 1;
    private int currentIndex = 0;

    private InputController inputController = InputController.getInstance();
    private ScreenListener listener;
    private boolean prevButtonA = true;
    private float joystickCooldown = 0f;
    private Texture chapTitleTexture;
    private Texture chap1TitleTexture;
    private Texture chap3TitleTexture;

    private Image chapLabel;


    private ParticleEngine particleEngine; // If needed for controller highlight

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

    private void createBasicUI() {

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        doorWidth = screenWidth * 0.2f;
        doorHeight = screenHeight * 0.4f;
        spacing = screenWidth * 0.001f;
        Texture chamberLabelTexture = new Texture(Gdx.files.internal("ui/chamber_sel.png"));
        Image chamberLabel = new Image(chamberLabelTexture);
        chamberLabel.setSize(screenWidth * 0.5f, screenHeight * 0.1f);
        chamberLabel.setPosition(
            screenWidth * 0.25f,
            screenHeight * 0.85f
        );
        stage.addActor(chamberLabel);
        chapTitleTexture = new Texture(Gdx.files.internal("ui/chap1_title.png"));
        chap1TitleTexture = new Texture(Gdx.files.internal("ui/chap_title.png"));
        chap3TitleTexture = new Texture(Gdx.files.internal("ui/chap3_title.png"));
        chapLabel = new Image(chapTitleTexture);

        chapLabel.setSize(screenWidth * 0.2f, screenHeight * 0.05f);
        float verticalSpacing = screenHeight * 0.02f;
        chapLabel.setPosition(
            (screenWidth - screenWidth * 0.2f) / 2f,
            chamberLabel.getY() - screenHeight * 0.05f - verticalSpacing
        );
        stage.addActor(chapLabel);



        // Setup door textures
        baseDoorTexture = new Texture(Gdx.files.internal("ui/doors/incompleteDoor.png"));

        Texture hoverTexture = new Texture(Gdx.files.internal("ui/doors/incompleteDoorHover.png"));

        doorEntries = new Array<>(15);

        for (int i = 0; i < 15; i++) {
            int baseIndex = i % 5;
            Texture defaultTexture = baseDoorTexture;



            Image door = new Image(defaultTexture);
            door.setSize(doorWidth, doorHeight);

            DoorEntry entry = new DoorEntry(
                door,
                new TextureRegionDrawable(new TextureRegion(defaultTexture)),
                new TextureRegionDrawable(new TextureRegion(hoverTexture)),
                (i < 5) ? (i + 1) : ((i - 5) + 1)
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
                    ((Image) event.getListenerActor()).setDrawable(entry.hoverDrawable);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                    ((Image) event.getListenerActor()).setDrawable(entry.defaultDrawable);
                }
            });

            doorEntries.add(entry);
            stage.addActor(door);
        }

        addArrowButtons();
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
                    updateDoorDisplay();

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
                    updateDoorDisplay();

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

    private void updateDoorDisplay() {
        float screenWidth = Gdx.graphics.getWidth();
        float offsetRight = screenWidth * 0.025f;
        float startX = screenWidth / 2f - (doorWidth * DOORS_VISIBLE + spacing * (DOORS_VISIBLE - 1)) / 2f + offsetRight;
        float yPos = Gdx.graphics.getHeight() * 0.185f;

        for (int i = 0; i < doorEntries.size; i++) {
            DoorEntry entry = doorEntries.get(i);
            entry.targetX = startX + (i - scrollIndex) * (doorWidth + spacing);
            entry.door.setY(yPos);
        }
    }

    private void updateDoorAnimation(float delta) {
        float lerpSpeed = 10f; // Higher = faster movement
        float screenWidth = Gdx.graphics.getWidth();
        float startX = screenWidth / 2f - (doorWidth * DOORS_VISIBLE + spacing * (DOORS_VISIBLE - 1)) / 2f;

        for (int i = 0; i < doorEntries.size; i++) {
            DoorEntry entry = doorEntries.get(i);

            float currentX = entry.door.getX();
            float newX = currentX + (entry.targetX - currentX) * lerpSpeed * delta;
            entry.door.setX(newX);

            // ✨ Control visibility
            if (i >= scrollIndex && i < scrollIndex + DOORS_VISIBLE) {
                entry.door.setVisible(true);
            } else {
                entry.door.setVisible(false);
            }
        }
    }
    private void controllerSelect() {
        if (inputController.isUsingController()) {
            XBoxController xbox = inputController.xbox;
            DoorEntry currentEntry = doorEntries.get(currentIndex);
            currentEntry.door.setDrawable(currentEntry.hoverDrawable);
            boolean start = xbox.getA();
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
                    //particleEngine.dispose();
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
        Gdx.gl.glClearColor(0.9f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Batch batch = stage.getBatch();
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
