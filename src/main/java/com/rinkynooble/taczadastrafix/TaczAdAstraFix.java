package com.rinkynooble.taczadastrafix;

import com.rinkynooble.taczadastrafix.client.SuitArmRenderer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TaczAdAstraFix.MODID)
public class TaczAdAstraFix {
    public static final String MODID = "taczadastrafix";
    private static final Logger LOGGER = LogManager.getLogger("TaCZ Ad Astra Arm Fix");

    /** Set once at client setup. While false the mixin does nothing and Ad Astra draws its arm as before. */
    private static volatile boolean active;

    public TaczAdAstraFix() {
        // Client setup never fires on a dedicated server, where the mod does nothing.
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    public static boolean isActive() {
        return active;
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        // On the main thread, so loading Ad Astra's classes never races with other mods' parallel setup.
        event.enqueueWork(() -> {
            try {
                SuitArmRenderer.probe();
                active = true;
                LOGGER.info("Fix applied: Ad Astra space suits now draw the matching first-person arm.");
            } catch (LinkageError | ReflectiveOperationException e) {
                // A future Ad Astra version without something the fix uses.
                LOGGER.warn("Fix not applied: this Ad Astra version draws suit arms differently ({}). "
                        + "Nothing is changed. If Ad Astra has fixed its first-person suit arm, this mod can be removed.",
                        e.toString());
            }
        });
    }
}
