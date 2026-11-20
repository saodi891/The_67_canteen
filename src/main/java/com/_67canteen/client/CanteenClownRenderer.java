package com._67canteen.client;

import com._67canteen.entity.CanteenClownEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** GeckoLib renderer for the invulnerable restaurant clown. */
public class CanteenClownRenderer extends GeoEntityRenderer<CanteenClownEntity> {
    public CanteenClownRenderer(EntityRendererProvider.Context context) {
        super(context, new CanteenClownModel());
        this.shadowRadius = 0.45F;
    }
}
