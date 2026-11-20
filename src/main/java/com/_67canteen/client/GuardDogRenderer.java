package com._67canteen.client;

import com._67canteen.entity.GuardDogEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** GeckoLib renderer that draws the custom guard dog geometry and its wolf-compatible texture. */
public class GuardDogRenderer extends GeoEntityRenderer<GuardDogEntity> {
    public GuardDogRenderer(EntityRendererProvider.Context context) {
        super(context, new GuardDogModel());
        this.shadowRadius = 0.45F;
    }
}
