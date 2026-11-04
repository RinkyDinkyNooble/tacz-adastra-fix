package com.rinkynooble.taczadastrafix.neoforge;

import com.rinkynooble.taczadastrafix.TaczAdAstraFix;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(TaczAdAstraFix.MODID)
public class TaczAdAstraFixNeoForge {
    public TaczAdAstraFixNeoForge(IEventBus modBus) {
        // Client setup never fires on a dedicated server, where the mod does nothing.
        modBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> TaczAdAstraFix.clientSetup(ModList.get().isLoaded("playeranimator")));
    }
}
