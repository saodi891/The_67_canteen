package com._67canteen.event;

import com._67canteen.CanteenMod;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Example for handling gameplay events on the Forge event bus. */
@Mod.EventBusSubscriber(modid = CanteenMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ExampleForgeEvents {
    private ExampleForgeEvents() {}

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        CanteenMod.LOGGER.info("Player joined: {}", event.getEntity().getGameProfile().getName());
    }
}
