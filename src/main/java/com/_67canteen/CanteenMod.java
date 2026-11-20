package com._67canteen;

import com._67canteen.registry.ModBlocks;
import com._67canteen.registry.ModItems;
import com._67canteen.registry.ModSounds;
import com._67canteen.registry.ModEffects;
import com._67canteen.registry.ModEntities;
import com._67canteen.registry.ModCreativeTabs;
import com._67canteen.registry.ModLootModifiers;
import com._67canteen.network.ModNetwork;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Main entry point for the 67餐厅 mod. */
@Mod(CanteenMod.MODID)
public class CanteenMod {
    public static final String MODID = "the67canteen";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public CanteenMod() {
        ModNetwork.register();

        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModLootModifiers.GLOBAL_LOOT_MODIFIERS.register(modEventBus);

        // This instance listener is registered explicitly; annotated event subscribers are discovered by Forge.
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("{} is ready!", MODID);
    }
}
