package com._67canteen.client;

import com._67canteen.CanteenMod;
import com._67canteen.entity.GuardDogEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** GeckoLib resources for the custom guard dog geometry, texture, and animations. */
public class GuardDogModel extends GeoModel<GuardDogEntity> {
    @Override
    public ResourceLocation getModelResource(GuardDogEntity animatable) {
        return new ResourceLocation(CanteenMod.MODID, "geo/guard_dog.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GuardDogEntity animatable) {
        return new ResourceLocation(CanteenMod.MODID, "textures/entity/guard_dog.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GuardDogEntity animatable) {
        return new ResourceLocation(CanteenMod.MODID, "animations/guard_dog.animation.json");
    }
}
