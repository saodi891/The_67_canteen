package com._67canteen.network;

import com._67canteen.CanteenMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

/** Sends private client-side visual triggers for effects applied by the clown. */
public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CanteenMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static boolean registered;

    private ModNetwork() {}

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        CHANNEL.registerMessage(
                0,
                EffectApparitionS2CPacket.class,
                (message, buffer) -> {},
                buffer -> new EffectApparitionS2CPacket(),
                (message, contextSupplier) -> {
                    var context = contextSupplier.get();
                    context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                            Dist.CLIENT,
                            () -> () -> com._67canteen.client.ClownEffectApparition.trigger()
                    ));
                    context.setPacketHandled(true);
                },
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    public static void sendEffectApparition(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new EffectApparitionS2CPacket());
    }

    private static final class EffectApparitionS2CPacket {}
}
