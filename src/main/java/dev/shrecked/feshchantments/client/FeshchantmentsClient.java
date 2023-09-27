package dev.shrecked.feshchantments.client;

import dev.shrecked.feshchantments.Feshchantments;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

public class FeshchantmentsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(Feshchantments.UPDATE_ENCHANTMENTS, (client, handler, buf, responseSender) -> {
            System.out.println("Received packet: " + buf.toString());
            Map<String, Integer> enchants = buf.readMap(PacketByteBuf::readString, PacketByteBuf::readInt);
            System.out.println("Enchants: " + enchants.toString());

            client.execute(() -> {
                if (client.player == null) return;
                client.player.sendMessage(Text.literal("Enchants: " + enchants.toString()));
            });
        });
    }
}
