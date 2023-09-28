package dev.shrecked.feshchantments;

import dev.shrecked.feshchantments.items.ScrollItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Feshchantments implements ModInitializer {
    public static final String MOD_ID = "feshchantments";
    public static final Identifier UPDATE_ENCHANTMENTS = new Identifier(MOD_ID, "update_enchantments");

    public static final ScrollItem SCROLL_ITEM
            = Registry.register(Registries.ITEM, new Identifier(MOD_ID, "scroll"), new ScrollItem(new FabricItemSettings()));

    public static final ItemGroup ITEM_GROUP = Registry.register(Registries.ITEM_GROUP, new Identifier(MOD_ID, "feshchantments_group"),
        FabricItemGroup.builder()
            .icon(() -> new ItemStack(SCROLL_ITEM))
            .displayName(Text.translatable("itemGroup.feshchantments.feshchantments"))
            .build()
    );

    @Override
    public void onInitialize() {
        // this is bad. do not do this. I only did this because I do not know how to do it correctly.
        List<Item> scrollItemList = new ArrayList<>();
        for (Enchantment enchant : Registries.ENCHANTMENT) {
            String name = getEnchantName(enchant);
            ScrollItem item
                    = Registry.register(Registries.ITEM, new Identifier(MOD_ID, "scroll_" + name), new ScrollItem(new FabricItemSettings(), enchant));
            scrollItemList.add(item);
        }

        Optional<RegistryKey<ItemGroup>> ITEM_GROUP_KEY = Registries.ITEM_GROUP.getKey(ITEM_GROUP);
        if (ITEM_GROUP_KEY.isEmpty()) {
            throw new RuntimeException("something fucked up");
        }
        ItemGroupEvents.modifyEntriesEvent(ITEM_GROUP_KEY.get()).register(content -> scrollItemList.forEach(content::add));

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

    public static String getEnchantName(Enchantment enchant) {
        return enchant.getName(0).getString().toLowerCase().replace(' ', '_').replace("_enchantment.level.0", "");
    }
}
