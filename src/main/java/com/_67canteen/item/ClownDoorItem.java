package com._67canteen.item;

import com._67canteen.CanteenMod;
import com._67canteen.entity.CanteenClownEntity;
import com._67canteen.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Optional;

/** Loads the saved restaurant template when the clown door is used. */
public class ClownDoorItem extends Item {
    private static final ResourceLocation RESTAURANT_ID =
            new ResourceLocation(CanteenMod.MODID, "67_restaurant");

    public ClownDoorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }

        Optional<StructureTemplate> optionalTemplate =
                serverLevel.getStructureManager().get(RESTAURANT_ID);
        if (optionalTemplate.isEmpty()) {
            showFailure(context, "message.the67canteen.clown_door.structure_missing");
            return InteractionResult.FAIL;
        }

        StructureTemplate template = optionalTemplate.get();
        Vec3i size = template.getSize();
        BlockPos clickedPos = context.getClickedPos();

        // Center the template around the clicked block and place its bottom layer above it.
        BlockPos origin = clickedPos.above().offset(-size.getX() / 2, 0, -size.getZ() / 2);
        if (!hasRoom(serverLevel, origin, size)) {
            showFailure(context, "message.the67canteen.clown_door.no_space");
            return InteractionResult.FAIL;
        }

        boolean placed = template.placeInWorld(
                serverLevel,
                origin,
                origin,
                new StructurePlaceSettings().setIgnoreEntities(true),
                serverLevel.getRandom(),
                3
        );
        if (!placed) {
            showFailure(context, "message.the67canteen.clown_door.no_space");
            return InteractionResult.FAIL;
        }

        spawnClown(serverLevel, origin, size);

        Player player = context.getPlayer();
        if (player == null || !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        serverLevel.playSound(null, clickedPos, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 0.8F, 0.9F);
        return InteractionResult.CONSUME;
    }

    private static boolean hasRoom(ServerLevel level, BlockPos origin, Vec3i size) {
        for (int x = 0; x < size.getX(); x++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (!level.getBlockState(pos).isAir() && !level.getBlockState(pos).canBeReplaced()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static void spawnClown(ServerLevel level, BlockPos origin, Vec3i size) {
        CanteenClownEntity clown = ModEntities.CANTEEN_CLOWN.get().create(level);
        if (clown == null) {
            return;
        }

        clown.moveTo(
                origin.getX() + size.getX() / 2.0D,
                origin.getY() + 1.0D,
                origin.getZ() + size.getZ() / 2.0D,
                180.0F,
                0.0F
        );
        clown.setPersistenceRequired();
        level.addFreshEntity(clown);
    }

    private static void showFailure(UseOnContext context, String translationKey) {
        Player player = context.getPlayer();
        if (player != null) {
            player.displayClientMessage(Component.translatable(translationKey), true);
        }
    }
}
