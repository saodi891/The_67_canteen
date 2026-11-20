package com._67canteen.registry;

import com._67canteen.CanteenMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Creative inventory tabs owned by the 67餐厅 mod. */
public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CanteenMod.MODID);

    /** The tab containing every currently registered 67餐厅 item/block item. */
    public static final RegistryObject<CreativeModeTab> CANTEEN_TAB = CREATIVE_MODE_TABS.register(
            "canteen", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.the67canteen.canteen"))
                    // The icon item uses the same 16x16 art as the 67餐厅 status effect.
                    .icon(() -> new ItemStack(ModItems.CANTEEN_TAB_ICON.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.CLOWN_DOOR.get());
                        output.accept(ModItems.GUARD_DOG_SPAWN_EGG.get());
                        output.accept(ModItems.CANTEEN_CLOWN_SPAWN_EGG.get());
                        output.accept(ModBlocks.CURRY_PLATE_ITEM.get());
                    })
                    .build());

    private ModCreativeTabs() {}
}
