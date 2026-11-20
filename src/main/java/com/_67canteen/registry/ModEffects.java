package com._67canteen.registry;

import com._67canteen.CanteenMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Registers status effects used by 67餐厅. */
public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, CanteenMod.MODID);

    // Marker effect for gameplay rules; its benefit can be configured later.
    public static final RegistryObject<MobEffect> CANTEEN_BUFF = EFFECTS.register("canteen_buff",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xD99B35) {});

    private ModEffects() {}
}
