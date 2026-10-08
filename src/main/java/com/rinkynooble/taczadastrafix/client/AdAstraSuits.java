package com.rinkynooble.taczadastrafix.client;

import earth.terrarium.adastra.common.items.armor.SpaceSuitItem;
import net.minecraft.world.item.ItemStack;

/**
 * This mod's only Ad Astra reference, kept out of the mixin so nothing loads it until
 * {@link com.rinkynooble.taczadastrafix.TaczAdAstraFix} has checked it exists.
 */
public final class AdAstraSuits {
    private AdAstraSuits() {
    }

    /** True for the chest items Ad Astra draws a first-person suit arm for (space suit, netherite space suit, jet suit). */
    public static boolean isSuit(ItemStack chest) {
        return chest.getItem() instanceof SpaceSuitItem;
    }
}
