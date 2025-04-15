package edu.cornell.cis3152.physics;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.ScreenUtils;
import edu.cornell.gdiac.util.ScreenListener;
import edu.cornell.gdiac.util.XBoxController;

public class LevelSelectScene implements Screen {
    private Stage stage;
    private Skin skin;
    int selectedLevel = 1;
    private ScreenListener listener;
    private Texture bgTexture;

    private Array<TextButton> levels = new Array<>(10);//10 level per scene
    private int currentIndex = 0;

    private ParticleEngine particleEngine;
    private boolean prevButtonA = true;

    private float joystickCooldown = 0f;

    private InputController inputController = InputController.getInstance();
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }




    public LevelSelectScene() {
        stage = new Stage(new ScreenViewport());
        skin = new Skin();

        bgTexture = new Texture(Gdx.files.internal("loading/menuScreen.png"));
        Image bgImage = new Image(bgTexture);
        bgImage.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.addActor(bgImage);

        createBasicUI();
    }

    private void createBasicUI() {
        // Create a basic font
        BitmapFont font = new BitmapFont();

        Pixmap pixmap = new Pixmap(200, 60, Pixmap.Format.RGB888);
        pixmap.setColor(1, 1, 1, 1);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        Drawable drawable = new TextureRegionDrawable(texture);

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = drawable;
        textButtonStyle.font = font;
        skin.add("default", textButtonStyle);

        float buttonWidth = 200;
        float buttonHeight = 60;
        float spacing = 50;

        float totalHeight = 3 * buttonHeight + 2 * spacing;
        float centerX = (Gdx.graphics.getWidth() / 2f - buttonWidth / 2f) + 325;
        float startY = Gdx.graphics.getHeight() / 2f + totalHeight / 2f - buttonHeight;

        textButtonStyle.fontColor = Color.DARK_GRAY;
        TextButton oneButton = new TextButton("Level 1", skin);
        oneButton.setSize(200, 60);
        oneButton.setPosition(centerX, startY);
        TextButton twoButton = new TextButton("Level 2", skin);
        twoButton.setSize(200, 60);
        twoButton.setPosition(centerX, startY - (buttonHeight + spacing));
        TextButton threeButton = new TextButton("Level 3", skin);
        threeButton.setSize(200, 60);
        threeButton.setPosition(centerX, startY - 2 * (buttonHeight + spacing));


        oneButton.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                                     float x, float y, int pointer, int button) {
                System.out.println("Button pressed");
                selectedLevel = 1;
                if (listener != null) {
                    listener.exitScreen(LevelSelectScene.this, 1); // You choose any exit code
                }
                return true;


            }
        });
        twoButton.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                                     float x, float y, int pointer, int button) {
                System.out.println("Button pressed");
                selectedLevel = 2;
                if (listener != null) {
                    listener.exitScreen(LevelSelectScene.this, 1); // You choose any exit code
                }
                return true;


            }
        });
        threeButton.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                                     float x, float y, int pointer, int button) {
                System.out.println("Button pressed");
                selectedLevel = 3;
                if (listener != null) {
                    listener.exitScreen(LevelSelectScene.this, 1); // You choose any exit code
                }
                return true;


            }
        });
        levels.add(oneButton);
        levels.add(twoButton);
        levels.add(threeButton);

        //controller support
        if (inputController.isUsingController()){
            if (levels.notEmpty()){//the first button appears as selected
            particleEngine = new ParticleEngine(levels.get(0));}

        }

        stage.addActor(oneButton);
        stage.addActor(twoButton);
        stage.addActor(threeButton);

    }

    private void updateLevelHighlight(){
        TextButton currentLevel = levels.get(currentIndex);
//        particleEngine = new ParticleEngine(levels.get(currentIndex));
//            Batch batch = stage.getBatch();
//            batch.begin();
//            particleEngine.draw(batch,currentLevel);
//            batch.end();
        currentLevel.setColor(Color.ORANGE);
    }

    private void controllerSelect(){
        TextButton currentLevel = levels.get(currentIndex);
        // begin controller listening
        if (inputController.isUsingController()){
            // starts listening for level confirmation
            XBoxController xbox = inputController.xbox;
            boolean start= xbox.getA();
            if(start && !prevButtonA){
                InputEvent downEvent = new InputEvent();
                downEvent.setType(InputEvent.Type.touchDown);
                downEvent.setStage(stage);
                downEvent.setTarget(currentLevel);
                downEvent.setButton(0);
                currentLevel.fire(downEvent);

                InputEvent upEvent = new InputEvent();
                upEvent.setType(InputEvent.Type.touchUp);
                upEvent.setStage(stage);
                upEvent.setTarget(currentLevel);
                upEvent.setButton(0);
                currentLevel.fire(upEvent);
            }
            prevButtonA =start;


            // starts listening for level change
            boolean moved = false; // debounce flag

            float vertical = xbox.getLeftY();
            if (joystickCooldown <=0){
            // Move UP
            if (vertical < -0.4f && !moved) {
                particleEngine.dispose();
                levels.get(currentIndex).setColor(Color.WHITE);
                currentIndex = (currentIndex - 1 + levels.size) % levels.size;
                currentLevel = levels.get(currentIndex);
                moved = true;
                updateLevelHighlight();
                joystickCooldown = 0.25f;
            }

            // Move DOWN
            else if (vertical > 0.4f && !moved) {
                levels.get(currentIndex).setColor(Color.WHITE);
                particleEngine.dispose();
                currentIndex = (currentIndex + 1) % levels.size;
                currentLevel = levels.get(currentIndex);
                moved = true;
                updateLevelHighlight();
                joystickCooldown = 0.25f;
            }

            // Reset debounce when stick is near center
            if (Math.abs(vertical) <= 0.01f) {
                moved = false;
            }
            }
            float time = Gdx.graphics.getDeltaTime();
            joystickCooldown -= Gdx.graphics.getDeltaTime();
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.9f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        controllerSelect();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public int getSelectedLevel(){
        System.out.println("SELECTED LEVEL: " + selectedLevel);
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
}
