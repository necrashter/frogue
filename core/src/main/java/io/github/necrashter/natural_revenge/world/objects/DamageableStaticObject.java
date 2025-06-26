package io.github.necrashter.natural_revenge.world.objects;

import com.badlogic.gdx.math.Vector3;
import io.github.necrashter.natural_revenge.AssetManager2;
import io.github.necrashter.natural_revenge.world.Damageable;

public class DamageableStaticObject extends StaticGameObject implements Damageable {
    public static final Vector3 tempPos = new Vector3();
    public float health = 80.0f;

    private boolean falling = false;
    private float fallTime = 0.0f;

    public DamageableStaticObject(AssetManager2.GameObjectTemplate template) {
        super(template);
    }

    @Override
    public boolean takeDamage(float amount, DamageAgent agent, DamageSource source) {
        if (falling) return false;
        if ((health -= amount) <= 0.0f) {
            setRequiresUpdates(true);
            falling = true;
            return true;
        }
        return false;
    }

    @Override
    public void update(float delta) {
        model.transform.translate(0, -5.0f * delta, 0);
        if ((fallTime += delta) >= 1.0f) {
            remove();
        } else {
            octreeNode.updateObject(this);
        }
    }
}
