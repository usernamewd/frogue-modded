package io.github.necrashter.natural_revenge.mods;

import com.badlogic.gdx.math.MathUtils;
import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.world.GameWorld;
import io.github.necrashter.natural_revenge.world.entities.GameEntity;
import io.github.necrashter.natural_revenge.world.player.Firearm;
import io.github.necrashter.natural_revenge.world.player.Player;
import io.github.necrashter.natural_revenge.world.player.PlayerWeapon;

/**
 * CheatManager - Core cheat functionality for Frogue
 * This class manages all cheat states and applies them to the game.
 */
public class CheatManager {
    private static CheatManager instance;

    // Cheat States
    private boolean godMode = false;
    private boolean infiniteAmmo = false;
    private boolean noReload = false;
    private boolean rapidFire = false;
    private boolean noSpread = false;
    private boolean oneHitKill = false;
    private boolean speedHack = false;
    private boolean superJump = false;
    private boolean noClip = false;
    private boolean invisibility = false;
    private boolean autoAim = false;
    private boolean freezeEnemies = false;
    private boolean instantKillAll = false;
    private boolean unlimitedWeaponSlots = false;
    private boolean extendedViewDistance = false;

    // Cheat Values
    private float speedMultiplier = 2.0f;
    private float jumpMultiplier = 2.0f;
    private float damageMultiplier = 10.0f;
    private float fireRateMultiplier = 5.0f;
    private float viewDistanceMultiplier = 3.0f;

    // Original values backup
    private float originalSpeed = 4.0f;
    private float originalJump = 6.0f;
    private float originalViewDistance = 25.0f;
    private int originalMaxWeapons = 6;

    // Reference to current game world
    private GameWorld currentWorld = null;
    private Player currentPlayer = null;

    private CheatManager() {}

    public static CheatManager getInstance() {
        if (instance == null) {
            instance = new CheatManager();
        }
        return instance;
    }

    /**
     * Set the current game world reference
     */
    public void setGameWorld(GameWorld world) {
        this.currentWorld = world;
        if (world != null) {
            this.originalViewDistance = world.viewDistance;
        }
    }

    /**
     * Called every frame to apply active cheats
     */
    public void update(float delta) {
        if (currentWorld == null) return;
        if (currentWorld.paused) return;

        // Always get fresh player reference from world (player may be created after world)
        currentPlayer = currentWorld.player;
        if (currentPlayer == null) return;

        // Backup original values on first access
        if (originalMaxWeapons == 6 && currentPlayer.maximumWeapons != 99) {
            originalMaxWeapons = currentPlayer.maximumWeapons;
        }

        applyGodMode();
        applyInfiniteAmmo();
        applyNoReload();
        applyRapidFire();
        applyNoSpread();
        applySpeedHack();
        applySuperJump();
        applyFreezeEnemies();
        applyExtendedViewDistance();
        applyUnlimitedWeaponSlots();

        if (instantKillAll) {
            killAllEnemies();
            instantKillAll = false;
        }
    }

    // ==================== CHEAT IMPLEMENTATIONS ====================

    private void applyGodMode() {
        if (godMode && currentPlayer != null) {
            currentPlayer.health = currentPlayer.maxHealth;
            currentPlayer.dead = false;
        }
    }

    private void applyInfiniteAmmo() {
        if (infiniteAmmo && currentPlayer != null && currentPlayer.activeWeapon != null) {
            if (currentPlayer.activeWeapon instanceof Firearm) {
                Firearm firearm = (Firearm) currentPlayer.activeWeapon;
                firearm.ammoInClip = firearm.maxAmmoInClip;
            }
        }
    }

    private void applyNoReload() {
        if (noReload && currentPlayer != null && currentPlayer.activeWeapon != null) {
            if (currentPlayer.activeWeapon instanceof Firearm) {
                Firearm firearm = (Firearm) currentPlayer.activeWeapon;
                firearm.ammoInClip = firearm.maxAmmoInClip;
            }
        }
    }

    private void applyRapidFire() {
        if (currentPlayer != null && currentPlayer.activeWeapon != null) {
            if (currentPlayer.activeWeapon instanceof Firearm) {
                Firearm firearm = (Firearm) currentPlayer.activeWeapon;
                if (rapidFire) {
                    // Increase recovery speed for rapid fire effect
                    firearm.recoverySpeed = 50.0f;
                }
            }
        }
    }

    private void applyNoSpread() {
        if (currentPlayer != null && currentPlayer.activeWeapon != null) {
            if (currentPlayer.activeWeapon instanceof Firearm) {
                Firearm firearm = (Firearm) currentPlayer.activeWeapon;
                if (noSpread) {
                    firearm.spread = 0f;
                }
            }
        }
    }

    private void applySpeedHack() {
        if (currentPlayer != null) {
            if (speedHack) {
                currentPlayer.movementSpeed = originalSpeed * speedMultiplier;
            } else {
                currentPlayer.movementSpeed = originalSpeed;
            }
        }
    }

    private void applySuperJump() {
        if (currentPlayer != null) {
            if (superJump) {
                currentPlayer.jumpVelocity = originalJump * jumpMultiplier;
            } else {
                currentPlayer.jumpVelocity = originalJump;
            }
        }
    }

    private void applyFreezeEnemies() {
        if (freezeEnemies && currentWorld != null && currentWorld.octree != null) {
            for (GameEntity entity : currentWorld.octree.entities) {
                if (entity != currentPlayer) {
                    entity.movement.setZero();
                    entity.hitBox.velocity.setZero();
                }
            }
        }
    }

    private void applyExtendedViewDistance() {
        if (currentWorld != null) {
            if (extendedViewDistance) {
                currentWorld.viewDistance = originalViewDistance * viewDistanceMultiplier;
                currentWorld.cam.far = currentWorld.viewDistance;
            } else {
                currentWorld.viewDistance = originalViewDistance;
                currentWorld.cam.far = originalViewDistance;
            }
        }
    }

    private void applyUnlimitedWeaponSlots() {
        if (currentPlayer != null) {
            if (unlimitedWeaponSlots) {
                currentPlayer.maximumWeapons = 99;
            } else {
                currentPlayer.maximumWeapons = originalMaxWeapons;
            }
        }
    }

    public void killAllEnemies() {
        if (currentWorld == null) return;
        Player player = currentWorld.player;
        if (currentWorld.octree != null) {
            for (GameEntity entity : currentWorld.octree.entities) {
                if (entity != player && !entity.dead) {
                    entity.health = 0;
                    entity.die();
                }
            }
        }
    }

    public void healPlayer() {
        if (currentWorld == null) return;
        Player player = currentWorld.player;
        if (player != null) {
            player.health = player.maxHealth;
        }
    }

    public void giveAllWeapons() {
        if (currentWorld == null) return;
        Player player = currentWorld.player;
        if (player != null) {
            // Trigger weapon delivery
            player.noWeaponTimer = 0f;
        }
    }

    public void teleportForward(float distance) {
        if (currentWorld == null) return;
        Player player = currentWorld.player;
        if (player != null) {
            float newX = player.hitBox.position.x + currentWorld.cam.direction.x * distance;
            float newZ = player.hitBox.position.z + currentWorld.cam.direction.z * distance;
            player.setPosition(newX, newZ);
        }
    }

    public void setPlayerHealth(float health) {
        if (currentWorld == null) return;
        Player player = currentWorld.player;
        if (player != null) {
            player.maxHealth = health;
            player.health = health;
        }
    }

    public void setDamageMultiplier(float multiplier) {
        this.damageMultiplier = multiplier;
        this.oneHitKill = multiplier >= 100f;
    }

    public float getDamageMultiplier() {
        if (oneHitKill) {
            return 9999f;
        }
        return 1.0f; // Normal damage when cheat is disabled
    }

    // ==================== GETTERS AND SETTERS ====================

    public boolean isGodMode() { return godMode; }
    public void setGodMode(boolean godMode) { this.godMode = godMode; }

    public boolean isInfiniteAmmo() { return infiniteAmmo; }
    public void setInfiniteAmmo(boolean infiniteAmmo) { this.infiniteAmmo = infiniteAmmo; }

    public boolean isNoReload() { return noReload; }
    public void setNoReload(boolean noReload) { this.noReload = noReload; }

    public boolean isRapidFire() { return rapidFire; }
    public void setRapidFire(boolean rapidFire) { this.rapidFire = rapidFire; }

    public boolean isNoSpread() { return noSpread; }
    public void setNoSpread(boolean noSpread) { this.noSpread = noSpread; }

    public boolean isOneHitKill() { return oneHitKill; }
    public void setOneHitKill(boolean oneHitKill) { this.oneHitKill = oneHitKill; }

    public boolean isSpeedHack() { return speedHack; }
    public void setSpeedHack(boolean speedHack) { this.speedHack = speedHack; }

    public boolean isSuperJump() { return superJump; }
    public void setSuperJump(boolean superJump) { this.superJump = superJump; }

    public boolean isNoClip() { return noClip; }
    public void setNoClip(boolean noClip) { this.noClip = noClip; }

    public boolean isInvisibility() { return invisibility; }
    public void setInvisibility(boolean invisibility) { this.invisibility = invisibility; }

    public boolean isAutoAim() { return autoAim; }
    public void setAutoAim(boolean autoAim) { this.autoAim = autoAim; }

    public boolean isFreezeEnemies() { return freezeEnemies; }
    public void setFreezeEnemies(boolean freezeEnemies) { this.freezeEnemies = freezeEnemies; }

    public boolean isUnlimitedWeaponSlots() { return unlimitedWeaponSlots; }
    public void setUnlimitedWeaponSlots(boolean unlimitedWeaponSlots) { this.unlimitedWeaponSlots = unlimitedWeaponSlots; }

    public boolean isExtendedViewDistance() { return extendedViewDistance; }
    public void setExtendedViewDistance(boolean extendedViewDistance) { this.extendedViewDistance = extendedViewDistance; }

    public float getSpeedMultiplier() { return speedMultiplier; }
    public void setSpeedMultiplier(float speedMultiplier) { this.speedMultiplier = speedMultiplier; }

    public float getJumpMultiplier() { return jumpMultiplier; }
    public void setJumpMultiplier(float jumpMultiplier) { this.jumpMultiplier = jumpMultiplier; }

    public float getFireRateMultiplier() { return fireRateMultiplier; }
    public void setFireRateMultiplier(float fireRateMultiplier) { this.fireRateMultiplier = fireRateMultiplier; }

    public float getViewDistanceMultiplier() { return viewDistanceMultiplier; }
    public void setViewDistanceMultiplier(float viewDistanceMultiplier) { this.viewDistanceMultiplier = viewDistanceMultiplier; }

    public void triggerInstantKillAll() { this.instantKillAll = true; }

    /**
     * Reset all cheats to default (off) state
     */
    public void resetAllCheats() {
        godMode = false;
        infiniteAmmo = false;
        noReload = false;
        rapidFire = false;
        noSpread = false;
        oneHitKill = false;
        speedHack = false;
        superJump = false;
        noClip = false;
        invisibility = false;
        autoAim = false;
        freezeEnemies = false;
        instantKillAll = false;
        unlimitedWeaponSlots = false;
        extendedViewDistance = false;

        // Reset player values if available
        if (currentWorld != null) {
            Player player = currentWorld.player;
            if (player != null) {
                player.movementSpeed = originalSpeed;
                player.jumpVelocity = originalJump;
                player.maximumWeapons = originalMaxWeapons;
            }
            currentWorld.viewDistance = originalViewDistance;
            currentWorld.cam.far = originalViewDistance;
        }
    }

    /**
     * Get a status string of all active cheats
     */
    public String getActiveCheatsSummary() {
        StringBuilder sb = new StringBuilder();
        if (godMode) sb.append("[GOD] ");
        if (infiniteAmmo) sb.append("[INF AMMO] ");
        if (noReload) sb.append("[NO RELOAD] ");
        if (rapidFire) sb.append("[RAPID] ");
        if (noSpread) sb.append("[NO SPREAD] ");
        if (oneHitKill) sb.append("[1HIT] ");
        if (speedHack) sb.append("[SPEED] ");
        if (superJump) sb.append("[JUMP] ");
        if (freezeEnemies) sb.append("[FREEZE] ");
        if (extendedViewDistance) sb.append("[VIEW] ");
        if (unlimitedWeaponSlots) sb.append("[SLOTS] ");
        return sb.toString();
    }
}
