package dev.shrecked.feshchantments.client;

import dev.shrecked.feshchantments.Feshchantments;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;

import java.util.HashMap;
import java.util.Map;

public class FeshchantmentsClient implements ClientModInitializer {
    public static Map<String, Integer> ENCHANTMENTS_MAP = new HashMap<>();

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(Feshchantments.UPDATE_ENCHANTMENTS, (client, handler, buf, responseSender) -> {
            ENCHANTMENTS_MAP = buf.readMap(PacketByteBuf::readString, PacketByteBuf::readInt);
        });
    }
}
