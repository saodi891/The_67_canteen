package com._67canteen.client;

import com._67canteen.CanteenMod;
import com._67canteen.entity.CanteenClownEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/** Resource paths for the 67小丑 Geo model, texture, and movement animations. */
public class CanteenClownModel extends GeoModel<CanteenClownEntity> {
    @Override
    public ResourceLocation getModelResource(CanteenClownEntity animatable) {
        return new ResourceLocation(CanteenMod.MODID, "geo/67_clown.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CanteenClownEntity animatable) {
        return new ResourceLocation(CanteenMod.MODID, "textures/entity/67_clown.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CanteenClownEntity animatable) {
        return new ResourceLocation(CanteenMod.MODID, "animations/67_clown.animation.json");
    }
}
