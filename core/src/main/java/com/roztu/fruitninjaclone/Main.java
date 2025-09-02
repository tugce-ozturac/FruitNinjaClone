package com.roztu.fruitninjaclone;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Random;

public class Main extends ApplicationAdapter implements InputProcessor {
    private SpriteBatch batch;
    private Texture background;
    private Texture banana, greengrepe, apple, lemon, bomb, coins, strawberry , blackCherry ;
    private BitmapFont font;
    private FreeTypeFontGenerator fontGen;
    private Random random = new Random();

    private Array<Fruit> fruitArray = new Array<>();

    private float genCounter = 0;
    private final float startGenSpeed = 1.1f;
    private float genSpeed = startGenSpeed;

    private int lives = 4;
    private int score = 0;
    private double gameOverTime = -1.0;
    private double startTime;

    @Override
    public void create() {
        batch = new SpriteBatch();

        // görseller
        background = new Texture("backround.jpg");
        banana = new Texture("banana.png");
        greengrepe = new Texture("green-grape.png");
        apple = new Texture("red-apple.png");
        lemon = new Texture("lemon.png");
        bomb = new Texture("bomb.png");
        coins = new Texture("coins.png");
        blackCherry = new Texture("black-cherry.png");
        strawberry = new Texture("strawberry.png");

        Fruit.radius = Math.max(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) / 20f;
        Gdx.input.setInputProcessor(this);

        // font
        fontGen = new FreeTypeFontGenerator(Gdx.files.internal("robotobold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter params = new FreeTypeFontGenerator.FreeTypeFontParameter();
        params.color = Color.WHITE;
        params.size = 40;
        params.characters = "0123456789 CutoplayScre : .+-";
        font = fontGen.generateFont(params);

        // başlangıç zamanı
        startTime = TimeUtils.millis() / 1000.0;
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        float elapsedTime = (float) ((TimeUtils.millis() / 1000.0) - startTime);

        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        if (lives <= 0 && gameOverTime == -1.0) {
            gameOverTime = TimeUtils.millis() / 1000.0;
        }

        if (lives > 0) {
            // oyun modu
            genSpeed = Math.max(0.5f, genSpeed - deltaTime * 0.015f);

            if (genCounter <= 0f) {
                genCounter = genSpeed;
                addItem(elapsedTime);
            } else {
                genCounter -= deltaTime;
            }

            // canları ekrana çiz
            for (int i = 0; i < Math.min(lives, 5); i++) {
                batch.draw(apple, i * 30f + 20f, Gdx.graphics.getHeight() - 25f, 25f, 25f);
            }


            // meyveleri güncelle ve çiz
            for (Fruit fruit : fruitArray) {
                fruit.update(deltaTime);
                switch (fruit.type) {
                    case REGULAR:
                        batch.draw(apple, fruit.getPos().x, fruit.getPos().y, Fruit.radius, Fruit.radius);
                        break;
                    case EXTRA:
                        switch (fruit.extraId) {
                            case 0: batch.draw(lemon, fruit.getPos().x, fruit.getPos().y, Fruit.radius, Fruit.radius); break;
                            case 1: batch.draw(greengrepe, fruit.getPos().x, fruit.getPos().y, Fruit.radius, Fruit.radius); break;
                            case 2: batch.draw(strawberry, fruit.getPos().x, fruit.getPos().y, Fruit.radius, Fruit.radius); break;
                            case 3: batch.draw(blackCherry, fruit.getPos().x, fruit.getPos().y, Fruit.radius, Fruit.radius); break;
                            case 4: batch.draw(banana, fruit.getPos().x, fruit.getPos().y, Fruit.radius, Fruit.radius); break;
                        }
                        break;
                    case BOMB:
                        batch.draw(bomb, fruit.getPos().x, fruit.getPos().y, Fruit.radius, Fruit.radius);
                        break;
                    case LIFE:
                        batch.draw(coins, fruit.getPos().x, fruit.getPos().y, Fruit.radius, Fruit.radius);
                        break;
                }
            }

            // ekrandan çıkanları temizle
            Array<Fruit> toRemove = new Array<>();
            boolean lostLife = false;
            for (Fruit fruit : fruitArray) {
                if (fruit.outOfScreen()) {
                    toRemove.add(fruit);
                    if (fruit.living && fruit.type == Fruit.Type.REGULAR) {
                        lives--;
                        lostLife = true;
                    }
                }
            }
            if (lostLife) {
                for (Fruit f : fruitArray) {
                    f.living = false;
                }
            }
            for (Fruit f : toRemove) {
                fruitArray.removeValue(f, true);
            }
        }

        // skor
        font.draw(batch, "Score: " + score, 30, 45);

        // game over yazısı
        if (lives <= 0) {
            font.getData().setScale(2f);
            font.draw(batch, "Cut to Play!", Gdx.graphics.getWidth() * 0.35f, Gdx.graphics.getHeight() * 0.5f);
            font.getData().setScale(1f);
        }

        batch.end();
    }

    private void addItem(float elapsedTime) {
        if (fruitArray.size >= 7) return; // max 7 meyve ekranda olsun

        float pos = random.nextFloat() * Gdx.graphics.getWidth();
        Fruit item = new Fruit(
            new Vector2(pos, -Fruit.radius),
            new Vector2((Gdx.graphics.getWidth() * 0.5f - pos) * 0.3f + (random.nextFloat() - 0.5f),
                Gdx.graphics.getHeight() * 0.5f)
        );

        // Zorluk ilerledikçe bombaların ihtimali artsın
        float difficulty = Math.min(1f, elapsedTime / 60f); // 1 dakikada max zorluk
        float r = random.nextFloat();

        if (r < 0.05f) {
            item.type = Fruit.Type.LIFE;   // %5
        } else if (r < 0.25f) {
            item.type = Fruit.Type.EXTRA;  // %20
            item.extraId = random.nextInt(5); // 0-4 arası ekstra meyve seç
        } else if (r < 0.25f + (0.1f + 0.2f * difficulty)) {
            item.type = Fruit.Type.BOMB;   // %10 → %30 arası
        } else {
            item.type = Fruit.Type.REGULAR; // geri kalan %45–65
        }

        fruitArray.add(item);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        fontGen.dispose();
        background.dispose();
        banana.dispose();
        greengrepe.dispose();
        apple.dispose();
        lemon.dispose();
        bomb.dispose();
        coins.dispose();
        strawberry.dispose();
        blackCherry.dispose();
    }

    // InputProcessor metodları
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (lives <= 0) {
            gameOverTime = -1.0;
            score = 0;
            lives = 4;
            genSpeed = startGenSpeed;
            fruitArray.clear();
        } else {
            Array<Fruit> toRemove = new Array<>();
            Vector2 pos = new Vector2(screenX, Gdx.graphics.getHeight() - screenY);
            int plusScore = 0;

            for (Fruit f : fruitArray) {
                if (f.clicked(pos)) {
                    toRemove.add(f);
                    switch (f.type) {
                        case REGULAR:
                            plusScore++;
                            break;
                        case EXTRA:
                            plusScore += 2;
                            score++;
                            break;
                        case BOMB:
                            lives--;
                            break;
                        case LIFE:
                            lives++;
                            if (lives > 5) lives = 5; // maksimum 5 can
                            break;

                    }
                }
            }
            score += plusScore * plusScore;
            for (Fruit f : toRemove) {
                fruitArray.removeValue(f, true);
            }
        }
        return false;
    }

    @Override public boolean keyDown(int keycode) { return false; }
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
}
