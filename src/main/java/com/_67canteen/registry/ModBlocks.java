package com._67canteen.registry;

import com._67canteen.CanteenMod;
import com._67canteen.block.CurryPlateBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Registers blocks and their matching inventory items. */
public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CanteenMod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CanteenMod.MODID);

    // Curry plate is a thin support-dependent block; its inventory icon uses a separate item texture.
    public static final RegistryObject<Block> CURRY_PLATE = BLOCKS.register("curry_plate", CurryPlateBlock::new);
    public static final RegistryObject<Item> CURRY_PLATE_ITEM = ITEMS.register("curry_plate",
            () -> new BlockItem(CURRY_PLATE.get(), new Item.Properties()));

    private ModBlocks() {}
}
