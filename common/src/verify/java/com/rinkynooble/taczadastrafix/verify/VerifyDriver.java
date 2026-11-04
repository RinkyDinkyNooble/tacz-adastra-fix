package com.rinkynooble.taczadastrafix.verify;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.slf4j.Logger;

/**
 * Dev-only, never shipped. Opens a flat world, holds a TaCZ gun, takes a screenshot of each case below and closes
 * the game. Compare the shots in run/verify/screenshots: the "suit-fix-on" ones should match the "no-suit" ones.
 * Run with {@code gradlew :neoforge:runVerifyClient} or {@code gradlew :fabric:runVerifyClient}.
 */
public final class VerifyDriver {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String GUN = "tacz:modern_kinetic_gun[custom_data={GunId:\"tacz:ak47\",GunFireMode:\"AUTO\","
            + "GunCurrentAmmoCount:30,HasBulletInBarrel:1b}]";
    /** Long enough for the gun to finish drawing and aiming to finish. */
    private static final int SETTLE_TICKS = 80;
    private static final int THIRD_PERSON_TICKS = 20;
    private static final int GIVE_UP_TICKS = 20 * 60 * 5;

    private record Shot(String name, boolean suit, boolean fix, boolean aim) {
    }

    private static final List<Shot> SHOTS = List.of(
            new Shot("1-no-suit", false, true, false),
            new Shot("2-suit-fix-on", true, true, false),
            new Shot("3-suit-fix-off", true, false, false),
            new Shot("4-no-suit-aim", false, true, true),
            new Shot("5-suit-fix-on-aim", true, true, true),
            new Shot("6-suit-fix-off-aim", true, false, true));

    private static boolean started;
    private static boolean inWorld;
    private static int ticks;
    private static int worldTicks;
    private static int next;

    private VerifyDriver() {
    }

    public static void tick(Minecraft mc) {
        if (++ticks > GIVE_UP_TICKS) {
            LOGGER.error("VERIFY FAILED: gave up after {} ticks, {} of {} shots taken.", ticks, next, SHOTS.size());
            mc.stop();
            return;
        }
        if (mc.getOverlay() != null) {
            return;
        }
        if (!started) {
            started = true;
            mc.options.pauseOnLostFocus = false;
            mc.options.tutorialStep = TutorialSteps.NONE;
            mc.options.getSoundSourceOptionInstance(SoundSource.MASTER).set(0.0);
            mc.tell(() -> createWorld(mc));
            return;
        }
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }
        try {
            inWorld(mc, player);
        } catch (ReflectiveOperationException e) {
            LOGGER.error("VERIFY FAILED", e);
            mc.stop();
        }
    }

    private static void createWorld(Minecraft mc) {
        String name = "verify-" + System.currentTimeMillis();
        LevelSettings settings = new LevelSettings(name, GameType.CREATIVE, false, Difficulty.PEACEFUL, true,
                new GameRules(), WorldDataConfiguration.DEFAULT);
        mc.createWorldOpenFlows().createFreshLevel(name, settings, new WorldOptions(1L, false, false),
                access -> access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value()
                        .createWorldDimensions(), mc.screen);
    }

    private static void inWorld(Minecraft mc, LocalPlayer player) throws ReflectiveOperationException {
        if (mc.screen != null) {
            mc.setScreen(null);
        }
        worldTicks++;
        if (!inWorld) {
            if (worldTicks < 100) {
                return;
            }
            inWorld = true;
            worldTicks = 0;
            player.connection.sendCommand("gamerule sendCommandFeedback false");
            player.connection.sendCommand("time set noon");
            player.connection.sendCommand("gamerule doDaylightCycle false");
            player.connection.sendCommand("weather clear");
            player.setYRot(0.0F);
            player.setXRot(0.0F);
            player.connection.sendCommand("item replace entity @s weapon.mainhand with " + GUN);
            // With hold to aim on, TaCZ cancels aiming every tick unless the real key is down.
            Object holdToAim = Class.forName("com.tacz.guns.config.client.KeyConfig").getField("HOLD_TO_AIM").get(null);
            holdToAim.getClass().getMethod("set", Object.class).invoke(holdToAim, false);
            apply(player, SHOTS.get(0));
            return;
        }
        // TaCZ only starts its third-person animations while the player model is drawn in third person.
        if (worldTicks == 1) {
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        } else if (worldTicks == THIRD_PERSON_TICKS) {
            mc.options.setCameraType(CameraType.FIRST_PERSON);
        }
        if (worldTicks == SETTLE_TICKS - 5) {
            mc.gui.getChat().clearMessages(false);
        }
        if (worldTicks < SETTLE_TICKS) {
            return;
        }
        worldTicks = 0;
        if (next < SHOTS.size()) {
            Shot shot = SHOTS.get(next++);
            // A shot taken in the wrong state would look like a pass, so check it really is that state.
            boolean suit = !player.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
            boolean aim = (boolean) operatorClass().getMethod("isAim").invoke(operator(player));
            if (suit != shot.suit() || aim != shot.aim()) {
                LOGGER.error("VERIFY FAILED: {} has suit {} and aim {}.", shot.name(), suit, aim);
                mc.stop();
                return;
            }
            Screenshot.grab(mc.gameDirectory, shot.name() + ".png", mc.getMainRenderTarget(),
                    message -> LOGGER.info("VERIFY shot {}: {}", shot.name(), message.getString()));
            if (next < SHOTS.size()) {
                apply(player, SHOTS.get(next));
            }
        } else {
            LOGGER.info("VERIFY DONE: {} shots.", next);
            mc.stop();
        }
    }

    private static void apply(LocalPlayer player, Shot shot) throws ReflectiveOperationException {
        player.connection.sendCommand(shot.suit()
                ? "item replace entity @s armor.chest with ad_astra:space_suit"
                : "item replace entity @s armor.chest with air");
        Field active = Class.forName("com.rinkynooble.taczadastrafix.TaczAdAstraFix").getDeclaredField("active");
        active.setAccessible(true);
        active.setBoolean(null, shot.fix());
        operatorClass().getMethod("aim", boolean.class).invoke(operator(player), shot.aim());
    }

    private static Class<?> operatorClass() throws ClassNotFoundException {
        return Class.forName("com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator");
    }

    private static Object operator(LocalPlayer player) throws ReflectiveOperationException {
        return operatorClass().getMethod("fromLocalPlayer", LocalPlayer.class).invoke(null, player);
    }
}
