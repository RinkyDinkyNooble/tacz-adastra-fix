package com.rinkynooble.taczadastrafix.client;

import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.adastra.client.models.armor.SpaceSuitModel;
import earth.terrarium.adastra.common.items.armor.SpaceSuitItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Draws an Ad Astra suit's first-person arm the way Ad Astra's own {@code PlayerRendererMixin} means to,
 * but with the suit arm that matches the hand being drawn. All of this mod's Ad Astra references live
 * here, so nothing loads them until {@link #probe()} has checked they exist.
 *
 * <p>Adapted from Ad Astra (Terrarium, MIT code licence). See NOTICE.
 */
public final class SuitArmRenderer {
    private SuitArmRenderer() {
    }

    /** Throws if this Ad Astra version lacks anything {@link #render} uses. Looks things up, calls nothing. */
    public static void probe() throws ReflectiveOperationException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        lookup.findStatic(SpaceSuitModel.class, "getLayerLocation",
                MethodType.methodType(ModelLayerLocation.class, ItemStack.class));
        lookup.findStatic(SpaceSuitModel.class, "getTextureLocation",
                MethodType.methodType(ResourceLocation.class, ItemStack.class));
        lookup.findConstructor(SpaceSuitModel.class, MethodType.methodType(void.class,
                ModelPart.class, EquipmentSlot.class, ItemStack.class, HumanoidModel.class));
        if (!DyeableLeatherItem.class.isAssignableFrom(SpaceSuitItem.class)) {
            throw new NoSuchMethodException("SpaceSuitItem is no longer dyeable");
        }
    }

    /** Whether Ad Astra has a suit arm for this chest item. If not, Ad Astra's own hook handles it as before. */
    public static boolean hasSuitArm(ItemStack chest) {
        return chest.getItem() instanceof SpaceSuitItem
                && SpaceSuitModel.getLayerLocation(chest) != null
                && SpaceSuitModel.getTextureLocation(chest) != null;
    }

    /**
     * Draws the suit arm for one hand, posed like {@code playerArm}. Same model, render type, light,
     * overlay and dye colour as Ad Astra, so shaders treat it the same.
     */
    public static void render(ItemStack chest, boolean rightHand, ModelPart playerArm,
                              PoseStack poseStack, MultiBufferSource buffer, int light) {
        // Ad Astra bakes a new model each time too; it's a few small objects for one arm.
        HumanoidModel<?> suitModel = new SpaceSuitModel(
                Minecraft.getInstance().getEntityModels().bakeLayer(SpaceSuitModel.getLayerLocation(chest)),
                EquipmentSlot.CHEST, chest, null);
        // Ad Astra compares the player's arm with the new model's arm, which never match, so it always
        // drew the left suit arm. Choose by hand instead.
        ModelPart suitArm = rightHand ? suitModel.rightArm : suitModel.leftArm;
        suitArm.copyFrom(playerArm);

        int color = ((DyeableLeatherItem) chest.getItem()).getColor(chest);
        suitArm.render(poseStack,
                buffer.getBuffer(RenderType.entityTranslucent(SpaceSuitModel.getTextureLocation(chest))),
                light, OverlayTexture.NO_OVERLAY,
                FastColor.ARGB32.red(color) / 255.0F,
                FastColor.ARGB32.green(color) / 255.0F,
                FastColor.ARGB32.blue(color) / 255.0F,
                1.0F);
    }
}
