package com._67canteen.registry;

import com._67canteen.CanteenMod;
import com._67canteen.entity.GuardDogEntity;
import com._67canteen.entity.CanteenClownEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Registers custom entity types for 67餐厅. */
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CanteenMod.MODID);

    // Add additional monster EntityTypes here and register their attributes/renderers in the corresponding Forge events.
    public static final RegistryObject<EntityType<GuardDogEntity>> GUARD_DOG = ENTITY_TYPES.register("guard_dog",
            () -> EntityType.Builder.of(GuardDogEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 0.85F)
                    .build("guard_dog"));

    public static final RegistryObject<EntityType<CanteenClownEntity>> CANTEEN_CLOWN = ENTITY_TYPES.register("67_clown",
            () -> EntityType.Builder.of(CanteenClownEntity::new, MobCategory.MONSTER)
                    .sized(0.9F, 2.0F)
                    .clientTrackingRange(12)
                    .updateInterval(3)
                    .build("67_clown"));

    private ModEntities() {}
}
