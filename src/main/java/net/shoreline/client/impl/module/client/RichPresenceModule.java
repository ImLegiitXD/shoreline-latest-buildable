package net.shoreline.client.impl.module.client;

import net.minecraft.client.gui.screen.TitleScreen;
import net.shoreline.client.BuildConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ConcurrentModule;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.FinishLoadingEvent;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.eventbus.annotation.EventListener;
import meteordevelopment.discordipc.DiscordIPC;
import meteordevelopment.discordipc.RichPresence;

/**
 * @author ImLegiitXD
 * @since 1.0
 */
public class RichPresenceModule extends ToggleModule {

    private static final long APPLICATION_ID = 1388334762602528818L;

    private final RichPresence presence = new RichPresence();

    Config<Boolean> SHOW_SERVER = register(new BooleanConfig("Show Server", "Shows the server IP in RPC", false));
    Config<Integer> UPDATE_DELAY = register(new NumberConfig<>("Delay", "Delay between RPC updates", 20, 60, 100));
    private int ticks = 0;

    public RichPresenceModule() {
        super("RPC", "Discord Rich Presence integration", ModuleCategory.CLIENT);
    }

    public void onEnable() {
        DiscordIPC.start(APPLICATION_ID, null);
        presence.setStart(System.currentTimeMillis() / 1000L);
        presence.setLargeImage((BuildConfig.BUILD_IDENTIFIER), ("(" + BuildConfig.BUILD_IDENTIFIER + "-" + BuildConfig.HASH + ")"));
    }

    public void onDisable() {
        DiscordIPC.stop();
    }

    @EventListener
    public void onGameFinishedInit(FinishLoadingEvent event)
    {
        if (isEnabled()) {
        onEnable();
        }
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (ticks-- <= 0) {
            updateDetails();
            DiscordIPC.setActivity(presence);
            ticks = UPDATE_DELAY.getValue();
        }
    }

    private void updateDetails() {
        if (mc == null) {
            presence.setDetails("Starting game...");
            presence.setState("(" + BuildConfig.BUILD_IDENTIFIER + "-" + BuildConfig.HASH + ") b" + BuildConfig.BUILD_NUMBER);
            return;
        }

        if (mc.currentScreen instanceof TitleScreen || mc.currentScreen instanceof net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen || mc.currentScreen instanceof net.minecraft.client.gui.screen.world.SelectWorldScreen) {
            presence.setDetails("In Menus");
            presence.setState("(" + BuildConfig.BUILD_IDENTIFIER + "-" + BuildConfig.HASH + ") b" + BuildConfig.BUILD_NUMBER);
            return;
        }

        if (mc.isInSingleplayer()) {
            presence.setDetails("Playing Singleplayer");
            presence.setState("(" + BuildConfig.BUILD_IDENTIFIER + "-" + BuildConfig.HASH + ") b" + BuildConfig.BUILD_NUMBER);
        } else if (mc.getCurrentServerEntry() != null) {
            presence.setDetails(SHOW_SERVER.getValue() ? "Playing " + mc.getCurrentServerEntry().address : "Playing Multiplayer");
            presence.setState("(" + BuildConfig.BUILD_IDENTIFIER + "-" + BuildConfig.HASH + ") b" + BuildConfig.BUILD_NUMBER);
        } else {
            presence.setDetails("Idling");
        }
    }
}