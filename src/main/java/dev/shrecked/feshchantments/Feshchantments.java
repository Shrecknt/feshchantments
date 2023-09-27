package dev.shrecked.feshchantments;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class Feshchantments implements ModInitializer {
    public static final String MOD_ID = "feshchantments";
    public static final Identifier UPDATE_ENCHANTMENTS = new Identifier(MOD_ID, "update_enchantments");

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((networkHandler, packetSender, server) -> {
            ServerPlayerEntity player = networkHandler.player;

            FeshchantmentsState serverState = FeshchantmentsState.getServerState(server);
            PlayerData playerState = FeshchantmentsState.getPlayerState(player);


            PacketByteBuf data = PacketByteBufs.create();
            data.writeMap(
                    playerState.enchants,
                    PacketByteBuf::writeString,
                    PacketByteBuf::writeInt
            );

            System.out.println("Sending enchants: " + playerState.enchants.toString());

            ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(player.getUuid());
            server.execute(() -> {
                if (playerEntity == null) return;
                ServerPlayNetworking.send(playerEntity, UPDATE_ENCHANTMENTS, data);
            });
        });
    }
}
