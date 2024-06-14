package com.absolut.custommusic.client;

import com.absolut.custommusic.CustomMusic;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;

import java.io.File;

public class CustommusicClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(CustomMusic.PLAY_MUSIC_PACKET_ID, (client, handler, buf, responseSender) -> {
            String musicFilePath = buf.readString(32767);
            playMusic(client, musicFilePath);
        });
    }

    private void playMusic(MinecraftClient client, String musicFilePath) {
        if (client.player == null || client.world == null) {
            System.err.println("Player or world is null.");
            return;
        }

        try {
            File musicFile = new File(musicFilePath);
            if (!musicFile.exists()) {
                System.out.println("Music file does not exist: " + musicFilePath);
                return;
            }

            client.world.playSound(client.player, client.player.getBlockPos(), CustomMusic.MUSIC_SOUND_EVENT, SoundCategory.MUSIC, 1.0F, 1.0F);
        } catch (Exception e) {
            System.err.println("Failed to play music: " + e.getMessage());
        }
    }
}
