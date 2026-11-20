package com._67canteen.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Minimal client Mixin example: copy this pattern and change target, method, and injection point. */
@Mixin(Minecraft.class)
public abstract class ExampleClientMixin {
    // HEAD runs before Minecraft.tick; keep injections small and side-effect-free unless needed.
    @Inject(method = "tick", at = @At("HEAD"))
    private void canteen$beforeClientTick(CallbackInfo callbackInfo) {
        // Example injection point. Add client behavior here.
    }
}
