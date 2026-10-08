package com.rinkynooble.taczadastrafix.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.rinkynooble.taczadastrafix.TaczAdAstraFix;
import com.rinkynooble.taczadastrafix.client.AdAstraSuits;
import com.rinkynooble.taczadastrafix.client.PlayerAnimatorCompat;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * First-person arms (vanilla's empty hand, TaCZ's gun hands, Ad Astra's zip gun) all go through
 * {@code renderRightHand}/{@code renderLeftHand}, which fire Forge's arm event and then call the private
 * {@code renderHand}. Ad Astra replaces {@code renderHand} with a suit arm whenever a space suit is worn.
 * That also skips playerAnimator's hook in {@code renderHand}, so TaCZ's third-person gun animations get
 * applied to the first-person arm. With a suit on, this draws the normal arm the way vanilla's
 * {@code renderHand} does, including playerAnimator's step, and skips the call, so Ad Astra's replacement
 * never runs. Anything else runs untouched.
 */
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    @Unique
    private static final String RENDER_HAND = "Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;renderHand("
            + "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I"
            + "Lnet/minecraft/client/player/AbstractClientPlayer;"
            + "Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;)V";

    private PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel<AbstractClientPlayer> model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Shadow
    protected abstract void setModelProperties(AbstractClientPlayer player);

    // After Forge's RenderArmEvent, so a mod that cancels that event still wins.
    @Inject(method = "renderRightHand", at = @At(value = "INVOKE", target = RENDER_HAND), cancellable = true)
    private void taczadastrafix$renderRightHand(PoseStack poseStack, MultiBufferSource buffer, int light,
                                                AbstractClientPlayer player, CallbackInfo ci) {
        if (taczadastrafix$renderPlainArm(poseStack, buffer, light, player, this.getModel().rightArm, this.getModel().rightSleeve)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderLeftHand", at = @At(value = "INVOKE", target = RENDER_HAND), cancellable = true)
    private void taczadastrafix$renderLeftHand(PoseStack poseStack, MultiBufferSource buffer, int light,
                                               AbstractClientPlayer player, CallbackInfo ci) {
        if (taczadastrafix$renderPlainArm(poseStack, buffer, light, player, this.getModel().leftArm, this.getModel().leftSleeve)) {
            ci.cancel();
        }
    }

    /** Draws the arm exactly as vanilla's renderHand does, if an Ad Astra suit is worn. Returns whether it drew. */
    @Unique
    private boolean taczadastrafix$renderPlainArm(PoseStack poseStack, MultiBufferSource buffer, int light,
                                                  AbstractClientPlayer player, ModelPart arm, ModelPart sleeve) {
        if (!TaczAdAstraFix.isActive() || !AdAstraSuits.isSuit(player.getItemBySlot(EquipmentSlot.CHEST))) {
            return false;
        }
        PlayerModel<AbstractClientPlayer> model = this.getModel();
        this.setModelProperties(player);
        model.attackTime = 0.0F;
        model.crouching = false;
        model.swimAmount = 0.0F;
        // Without this, TaCZ's third-person gun animations (via playerAnimator) land on the first-person arm.
        if (TaczAdAstraFix.hasPlayerAnimator()) {
            PlayerAnimatorCompat.prepForFirstPersonRender(model, player);
        }
        model.setupAnim(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        arm.xRot = 0.0F;
        arm.render(poseStack, buffer.getBuffer(RenderType.entitySolid(player.getSkinTextureLocation())), light, OverlayTexture.NO_OVERLAY);
        sleeve.xRot = 0.0F;
        sleeve.render(poseStack, buffer.getBuffer(RenderType.entityTranslucent(player.getSkinTextureLocation())), light, OverlayTexture.NO_OVERLAY);
        return true;
    }
}
