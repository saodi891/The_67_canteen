package com._67canteen.registry;

import com._67canteen.CanteenMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Registers sound event identifiers; add matching audio files and sounds.json assets as needed. */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, CanteenMod.MODID);

    // Register additional sound ids here, then define their files in assets/the67canteen/sounds.json.
    public static final RegistryObject<SoundEvent> SAMPLE_SOUND = SOUND_EVENTS.register("sample_sound",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(CanteenMod.MODID, "sample_sound")));

    private ModSounds() {}
}
