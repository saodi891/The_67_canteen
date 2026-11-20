package com._67canteen.registry;

import com._67canteen.CanteenMod;
import com._67canteen.item.ClownDoorItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Registers standalone items for 67餐厅. */
public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CanteenMod.MODID);

    // Right-click this item on the ground to manually build the restaurant structure.
    public static final RegistryObject<Item> CLOWN_DOOR = ITEMS.register("clown_door",
            () -> new ClownDoorItem(new Item.Properties().stacksTo(1)));

    // Spawn eggs use the registered entity type and vanilla two-layer spawn-egg rendering.
    public static final RegistryObject<Item> GUARD_DOG_SPAWN_EGG = ITEMS.register("guard_dog_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.GUARD_DOG, 0x2F9D4F, 0x2F9D4F, new Item.Properties()));
    public static final RegistryObject<Item> CANTEEN_CLOWN_SPAWN_EGG = ITEMS.register("67_clown_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.CANTEEN_CLOWN, 0x563873, 0x2F9D4F, new Item.Properties()));

    // Internal icon item for the custom creative tab; its model reuses the status-effect texture.
    // It is intentionally not added to the tab contents, so it only appears as the tab icon.
    public static final RegistryObject<Item> CANTEEN_TAB_ICON = ITEMS.register("canteen_tab_icon",
            () -> new Item(new Item.Properties().stacksTo(1)));

    private ModItems() {}
}
