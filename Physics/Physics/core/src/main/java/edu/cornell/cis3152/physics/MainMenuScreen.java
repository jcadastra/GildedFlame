package edu.cornell.cis3152.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.ScreenUtils;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.ScreenListener;
import com.badlogic.gdx.audio.Sound;
import edu.cornell.gdiac.util.XBoxController;


public class MainMenuScreen implements Screen {
    private Stage stage;
    private Skin skin;
    private Texture bgTexture;
    private boolean startClicked = false;
    private ScreenListener listener;
    private InputController inputController = InputController.getInstance();
    private boolean prevButtonA = false;
    protected SoundEngine soundEngine;
    /** The asset directory for retrieving textures, atlases */
    protected AssetDirectory directory;
    private int scrollIndex,currentIndex,currentSelect = 0;
    private Array<Entry> entries = new Array<Entry>();
    private float joystickCooldown = 0f;

    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    public class Entry{
        public ImageButton image;
        public TextureRegionDrawable defaultDrawable;
        public TextureRegionDrawable hoverDrawable;
        public Entry(ImageButton image, TextureRegionDrawable defaultDrawable, TextureRegionDrawable hoverDrawable){
            this.image = image;
            this.defaultDrawable = defaultDrawable;
            this.hoverDrawable = hoverDrawable;
        }
    }
    public MainMenuScreen(AssetDirectory directory, SoundEngine soundEngine) {
        this.directory = directory;
        this.soundEngine = soundEngine;
        stage = new Stage(new ScreenViewport());
        skin = new Skin();

        bgTexture = directory.getEntry("menuScreen", Texture.class);
        //bgTexture = new Texture(Gdx.files.internal("loading/menuScreen.png"));
        Image bgImage = new Image(bgTexture);
        bgImage.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.addActor(bgImage);

        createBasicUI();
    }

    public boolean getStartClicked(){
        return startClicked;
    }

    private void createBasicUI() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        BitmapFont font = new BitmapFont();

        Pixmap pixmap = new Pixmap(200, 60, Pixmap.Format.RGB888);
        pixmap.setColor(1, 1, 1, 1);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        Drawable drawable = new TextureRegionDrawable(texture);

        Texture newGameText = directory.getEntry("newGameButt", Texture.class);
        Texture newGameClickText = directory.getEntry("newGameButtClick", Texture.class);

        TextureRegionDrawable buttonUp = new TextureRegionDrawable(new TextureRegion(newGameText));
        TextureRegionDrawable buttonOver = new TextureRegionDrawable(new TextureRegion(newGameClickText));


        ImageButton.ImageButtonStyle newGame = new ImageButton.ImageButtonStyle();
        newGame.up = buttonUp;
        newGame.over = buttonOver;
        ImageButton imageButton = new ImageButton(newGame);
        Entry newButt = new Entry(imageButton,buttonUp,buttonOver);
        entries.add(newButt);

        Texture contText = directory.getEntry("contButt", Texture.class);
        Texture contClickText = directory.getEntry("contButtClick", Texture.class);
        TextureRegionDrawable contButtUp = new TextureRegionDrawable(new TextureRegion(contText));
        TextureRegionDrawable contButtOver = new TextureRegionDrawable(new TextureRegion(contClickText));


        ImageButton.ImageButtonStyle contButt = new ImageButton.ImageButtonStyle();
        contButt.up = contButtUp;
        contButt.over = contButtOver;

        ImageButton contButt1 = new ImageButton(contButt);

        Entry conButt = new Entry(contButt1,contButtUp,contButtOver);
        entries.add(conButt);

        Texture settingsText = directory.getEntry("settingsButt", Texture.class);
        Texture settingsClickText = directory.getEntry("settingsButtClick", Texture.class);
        TextureRegionDrawable settingsTextUp = new TextureRegionDrawable(new TextureRegion(settingsText));
        TextureRegionDrawable settingsTextOver = new TextureRegionDrawable(new TextureRegion(settingsClickText));

        ImageButton.ImageButtonStyle settingsButt = new ImageButton.ImageButtonStyle();
        settingsButt.up = settingsTextUp;
        settingsButt.over = settingsTextOver;

        ImageButton settingsButt1 = new ImageButton(settingsButt);
        Entry setButt = new Entry(settingsButt1,settingsTextUp,settingsTextOver);
        entries.add(setButt);

        imageButton.setSize(screenWidth * 0.2f, screenHeight * 0.06f);
        imageButton.setPosition(
            screenWidth * 0.69f,
            screenHeight * 0.5f
        );

        contButt1.setSize(screenWidth * 0.2f, screenHeight * 0.06f);
        contButt1.setPosition(
            screenWidth * 0.69f,
            screenHeight * 0.4f
        );

        settingsButt1.setSize(screenWidth * 0.2f, screenHeight * 0.06f);
        settingsButt1.setPosition(
            screenWidth * 0.69f,
            screenHeight * 0.3f
        );



// Add your input listener
        imageButton.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                                     float x, float y, int pointer, int button) {
//                System.out.println("ImageButton pressed");
                soundEngine.playSoundEffect("clickSound");
                startClicked = true;
                if (listener != null) {
                    listener.exitScreen(MainMenuScreen.this, 1);
                }
                return true;
            }
        });
        contButt1.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                                     float x, float y, int pointer, int button) {
                soundEngine.playSoundEffect("clickSound");
//                System.out.println("ImageButton pressed");
                startClicked = true;
                if (listener != null) {
                    SavedDataHandler handler = new SavedDataHandler();
                    String lastCompleted = handler.getDataVal("lastLevel");
//                    System.out.println("last completed" +lastCompleted);
                    int nextLevel = 1;
                    try {
                        if (lastCompleted != null) {
                            nextLevel = Integer.parseInt(lastCompleted) + 1;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid level stored: " + lastCompleted);
                    }

                    // Optional: cap nextLevel to the max number of levels
                    int maxLevel = 10; // change to however many levels you have
                    if (nextLevel > maxLevel) {
                        nextLevel = maxLevel;
                    }

                    // Go to the next level
                    listener.exitScreen(MainMenuScreen.this, -1);
                }
                return true;
            }
        });
        settingsButt1.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                                     float x, float y, int pointer, int button) {
                soundEngine.playSoundEffect("clickSound");
//                System.out.println("ImageButton pressed");
                startClicked = true;
                if (listener != null) {
                    listener.exitScreen(MainMenuScreen.this, 2);
                }
                return true;
            }
        });



// Add the ImageButton to your stage
        stage.addActor(imageButton);
        stage.addActor(contButt1);
        stage.addActor(settingsButt1);


        Texture topRightTexture = directory.getEntry("textButt", Texture.class);
        Image topRightImage = new Image(topRightTexture);

        topRightImage.setSize(screenWidth * 0.3f, screenHeight * 0.2f);

        topRightImage.setPosition(
            screenWidth * 0.65f,
            screenHeight * 0.65f
        );

        // Add the image actor to the stage
        stage.addActor(topRightImage);

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        controllerSelect();
        stage.act(delta);
        stage.draw();
    }

    private void controllerSelect() {
        if (inputController.isUsingController()) {
            XBoxController xbox = inputController.xbox;
            boolean start = xbox.getA();
            Entry currentEntry = entries.get(currentIndex);
            ImageButton.ImageButtonStyle style = currentEntry.image.getStyle();
            style.up = currentEntry.hoverDrawable;
            currentEntry.image.setStyle(style);
            if (start && !prevButtonA) {
                int selectionIndex = scrollIndex + currentIndex;
                if (selectionIndex >= 0 && selectionIndex < entries.size) {

                    InputEvent downEvent = new InputEvent();
                    downEvent.setType(InputEvent.Type.touchDown);
                    downEvent.setStage(stage);
                    downEvent.setTarget(currentEntry.image);
                    downEvent.setButton(0);
                    currentEntry.image.fire(downEvent);
                }
            }
            prevButtonA = start;

            boolean moved = false;
            float vertical = xbox.getLeftY();
            if (joystickCooldown <= 0) {
                if (vertical < -0.4f && !moved) {
//                    particleEngine.dispose();
                    ImageButton.ImageButtonStyle s = currentEntry.image.getStyle();
                    s.up = currentEntry.defaultDrawable;
                    currentEntry.image.setStyle(s);
                    currentIndex = Math.max(currentIndex - 1, 0);  // Prevent going negative
                    moved = true;
                    joystickCooldown = 0.25f;
                } else if (vertical > 0.4f && !moved) {
                    ImageButton.ImageButtonStyle s = currentEntry.image.getStyle();
                    s.up = currentEntry.defaultDrawable;
                    currentEntry.image.setStyle(s);
                    currentIndex = Math.min(currentIndex + 1, entries.size-1);  // Prevent going off right
                    moved = true;
                    joystickCooldown = 0.25f;
                }
                if (Math.abs(vertical) <= 0.01f) {
                    moved = false;
                }
            }
            joystickCooldown -= Gdx.graphics.getDeltaTime();
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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
}
