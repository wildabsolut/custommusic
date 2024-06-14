package com.absolut.custommusic.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.absolut.custommusic.CustomMusic;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import io.netty.buffer.Unpooled;

public class PlayMusicCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("custommusic")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("play")
                        .then(CommandManager.argument("url", StringArgumentType.string())
                                .executes(PlayMusicCommand::executePlay))));
    }

    private static int executePlay(CommandContext<ServerCommandSource> context) {
        String url = StringArgumentType.getString(context, "url");
        ServerCommandSource source = context.getSource();

        if (!url.endsWith(".ogg") && !url.endsWith(".mp3")) {
            source.sendError(Text.literal("Only .ogg and .mp3 files are supported."));
            return 0;
        }

        new Thread(() -> {
            try {
                File musicFile = downloadFile(url);
                if (musicFile == null) {
                    source.sendError(Text.literal("Failed to download the file."));
                    return;
                }

                MinecraftServer server = source.getServer();
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                    buf.writeString(musicFile.getAbsolutePath());
                    ServerPlayNetworking.send(player, CustomMusic.PLAY_MUSIC_PACKET_ID, buf);
                }

                source.sendFeedback(() -> Text.literal("Playing music from URL: " + url), false);
            } catch (Exception e) {
                source.sendError(Text.literal("Error playing music: " + e.getMessage()));
            }
        }).start();

        return 1;
    }

    private static File downloadFile(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        if (connection.getResponseCode() != 200) {
            return null;
        }

        File tempFile = File.createTempFile("custommusic", urlStr.endsWith(".ogg") ? ".ogg" : ".mp3");
        try (InputStream in = connection.getInputStream(); FileOutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        return tempFile;
    }
}
