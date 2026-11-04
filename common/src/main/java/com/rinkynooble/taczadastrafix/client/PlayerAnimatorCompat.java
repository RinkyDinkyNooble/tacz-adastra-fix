package com.rinkynooble.taczadastrafix.client;

import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.IPlayerModel;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;

/**
 * playerAnimator (used by TaCZ for its third-person gun animations) hooks vanilla's renderHand just before
 * setupAnim to mark the model as a first-person render, so those animations stay off the first-person arm.
 * The mixin skips renderHand for Ad Astra suits, so it does the same here. Only loaded once
 * {@link com.rinkynooble.taczadastrafix.TaczAdAstraFix} has checked playerAnimator is present.
 */
public final class PlayerAnimatorCompat {
    private PlayerAnimatorCompat() {
    }

    /** Throws if this playerAnimator version lacks anything {@link #prepForFirstPersonRender} uses. */
    public static void probe() throws ReflectiveOperationException {
        IPlayerModel.class.getMethod("playerAnimator_prepForFirstPersonRender");
        IAnimatedPlayer.class.getMethod("playerAnimator_getAnimation");
        AnimationApplier.class.getMethod("getFirstPersonMode");
        FirstPersonMode.class.getMethod("isEnabled");
    }

    /** Same as playerAnimator's own renderHand hook. */
    public static void prepForFirstPersonRender(PlayerModel<?> model, AbstractClientPlayer player) {
        if (model instanceof IPlayerModel playerModel && player instanceof IAnimatedPlayer animatedPlayer
                && !animatedPlayer.playerAnimator_getAnimation().getFirstPersonMode().isEnabled()) {
            playerModel.playerAnimator_prepForFirstPersonRender();
        }
    }
}
