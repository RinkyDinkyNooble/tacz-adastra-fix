package com.rinkynooble.taczadastrafix.verify.fabric;

import com.rinkynooble.taczadastrafix.verify.VerifyDriver;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class VerifyFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(VerifyDriver::tick);
    }
}
