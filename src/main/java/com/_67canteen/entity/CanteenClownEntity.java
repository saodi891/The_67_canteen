package com._67canteen.entity;

import com._67canteen.network.ModNetwork;
import com._67canteen.registry.ModEffects;
import com._67canteen.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Passive 67小丑 that protects itself only from players carrying the 67餐厅 effect. */
public class CanteenClownEntity extends PathfinderMob implements GeoEntity {
    private static final double EFFECT_RADIUS = 50.0D;
    private static final int EFFECT_DURATION_TICKS = 6000;
    private static final int EFFECT_CHECK_INTERVAL_TICKS = 20;
    private static final long EFFECT_SKILL_COOLDOWN_TICKS = 20L * 60L * 10L;
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.67_clown.walk");
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.67_clown.idle");

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private long nextEffectCastGameTime;
    private boolean halfHealthSkillTriggered;

    public CanteenClownEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, EFFECT_RADIUS);
    }

    @Override
    protected void registerGoals() {
        // Passive villager-like roaming and looking; intentionally no target or attack goals.
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }

        long gameTime = level().getGameTime();
        triggerHalfHealthSkillIfNeeded();

        if (tickCount != 1 && tickCount % EFFECT_CHECK_INTERVAL_TICKS != 0) {
            return;
        }
        if (gameTime < nextEffectCastGameTime) {
            return;
        }

        castEffectSkill(gameTime, false, false);
    }

    private void castEffectSkill(long gameTime, boolean ignoreCooldown, boolean forceRefresh) {
        if (!ignoreCooldown && gameTime < nextEffectCastGameTime) {
            return;
        }

        double radiusSqr = EFFECT_RADIUS * EFFECT_RADIUS;
        boolean appliedEffect = false;
        for (Player player : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(EFFECT_RADIUS),
                candidate -> !candidate.isSpectator()
                        && candidate.distanceToSqr(this) <= radiusSqr)) {
            if (forceRefresh) {
                player.removeEffect(ModEffects.CANTEEN_BUFF.get());
            }
            if (player.addEffect(new MobEffectInstance(ModEffects.CANTEEN_BUFF.get(), EFFECT_DURATION_TICKS))) {
                appliedEffect = true;
                if (player instanceof ServerPlayer serverPlayer) {
                    ModNetwork.sendEffectApparition(serverPlayer);
                }
            }
        }

        if (appliedEffect) {
            nextEffectCastGameTime = gameTime + EFFECT_SKILL_COOLDOWN_TICKS;
        }
    }

    private void triggerHalfHealthSkillIfNeeded() {
        if (!halfHealthSkillTriggered && isAlive() && getHealth() <= getMaxHealth() * 0.5F) {
            halfHealthSkillTriggered = true;
            castEffectSkill(level().getGameTime(), true, true);
        }
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("NextEffectCastGameTime", nextEffectCastGameTime);
        tag.putBoolean("HalfHealthSkillTriggered", halfHealthSkillTriggered);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        nextEffectCastGameTime = tag.getLong("NextEffectCastGameTime");
        halfHealthSkillTriggered = tag.getBoolean("HalfHealthSkillTriggered");
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Player attackingPlayer = getAttackingPlayer(source);
        if (attackingPlayer != null && attackingPlayer.hasEffect(ModEffects.CANTEEN_BUFF.get())) {
            return false;
        }
        boolean damaged = super.hurt(source, amount);
        if (damaged && !level().isClientSide) {
            triggerHalfHealthSkillIfNeeded();
        }
        return damaged;
    }

    private static Player getAttackingPlayer(DamageSource source) {
        if (source.getEntity() instanceof Player player) {
            return player;
        }
        if (source.getDirectEntity() instanceof Player player) {
            return player;
        }
        if (source.getDirectEntity() instanceof Projectile projectile
                && projectile.getOwner() instanceof Player player) {
            return player;
        }
        return null;
    }

    @Override
    public boolean shouldDespawnInPeaceful() {
        return true;
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.world.damagesource.DamageSource source,
                                       int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        spawnAtLocation(new ItemStack(ModItems.CLOWN_DOOR.get()));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 3,
                state -> state.setAndContinue(state.isMoving() ? WALK : IDLE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }
}

