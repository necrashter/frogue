package io.github.necrashter.natural_revenge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import io.github.necrashter.natural_revenge.world.geom.BoxShape;
import io.github.necrashter.natural_revenge.world.geom.Shape;
import io.github.necrashter.natural_revenge.world.geom.SphereShape;
import io.github.necrashter.natural_revenge.world.objects.HealthPickupObject;
import io.github.necrashter.natural_revenge.world.objects.WeaponPickupObject;
import io.github.necrashter.natural_revenge.world.objects.TreeObject;
import io.github.necrashter.natural_revenge.world.player.Firearm;
import io.github.necrashter.natural_revenge.world.player.Player;

public class AssetManager2 extends AssetManager {
    public static final BoundingBox tempBox = new BoundingBox();
    public Model treeModel;
    public Model npcModel;
    public Model frogModel;
    public Model frogParticleModel;
    public Model weaponsModel;
    public Model medkitModel;

    public Texture muzzleFlash = new Texture(Gdx.files.internal("textures/muzzle.png"));
    public TextureRegion muzzleFlashRegion = new TextureRegion(muzzleFlash);
    public Texture bulletTrace = new Texture(Gdx.files.internal("textures/trace.png"));
    public Texture bottomGrad = new Texture(Gdx.files.internal("textures/bottomGrad.png"));
    public Texture hurtOverlay = new Texture(Gdx.files.internal("textures/hurtOverlay.png"));

    public Sound[] swooshes = new Sound[] {
            Gdx.audio.newSound(Gdx.files.internal("sounds/swoosh0.ogg")),
            Gdx.audio.newSound(Gdx.files.internal("sounds/swoosh1.ogg")),
            Gdx.audio.newSound(Gdx.files.internal("sounds/swoosh2.ogg")),
    };

    public Sound[] slowSwooshes = new Sound[] {
            Gdx.audio.newSound(Gdx.files.internal("sounds/swoosh-slow0.ogg")),
            Gdx.audio.newSound(Gdx.files.internal("sounds/swoosh-slow1.ogg")),
            Gdx.audio.newSound(Gdx.files.internal("sounds/swoosh-slow2.ogg")),
    };

    public Sound enemyPistol = Gdx.audio.newSound(Gdx.files.internal("sounds/enemy-pistol.ogg"));
    public Sound enemyRifle = Gdx.audio.newSound(Gdx.files.internal("sounds/enemy-rifle.ogg"));
    public Sound gunEmpty = Gdx.audio.newSound(Gdx.files.internal("sounds/gun-empty.ogg"));

    public Sound voiceShoot = Gdx.audio.newSound(Gdx.files.internal("sounds/voice-shoot.ogg"));
    public Sound voiceReload = Gdx.audio.newSound(Gdx.files.internal("sounds/voice-reload.ogg"));
    public Sound m4shoot = Gdx.audio.newSound(Gdx.files.internal("sounds/m4-shoot.ogg"));

    public Sound frogDie = Gdx.audio.newSound(Gdx.files.internal("sounds/frog-die.ogg"));
    public Sound frogEmerge = Gdx.audio.newSound(Gdx.files.internal("sounds/frog-emerge.ogg"));
    public Sound[] frogSounds = new Sound[] {
        Gdx.audio.newSound(Gdx.files.internal("sounds/frog0.ogg")),
        Gdx.audio.newSound(Gdx.files.internal("sounds/frog1.ogg")),
    };
    public Sound zombieEmerge = Gdx.audio.newSound(Gdx.files.internal("sounds/9582k30ebgr-zombie-sfx-1.ogg"));
    public Sound[] zombieSounds = new Sound[] {
        Gdx.audio.newSound(Gdx.files.internal("sounds/zombie0.ogg")),
        Gdx.audio.newSound(Gdx.files.internal("sounds/zombie1.ogg")),
        Gdx.audio.newSound(Gdx.files.internal("sounds/zombie2.ogg")),
    };
    public Sound[] zombieDieSounds = new Sound[] {
        Gdx.audio.newSound(Gdx.files.internal("sounds/zombie-die1.ogg")),
        Gdx.audio.newSound(Gdx.files.internal("sounds/zombie-die2.ogg")),
        Gdx.audio.newSound(Gdx.files.internal("sounds/zombie-die3.ogg")),
        Gdx.audio.newSound(Gdx.files.internal("sounds/zombie-die4.ogg")),
    };

    public Sound[] death = new Sound[] {
            Gdx.audio.newSound(Gdx.files.internal("sounds/man-death-0.ogg")),
            Gdx.audio.newSound(Gdx.files.internal("sounds/man-death-1.ogg")),
    };

    public Sound[] stabs = new Sound[] {
            Gdx.audio.newSound(Gdx.files.internal("sounds/stab-0.ogg")),
            Gdx.audio.newSound(Gdx.files.internal("sounds/stab-1.ogg")),
            Gdx.audio.newSound(Gdx.files.internal("sounds/stab-2.ogg")),
    };

    public Sound[] woodCuts = new Sound[] {
            Gdx.audio.newSound(Gdx.files.internal("sounds/wood-0.ogg")),
            Gdx.audio.newSound(Gdx.files.internal("sounds/wood-1.ogg")),
            Gdx.audio.newSound(Gdx.files.internal("sounds/wood-2.ogg")),
            Gdx.audio.newSound(Gdx.files.internal("sounds/wood-3.ogg")),
    };

    public Sound[] metalHits = new Sound[] {
            Gdx.audio.newSound(Gdx.files.internal("sounds/metal-0.ogg")),
            Gdx.audio.newSound(Gdx.files.internal("sounds/metal-1.ogg")),
            Gdx.audio.newSound(Gdx.files.internal("sounds/metal-2.ogg")),
            Gdx.audio.newSound(Gdx.files.internal("sounds/metal-3.ogg")),
            Gdx.audio.newSound(Gdx.files.internal("sounds/metal-4.ogg")),
    };

    public AssetManager2() {
        super();
        load("models/tree.g3db", Model.class);
        load("models/npcs.g3db", Model.class);
        load("models/frog.g3db", Model.class);
        load("models/frog-particle.g3db", Model.class);
        load("models/weapons.g3db", Model.class);
        load("models/medkit.g3db", Model.class);
        load("crosshair010.png", Texture.class);
        load("crosshair010.png", Texture.class);
        load("textures/grass.png", Texture.class);
    }

    public static class GameObjectTemplate {
        public final ModelInstance model;
        public final Shape modelShape;
        public final Shape physicsShape;

        public GameObjectTemplate(ModelInstance model, Shape modelShape, Shape physicsShape) {
            this.model = model;
            this.modelShape = modelShape;
            this.physicsShape = physicsShape;
        }
    }

    public GameObjectTemplate treeTemplate;
    public Firearm.Template pistolTemplate, autoRifleTemplate;
    public ModelInstance medkitModelInstance;
    public Shape medkitShape;

    public void done() {
        treeModel = get("models/tree.g3db", Model.class);
        npcModel = Main.assets.get("models/npcs.g3db", Model.class);
        frogModel = Main.assets.get("models/frog.g3db", Model.class);
        weaponsModel = get("models/weapons.g3db", Model.class);
        frogParticleModel = Main.assets.get("models/frog-particle.g3db", Model.class);
        medkitModel = Main.assets.get("models/medkit.g3db", Model.class);

        Model[] models = {treeModel, npcModel, weaponsModel, frogParticleModel};
        for (Model model : models) {
            for (Material material : model.materials) {
                for (Attribute attr : material) {
                    if (attr instanceof TextureAttribute) {
                        TextureDescriptor<Texture> descriptor = ((TextureAttribute) attr).textureDescription;
                        descriptor.minFilter = Texture.TextureFilter.Nearest;
                        descriptor.magFilter = Texture.TextureFilter.Nearest;
                    }
                }
            }
        }

        treeTemplate = buildObjectTemplate("tree");
        treeTemplate.model.materials.get(1).set(
                new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA),
                FloatAttribute.createAlphaTest(0.25f),
                IntAttribute.createCullFace(GL20.GL_NONE)
        );

        pistolTemplate = buildFirearmTemplate("pistol");
        autoRifleTemplate = buildFirearmTemplate("ak");
        medkitModelInstance = new ModelInstance(medkitModel);
        medkitShape = new SphereShape(medkitModelInstance.calculateBoundingBox(tempBox));

//        System.out.println("done");
    }

    private GameObjectTemplate buildObjectTemplate(String name) {
        Node node = treeModel.getNode(name);
        BoxShape visibilityHitBox = new BoxShape(node.calculateBoundingBox(tempBox));
        BoxShape hitBox = visibilityHitBox;
        Node hitBoxNode = treeModel.getNode(name + ".hitbox0");
        if (hitBoxNode != null) {
            hitBox = new BoxShape(hitBoxNode.calculateBoundingBox(tempBox));
        }
        return new GameObjectTemplate(
                new ModelInstance(treeModel, name),
                visibilityHitBox,
                hitBox
        );
    }

    private Firearm.Template buildFirearmTemplate(String name) {
        Vector3 muzzlePoint = new Vector3();
        Node muzzle = weaponsModel.getNode(name + ".muzzle");
        muzzlePoint.set(muzzle.translation);
        ModelInstance model = new ModelInstance(weaponsModel, name);
        Shape shape = new SphereShape(model.calculateBoundingBox(tempBox));
        return new Firearm.Template(model, shape, muzzlePoint,
                Gdx.audio.newSound(Gdx.files.internal("sounds/"+ name + "-shoot.ogg")),
                Gdx.audio.newSound(Gdx.files.internal("sounds/"+ name + "-reload.ogg"))
                );
    }

    public TreeObject createTree() {
        return new TreeObject(treeTemplate);
    }

    public HealthPickupObject createHealthPickup(Vector3 position, float amount) {
        return new HealthPickupObject(
            new ModelInstance(medkitModel),
            medkitShape,
                position,
                amount
        );
    }

    public HealthPickupObject createHealthPickup(Vector3 position) {
        return createHealthPickup(position, 20);
    }
}
