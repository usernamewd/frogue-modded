package io.github.necrashter.natural_revenge.world.entities;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.world.Damageable;
import io.github.necrashter.natural_revenge.world.GameWorld;

/**
 * BotPlayer - AI-controlled player bot that behaves like a real player.
 * - Wanders around the map
 * - Shoots at nearby enemies
 * - Enemies will chase and attack bots
 * - Has health and can die
 */
public class BotPlayer extends NPC {

    // Bot stats
    public static final float BOT_MAX_HEALTH = 100f;
    public static final float BOT_DAMAGE = 5f;
    public static final float BOT_SHOOT_RANGE = 15f;
    public static final float BOT_SHOOT_COOLDOWN = 0.5f;
    public static final float BOT_MOVEMENT_SPEED = 3f;

    // States
    private WanderState wanderState;
    private CombatState combatState;

    // Shooting
    private float shootCooldown = 0f;
    private GameEntity currentTarget = null;

    // Bot identification
    public String botName;
    private static int botCounter = 0;

    public BotPlayer(GameWorld world) {
        super(world, 1.8f, 0.4f);

        // Use player model (same as zombies but different skin)
        modelInstance = new ModelInstance(Main.assets.npcModel, "ManArmature", "ZombieMesh");
        modelInstance.transform.setToTranslation(hitBox.position);
        animationController = new AnimationController(modelInstance);

        maxHealth = BOT_MAX_HEALTH;
        health = maxHealth;

        botName = "Бот " + (++botCounter);

        // Initialize states
        wanderState = new WanderState();
        combatState = new CombatState();

        deathAnim = "zombie-die";
        removeOnDeath = true;

        switchState(wanderState);
    }

    public void spawn() {
        dead = false;
        health = maxHealth;
        Vector2 point = world.randomPointNearPlayer(5f, 15f);
        setPosition(point.x, point.y);
        world.octree.add(this);
        init();
        updateTransform();
        switchState(wanderState);
    }

    @Override
    public void reset() {
        dead = false;
        health = maxHealth;
        shootCooldown = 0f;
        currentTarget = null;
    }

    @Override
    public void update(float delta) {
        if (dead) {
            super.update(delta);
            return;
        }

        // Update shoot cooldown
        if (shootCooldown > 0) {
            shootCooldown -= delta;
        }

        // Find nearest enemy
        currentTarget = findNearestEnemy();

        // Switch to combat state if enemy found
        if (currentTarget != null && !(currentState instanceof CombatState)) {
            switchState(combatState);
        } else if (currentTarget == null && !(currentState instanceof WanderState)) {
            switchState(wanderState);
        }

        super.update(delta);
    }

    private GameEntity findNearestEnemy() {
        if (world.octree == null) return null;

        GameEntity nearest = null;
        float nearestDist = BOT_SHOOT_RANGE * BOT_SHOOT_RANGE;

        for (GameEntity entity : world.octree.entities) {
            // Skip self, player, other bots, and dead entities
            if (entity == this || entity == world.player || entity.dead) continue;
            if (entity instanceof BotPlayer) continue;

            // Calculate distance
            float dist = Vector3.dst2(
                hitBox.position.x, hitBox.position.y, hitBox.position.z,
                entity.hitBox.position.x, entity.hitBox.position.y, entity.hitBox.position.z
            );

            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = entity;
            }
        }

        return nearest;
    }

    private void shootAt(GameEntity target) {
        if (shootCooldown > 0 || target == null || target.dead) return;

        shootCooldown = BOT_SHOOT_COOLDOWN;

        // Play shoot sound
        world.playSound(Main.assets.enemyPistol, hitBox.position);

        // Deal damage to target
        target.takeDamage(BOT_DAMAGE, Damageable.DamageAgent.Player, Damageable.DamageSource.Firearm);

        // Add bullet trace visual
        Vector3 targetPos = new Vector3(target.hitBox.position);
        targetPos.y += target.hitBox.height / 2f;
        world.decalPool.addBulletTrace(hitBox.position, targetPos);
    }

    @Override
    public void die() {
        dead = true;
        world.playSound(Main.assets.death[MathUtils.random.nextInt(Main.assets.death.length)], hitBox.position);
        animationController.setAnimation(deathAnim, animationListener);
    }

    @Override
    public boolean onRemove(boolean worldDisposal) {
        if (worldDisposal) return true;
        // Bots don't drop items
        return true;
    }

    /**
     * Check if this bot should be targeted by enemies.
     * Enemies should treat bots as valid targets just like the player.
     */
    public boolean isValidTarget() {
        return !dead && health > 0;
    }

    // ==================== BOT STATES ====================

    /**
     * Wander state - bot walks around randomly
     */
    class WanderState extends State {
        private final Vector3 targetPosition = new Vector3();
        private float wanderTimer = 0f;
        private static final float WANDER_INTERVAL = 3f;
        private boolean moving = false;

        @Override
        void init() {
            pickNewDestination();
            animationController.setAnimation("zombie-walk", -1, animationListener);
            moving = true;
        }

        private void pickNewDestination() {
            // Pick random position near current location
            float angle = MathUtils.random(MathUtils.PI2);
            float distance = MathUtils.random(5f, 15f);
            targetPosition.set(
                hitBox.position.x + MathUtils.cos(angle) * distance,
                0,
                hitBox.position.z + MathUtils.sin(angle) * distance
            );
            // Clamp to terrain bounds
            targetPosition.x = world.terrain.clampX(targetPosition.x, hitBox.radius);
            targetPosition.z = world.terrain.clampZ(targetPosition.z, hitBox.radius);
            targetPosition.y = world.terrain.getHeight(targetPosition.x, targetPosition.z);
            wanderTimer = 0f;
        }

        @Override
        boolean update(float delta) {
            if (super.update(delta)) return true;

            wanderTimer += delta;

            // Move towards target
            movement.set(targetPosition).sub(hitBox.position);
            float dist = movement.len();
            movement.y = 0;
            forward.set(movement.nor());

            if (dist < 2f || wanderTimer > WANDER_INTERVAL) {
                pickNewDestination();
            }

            movement.scl(BOT_MOVEMENT_SPEED);

            return false;
        }
    }

    /**
     * Combat state - bot engages enemies
     */
    class CombatState extends State {
        private float shootTimer = 0f;

        @Override
        void init() {
            animationController.setAnimation("zombie-walk", -1, animationListener);
            shootTimer = 0f;
        }

        @Override
        boolean update(float delta) {
            if (super.update(delta)) return true;

            if (currentTarget == null || currentTarget.dead) {
                return false;
            }

            // Face the target
            movement.set(currentTarget.hitBox.position).sub(hitBox.position);
            float dist = movement.len();
            movement.y = 0;
            forward.set(movement.nor());

            // If too far, move closer
            if (dist > BOT_SHOOT_RANGE * 0.6f) {
                movement.scl(BOT_MOVEMENT_SPEED);
            } else if (dist < 3f) {
                // Too close, back up
                movement.scl(-BOT_MOVEMENT_SPEED * 0.5f);
            } else {
                // Good distance, strafe a bit
                movement.rotateRad(Vector3.Y, MathUtils.HALF_PI);
                movement.scl(BOT_MOVEMENT_SPEED * 0.3f);
            }

            // Shoot periodically
            shootTimer += delta;
            if (shootTimer >= BOT_SHOOT_COOLDOWN) {
                shootAt(currentTarget);
                shootTimer = 0f;
            }

            return false;
        }
    }

    // ==================== STATIC HELPERS ====================

    private static Array<BotPlayer> activeBots = new Array<>();

    public static Array<BotPlayer> getActiveBots() {
        return activeBots;
    }

    public static void spawnBot(GameWorld world) {
        BotPlayer bot = new BotPlayer(world);
        bot.spawn();
        activeBots.add(bot);
    }

    public static void removeAllBots(GameWorld world) {
        for (BotPlayer bot : activeBots) {
            if (!bot.dead) {
                bot.remove();
            }
        }
        activeBots.clear();
    }

    public static void clearBotReferences() {
        activeBots.clear();
        botCounter = 0;
    }

    /**
     * Get nearest target for enemies (player or bot).
     * Enemies should call this to find the closest valid target.
     */
    public static GameEntity getNearestPlayerOrBot(GameWorld world, Vector3 position) {
        GameEntity nearest = null;
        float nearestDist = Float.MAX_VALUE;

        // Check player
        if (world.player != null && !world.player.dead) {
            float dist = Vector3.dst2(
                position.x, position.y, position.z,
                world.player.hitBox.position.x, world.player.hitBox.position.y, world.player.hitBox.position.z
            );
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = world.player;
            }
        }

        // Check bots
        for (BotPlayer bot : activeBots) {
            if (bot.dead) continue;
            float dist = Vector3.dst2(
                position.x, position.y, position.z,
                bot.hitBox.position.x, bot.hitBox.position.y, bot.hitBox.position.z
            );
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = bot;
            }
        }

        return nearest;
    }
}
