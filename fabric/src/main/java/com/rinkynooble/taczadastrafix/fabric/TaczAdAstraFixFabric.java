package com.rinkynooble.taczadastrafix.fabric;

import com.rinkynooble.taczadastrafix.TaczAdAstraFix;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class TaczAdAstraFixFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TaczAdAstraFix.clientSetup(FabricLoader.getInstance().isModLoaded("playeranimator"));
    }
}
