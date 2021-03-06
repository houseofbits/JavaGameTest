package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonReader;

/**
 * Created by T510 on 8/6/2017.
 */

public class Renderable {

    private GameObject gameObject;

    private String modelName = "";

    public ModelBatch modelBatch = null;
    public ModelInstance modelInstance = null;
    //private Texture texture = null;

    public Renderable(GameObject o){
        gameObject = o;
    }
    public Renderable(GameObject o, String filename){
        gameObject = o;
        modelName = filename;
    }

    public void create(String m){
        modelName = m;
        gameObject.getSceneManager().assetsManager.load(modelName, Model.class);
    }

    public void create(){
        gameObject.getSceneManager().assetsManager.load(modelName, Model.class);
    }

    public void init(){

        modelBatch = new ModelBatch();

        if(gameObject.getSceneManager().assetsManager.isLoaded(modelName)) {
            Model model = gameObject.getSceneManager().assetsManager.get(modelName, Model.class);
            modelInstance = new ModelInstance(model);
        }else{
            System.out.println("Renderable:init asset not loaded "+modelName);
        }
    }

    public void translate(Vector3 pos){
        if(modelInstance != null) {
            modelInstance.transform.idt();
            modelInstance.transform.translate(pos);
        }
    }

    public void setColor(float r, float g, float b){
        if(modelInstance != null)modelInstance.materials.get(0).set(ColorAttribute.createDiffuse(r,g,b,1));
    }

    public void render(PerspectiveCamera cam, Environment env){
        modelBatch.begin(cam);
        if(modelInstance != null)modelBatch.render(modelInstance, env);
        else System.out.println("Renderable:render instance not created "+modelName);
        modelBatch.end();
    }

    public void dispose(){
        modelBatch.dispose();
    }

}
