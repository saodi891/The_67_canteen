package com._67canteen.event;

import com._67canteen.CanteenMod;
import com._67canteen.entity.GuardDogEntity;
import com._67canteen.registry.ModEffects;
import com._67canteen.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingUseTotemEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Spawns guard dogs when a player's canteen buff naturally expires. */
@Mod.EventBusSubscriber(modid = CanteenMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CanteenBuffEvents {
    private static final int GUARD_DOG_COUNT = 4;
    private static final Set<UUID> MILK_USE_WITH_BUFF = new HashSet<>();
    private static final Set<UUID> TOTEM_REMOVAL_PENDING = new HashSet<>();

    private CanteenBuffEvents() {}

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (!isCanteenEffect(event.getEffectInstance()) || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        MILK_USE_WITH_BUFF.remove(player.getUUID());
        TOTEM_REMOVAL_PENDING.remove(player.getUUID());
        spawnGuardDogs(player);
    }

    @SubscribeEvent
    public static void onMilkStart(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getItem().is(Items.MILK_BUCKET)
                && player.hasEffect(ModEffects.CANTEEN_BUFF.get())) {
            MILK_USE_WITH_BUFF.add(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onMilkStop(LivingEntityUseItemEvent.Stop event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getItem().is(Items.MILK_BUCKET)) {
            MILK_USE_WITH_BUFF.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onMilkFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !event.getItem().is(Items.MILK_BUCKET)
                || !MILK_USE_WITH_BUFF.remove(player.getUUID())) {
            return;
        }
        if (!player.hasEffect(ModEffects.CANTEEN_BUFF.get())) {
            spawnGuardDogs(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTotemUsed(LivingUseTotemEvent event) {
        if (!event.isCanceled() && event.getEntity() instanceof ServerPlayer player
                && player.hasEffect(ModEffects.CANTEEN_BUFF.get())) {
            TOTEM_REMOVAL_PENDING.add(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (!isCanteenEffect(event.getEffectInstance()) || !(event.getEntity() instanceof ServerPlayer player)
                || !TOTEM_REMOVAL_PENDING.remove(player.getUUID())) {
            return;
        }
        spawnGuardDogs(player);
    }

    private static boolean isCanteenEffect(net.minecraft.world.effect.MobEffectInstance instance) {
        return instance != null && instance.getEffect() == ModEffects.CANTEEN_BUFF.get();
    }

    private static void spawnGuardDogs(ServerPlayer player) {
        if (!player.isAlive()) {
            return;
        }

        ServerLevel level = player.serverLevel();
        BlockPos playerPos = player.blockPosition();
        List<BlockPos> fullBlockSpots = findSpawnSpots(level, player, playerPos, true);
        List<BlockPos> partialBlockSpots = findSpawnSpots(level, player, playerPos, false);
        UUID packId = UUID.randomUUID();

        for (int i = 0; i < GUARD_DOG_COUNT; i++) {
            GuardDogEntity dog = ModEntities.GUARD_DOG.get().create(level);
            if (dog == null) {
                continue;
            }

            BlockPos spawnPos = findUsableSpot(dog, player, level, fullBlockSpots);
            if (spawnPos == null) {
                spawnPos = findUsableSpot(dog, player, level, partialBlockSpots);
            }

            if (spawnPos != null) {
                dog.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                        player.getRandom().nextFloat() * 360.0F, 0.0F);
            } else {
                // Last resort: overlap the player's block so the dog can immediately acquire and reach its target.
                double angle = Math.PI * 2.0D * i / GUARD_DOG_COUNT;
                dog.moveTo(player.getX() + Math.cos(angle) * 0.2D, player.getY(),
                        player.getZ() + Math.sin(angle) * 0.2D, player.getRandom().nextFloat() * 360.0F, 0.0F);
            }

            dog.finalizeSpawn(level, level.getCurrentDifficultyAt(dog.blockPosition()), MobSpawnType.EVENT, null, null);
            dog.setHealth(dog.getMaxHealth());
            // Lock each dog to the player whose canteen effect just ended.
            dog.setHuntLock(player, packId);
            level.addFreshEntity(dog);
        }
    }

    @SubscribeEvent
    public static void onHuntTargetDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        MinecraftServer server = player.server;
        UUID targetId = player.getUUID();
        List<GuardDogEntity> pack = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof GuardDogEntity dog
                        && targetId.equals(dog.getLockedTargetId())
                        && dog.getPackId() != null) {
                    pack.add(dog);
                }
            }
        }
        // Remove after scanning all dimensions; discard() does not invoke any loot-drop logic.
        pack.forEach(GuardDogEntity::discard);
    }

    private static List<BlockPos> findSpawnSpots(ServerLevel level, ServerPlayer player, BlockPos center, boolean fullBlock) {
        List<BlockPos> spots = new ArrayList<>();
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    double distanceSquared = dx * dx + dz * dz;
                    if (distanceSquared < 2.0D || distanceSquared > 16.0D) {
                        continue;
                    }

                    BlockPos feetPos = center.offset(dx, dy, dz);
                    if (!level.getBlockState(feetPos).isAir()) {
                        continue;
                    }

                    BlockPos supportPos = feetPos.below();
                    BlockState support = level.getBlockState(supportPos);
                    VoxelShape supportShape = support.getCollisionShape(level, supportPos);
                    boolean hasSupport = support.isFaceSturdy(level, supportPos, Direction.UP) || !supportShape.isEmpty();
                    if (!hasSupport || isFullBlock(supportShape) != fullBlock) {
                        continue;
                    }
                    spots.add(feetPos.immutable());
                }
            }
        }

        spots.sort(Comparator.comparingDouble((BlockPos pos) -> pos.distToCenterSqr(player.position())));
        return spots;
    }

    private static boolean isFullBlock(VoxelShape shape) {
        return Shapes.block().equals(shape);
    }

    private static BlockPos findUsableSpot(GuardDogEntity dog, ServerPlayer player, ServerLevel level, List<BlockPos> spots) {
        for (BlockPos pos : spots) {
            dog.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
            if (level.noCollision(dog) && dog.hasLineOfSight(player)) {
                return pos;
            }
        }
        return null;
    }
}
