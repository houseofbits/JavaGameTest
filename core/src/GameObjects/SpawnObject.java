package GameObjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.mygdx.game.GameObject;
import com.mygdx.game.SceneManager;

import java.util.Random;

import GameEvents.ActorEvent;
import GameEvents.SpawnEvent;
import Utils.Error;
import Utils.RandomDistribution;

/**
 * Created by T510 on 7/31/2017.
 */

public class SpawnObject extends GameObject {

    public enum State {
        FREE,                   //waiting to spawn object
        OCCUPIED,               //occupied by spawned object
        READY,                  //ready to spawn new object
    }

    private Array<Vector3> spawnPoints = new Array<Vector3>();
    private Array<String> affectedDoors = new Array<String>();
    private RandomDistribution<ActorObject.ActorType> actorDistribution = new RandomDistribution<ActorObject.ActorType>();
    public Vector3 position;
    public State state = null;
    private static Random random = new Random();
    private boolean enabled = true;
    private boolean globalDistribution = true;

    public SpawnObject(String name, String doorName, Vector3 pos){
        this.collide = false;
        this.setName(name);
        position = pos;

        addSpawnPoint(pos);
        addAffectedDoor(doorName);
    }

    public SpawnObject(String name) {
        this.collide = false;
        this.setName(name);
    }

    public Vector3 getSpawnedPosition(){
        return position;
    }

    public void onCreate(SceneManager sceneManagerRef){

        super.onCreate(sceneManagerRef);

        setFree();
    }

    public void onInit(){    }

    //ActorEvent from actor attached to this spawn point
    public void onActorEvent(ActorEvent e){
        if(e.state == ActorEvent.State.REMOVED){
            setFree();
            freeReadyToSpawnObjects();
        }
    }

    //When spawned object dies it sets free spawn point so it can begin wait for new action
    private void setFree(){
        state = State.FREE;
        sendEvent(new SpawnEvent(State.FREE));

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if(state == State.FREE && enabled) {
                    state = State.READY;
                    //Add this object to readyToSpawn structure
                    addReadyToSpawn();
                }
                cancel();
            }
        }, random.nextFloat());
    }

    //Perform spawn by actor type
    public void spawn(ActorObject.ActorType t){
        if(spawnPoints.size > 0) {
            position = spawnPoints.get(random.nextInt(spawnPoints.size));
            getSceneManager().addGameObject(t.createInstance(this));
            state = State.OCCUPIED;
            //Notify other SpawnObjects that this object is occupied
            sendEvent(new SpawnEvent(State.OCCUPIED));
            //Remove from readyToSpawn structure
            removeReadyToSpawn();
        }
    }

    //Perform spawn by RandomDistribution, by global distribution or local distribution
    //TODO: Do I need local distribution? Can it be implemented other way?
    public void spawn(){
        if(globalDistribution){
            ActorObject.ActorType actor = getScene().getRandomActorType();
            if(actor != null)spawn(actor);
        }
        else{
            RandomDistribution<ActorObject.ActorType>.Node node = null;
            node = actorDistribution.get();
            if(node != null)spawn(node.data);
        }
        //Error.logToFile(" do spawn:"+getName(), "spawnTest.txt");
    }

    public void onUpdate() {}
    public void render () { }
    public void dispose () {
        super.dispose();
    }

    public void addSpawnPoint(Vector3... p){
        for (int i = 0; i < p.length; i++) {
            spawnPoints.add(p[i]);
        }
    }

    public void removeActorType(ActorObject.ActorType type){
        actorDistribution.remove(type);
    }
    public void setActorWeight(ActorObject.ActorType type, float weight){actorDistribution.set(type, weight);}
    public void addActorType(ActorObject.ActorType type, float weight){
        actorDistribution.add(type, weight);
    }

    public void addActorType(ActorObject.ActorType... type){
        for (int i = 0; i < type.length; i++) {
            actorDistribution.add(type[i], 1.0f);
        }
    }

    public void addAffectedDoor(String... name){
        for (int i = 0; i < name.length; i++) {
            affectedDoors.add(name[i]);
        }
    }

    public void setAffectedDoorsState(DoorObject.State doorState){
        for (int i=0; i<affectedDoors.size; i++){
            DoorObject d = (DoorObject)getSceneManager().getObjectByName(affectedDoors.get(i));
            if(d != null){
                d.setState(doorState);
            }
        }
    }

    // Spawn points activate only when others are not blocked
    // Spawn group allows for multiple simultaneous spawns to occur
    public void addSpawnGroup(String... name){
        for (int i = 0; i < name.length; i++) {
            spawnGroup.add(name[i]);
        }
    }
    private Array<String> spawnGroup = new Array<String>();

    // Ready to spawn objects are all active spawn points, activated randomly
    static private Array<SpawnObject> readyToSpawnObjects = new Array<SpawnObject>();

    //Try to spawn new objects
    //Static method called outside object update loop
    public static void updateAndSpawn(){
        if(readyToSpawnObjects.size > 0) {
            int r = 0;
            for (int i = 0; i < 10; i++) {
                r = random.nextInt(readyToSpawnObjects.size);

//                String str = "Ready to spawn: ";
//                for (int j = 0; j < readyToSpawnObjects.size; j++) {
//                    str = str+" "+readyToSpawnObjects.get(j).getName();
//                }

                SpawnObject sp = readyToSpawnObjects.get(r);

//                str += " sp:"+sp.getName();
//                str += " block:"+sp.blockingSpawnObjects.size;
//                if(sp.blockingSpawnObjects.size > 0){
//                    str += "(";
//                    for (int j = 0; j < sp.blockingSpawnObjects.size; j++) {
//                        str += " "+sp.blockingSpawnObjects.get(j).getName();
//                    }
//                    str+=")";
//                }
//                Error.logToFile(str, "spawnTest.txt");

                if (sp.tryToSpawn()) {
                    readyToSpawnObjects.removeValue(sp, true);
                    break;
                }
            }
        }
    }

    public static void resetReadyToSpawn(){
        readyToSpawnObjects.clear();
    }

    public static void freeReadyToSpawnObjects(){
        if(readyToSpawnObjects.size > 0) {
            for (int i = 0; i < readyToSpawnObjects.size; i++) {
                SpawnObject sp = readyToSpawnObjects.get(i);
                sp.setFree();
            }
        }
        resetReadyToSpawn();
    }

    private void addReadyToSpawn() {
        readyToSpawnObjects.add(this);
    }
    private void removeReadyToSpawn(){
        readyToSpawnObjects.removeValue(this, true);
    }

    private Array<SpawnObject> blockingSpawnObjects = new Array<SpawnObject>();

    public void onSpawnEvent(SpawnEvent e){

        if(e.action != null) {
            switch (e.action) {
                case ADD_ACTOR:
                    if(e.actorType != null)addActorType(e.actorType, e.actorWeight);
                    break;
                case REMOVE_ACTOR:
                    if(e.actorType != null)removeActorType(e.actorType);
                    break;
                case SET_ACTOR_WEIGHT:
                    if(e.actorType != null)setActorWeight(e.actorType, e.actorWeight);
                    break;
                case SET_ENABLED:
                    enabled = e.enabled;
                    break;

            }
        }else if(e.state != null) {
            if (e.senderObject != null && e.senderObject.getClass() == SpawnObject.class) {
                if (e.state == State.FREE) {
                    blockingSpawnObjects.removeValue((SpawnObject) e.senderObject, true);
                } else {
                    //Error.log(getName()+" contains "+e.senderObject.getName()+" = "+spawnGroup.contains(e.senderObject.getName(), false));
                    if (!spawnGroup.contains(e.senderObject.getName(), false))
                        blockingSpawnObjects.add((SpawnObject) e.senderObject);
                }
            }
        }
    }

    //Tries to activate based on group conditions
    private boolean tryToSpawn(){
        if(blockingSpawnObjects.size == 0){
            spawn();
            return true;
        }
        return false;
    }



}
