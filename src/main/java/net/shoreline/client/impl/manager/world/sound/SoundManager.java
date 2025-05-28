package net.shoreline.client.impl.manager.world.sound;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.shoreline.client.util.Globals;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * @author linus
 * @since 1.0
 */
public class SoundManager implements Globals {
    private static final Set<String> REGISTERED_SOUND_FILES = new HashSet<>();

    public static final SoundEvent GUI_CLICK = registerSound("gui_click");

    // PM Sounds
    public static final SoundEvent TWITTER = registerSound("twitter");
    public static final SoundEvent IOS = registerSound("ios");
    public static final SoundEvent DISCORD = registerSound("discord");
    public static final SoundEvent STEAM = registerSound("steam");

    private static SoundEvent registerSound(String name) {
        registerSoundFile(name + ".ogg");
        Identifier id = Identifier.of("shoreline", name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    private static void registerSoundFile(String soundFile) {
        REGISTERED_SOUND_FILES.add(soundFile);
    }

    private static InputStream loadResource(String resourcePath) {
        try {
            return SoundManager.class.getClassLoader().getResourceAsStream(resourcePath);
        } catch (Exception e) {
            return null;
        }
    }

    public void playSound(SoundEvent sound) {
        playSound(sound, 1.2f, 0.75f);
    }

    public void playSound(SoundEvent sound, float volume, float pitch) {
        if (mc.player != null) {
            mc.executeSync(() -> mc.player.playSound(sound, volume, pitch));
        }
    }

    public static Set<String> getRegisteredSoundFiles() {
        return new HashSet<>(REGISTERED_SOUND_FILES);
    }
}