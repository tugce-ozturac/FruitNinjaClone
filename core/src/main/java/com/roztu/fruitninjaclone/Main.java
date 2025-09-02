package com.roztu.fruitninjaclone;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Random;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter implements InputProcessor {
    private SpriteBatch batch;
    private Texture background;
    Texture banana;
    Texture greengrepe;
    Texture apple ;
    Texture lemon ;
    Texture bomb;
    Texture coins;
    BitmapFont font;
    FreeTypeFontGenerator fontGen ;
    Random random = new Random();
    Array<Fruit>fruitArray =new Array<Fruit>();


    float genCounter = 0;
    private final float startGenSpeed = 1.1f;
    float genSpeed = startGenSpeed;


    int lives = 4;
    int score = 0;
    private double currentTime;
    private double gameOverTime = -1.0f;







    @Override
    public void create() {
        batch = new SpriteBatch();
        background = new Texture("backround.jpg");
        banana = new Texture("banana.png");
        greengrepe = new Texture("green-grape.png");
        apple = new Texture("red-apple.png");
        lemon = new Texture("lemon.png");
        bomb = new Texture("bomb.png");
        coins = new Texture("coins.png");

        Fruit.radius = Math.max(Gdx.graphics.getHeight(),Gdx.graphics.getWidth())/20f;
        Gdx.input.setInputProcessor(this);

        fontGen = new FreeTypeFontGenerator(Gdx.files.internal("robotobold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter params = new FreeTypeFontGenerator.FreeTypeFontParameter();
        params.color = Color.WHITE ;
        params.size = 40;
        params.characters = "0123456789 CutoplayScre : .+-";
        font = fontGen.generateFont(params);
    }

    @Override
    public void render() {

        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        double newTime = TimeUtils.millis() / 1000.0;
        System.out.println("newtime: " + newTime);
        double frameTime = Math.min(newTime - currentTime, 0.3);
        System.out.println("frametime: " + frameTime);
        float deltaTime = (float) frameTime;
        System.out.println("deltatime: " + deltaTime);
        currentTime = newTime;

        if (lives <= 0 && gameOverTime == 0f) {
            //end game
            gameOverTime = currentTime;
        }

        if (lives > 0) {
            //game  mode

            genSpeed -= deltaTime * 0.015f;

            System.out.println("genspeed: " + genSpeed);
            System.out.println("gencounter: " + genCounter);

            if (genCounter <= 0f) {
                genCounter = genSpeed;
                addItem();
            } else {
                genCounter -= deltaTime;
            }


            for (int i = 0; i < lives; i++) {
                batch.draw(apple, i * 30f + 20f, Gdx.graphics.getHeight() - 25f, 25f, 25f);
            }
            for (Fruit fruit : fruitArray) {
                fruit.update(deltaTime);
                switch (fruit.type) {
                    case REGULAR:
                        batch.draw(apple, fruit.getPos().x, fruit.getPos().y, Fruit.radius, Fruit.radius);
                        break;
                    case EXTRA:
                        batch.draw(lemon, fruit.getPos().x, fruit.getPos().y, Fruit.radius, Fruit.radius);
                        break;
                    case BOMB:
                        batch.draw(bomb, fruit.getPos().x, fruit.getPos().y, Fruit.radius, Fruit.radius);
                        break;
                    case LIFE:
                        batch.draw(coins, fruit.getPos().x, fruit.getPos().y, Fruit.radius, Fruit.radius);
                        break;
                }

            }

            //remove out of screen
            boolean holdlives = false;
            Array<Fruit> toRemove = new Array<Fruit>();
            for (Fruit fruit : fruitArray) {
                if (fruit.outOfScreen()) {
                    toRemove.add(fruit);
                    if (fruit.living && fruit.type == Fruit.Type.REGULAR) {
                        lives--;
                        holdlives = true;
                        //holdlives=true;
                        break;
                    }
                }
            }
            if (holdlives) {
                for (Fruit f : fruitArray) {
                    f.living = false;
                }
            }
            for (Fruit f : toRemove) {
                fruitArray.removeValue(f, true);
            }


        }
        font.draw(batch,"Score: "+score,30,45);
        if(lives <= 0){
            font.draw(batch,"Cut to play",Gdx.graphics.getWidth()*0.5f,Gdx.graphics.getHeight()*0.5f);
        }

        batch.end();
    }

    private void addItem(){
        float pos = random.nextFloat()*Gdx.graphics.getWidth();
       // Fruit item = new Fruit(new Vector2(pos,pos),new Vector2(pos,pos));
        Fruit item = new Fruit(new Vector2(pos,-Fruit.radius),new Vector2((Gdx.graphics.getWidth()*0.5f - pos) * 0.3f+(random.nextFloat() - 0.5f),Gdx.graphics.getHeight()*0.5f));
        float type = random.nextFloat();
        if(type>0.96){
            item.type = Fruit.Type.LIFE;
        } else if(type>0.88){
            item.type = Fruit.Type.EXTRA;
        } else if(type>0.78){
            item.type = Fruit.Type.BOMB;
        }
        fruitArray.add(item);

    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        fontGen.dispose();

    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if(lives<=0 && currentTime - gameOverTime > 2f){//menu mode
            gameOverTime = 0f;
            score=0;
            lives=4;//restart game
            genSpeed = startGenSpeed;
            fruitArray.clear();
        }else{

            //game mode
            Array<Fruit> toRemove = new Array<Fruit>();
            Vector2 pos = new Vector2(screenX,Gdx.graphics.getHeight()-screenY);
            int plusScore = 0;
            for(Fruit f : fruitArray){
                System.out.println("getHeight - y: " + screenY);
                System.out.println("getHeight - y: " + (Gdx.graphics.getHeight()-screenY));
                System.out.println("getHeight - y: " + f.getPos());
                System.out.println("distance: " + pos.dst2(f.pos));
                System.out.println("distance: " + f.clicked(pos));
                System.out.println("distance: " + Fruit.radius * Fruit.radius + 1);
                if(f.clicked(pos)){
                    toRemove.add(f);
                    switch(f.type){
                        case REGULAR:
                            plusScore++;
                            break;
                        case EXTRA:
                            plusScore+=2;
                            score++;
                            break;
                        case BOMB:
                            lives--;
                            break;
                        case LIFE:
                            lives++;
                            break;
                    }
                }
            }
            score += plusScore*plusScore;
            for(Fruit f : toRemove){
                fruitArray.removeValue(f, true);
            }
        }
        return false;


    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
