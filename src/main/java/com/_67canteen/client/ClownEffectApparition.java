package com._67canteen.client;

import com._67canteen.CanteenMod;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.InputStream;
import java.util.Optional;

/** Plays the private seven-frame clown screen effect sent to the affected client. */
@Mod.EventBusSubscriber(modid = CanteenMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClownEffectApparition {
    private static final ResourceLocation EFFECT_TEXTURE = new ResourceLocation(
            CanteenMod.MODID, "textures/entity/67_clown_effect/67_clown_effect.png");
    private static final int FRAME_COUNT = 7;
    private static final int DURATION_TICKS = 40;

    private static boolean active;
    private static int age;
    private static int textureWidth = 1;
    private static int textureHeight = 1;
    private static int frameWidth = 1;
    private static int frameHeight = 1;
    private static boolean horizontalFrames;

    private ClownEffectApparition() {}

    public static void trigger() {
        if (Minecraft.getInstance().player == null) {
            return;
        }

        readTextureLayout();
        age = 0;
        active = true;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !active) {
            return;
        }

        if (Minecraft.getInstance().level == null || ++age >= DURATION_TICKS) {
            active = false;
        }
    }

    public static void render(GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        if (!active || Minecraft.getInstance().player == null) {
            return;
        }

        float elapsed = Mth.clamp(age + partialTick, 0.0F, DURATION_TICKS - 0.001F);
        float progress = elapsed / DURATION_TICKS;
        int frame = Math.min(FRAME_COUNT - 1, (int) (progress * FRAME_COUNT));
        int sourceX = horizontalFrames ? frame * frameWidth : 0;
        int sourceY = horizontalFrames ? 0 : frame * frameHeight;

        float maxWidth = screenWidth * 0.72F;
        float maxHeight = screenHeight * 0.82F;
        float fitScale = Math.min(maxWidth / frameWidth, maxHeight / frameHeight);
        float pulse = 0.88F
                + 0.12F * Mth.sin(progress * Mth.PI)
                + 0.025F * Mth.sin(progress * Mth.PI * 8.0F);
        int renderWidth = Math.max(1, Math.round(frameWidth * fitScale * pulse));
        int renderHeight = Math.max(1, Math.round(frameHeight * fitScale * pulse));
        int wobbleX = Math.round(Mth.sin(progress * Mth.PI * 6.0F) * screenWidth * 0.012F);
        int wobbleY = Math.round(Mth.sin(progress * Mth.PI * 4.0F) * screenHeight * 0.008F);
        int x = (screenWidth - renderWidth) / 2 + wobbleX;
        int y = (screenHeight - renderHeight) / 2 + wobbleY;
        float alpha = progress > 0.85F ? (1.0F - progress) / 0.15F : 1.0F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        graphics.setColor(1.0F, 1.0F, 1.0F, Mth.clamp(alpha, 0.0F, 1.0F));
        graphics.blit(EFFECT_TEXTURE, x, y, renderWidth, renderHeight,
                sourceX, sourceY,
                frameWidth, frameHeight, textureWidth, textureHeight);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void readTextureLayout() {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(EFFECT_TEXTURE);
        if (resource.isEmpty()) {
            useFallbackLayout();
            CanteenMod.LOGGER.warn("Missing clown effect texture: {}", EFFECT_TEXTURE);
            return;
        }

        try (InputStream stream = resource.get().open(); NativeImage image = NativeImage.read(stream)) {
            textureWidth = image.getWidth();
            textureHeight = image.getHeight();

            boolean canBeVertical = textureHeight % FRAME_COUNT == 0;
            boolean canBeHorizontal = textureWidth % FRAME_COUNT == 0;
            if (canBeVertical && (!canBeHorizontal || textureHeight >= textureWidth)) {
                horizontalFrames = false;
                frameWidth = textureWidth;
                frameHeight = textureHeight / FRAME_COUNT;
            } else if (canBeHorizontal) {
                horizontalFrames = true;
                frameWidth = textureWidth / FRAME_COUNT;
                frameHeight = textureHeight;
            } else {
                frameWidth = textureWidth;
                frameHeight = textureHeight;
                horizontalFrames = false;
                CanteenMod.LOGGER.warn(
                        "Clown effect texture must contain 7 equal frames in one horizontal or vertical strip: {}",
                        EFFECT_TEXTURE);
            }
        } catch (Exception exception) {
            useFallbackLayout();
            CanteenMod.LOGGER.error("Could not read clown effect texture {}", EFFECT_TEXTURE, exception);
        }
    }

    private static void useFallbackLayout() {
        textureWidth = 1;
        textureHeight = 1;
        frameWidth = 1;
        frameHeight = 1;
        horizontalFrames = false;
    }
}
