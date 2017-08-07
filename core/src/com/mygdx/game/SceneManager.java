package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import events.EventManager;

/**
 * Created by T510 on 8/2/2017.
 */

public class SceneManager {

    public EventManager eventManager = new EventManager();

    protected Array<GameObject> gameObjectArray = new Array<GameObject>();
    protected Array<GameObject> createGameObjectArray = new Array<GameObject>();

    protected TimeUtils time = new TimeUtils();
    protected long prev_frame_time = 0;
    public float frame_time_s = 0;

    public Environment environment;
    public PerspectiveCamera cam;
    public CameraInputController camController;

    public AssetManager assetsManager;
    public boolean  assetsLoaded = false;

    public Scene    scene = null;

    public SceneManager(){

        assetsManager = new AssetManager();

        assetsManager.setErrorListener(new AssetErrorListener() {
            @Override
            public void error(AssetDescriptor assetDescriptor, Throwable throwable) {
                System.out.println("ASSET: "+assetDescriptor.toString()+" - "+throwable.getMessage());
            }
        });

        prev_frame_time = time.millis();
        cam = new PerspectiveCamera(40, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(-0.7f, 1.3f, 6f);
        cam.lookAt(0,1.7f,0);
        cam.near = 1f;
        cam.far = 500f;
        cam.update();

        camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
    }

    public void AddGameObject(GameObject object){
        object.onCreate(this);
        this.createGameObjectArray.add(object);
    }

    public GameObject getObjectByName(String name){
        for(int i=0; i<gameObjectArray.size; i++){
            GameObject o = gameObjectArray.get(i);
            if(o.getName() == name)return o;
        }
        return null;
    }

    void processFrame(){

        if(!assetsManager.update()){
            assetsLoaded = false;

            //draw loading screen

            return;
        }
        assetsLoaded = true;

        long current_time_ms = time.millis();
        frame_time_s = (current_time_ms - prev_frame_time) / 1000.0f;

        if(frame_time_s > 0.025f)frame_time_s = 0.025f;

        camController.update();

        for (final GameObject go : this.gameObjectArray) {
            go.onUpdate();
        }

        for (final GameObject go : this.gameObjectArray) {
            go.render();
        }

        prev_frame_time = current_time_ms;

        //Process events
        eventManager.process();

        //Remove marked GameObjects
        for(int i=0; i<gameObjectArray.size; i++){
            GameObject o = gameObjectArray.get(i);
            if(o.isDisposable()){
                o.dispose();
                o = null;
                gameObjectArray.removeIndex(i);
            }
        }

        //Load all assets before creating new objects
        if(assetsManager.getQueuedAssets() > 0 && createGameObjectArray.size > 0) {
            assetsManager.finishLoading();
        }

        //Add new game GameObjects
        for (int i = 0; i < createGameObjectArray.size; i++) {
            GameObject o = createGameObjectArray.get(i);
            gameObjectArray.add(o);
            o.onInit();
        }
        createGameObjectArray.clear();
    }

    public void dispose(){
        for (final GameObject go : this.gameObjectArray) {
            go.dispose();
        }
    }
}
