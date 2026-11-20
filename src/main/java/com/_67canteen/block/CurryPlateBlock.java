package com._67canteen.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import com._67canteen.CanteenMod;
import com._67canteen.registry.ModEffects;
import java.util.List;

/** A four-bite curry plate; once tasted, a broken plate leaves no item behind. */
public class CurryPlateBlock extends Block {
    public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, 3);
    private static final ResourceKey<DamageType> INTERNAL_ORGANS_DAMAGE = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(CanteenMod.MODID, "internal_organs"));
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 4, 15);

    public CurryPlateBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(0.0F).noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(BITES, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BITES);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return !level.getBlockState(pos.below()).isAir();
    }

    @Override
    public BlockState updateShape(BlockState state, net.minecraft.core.Direction direction, BlockState neighborState,
                                  net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == net.minecraft.core.Direction.DOWN && neighborState.isAir()) {
            level.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public List<net.minecraft.world.item.ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        // Partially eaten plates cannot be recovered, whether mined or unsupported.
        if (state.getValue(BITES) > 0) {
            return List.of();
        }
        // Keep the untouched plate drop explicit so it works even without a generated loot table.
        return List.of(new ItemStack(com._67canteen.registry.ModBlocks.CURRY_PLATE_ITEM.get()));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        boolean hasNausea = player.hasEffect(MobEffects.CONFUSION);
        boolean hasPoison = player.hasEffect(MobEffects.POISON);
        boolean hasBlindness = player.hasEffect(MobEffects.BLINDNESS);

        // FoodData multiplies saturation by 2, so 0.5 adds exactly one saturation point.
        player.getFoodData().eat(1, 0.5F);
        if (hasNausea && hasPoison && hasBlindness) {
            Holder<DamageType> damageType = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(INTERNAL_ORGANS_DAMAGE);
            DamageSource source = new DamageSource(damageType);
            // Route the kill through normal damage processing so Minecraft uses
            // death.attack.internal_organs for the final death message.
            player.hurt(source, Float.MAX_VALUE);
        } else if (hasNausea && hasPoison) {
            refresh(player, MobEffects.CONFUSION);
            refresh(player, MobEffects.POISON);
            refresh(player, MobEffects.BLINDNESS);
        } else if (hasNausea) {
            refresh(player, MobEffects.CONFUSION);
            refresh(player, MobEffects.POISON);
        } else {
            refresh(player, MobEffects.CONFUSION);
        }

        // Play the vanilla eating sound once for every successful serving.
        level.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.PLAYERS,
                0.8F, 0.9F + level.random.nextFloat() * 0.2F);
        level.gameEvent(net.minecraft.world.level.gameevent.GameEvent.EAT, pos,
                net.minecraft.world.level.gameevent.GameEvent.Context.of(player));

        int bites = state.getValue(BITES);
        if (bites >= 3) {
            // The fourth serving finishes this plate and removes the restaurant effect.
            player.removeEffect(ModEffects.CANTEEN_BUFF.get());
            level.removeBlock(pos, false);
        } else {
            level.setBlock(pos, state.setValue(BITES, bites + 1), 3);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void refresh(Player player, net.minecraft.world.effect.MobEffect effect) {
        player.addEffect(new MobEffectInstance(effect, 200));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
