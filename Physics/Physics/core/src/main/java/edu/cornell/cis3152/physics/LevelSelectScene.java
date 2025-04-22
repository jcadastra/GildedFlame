package edu.cornell.cis3152.physics;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
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
    private Stage stage;
    private Skin skin;
    int selectedLevel = 1;
    private ScreenListener listener;
    private Texture bgTexture;

    private Array<Image> levels = new Array<Image>(10);//10 level per scene
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

        bgTexture = new Texture(Gdx.files.internal("ui/plain_back.png"));
        Image bgImage = new Image(bgTexture);
        bgImage.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.addActor(bgImage);

        createBasicUI();
    }

    private void createBasicUI() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        Texture chamberLabelTexture = new Texture(Gdx.files.internal("ui/chamber_sel.png"));
        Image chamberLabel = new Image(chamberLabelTexture);
        chamberLabel.setSize(screenWidth * 0.5f, screenHeight * 0.1f);
        chamberLabel.setPosition(
            screenWidth * 0.25f,
            screenHeight * 0.85f
        );
        stage.addActor(chamberLabel);
        Texture chapLableTexture = new Texture(Gdx.files.internal("ui/chap_title.png"));
        Image chapLabel = new Image(chapLableTexture);
        chapLabel.setSize(screenWidth * 0.2f, screenHeight * 0.05f);
        float verticalSpacing = screenHeight * 0.02f;
        chapLabel.setPosition(
            (screenWidth - screenWidth * 0.2f) / 2f,
            chamberLabel.getY() - screenHeight * 0.05f - verticalSpacing
        );
        stage.addActor(chapLabel);


        Texture[] doorTextures = new Texture[] {
            new Texture(Gdx.files.internal("ui/doors/door1.png")),
            new Texture(Gdx.files.internal("ui/doors/door2.png")),
            new Texture(Gdx.files.internal("ui/doors/door3.png")),
            new Texture(Gdx.files.internal("ui/doors/door4.png")),
            new Texture(Gdx.files.internal("ui/doors/door5.png"))
        };

        float doorWidth = screenWidth * 0.15f;
        float doorHeight = screenHeight * 0.2f;
        float spacing = screenWidth * 0.03f; //
        float totalWidth = doorWidth * doorTextures.length + spacing * (doorTextures.length - 1);
        int centerIndex = doorTextures.length / 2; // 2 for 5 doors
        float startX = screenWidth / 2f - doorWidth / 3f - (centerIndex * (doorWidth + spacing));
        float yPos = screenHeight * 0.185f;

        for (int i = 0; i < doorTextures.length; i++) {
            final int levelIndex = i + 1;
            final Texture defaultTexture = doorTextures[i];
            final Texture hoverTexture = (i < 3)
                ? new Texture(Gdx.files.internal("ui/doors/door" + levelIndex + "_click.png"))
                : defaultTexture;

            Image door = new Image(defaultTexture);
            door.setSize(doorWidth, doorHeight);
            door.setPosition(startX + i * (doorWidth + spacing), yPos);

            if (i < 3) {
                door.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        selectedLevel = levelIndex;
                        if (listener != null) {
                            listener.exitScreen(LevelSelectScene.this, 1);
                        }
                        return true;
                    }

                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                        ((Image) event.getListenerActor()).setDrawable(new Image(hoverTexture).getDrawable());
                    }

                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                        ((Image) event.getListenerActor()).setDrawable(new Image(defaultTexture).getDrawable());
                    }
                });

                levels.add(door);
            }

            stage.addActor(door);
        }
        Texture l_arrow_text = new Texture(Gdx.files.internal("ui/l_arrow.png"));
        Texture l_arrow_hov_text = new Texture(Gdx.files.internal("ui/l_arrow_click.png"));
        Image l_arrow = new Image(l_arrow_text);
        l_arrow.setSize(screenWidth * 0.05f, screenHeight * 0.05f);
        l_arrow.setPosition(
            screenWidth * 0.01f,
            screenHeight * 0.25f
        );
        stage.addActor(l_arrow);

        Texture r_arrow_text = new Texture(Gdx.files.internal("ui/r_arrow.png"));
        Texture r_arrow_hov_text = new Texture(Gdx.files.internal("ui/r_arrow_click.png"));
        Image r_arrow = new Image(r_arrow_text);
        r_arrow.setSize(screenWidth * 0.05f, screenHeight * 0.05f);
        r_arrow.setPosition(
            screenWidth * 0.94f,
            screenHeight * 0.25f
        );
        stage.addActor(r_arrow);
        final TextureRegionDrawable l_arrow_drawable = new TextureRegionDrawable(new TextureRegion(l_arrow_text));
        final TextureRegionDrawable l_arrow_hover_drawable = new TextureRegionDrawable(new TextureRegion(l_arrow_hov_text));
        final TextureRegionDrawable r_arrow_drawable = new TextureRegionDrawable(new TextureRegion(r_arrow_text));
        final TextureRegionDrawable r_arrow_hover_drawable = new TextureRegionDrawable(new TextureRegion(r_arrow_hov_text));

        l_arrow.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                ((Image) event.getListenerActor()).setDrawable(l_arrow_hover_drawable);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                ((Image) event.getListenerActor()).setDrawable(l_arrow_drawable);
            }
        });

        r_arrow.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
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

    private void updateLevelHighlight(){
        Image currentLevel = levels.get(currentIndex);
//        particleEngine = new ParticleEngine(levels.get(currentIndex));
//            Batch batch = stage.getBatch();
//            batch.begin();
//            particleEngine.draw(batch,currentLevel);
//            batch.end();
        currentLevel.setColor(Color.ORANGE);
    }

    private void controllerSelect(){
        Image currentLevel = levels.get(currentIndex);
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
