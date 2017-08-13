package com.mygdx.game;

//import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
//import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
//import GUI.GUIStage;

public class MyGdxGame extends InputAdapter implements ApplicationListener {

    protected Stage stage;
    protected Label label;
    protected BitmapFont font;
    protected StringBuilder stringBuilder;

    protected SceneManager sceneManager;

	@Override
	public void create () {

        stage = new Stage();
        font = new BitmapFont();
        label = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
        stage.addActor(label);
        stringBuilder = new StringBuilder();

        sceneManager = new SceneManager();

        //Gdx.input.setInputProcessor(new InputMultiplexer(this, camController));

        //Gdx.input.setInputProcessor(this);
    }

	@Override
	public void render () {

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glClearColor(0.5f,0.5f,0.5f,1.0f);
        Gdx.gl.glEnable(GL30.GL_DEPTH_TEST);

        sceneManager.processFrame();

        //HUD
        stringBuilder.setLength(0);
        stringBuilder.append(" FPS: ").append(Gdx.graphics.getFramesPerSecond());

        if(!sceneManager.assetsLoaded){
            float progress = sceneManager.assetsManager.getProgress() * 100;
            stringBuilder.append("  [ LOADING "+progress+"% ] ");
        }

        label.setText(stringBuilder);
        stage.draw();

	}

    @Override
    public void resize(int width, int height) {
    }

	@Override
	public void dispose () {

        sceneManager.dispose();

	}
    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

}
