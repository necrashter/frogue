package io.github.necrashter.natural_revenge.world.objects;

import com.badlogic.gdx.math.MathUtils;
import io.github.necrashter.natural_revenge.AssetManager2;
import io.github.necrashter.natural_revenge.Main;

public class TreeObject extends DamageableStaticObject {

    public TreeObject(AssetManager2.GameObjectTemplate template) {
        super(template);
    }

    @Override
    public boolean takeDamage(float amount, DamageAgent agent, DamageSource source) {
        if (source == DamageSource.Melee) {
            amount *= 3;
            world.playSound(
                    Main.assets.woodCuts[MathUtils.random.nextInt(Main.assets.woodCuts.length)],
                    model.transform.getTranslation(tempPos)
            );
        }
        return super.takeDamage(amount, agent, source);
    }

    @Override
    public void remove() {
        super.remove();
    }
}
