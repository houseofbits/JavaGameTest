package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by T510 on 8/6/2017.
 */

public class AnimatedRenderable implements AnimationController.AnimationListener{

    private GameObject gameObject;

    private String modelName = "";

    public ModelBatch modelBatch = null;
    public ModelInstance modelInstance = null;
    private AnimationController controller = null;
    //private Texture texture = null;

    public AnimatedRenderable(GameObject o){
        gameObject = o;
    }
    public AnimatedRenderable(GameObject o, String filename){
        gameObject = o;
        modelName = filename;
    }

    public void create(String m){
        modelName = m;
        gameObject.sceneManager.assetsManager.load(modelName, Model.class);
    }

    public void create(){
        gameObject.sceneManager.assetsManager.load(modelName, Model.class);
    }

    public void init(){

        modelBatch = new ModelBatch();

        if(gameObject.sceneManager.assetsManager.isLoaded(modelName)) {
            Model model = gameObject.sceneManager.assetsManager.get(modelName, Model.class);
            modelInstance = new ModelInstance(model);
            controller = new AnimationController(modelInstance);
        }else{
            System.out.println("AnimatedRenderable:init asset not loaded "+modelName);
        }
        PlayAnim();
    }

    public void translate(Vector3 pos){
        if(modelInstance != null) {
            modelInstance.transform.idt();
            modelInstance.transform.translate(pos);
        }
    }

    public void PlayAnim(){

        controller.setAnimation("Take1",-1, 0.1f, this);

    }

    public void setColor(float r, float g, float b){
        if(modelInstance != null)modelInstance.materials.get(0).set(ColorAttribute.createDiffuse(r,g,b,1));
    }

    public void render(PerspectiveCamera cam, Environment env){
        controller.update(Gdx.graphics.getDeltaTime());
        modelBatch.begin(cam);
        if(modelInstance != null)modelBatch.render(modelInstance, env);
        else System.out.println("Renderable:render instance not created "+modelName);
        modelBatch.end();
    }

    public void dispose(){
        modelBatch.dispose();
    }


    @Override
    public void onEnd(AnimationController.AnimationDesc animation) {   }

    @Override
    public void onLoop(AnimationController.AnimationDesc animation) {   }
}