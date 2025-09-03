// Fruit.java
package com.roztu.fruitninjaclone;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

public class Fruit {
    public static float radius = 60f;
    public enum Type { REGULAR, EXTRA, BOMB, LIFE }
    Type type;
    Vector2 pos, velocity;
    public boolean living = true;

    public Fruit(Vector2 pos, Vector2 velocity) {
        this.pos = pos; this.velocity = velocity; type = Type.REGULAR;
    }

    public boolean clicked(Vector2 click) {
        return pos.dst2(click) <= radius * radius + 1;
    }

    public void update(float dt) {
        velocity.y -= dt * (Gdx.graphics.getHeight() * 0.2f);
        velocity.x -= dt * Math.signum(velocity.x) * 5f;
        pos.mulAdd(velocity, dt);
    }

    public boolean outOfScreen() {
        return pos.y < -2f * radius;
    }

    public Vector2 getPos() { return pos; }
}
