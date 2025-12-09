package io.github.necrashter.natural_revenge.mods;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
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
    private boolean freezeEnemies = false;
    private boolean instantKillAll = false;
    private boolean unlimitedWeaponSlots = false;
    private boolean extendedViewDistance = false;

    // New cheats
    private boolean noRecoil = false;
    private boolean thirdPerson = false;
    private boolean bunnyHop = false;
    private boolean airStrafe = false;
    private boolean silentAim = false;
    private boolean drawAimFov = false;

    // Aimbot settings
    private float aimFov = 60f; // FOV in degrees (5-360)
    private GameEntity currentAimTarget = null;

    // Cheat Values
    private float speedMultiplier = 2.0f;
    private float jumpMultiplier = 2.0f;
    private float damageMultiplier = 10.0f;
    private float fireRateMultiplier = 5.0f;
    private float viewDistanceMultiplier = 3.0f;
    private float thirdPersonDistance = 5.0f;

    // Original values backup
    private float originalSpeed = 4.0f;
    private float originalJump = 6.0f;
    private float originalViewDistance = 25.0f;
    private int originalMaxWeapons = 6;
    private float originalKnockback = 2f;
    private float originalPitchMod = 0f;

    // Reference to current game world
    private GameWorld currentWorld = null;
    private Player currentPlayer = null;

    // Track if original values have been backed up
    private boolean originalsBackedUp = false;

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
        if (world != null && !originalsBackedUp && !extendedViewDistance) {
            this.originalViewDistance = world.viewDistance;
        }
    }

    /**
     * Called every frame to apply active cheats
     */
    public void update(float delta) {
        if (currentWorld == null) return;
        if (currentWorld.paused) return;

        // Always get fresh player reference from world
        currentPlayer = currentWorld.player;
        if (currentPlayer == null) return;

        // Backup original values on first access (only once)
        if (!originalsBackedUp) {
            originalMaxWeapons = currentPlayer.maximumWeapons;
            originalViewDistance = currentWorld.viewDistance;
            originalsBackedUp = true;
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
        applyNoRecoil();
        applyBunnyHop();
        applyAirStrafe();
        applySilentAim();
        applyThirdPerson();

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

    private void applyNoRecoil() {
        if (noRecoil && currentPlayer != null) {
            // Reset pitch modification (visual recoil)
            currentPlayer.pitchMod = 0f;
            // Zero out knockback on weapon
            if (currentPlayer.activeWeapon instanceof Firearm) {
                Firearm firearm = (Firearm) currentPlayer.activeWeapon;
                firearm.knockback = 0f;
                firearm.knockForward = 0f;
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

    private void applyBunnyHop() {
        if (bunnyHop && currentPlayer != null) {
            // Auto-jump when on ground and moving
            if ((currentPlayer.hitBox.onGround || currentPlayer.hitBox.onObject)
                && currentPlayer.movement.len2() > 0.01f) {
                currentPlayer.jump(currentPlayer.jumpVelocity);
            }
        }
    }

    private void applyAirStrafe() {
        if (airStrafe && currentPlayer != null) {
            // Enhanced air control
            if (!currentPlayer.hitBox.onGround && !currentPlayer.hitBox.onObject) {
                // Apply movement in air with better control
                currentPlayer.hitBox.velocity.x += currentPlayer.movement.x * 0.5f;
                currentPlayer.hitBox.velocity.z += currentPlayer.movement.z * 0.5f;
            }
        }
    }

    private void applySilentAim() {
        if (!silentAim || currentPlayer == null || currentWorld == null) {
            currentAimTarget = null;
            return;
        }

        // Find closest enemy within FOV
        GameEntity closestTarget = null;
        float closestAngle = aimFov / 2f; // Half angle for cone

        Vector3 camDir = currentWorld.cam.direction;
        Vector3 camPos = currentWorld.cam.position;

        if (currentWorld.octree != null) {
            for (GameEntity entity : currentWorld.octree.entities) {
                if (entity == currentPlayer || entity.dead) continue;

                // Calculate direction to entity
                Vector3 toEntity = new Vector3(entity.hitBox.position).sub(camPos);
                float distance = toEntity.len();
                if (distance < 0.1f) continue;

                toEntity.nor();

                // Calculate angle between camera direction and entity direction
                float dot = camDir.dot(toEntity);
                float angle = (float) Math.toDegrees(Math.acos(MathUtils.clamp(dot, -1f, 1f)));

                if (angle < closestAngle) {
                    closestAngle = angle;
                    closestTarget = entity;
                }
            }
        }

        currentAimTarget = closestTarget;
    }

    private void applyThirdPerson() {
        if (thirdPerson && currentPlayer != null && currentWorld != null) {
            // Move camera behind player
            Vector3 offset = new Vector3(currentWorld.cam.direction).scl(-thirdPersonDistance);
            offset.y += 1.5f; // Slightly above
            currentWorld.cam.position.set(currentPlayer.hitBox.position).add(offset);
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

    /**
     * Get the current aim target for silent aim (used in shooting)
     */
    public GameEntity getAimTarget() {
        return silentAim ? currentAimTarget : null;
    }

    /**
     * Check if FOV circle should be drawn
     */
    public boolean shouldDrawFovCircle() {
        return silentAim && drawAimFov;
    }

    /**
     * Get aim FOV in degrees
     */
    public float getAimFov() {
        return aimFov;
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
        return 1.0f;
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

    public boolean isFreezeEnemies() { return freezeEnemies; }
    public void setFreezeEnemies(boolean freezeEnemies) { this.freezeEnemies = freezeEnemies; }

    public boolean isUnlimitedWeaponSlots() { return unlimitedWeaponSlots; }
    public void setUnlimitedWeaponSlots(boolean unlimitedWeaponSlots) { this.unlimitedWeaponSlots = unlimitedWeaponSlots; }

    public boolean isExtendedViewDistance() { return extendedViewDistance; }
    public void setExtendedViewDistance(boolean extendedViewDistance) { this.extendedViewDistance = extendedViewDistance; }

    public boolean isNoRecoil() { return noRecoil; }
    public void setNoRecoil(boolean noRecoil) { this.noRecoil = noRecoil; }

    public boolean isThirdPerson() { return thirdPerson; }
    public void setThirdPerson(boolean thirdPerson) { this.thirdPerson = thirdPerson; }

    public boolean isBunnyHop() { return bunnyHop; }
    public void setBunnyHop(boolean bunnyHop) { this.bunnyHop = bunnyHop; }

    public boolean isAirStrafe() { return airStrafe; }
    public void setAirStrafe(boolean airStrafe) { this.airStrafe = airStrafe; }

    public boolean isSilentAim() { return silentAim; }
    public void setSilentAim(boolean silentAim) { this.silentAim = silentAim; }

    public boolean isDrawAimFov() { return drawAimFov; }
    public void setDrawAimFov(boolean drawAimFov) { this.drawAimFov = drawAimFov; }

    public void setAimFov(float aimFov) { this.aimFov = MathUtils.clamp(aimFov, 5f, 360f); }

    public float getSpeedMultiplier() { return speedMultiplier; }
    public void setSpeedMultiplier(float speedMultiplier) { this.speedMultiplier = speedMultiplier; }

    public float getJumpMultiplier() { return jumpMultiplier; }
    public void setJumpMultiplier(float jumpMultiplier) { this.jumpMultiplier = jumpMultiplier; }

    public float getFireRateMultiplier() { return fireRateMultiplier; }
    public void setFireRateMultiplier(float fireRateMultiplier) { this.fireRateMultiplier = fireRateMultiplier; }

    public float getViewDistanceMultiplier() { return viewDistanceMultiplier; }
    public void setViewDistanceMultiplier(float viewDistanceMultiplier) { this.viewDistanceMultiplier = viewDistanceMultiplier; }

    public float getThirdPersonDistance() { return thirdPersonDistance; }
    public void setThirdPersonDistance(float distance) { this.thirdPersonDistance = distance; }

    public void triggerInstantKillAll() { this.instantKillAll = true; }

    public GameWorld getCurrentWorld() { return currentWorld; }

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
        freezeEnemies = false;
        instantKillAll = false;
        unlimitedWeaponSlots = false;
        extendedViewDistance = false;
        noRecoil = false;
        thirdPerson = false;
        bunnyHop = false;
        airStrafe = false;
        silentAim = false;
        drawAimFov = false;

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
        if (infiniteAmmo) sb.append("[AMMO] ");
        if (noReload) sb.append("[RELOAD] ");
        if (rapidFire) sb.append("[RAPID] ");
        if (noSpread) sb.append("[SPREAD] ");
        if (oneHitKill) sb.append("[1HIT] ");
        if (speedHack) sb.append("[SPEED] ");
        if (superJump) sb.append("[JUMP] ");
        if (freezeEnemies) sb.append("[FREEZE] ");
        if (noRecoil) sb.append("[RECOIL] ");
        if (thirdPerson) sb.append("[3RD] ");
        if (bunnyHop) sb.append("[BHOP] ");
        if (airStrafe) sb.append("[AIR] ");
        if (silentAim) sb.append("[AIM] ");
        return sb.toString();
    }
}
