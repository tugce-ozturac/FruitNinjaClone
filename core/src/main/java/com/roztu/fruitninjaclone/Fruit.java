package com.roztu.fruitninjaclone;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

public class Fruit {
    public static float radius = 60f;

    public enum Type {
        REGULAR, EXTRA, BOMB, LIFE
    }

    private static final float GRAVITY = 0.2f;   // yerçekimi katsayısı
    private static final float FRICTION = 5f;    // yatay yavaşlama

    public Type type;
    private Vector2 pos, velocity;
    public boolean living = true;

    // EXTRA meyveler için id
    public int extraId = -1;

    public Fruit(Vector2 pos, Vector2 velocity) {
        this.pos = pos;
        this.velocity = velocity;
        this.type = Type.REGULAR;
    }

    public boolean clicked(Vector2 click) {
        return pos.dst2(click) <= radius * radius;
    }

    public void update(float dt) {
        // Yerçekimi ve sürtünme etkisi
        velocity.y -= dt * (Gdx.graphics.getHeight() * GRAVITY);
        velocity.x -= dt * Math.signum(velocity.x) * FRICTION;

        pos.mulAdd(velocity, dt);
    }

    public boolean outOfScreen() {
        return (pos.y < -2f * radius || pos.x < -2f * radius || pos.x > Gdx.graphics.getWidth() + 2f * radius);
    }

    public Vector2 getPos() {
        return pos;
    }
}
