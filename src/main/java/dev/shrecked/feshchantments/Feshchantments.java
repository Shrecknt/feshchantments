package dev.shrecked.feshchantments;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class Feshchantments implements ModInitializer {
    public static final String MOD_ID = "feshchantments";
    public static final Identifier UPDATE_ENCHANTMENTS = new Identifier(MOD_ID, "update_enchantments");

    @Override
    public void onInitialize() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
            if (state.getBlock() == Blocks.GRASS_BLOCK || state.getBlock() == Blocks.DIRT) {
                MinecraftServer server = world.getServer();
                if (server == null) return;
                FeshchantmentsState serverState = FeshchantmentsState.getServerState(server);
                PlayerData playerState = FeshchantmentsState.getPlayerState(player);


                PacketByteBuf data = PacketByteBufs.create();
                data.writeMap(
                        playerState.enchants,
                        PacketByteBuf::writeString,
                        PacketByteBuf::writeInt
                );

                ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(player.getUuid());
                server.execute(() -> {
                    if (playerEntity == null) return;
                    ServerPlayNetworking.send(playerEntity, UPDATE_ENCHANTMENTS, data);
                });
            }
        });
    }
}
