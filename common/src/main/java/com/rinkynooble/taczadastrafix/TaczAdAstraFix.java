package com.rinkynooble.taczadastrafix;

import com.rinkynooble.taczadastrafix.client.AdAstraSuits;
import com.rinkynooble.taczadastrafix.client.PlayerAnimatorCompat;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** The startup check both loaders share. Each loader's entry point calls {@link #clientSetup} once. */
public final class TaczAdAstraFix {
    public static final String MODID = "taczadastrafix";
    private static final Logger LOGGER = LogManager.getLogger("TaCZ Ad Astra Arm Fix");

    /** Set once at client setup. While false the mixin does nothing and Ad Astra draws its suit arm as before. */
    private static volatile boolean active;
    /** Set once at client setup, when playerAnimator is installed and has what the fix calls. */
    private static volatile boolean playerAnimator;

    private TaczAdAstraFix() {
    }

    public static boolean isActive() {
        return active;
    }

    public static boolean hasPlayerAnimator() {
        return playerAnimator;
    }

    /** Runs on the main thread, so loading other mods' classes never races with their parallel setup. */
    public static void clientSetup(boolean playerAnimatorInstalled) {
        try {
            // Runs the same check the mixin uses, which loads Ad Astra's SpaceSuitItem.
            AdAstraSuits.isSuit(ItemStack.EMPTY);
        } catch (LinkageError e) {
            // A future Ad Astra version without SpaceSuitItem.
            LOGGER.warn("Fix not applied: this Ad Astra version has no SpaceSuitItem ({}). Nothing is changed.",
                    e.toString());
            return;
        }
        if (playerAnimatorInstalled) {
            try {
                PlayerAnimatorCompat.probe();
                playerAnimator = true;
            } catch (LinkageError | ReflectiveOperationException e) {
                LOGGER.warn("This playerAnimator version is different ({}). Its third-person animations may still "
                        + "show on first-person arms in an Ad Astra suit.", e.toString());
            }
        }
        active = true;
        LOGGER.info("Fix applied: with an Ad Astra space suit on, first-person arms are drawn as normal arms{}.",
                playerAnimator ? ", without playerAnimator's third-person animations" : "");
    }
}
