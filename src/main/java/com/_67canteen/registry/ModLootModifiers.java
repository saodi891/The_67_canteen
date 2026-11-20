package com._67canteen.registry;

import com._67canteen.CanteenMod;
import com._67canteen.loot.AddClownDoorModifier;
import com.mojang.serialization.Codec;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Registers global loot modifier codecs used by the mod. */
public final class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, CanteenMod.MODID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ADD_CLOWN_DOOR =
            GLOBAL_LOOT_MODIFIERS.register("add_clown_door", () -> AddClownDoorModifier.CODEC);

    private ModLootModifiers() {}
}
