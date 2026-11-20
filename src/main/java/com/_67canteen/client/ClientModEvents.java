package com._67canteen.client;

import com._67canteen.CanteenMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Example client-only mod lifecycle setup; put client renderer and key mapping setup here. */
@Mod.EventBusSubscriber(modid = CanteenMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        CanteenMod.LOGGER.info("Client setup for {}", CanteenMod.MODID);
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("clown_effect", (gui, graphics, partialTick, screenWidth, screenHeight) ->
                ClownEffectApparition.render(graphics, partialTick, screenWidth, screenHeight));
    }
}
