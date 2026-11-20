package com._67canteen.entity;

import com._67canteen.registry.ModEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** A vanilla-behavior wolf that additionally hunts living entities carrying the canteen buff. */
public class GuardDogEntity extends Wolf implements GeoEntity {
    public static final double MAX_HEALTH = 20.0D;
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.guard_dog.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.guard_dog.walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenLoop("animation.guard_dog.attack");
    private static final RawAnimation SIT = RawAnimation.begin().thenLoop("animation.guard_dog.sit");
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    @Nullable
    private UUID lockedTargetId;
    @Nullable
    private UUID packId;

    public GuardDogEntity(EntityType<? extends Wolf> entityType, Level level) {
        super(entityType, level);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(MAX_HEALTH);
        setHealth((float) MAX_HEALTH);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // The target predicate is evaluated by vanilla's nearest-target goal; do not target dead entities.
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
                target -> target.isAlive()
                        && !(target instanceof AbstractSkeleton)
                        && target.hasEffect(ModEffects.CANTEEN_BUFF.get())));
    }

    /** Locks this summoned pack member to its original target and pack. */
    public void setHuntLock(LivingEntity target, UUID packId) {
        this.lockedTargetId = target.getUUID();
        this.packId = packId;
        super.setTarget(target);
    }

    @Nullable
    public UUID getPackId() {
        return packId;
    }

    @Nullable
    public UUID getLockedTargetId() {
        return lockedTargetId;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        // Wolves must not retaliate against or acquire skeletons as targets.
        if (target instanceof AbstractSkeleton) {
            return;
        }
        // Vanilla wolf retaliation and target goals must not switch the summoned pack's target.
        if (lockedTargetId != null) {
            if (target == null || !lockedTargetId.equals(target.getUUID())) {
                return;
            }
        }
        super.setTarget(target);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // Do not allow bones or other wolf interactions to tame this special entity.
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && lockedTargetId != null) {
            if (level() instanceof ServerLevel serverLevel
                    && serverLevel.getEntity(lockedTargetId) instanceof LivingEntity target
                    && target.isAlive()) {
                if (getTarget() != target) {
                    super.setTarget(target);
                }
            }
        }
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }

    @Override
    public boolean shouldDespawnInPeaceful() {
        return true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (lockedTargetId != null) {
            tag.putUUID("CanteenLockedTarget", lockedTargetId);
        }
        if (packId != null) {
            tag.putUUID("CanteenPack", packId);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        lockedTargetId = tag.hasUUID("CanteenLockedTarget") ? tag.getUUID("CanteenLockedTarget") : null;
        packId = tag.hasUUID("CanteenPack") ? tag.getUUID("CanteenPack") : null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "guard_dog", 5, state -> {
            if (isOrderedToSit()) {
                return state.setAndContinue(SIT);
            }
            if (state.isMoving()) {
                return state.setAndContinue(WALK);
            }
            if (isAggressive()) {
                return state.setAndContinue(ATTACK);
            }
            return state.setAndContinue(IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }
}
