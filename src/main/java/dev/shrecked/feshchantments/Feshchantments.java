package dev.shrecked.feshchantments;

import dev.shrecked.feshchantments.items.ForgetScrollItem;
import dev.shrecked.feshchantments.items.ScrollItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Feshchantments implements ModInitializer {
    public static final String MOD_ID = "feshchantments";
    public static final Identifier UPDATE_ENCHANTMENTS = new Identifier(MOD_ID, "update_enchantments");
    public static Map<String, Item> CUSTOM_ITEM_MAP = new HashMap<>();

    public static final ScrollItem SCROLL_ITEM
            = Registry.register(Registries.ITEM, new Identifier(MOD_ID, "scroll"), new ScrollItem(new FabricItemSettings()));
    public static final ForgetScrollItem FORGET_SCROLL_ITEM
            = Registry.register(Registries.ITEM, new Identifier(MOD_ID, "scroll_forget"), new ForgetScrollItem(new FabricItemSettings()));

    public static final ItemGroup ITEM_GROUP = Registry.register(Registries.ITEM_GROUP, new Identifier(MOD_ID, "feshchantments_group"),
        FabricItemGroup.builder()
            .icon(() -> new ItemStack(SCROLL_ITEM))
            .displayName(Text.translatable("itemGroup.feshchantments.feshchantments"))
            .build()
    );

    @Override
    public void onInitialize() {
        // this is bad. do not do this. I only did this because I do not know how to do it correctly.
        CUSTOM_ITEM_MAP.put("forget", FORGET_SCROLL_ITEM);
        for (Enchantment enchant : Registries.ENCHANTMENT) {
            String name = getEnchantName(enchant);
            ScrollItem item
                    = Registry.register(Registries.ITEM, new Identifier(MOD_ID, "scroll_" + name), new ScrollItem(new FabricItemSettings(), enchant));
            CUSTOM_ITEM_MAP.put(name, item);
        }

        Optional<RegistryKey<ItemGroup>> ITEM_GROUP_KEY = Registries.ITEM_GROUP.getKey(ITEM_GROUP);
        if (ITEM_GROUP_KEY.isEmpty()) {
            throw new RuntimeException("something fucked up");
        }
        ItemGroupEvents.modifyEntriesEvent(ITEM_GROUP_KEY.get()).register(content -> CUSTOM_ITEM_MAP.values().forEach(content::add));

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

            server.execute(() -> ServerPlayNetworking.send(player, UPDATE_ENCHANTMENTS, data));
        });

        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (!source.isBuiltin()) return;
            modifyTable(tableBuilder, id, EntityType.WANDERING_TRADER.getLootTableId(), "projectile_protection", 1);
            modifyTable(tableBuilder, id, LootTables.END_CITY_TREASURE_CHEST, "mending", 0.1);
        });
    }

    public static void modifyTable(LootTable.Builder tableBuilder, Identifier id, Identifier lootTable, String enchantName, double chance) {
        if (lootTable.equals(id)) {
            LootPool.Builder poolBuilder = LootPool.builder()
                .with(ItemEntry.builder(CUSTOM_ITEM_MAP.get(enchantName)));

            if (chance < 1) {
                poolBuilder = poolBuilder.conditionally(new LootCondition() {
                    @Override
                    public LootConditionType getType() {
                        return LootConditionTypes.RANDOM_CHANCE;
                    }

                    @Override
                    public boolean test(LootContext lootContext) {
                        return lootContext.getRandom().nextBetween(0, (int) (1 / chance) - 1) == 0;
                    }
                });
            }

            tableBuilder.pool(poolBuilder);
        }
    }

    public static String getEnchantName(Enchantment enchant) {
        return enchant
            .getName(0)
            .getString()
            .toLowerCase()
            .replace(' ', '_')
            .replace("_enchantment.level.0", "");
    }
}
