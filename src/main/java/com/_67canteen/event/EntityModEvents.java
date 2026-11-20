package com._67canteen.event;

import com._67canteen.CanteenMod;
import com._67canteen.client.GuardDogRenderer;
import com._67canteen.client.CanteenClownRenderer;
import com._67canteen.entity.CanteenClownEntity;
import com._67canteen.entity.GuardDogEntity;
import com._67canteen.registry.ModEntities;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Registers the guard dog's vanilla wolf attributes and its client-side wolf renderer. */
@Mod.EventBusSubscriber(modid = CanteenMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class EntityModEvents {
    private static final double GUARD_DOG_MOVEMENT_SPEED = 0.4D;

    private EntityModEvents() {}

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.GUARD_DOG.get(), Wolf.createAttributes()
                .add(Attributes.MAX_HEALTH, GuardDogEntity.MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, GUARD_DOG_MOVEMENT_SPEED)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.ATTACK_DAMAGE, 16.0D)
                .build());
        event.put(ModEntities.CANTEEN_CLOWN.get(), CanteenClownEntity.createAttributes().build());
    }

    @Mod.EventBusSubscriber(modid = CanteenMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Client {
        private Client() {}

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.GUARD_DOG.get(), GuardDogRenderer::new);
            event.registerEntityRenderer(ModEntities.CANTEEN_CLOWN.get(), CanteenClownRenderer::new);
        }
    }
}
