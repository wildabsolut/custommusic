package com.absolut.custommusic;

import com.absolut.custommusic.commands.PlayMusicCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class CustomMusic implements ModInitializer {
    public static final Identifier PLAY_MUSIC_PACKET_ID = new Identifier("custommusic", "play_music");
    public static final Identifier MUSIC_SOUND_ID = new Identifier("custommusic", "custom_music");
    public static SoundEvent MUSIC_SOUND_EVENT = SoundEvent.of(MUSIC_SOUND_ID);

    @Override
    public void onInitialize() {
        Registry.register(Registries.SOUND_EVENT, MUSIC_SOUND_ID, MUSIC_SOUND_EVENT);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            PlayMusicCommand.register(dispatcher);
        });
    }
}
