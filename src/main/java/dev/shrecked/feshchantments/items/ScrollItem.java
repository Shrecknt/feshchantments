package dev.shrecked.feshchantments.items;

import dev.shrecked.feshchantments.Feshchantments;
import dev.shrecked.feshchantments.FeshchantmentsState;
import dev.shrecked.feshchantments.PlayerData;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static dev.shrecked.feshchantments.Feshchantments.UPDATE_ENCHANTMENTS;
import static dev.shrecked.feshchantments.Feshchantments.getEnchantName;
import static dev.shrecked.feshchantments.client.FeshchantmentsClient.ENCHANTMENTS_MAP;

public class ScrollItem extends Item {
    @Nullable
    public Enchantment enchantment;

    public ScrollItem(Item.Settings settings) {
        super(settings
            .maxCount(1)
            .rarity(Rarity.EPIC)
            .food(new FoodComponent(
                0,
                0,
                false,
                true,
                false,
                new ArrayList<>()
            ))
        );
    }

    public ScrollItem(Item.Settings settings, @Nullable Enchantment enchantment) {
        this(settings);
        this.enchantment = enchantment;
    }

    @Override
    public void appendTooltip(ItemStack stack1, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (stack1.getItem() instanceof ScrollItem stack) {
            if (stack.enchantment == null) return;
            tooltip.add(Text.translatable(stack.enchantment.getTranslationKey()).formatted(Formatting.GRAY));
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        player.setCurrentHand(hand);
        return TypedActionResult.consume(player.getStackInHand(hand));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity player) {
        if (this.enchantment != null && player instanceof PlayerEntity player1) {
            String enchantName = getEnchantName(this.enchantment);
            int enchantLevel = ENCHANTMENTS_MAP.getOrDefault(enchantName, 0);
            if (enchantLevel >= this.enchantment.getMaxLevel()) {
                player.playSound(SoundEvents.ITEM_TRIDENT_RETURN, 1.0F, 1.0F);
                player1.sendMessage(Text.literal("§cThe enchantment §r" + enchantName.replace('_', ' ') + "§c is already at max level"), true);
                return stack;
            }
        }

        if (this.enchantment != null && player instanceof ServerPlayerEntity player1) {
            MinecraftServer server = world.getServer();
            assert server != null;
            PlayerData playerState = FeshchantmentsState.getPlayerState(player, server);

            String enchantName = getEnchantName(this.enchantment);
            int enchantLevel = playerState.enchants.getOrDefault(enchantName, 0);

            if (enchantLevel >= this.enchantment.getMaxLevel()) {
                return stack;
            }

            player1.sendMessage(Text.literal("§6Max level of §r" + enchantName.replace('_', ' ') + "§6 increased to §r" + (enchantLevel + 1)), true);

            playerState.enchants.put(enchantName, enchantLevel + 1);

            PacketByteBuf data = PacketByteBufs.create();
            data.writeMap(
                    playerState.enchants,
                    PacketByteBuf::writeString,
                    PacketByteBuf::writeInt
            );

            server.execute(() -> ServerPlayNetworking.send(player1, UPDATE_ENCHANTMENTS, data));
        }

        player.playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 1.0F);
        if (!(player instanceof PlayerEntity) || !((PlayerEntity)player).getAbilities().creativeMode) {
            stack.decrement(1);
        }
        return stack;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.EAT;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 80;
    }

    @Override
    public SoundEvent getEatSound() {
        return Feshchantments.WRITING_SOUND_EVENT;
    }
}