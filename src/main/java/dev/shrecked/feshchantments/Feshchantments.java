package dev.shrecked.feshchantments;

import dev.shrecked.feshchantments.items.ScrollItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.Optional;

public class Feshchantments implements ModInitializer {
    public static final String MOD_ID = "feshchantments";
    public static final Identifier UPDATE_ENCHANTMENTS = new Identifier(MOD_ID, "update_enchantments");

    public static final ScrollItem SCROLL_ITEM = new ScrollItem(new FabricItemSettings()
            .maxCount(1)
            .rarity(Rarity.EPIC)
    );

    public static final ItemGroup ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(SCROLL_ITEM))
            .displayName(Text.translatable("itemGroup.feshchantments.feshchantments"))
            .build();

    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "scroll"), SCROLL_ITEM);
        Registry.register(Registries.ITEM_GROUP, new Identifier(MOD_ID, "feshchantments_group"), ITEM_GROUP);

        // this is bad. do not do this. I only did this because I do not know how to do it correctly.
        Optional<RegistryKey<ItemGroup>> ITEM_GROUP_KEY = Registries.ITEM_GROUP.getKey(ITEM_GROUP);
        if (ITEM_GROUP_KEY.isEmpty()) {
            throw new RuntimeException("something fucked up");
        }

        ItemGroupEvents.modifyEntriesEvent(ITEM_GROUP_KEY.get()).register(content -> content.add(SCROLL_ITEM));

        ServerPlayConnectionEvents.JOIN.register((networkHandler, packetSender, server) -> {
            ServerPlayerEntity player = networkHandler.getPlayer();

            // FeshchantmentsState serverState = FeshchantmentsState.getServerState(server);
            PlayerData playerState = FeshchantmentsState.getPlayerState(player);


            PacketByteBuf data = PacketByteBufs.create();
            data.writeMap(
                    playerState.enchants,
                    PacketByteBuf::writeString,
                    PacketByteBuf::writeInt
            );

            System.out.println("Sending enchants: " + playerState.enchants.toString());

            server.execute(() -> ServerPlayNetworking.send(player, UPDATE_ENCHANTMENTS, data));
        });
    }
}
