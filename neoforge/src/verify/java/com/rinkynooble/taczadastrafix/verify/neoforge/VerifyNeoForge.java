package com.rinkynooble.taczadastrafix.verify.neoforge;

import com.rinkynooble.taczadastrafix.verify.VerifyDriver;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "taczadastrafixverify", dist = Dist.CLIENT)
public class VerifyNeoForge {
    public VerifyNeoForge() {
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> VerifyDriver.tick(Minecraft.getInstance()));
    }
}
