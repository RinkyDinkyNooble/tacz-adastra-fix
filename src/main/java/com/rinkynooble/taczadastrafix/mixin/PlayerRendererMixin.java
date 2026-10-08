package com.rinkynooble.taczadastrafix.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.rinkynooble.taczadastrafix.TaczAdAstraFix;
import com.rinkynooble.taczadastrafix.client.SuitArmRenderer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * First-person arms (vanilla's empty hand, TaCZ's gun hands, Ad Astra's zip gun) all go through
 * {@code renderRightHand}/{@code renderLeftHand}, which call the private {@code renderHand}. Ad Astra
 * replaces {@code renderHand} for space suits but always draws the suit's left arm. With a suit on, this
 * draws the matching suit arm here instead and skips {@code renderHand}. Anything else runs untouched.
 */
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel<AbstractClientPlayer> model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Shadow
    protected abstract void setModelProperties(AbstractClientPlayer player);

    @Inject(method = "renderRightHand", at = @At("HEAD"), cancellable = true)
    private void taczadastrafix$renderRightHand(PoseStack poseStack, MultiBufferSource buffer, int light,
                                                AbstractClientPlayer player, CallbackInfo ci) {
        taczadastrafix$renderSuitArm(poseStack, buffer, light, player, true, ci);
    }

    @Inject(method = "renderLeftHand", at = @At("HEAD"), cancellable = true)
    private void taczadastrafix$renderLeftHand(PoseStack poseStack, MultiBufferSource buffer, int light,
                                               AbstractClientPlayer player, CallbackInfo ci) {
        taczadastrafix$renderSuitArm(poseStack, buffer, light, player, false, ci);
    }

    @Unique
    private void taczadastrafix$renderSuitArm(PoseStack poseStack, MultiBufferSource buffer, int light,
                                              AbstractClientPlayer player, boolean rightHand, CallbackInfo ci) {
        if (!TaczAdAstraFix.isActive()) {
            return;
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!SuitArmRenderer.hasSuitArm(chest)) {
            return;
        }

        // Pose the player model as vanilla's renderHand (and Ad Astra) does.
        PlayerModel<AbstractClientPlayer> model = this.getModel();
        this.setModelProperties(player);
        model.attackTime = 0.0F;
        model.crouching = false;
        model.swimAmount = 0.0F;
        model.setupAnim(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        ModelPart playerArm = rightHand ? model.rightArm : model.leftArm;
        playerArm.xRot = 0.0F;

        SuitArmRenderer.render(chest, rightHand, playerArm, poseStack, buffer, light);
        ci.cancel();
    }
}
