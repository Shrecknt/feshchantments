package dev.shrecked.feshchantments;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FeshchantmentsState extends PersistentState {
    public Map<UUID, PlayerData> players = new HashMap<>();


    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound playersNbt = new NbtCompound();
        players.forEach((uuid, playerData) -> {
            NbtCompound playerNbt = new NbtCompound();

            playerData.enchants.forEach((enchantName, enchantValue) -> {
                playerNbt.put(enchantName, NbtInt.of(enchantValue));
            });

            playersNbt.put(uuid.toString(), playerNbt);
        });
        nbt.put("players", playersNbt);

        return nbt;
    }

    public static FeshchantmentsState createFromNbt(NbtCompound tag) {
        FeshchantmentsState state = new FeshchantmentsState();
        NbtCompound playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            PlayerData playerData = new PlayerData();

            NbtCompound enchants = playersNbt.getCompound(key);
            for (String enchant : enchants.getKeys()) {
                playerData.enchants.put(enchant, enchants.getInt(enchant));
            }

            UUID uuid = UUID.fromString(key);
            state.players.put(uuid, playerData);
        });

        return state;
    }

    public static FeshchantmentsState getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        Type<FeshchantmentsState> type = new Type<>(
                FeshchantmentsState::new,
                FeshchantmentsState::createFromNbt,
                DataFixTypes.PLAYER
        );
        FeshchantmentsState state = persistentStateManager.getOrCreate(
                type,
                Feshchantments.MOD_ID
        );

        state.markDirty();

        return state;
    }

    public static PlayerData getPlayerState(LivingEntity player) {
        MinecraftServer server = player.getWorld().getServer();
        assert server != null;
        FeshchantmentsState serverState = getServerState(server);
        return serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerData());
    }

    public static PlayerData getPlayerState(LivingEntity player, MinecraftServer server) {
        FeshchantmentsState serverState = getServerState(server);
        return serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerData());
    }
}
