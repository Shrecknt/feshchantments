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
import net.minecraft.sound.SoundEvent;
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

    public static final Identifier WRITING_SOUND_ID = new Identifier("feshchantments:writing");
    public static SoundEvent WRITING_SOUND_EVENT = SoundEvent.of(WRITING_SOUND_ID);

    @Override
    public void onInitialize() {
        Registry.register(Registries.SOUND_EVENT, Feshchantments.WRITING_SOUND_ID, Feshchantments.WRITING_SOUND_EVENT);

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

            // Modify loot tables to add a chance for
            // enchanted scrolls, chance is from 0 to
            // 1 where 0 means impossible to obtain
            // and 1 means guaranteed.

            addLootItem(tableBuilder, id, LootTables.UNDERWATER_RUIN_SMALL_CHEST, "aqua_affinity", 0.3);
            addLootItem(tableBuilder, id, EntityType.CAVE_SPIDER.getLootTableId(), "bane_of_arthropods", 0.005);
            addLootItem(tableBuilder, id, EntityType.CREEPER.getLootTableId(), "blast_protection", 0.01);
            addLootItem(tableBuilder, id, LootTables.UNDERWATER_RUIN_SMALL_CHEST, "channeling", 0.15);
            addLootItem(tableBuilder, id, LootTables.PILLAGER_OUTPOST_CHEST, "curse_of_vanishing", 0.4);
            addLootItem(tableBuilder, id, LootTables.PILLAGER_OUTPOST_CHEST, "curse_of_binding", 0.4);
            addLootItem(tableBuilder, id, LootTables.UNDERWATER_RUIN_BIG_CHEST, "depth_strider", 0.2);
            addLootItem(tableBuilder, id, LootTables.VILLAGE_MASON_CHEST, "efficiency", 0.3);
            addLootItem(tableBuilder, id, LootTables.ANCIENT_CITY_ICE_BOX_CHEST, "feather_falling", 0.75);
            addLootItem(tableBuilder, id, EntityType.BLAZE.getLootTableId(), "fire_aspect", 1);
            addLootItem(tableBuilder, id, EntityType.MAGMA_CUBE.getLootTableId(), "fire_protection", 0.05);
            addLootItem(tableBuilder, id, EntityType.STRIDER.getLootTableId(), "flame", 0.03);
            addLootItem(tableBuilder, id, EntityType.WITHER.getLootTableId(), "fortune", 1);
            addLootItem(tableBuilder, id, EntityType.STRAY.getLootTableId(), "frost_walker", 0.032);
            addLootItem(tableBuilder, id, EntityType.DROWNED.getLootTableId(), "impaling", 0.027);
            addLootItem(tableBuilder, id, LootTables.JUNGLE_TEMPLE_DISPENSER_CHEST, "infinity", 0.6);
            addLootItem(tableBuilder, id, EntityType.IRON_GOLEM.getLootTableId(), "knockback", 0.024);
            addLootItem(tableBuilder, id, EntityType.WITHER_SKELETON.getLootTableId(), "looting", 0.02);
            addLootItem(tableBuilder, id, EntityType.GUARDIAN.getLootTableId(), "loyalty", 0.08);
            addLootItem(tableBuilder, id, EntityType.PUFFERFISH.getLootTableId(), "luck_of_the_sea", 0.2);
            addLootItem(tableBuilder, id, EntityType.SALMON.getLootTableId(), "lure", 0.2);
            addLootItem(tableBuilder, id, LootTables.END_CITY_TREASURE_CHEST, "mending", 0.1);
            addLootItem(tableBuilder, id, LootTables.VILLAGE_FLETCHER_CHEST, "multishot", 0.2);
            addLootItem(tableBuilder, id, EntityType.PILLAGER.getLootTableId(), "impaling", 0.027);
            addLootItem(tableBuilder, id, EntityType.SKELETON.getLootTableId(), "power", 0.005);
            addLootItem(tableBuilder, id, EntityType.WANDERING_TRADER.getLootTableId(), "projectile_protection", 1);
            addLootItem(tableBuilder, id, LootTables.ANCIENT_CITY_CHEST, "protection", 0.1);
            // punch
            // quick charge
            // respiration
            addLootItem(tableBuilder, id, EntityType.SQUID.getLootTableId(), "riptide", 0.01);
            addLootItem(tableBuilder, id, Blocks.SPAWNER.getLootTableId(), "sharpness", 1);
            addLootItem(tableBuilder, id, EntityType.ENDERMAN.getLootTableId(), "silk_touch", 0.01);
            // smite
            addLootItem(tableBuilder, id, LootTables.BASTION_TREASURE_CHEST, "soul_speed", 0.5);
            modifyTable(tableBuilder, id, LootTables.TRAIL_RUINS_RARE_ARCHAEOLOGY, "sweeping_edge", 2);
            addLootItem(tableBuilder, id, EntityType.WARDEN.getLootTableId(), "swift_sneak", 1);
            addLootItem(tableBuilder, id, EntityType.ELDER_GUARDIAN.getLootTableId(), "thorns", 1);
            addLootItem(tableBuilder, id, LootTables.VILLAGE_TOOLSMITH_CHEST, "unbreaking", 0.3);
        });
    }

    public static void addLootItem(LootTable.Builder tableBuilder, Identifier id, Identifier lootTable, String enchantName, double chance) {
        if (lootTable.equals(id)) {
            ItemEntry.Builder<?> entry = ItemEntry.builder(CUSTOM_ITEM_MAP.get(enchantName));

            if (chance < 1) {
                entry.conditionally(() -> new LootCondition() {
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

            tableBuilder.pool(LootPool.builder().with(entry));
        }
    }

    public static void modifyTable(LootTable.Builder tableBuilder, Identifier id, Identifier lootTable, String enchantName, int weight) {
        if (lootTable.equals(id)) {
            tableBuilder.modifyPools((LootPool.Builder modifier) -> {
                modifier.with(ItemEntry.builder(CUSTOM_ITEM_MAP.get(enchantName)).weight(weight));
            });
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
