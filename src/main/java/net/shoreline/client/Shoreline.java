package net.shoreline.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.shoreline.client.api.Identifiable;
import net.shoreline.client.api.file.ClientConfiguration;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.init.Managers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Client main class. Handles main client mod initializing of static handler
 * instances and client managers.
 *
 * @author linus
 * @see ShorelineMod
 * @since 1.0
 */
public class Shoreline implements ClientModInitializer, PreLaunchEntrypoint {
    private static final Logger LOGGER = LogManager.getLogger("Shoreline");
    public static final String VERSION = "r1.0.2";

    // Client configuration handler. This master saves/loads the client
    // configuration files which have been saved locally.
    public static ClientConfiguration CONFIG;
    // Client shutdown hooks which will run once when the MinecraftClient
    // game instance is shutdown.
    public static ShutdownHook SHUTDOWN;
    public static Executor EXECUTOR;

    static {
        info("Loading Shoreline...");

        try {
            loadNatives();
        } catch (Throwable t) {
            error("Failed to load Shoreline's dependant libraries.");

            JOptionPane.showMessageDialog(
                    null,
                    "Failed to load Shoreline's dependant libraries.\n\n" + t.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            System.exit(-1);
        }

        //performVersionCheck(VERSION);
    }

    @Override
    public void onPreLaunch() {
        info("Executing pre-launch tasks...");
    }

    @Override
    public void onInitializeClient() {
        init();
    }

    /**
     * Called during {@link ShorelineMod#onInitializeClient()}
     */
    public static void init() {
        // Debug information - required when submitting a crash / bug report
        info("This build of Shoreline is on Git hash {} and was compiled on {}", BuildConfig.HASH, BuildConfig.BUILD_TIME);
        info("Starting preInit ...");

        EXECUTOR = Executors.newFixedThreadPool(1);

        info("Starting init ...");
        Managers.init();

        info("Starting postInit ...");
        CONFIG = new ClientConfiguration();
        Managers.postInit();

        SHUTDOWN = new ShutdownHook();
        Runtime.getRuntime().addShutdownHook(SHUTDOWN);
        CONFIG.loadClient();

        IRCManager.getInstance(); // Create new IRC manager
    }

    private static void loadNatives() throws Throwable {
        String ext = getExt();
    }

    private static String getExt() {
        String os_name = System.getProperty("os.name");

        if (os_name.contains("Windows")) {
            return "dll";
        }

        if (os_name.contains("Linux")) {
            return "so";
        }

        if (os_name.contains("OS X")) {
            return "dylib";
        }

        error("Unsupported OS: {}", os_name);
        throw new IllegalStateException("Unsupported OS: " + os_name);
    }

    private static native Object performVersionCheck(Object currentVersion);

    public static native Object showErrorWindow(Object message);

    public static void info(String message) {
        LOGGER.info(String.format("[Shoreline] %s", message));
    }

    public static void info(String message, Object... params) {
        LOGGER.info(String.format("[Shoreline] %s", message), params);
    }

    public static void info(Identifiable feature, String message) {
        LOGGER.info(String.format("[Shoreline] [%s] %s", feature.getId(), message));
    }

    public static void info(Identifiable feature, String message, Object... params) {
        LOGGER.info(String.format("[Shoreline] [%s] %s", feature.getId(), message), params);
    }

    public static void error(String message) {
        LOGGER.error(String.format("[Shoreline] %s", message));
    }

    public static void error(String message, Object... params) {
        LOGGER.error(String.format("[Shoreline] %s", message), params);
    }

    public static void error(Identifiable feature, String message) {
        LOGGER.error(String.format("[Shoreline] [%s] %s", feature.getId(), message));
    }

    public static void error(Identifiable feature, String message, Object... params) {
        LOGGER.error(String.format("[Shoreline] [%s] %s", feature.getId(), message), params);
    }

    public static InputStream getResource(String name) {
        InputStream is;
        if ((is = (InputStream) getResourceInternal(name)) != null) {
            return is;
        }
        return Shoreline.class.getClassLoader().getResourceAsStream(name);
    }

    private static Object getResourceInternal(String name) {
        return Shoreline.class.getClassLoader().getResourceAsStream(name);
    }
}