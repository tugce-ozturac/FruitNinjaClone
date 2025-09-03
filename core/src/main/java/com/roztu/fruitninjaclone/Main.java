package com.roztu.fruitninjaclone;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Random;

public class Main extends ApplicationAdapter implements InputProcessor {
    private SpriteBatch batch;
    private Texture background;
    Texture banana, greengrepe, apple, lemon, bomb, coins;
    BitmapFont font;
    FreeTypeFontGenerator fontGen;
    Random random = new Random();
    Array<Fruit> fruitArray = new Array<Fruit>();

    // Her Fruit için hangi görselle çizileceğini tutar
    private ObjectMap<Fruit, TextureRegion> fruitSkins = new ObjectMap<Fruit, TextureRegion>();

    // Trail (neon blade)
    private Array<TrailPoint> bladeTrail = new Array<TrailPoint>();
    private Texture trailTexRaw, glowTexRaw;
    private TextureRegion trailTex, glowTex;
    private int MAX_TRAIL_SIZE = 40;
    private float TRAIL_BASE_WIDTH;
    private final float TRAIL_FADE_SPEED = 2.2f;
    private final float MIN_POINT_DISTANCE = 8f;

    // Parçalanma için dilimler
    private Array<Slice> slices = new Array<Slice>();

    // Particle efektleri
    private Array<Particle> particles = new Array<Particle>();

    float genCounter = 0;
    private final float startGenSpeed = 1.1f;
    float genSpeed = startGenSpeed;

    int lives = 4;
    int score = 0;
    private double currentTime;
    private double gameOverTime = -1.0;

    // Background music
    private Music bgMusic;

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

        // music (put a file named "music.mp3" into assets folder)
        try {
            bgMusic = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
            bgMusic.setLooping(true);
            bgMusic.setVolume(0.6f); // isteğe göre değiştir
            bgMusic.play();
        } catch (Exception e) {
            Gdx.app.log("Main", "Background music failed to load: " + e.getMessage());
        }

        Fruit.radius = Math.max(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) / 20f;
        Gdx.input.setInputProcessor(this);

        fontGen = new FreeTypeFontGenerator(Gdx.files.internal("robotobold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter params = new FreeTypeFontGenerator.FreeTypeFontParameter();
        params.color = Color.WHITE;
        params.size = 40;
        params.characters = "0123456789 CutoplayScre : .+-*";
        font = fontGen.generateFont(params);

        // trail texture (1x1 white)
        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(1f, 1f, 1f, 1f);
        px.fill();
        trailTexRaw = new Texture(px);
        trailTex = new TextureRegion(trailTexRaw);
        px.dispose();

        // glow texture (radial alpha)
        int gsize = 64;
        Pixmap glow = new Pixmap(gsize, gsize, Pixmap.Format.RGBA8888);
        for (int x = 0; x < gsize; x++) {
            for (int y = 0; y < gsize; y++) {
                float dx = x - gsize / 2f + 0.5f;
                float dy = y - gsize / 2f + 0.5f;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float max = gsize / 2f;
                float a = Math.max(0f, 1f - dist / max);
                a = a * a;
                int c = Color.rgba8888(1f, 1f, 1f, a);
                glow.drawPixel(x, y, c);
            }
        }
        glowTexRaw = new Texture(glow);
        glowTex = new TextureRegion(glowTexRaw);
        glow.dispose();

        TRAIL_BASE_WIDTH = Math.max(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) / 60f;

        // İlk framedeki büyük delta'yı önlemek için başlat
        currentTime = TimeUtils.millis() / 1000.0;
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        double newTime = TimeUtils.millis() / 1000.0;
        double frameTime = Math.min(newTime - currentTime, 0.3);
        float deltaTime = (float) frameTime;
        currentTime = newTime;

        // update and fade blade trail points
        for (int i = bladeTrail.size - 1; i >= 0; i--) {
            TrailPoint tp = bladeTrail.get(i);
            tp.update(deltaTime);
            if (tp.isDead()) bladeTrail.removeIndex(i);
        }

        if (lives <= 0 && gameOverTime == 0f) {
            gameOverTime = currentTime;
        }

        if (lives > 0) {
            genSpeed -= deltaTime * 0.015f;

            if (genCounter <= 0f) {
                genCounter = genSpeed;
                addItem();
            } else {
                genCounter -= deltaTime;
            }

            // Can ikonları (küçük elma)
            for (int i = 0; i < lives; i++) {
                batch.draw(apple, i * 30f + 20f, Gdx.graphics.getHeight() - 25f, 25f, 25f);
            }

            // Meyveleri güncelle/çiz
            for (Fruit fruit : fruitArray) {
                fruit.update(deltaTime);
                TextureRegion skin = fruitSkins.get(fruit);
                if (skin == null) {
                    // güvenlik için; olmaması gerekir
                    skin = new TextureRegion(apple);
                }
                batch.draw(skin, fruit.getPos().x, fruit.getPos().y, Fruit.radius, Fruit.radius);
            }

            // Ekran dışına çıkanlar / can düşürme
            boolean holdlives = false;
            Array<Fruit> toRemove = new Array<Fruit>();
            for (Fruit fruit : fruitArray) {
                if (fruit.outOfScreen()) {
                    toRemove.add(fruit);
                    if (fruit.living && fruit.type == Fruit.Type.REGULAR) {
                        lives--;
                        holdlives = true;
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
                fruitSkins.remove(f);
            }
        }

        // Slice update & draw
        Array<Slice> deadSlices = new Array<Slice>();
        for (Slice s : slices) {
            s.update(deltaTime);
            s.draw(batch);
            if (s.life <= 0) deadSlices.add(s);
        }
        slices.removeAll(deadSlices, true);

        // Particle update & draw
        Array<Particle> toRemoveParticles = new Array<>();
        for (Particle p : particles) {
            p.update(deltaTime);
            p.draw(batch);
            if (p.life <= 0) toRemoveParticles.add(p);
        }
        particles.removeAll(toRemoveParticles, true);

        batch.end();

        // Blade trail çizimi (neon)
        drawBladeTrail();

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "Score: " + score, 30, 45);
        if (lives <= 0) {
            font.draw(batch, "Cut to play", Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.5f);
        }
        batch.end();
    }

    private void drawBladeTrail() {
        if (bladeTrail.size < 1) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);

        // --- Glow pass (additive, degrade) ---
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.begin();
        for (int i = 1; i < bladeTrail.size; i++) {
            TrailPoint p1 = bladeTrail.get(i - 1);
            TrailPoint p2 = bladeTrail.get(i);

            Vector2 d = new Vector2(p2.pos).sub(p1.pos);
            float len = d.len();
            float angle = d.angleDeg();

            float lifeStart = p1.life;
            float lifeEnd = p2.life;

            float alpha = (lifeStart + lifeEnd) * 0.5f * 0.2f; // glow daha saydam
            float width = TRAIL_BASE_WIDTH * 1.5f * ((lifeStart + lifeEnd) * 0.5f); // glow geniş

            // kahverengi degrade
            float life = (lifeStart + lifeEnd) * 0.5f;
            float r = 0.36f * life + 0.45f * (1 - life);
            float g = 0.25f * life + 0.18f * (1 - life);
            float b = 0.20f * life + 0.15f * (1 - life);

            batch.setColor(r, g, b, alpha);
            batch.draw(trailTex, p1.pos.x, p1.pos.y - width / 2f, 0, width / 2f, len, width, 1f, 1f, angle);

            float cap = width * 1.5f;
            batch.setColor(r, g, b, alpha * 0.7f);
            batch.draw(glowTex, p1.pos.x - cap / 2f, p1.pos.y - cap / 2f, cap, cap);
        }
        batch.setColor(Color.WHITE);
        batch.end();

        // --- Core pass (ince ana çizgi) ---
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.begin();
        for (int i = 1; i < bladeTrail.size; i++) {
            TrailPoint p1 = bladeTrail.get(i - 1);
            TrailPoint p2 = bladeTrail.get(i);

            Vector2 d = new Vector2(p2.pos).sub(p1.pos);
            float len = d.len();
            float angle = d.angleDeg();

            float life = (p1.life + p2.life) * 0.5f;
            float width = TRAIL_BASE_WIDTH * 0.4f * life; // ince core

            float r = 0.36f * life + 0.45f * (1 - life);
            float g = 0.25f * life + 0.18f * (1 - life);
            float b = 0.20f * life + 0.15f * (1 - life);
            batch.setColor(r, g, b, life);

            batch.draw(trailTex, p1.pos.x, p1.pos.y - width / 2f, 0, width / 2f, len, width, 1f, 1f, angle);
        }
        batch.setColor(Color.WHITE);
        batch.end();
    }

    private TextureRegion randRegularSkin() {
        int pick = random.nextInt(4);
        switch (pick) {
            case 0: return new TextureRegion(banana);
            case 1: return new TextureRegion(greengrepe);
            case 2: return new TextureRegion(apple);
            default: return new TextureRegion(lemon);
        }
    }

    private TextureRegion randExtraSkin() {
        // EXTRA için de çeşitlilik
        int pick = random.nextInt(4);
        switch (pick) {
            case 0: return new TextureRegion(banana);
            case 1: return new TextureRegion(greengrepe);
            case 2: return new TextureRegion(apple);
            default: return new TextureRegion(lemon);
        }
    }

    private void addItem() {
        // Limit: ekranda en fazla 8 item olsun
        if (fruitArray.size >= 8) return;

        float pos = random.nextFloat() * Gdx.graphics.getWidth();
        Fruit item = new Fruit(new Vector2(pos, -Fruit.radius),
            new Vector2((Gdx.graphics.getWidth() * 0.5f - pos) * 0.3f + (random.nextFloat() - 0.5f),
                Gdx.graphics.getHeight() * 0.5f));

        float type = random.nextFloat();
        TextureRegion skin;

        if (type > 0.96f) {
            item.type = Fruit.Type.LIFE;
            skin = new TextureRegion(coins);
        } else if (type > 0.88f) {
            item.type = Fruit.Type.EXTRA;
            skin = randExtraSkin();
        } else if (type > 0.78f) {
            item.type = Fruit.Type.BOMB;
            skin = new TextureRegion(bomb);
        } else {
            item.type = Fruit.Type.REGULAR;
            skin = randRegularSkin();
        }

        fruitArray.add(item);
        fruitSkins.put(item, skin);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        fontGen.dispose();
        if (trailTexRaw != null) trailTexRaw.dispose();
        if (glowTexRaw != null) glowTexRaw.dispose();
        background.dispose();
        banana.dispose();
        greengrepe.dispose();
        apple.dispose();
        lemon.dispose();
        bomb.dispose();
        coins.dispose();
        if (bgMusic != null) {
            bgMusic.stop();
            bgMusic.dispose();
        }
    }

    @Override public boolean keyDown(int keycode) { return false; }
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Vector2 pos = new Vector2(screenX, Gdx.graphics.getHeight() - screenY);

        if (bladeTrail.size == 0) {
            bladeTrail.add(new TrailPoint(pos.cpy()));
        } else {
            TrailPoint last = bladeTrail.peek();
            if (last.pos.dst(pos) > MIN_POINT_DISTANCE) {
                bladeTrail.add(new TrailPoint(pos.cpy()));
                if (bladeTrail.size > MAX_TRAIL_SIZE) bladeTrail.removeIndex(0);
            } else {
                last.pos.set(pos);
                last.life = 1f;
            }
        }

        if (lives <= 0 && currentTime - gameOverTime > 2f) {
            gameOverTime = 0f;
            score = 0;
            lives = 4;
            genSpeed = startGenSpeed;
            fruitArray.clear();
            fruitSkins.clear();
            slices.clear();
            particles.clear();
        } else {
            Array<Fruit> toRemove = new Array<Fruit>();
            int plusScore = 0;
            for (Fruit f : fruitArray) {
                if (f.clicked(pos)) {
                    toRemove.add(f);

                    TextureRegion skin = fruitSkins.get(f);
                    if (skin == null) skin = new TextureRegion(apple);

                    // Basit parçalanma: ikiye böl, iki yana savur
                    spawnSlices(f.getPos().cpy(), skin);

                    switch (f.type) {
                        case REGULAR:
                            plusScore++;
                            spawnParticles(f.getPos(), Color.RED);
                            break;
                        case EXTRA:
                            plusScore += 2;
                            score++;
                            spawnParticles(f.getPos(), Color.YELLOW);
                            break;
                        case BOMB:
                            lives--;
                            spawnParticles(f.getPos(), Color.BLACK);
                            break;
                        case LIFE:
                            lives++;
                            spawnParticles(f.getPos(), Color.GREEN);
                            break;
                    }
                }
            }
            score += plusScore * plusScore;
            for (Fruit f : toRemove) {
                fruitArray.removeValue(f, true);
                fruitSkins.remove(f);
            }
        }
        return false;
    }

    private void spawnParticles(Vector2 pos, Color color) {
        for (int i = 0; i < 8; i++) {
            particles.add(new Particle(pos.cpy(), color));
        }
    }

    private void spawnSlices(Vector2 pos, TextureRegion whole) {
        // TextureRegion'ı iki parçaya böl
        int rx = whole.getRegionX();
        int ry = whole.getRegionY();
        int rw = whole.getRegionWidth();
        int rh = whole.getRegionHeight();

        TextureRegion left = new TextureRegion(whole.getTexture(), rx, ry, rw / 2, rh);
        TextureRegion right = new TextureRegion(whole.getTexture(), rx + rw / 2, ry, rw - rw / 2, rh);

        // iki parça için hafif farklı hız/rotasyon ver
        float base = 220f;
        Slice sLeft = new Slice(left, pos.cpy().add(-Fruit.radius * 0.1f, 0),
            new Vector2(-(base + random.nextFloat() * 60f), base * 0.6f + random.nextFloat() * 60f),
            -180f - random.nextFloat() * 90f);

        Slice sRight = new Slice(right, pos.cpy().add(Fruit.radius * 0.1f, 0),
            new Vector2((base + random.nextFloat() * 60f), base * 0.6f + random.nextFloat() * 60f),
            180f + random.nextFloat() * 90f);

        slices.add(sLeft);
        slices.add(sRight);
    }

    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }

    // ==== İç sınıflar ====

    class TrailPoint {
        Vector2 pos;
        float life = 1f;
        TrailPoint(Vector2 pos) { this.pos = pos; this.life = 1f; }
        void update(float dt) { life -= dt * TRAIL_FADE_SPEED; if (life < 0) life = 0; }
        boolean isDead() { return life <= 0f; }
    }

    class Particle {
        Vector2 pos, vel;
        float life = 1f;
        Color color;

        Particle(Vector2 pos, Color color) {
            this.pos = pos;
            this.color = color;
            this.vel = new Vector2((random.nextFloat() - 0.5f) * 200, random.nextFloat() * 200);
        }

        void update(float dt) {
            pos.add(vel.x * dt, vel.y * dt);
            vel.y -= 200 * dt;
            life -= dt;
        }

        void draw(SpriteBatch batch) {
            font.setColor(color.r, color.g, color.b, life);
            font.draw(batch, "*", pos.x, pos.y);
            font.setColor(Color.WHITE);
        }
    }

    class Slice {
        TextureRegion region;
        Vector2 pos;
        Vector2 vel;
        float rotation;     // derece
        float angVel;       // derece/sn
        float life = 1.1f;  // biraz uzun yaşasın

        Slice(TextureRegion region, Vector2 pos, Vector2 vel, float angVel) {
            this.region = region;
            this.pos = pos;
            this.vel = vel;
            this.angVel = angVel;
            this.rotation = 0f;
        }

        void update(float dt) {
            pos.add(vel.x * dt, vel.y * dt);
            vel.y -= 380f * dt;     // yerçekimi
            rotation += angVel * dt;
            life -= dt * 0.9f;      // sönme
        }

        void draw(SpriteBatch batch) {
            if (life <= 0) return;
            float w = Fruit.radius * 0.5f; // yarım genişlik
            float h = Fruit.radius;
            batch.setColor(1f, 1f, 1f, Math.max(0f, life));
            batch.draw(region,
                pos.x, pos.y,
                w * 0.5f, h * 0.5f,     // origin (orta)
                w, h,
                1f, 1f,
                rotation);
            batch.setColor(Color.WHITE);
        }
    }
}
