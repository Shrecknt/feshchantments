package dev.shrecked.feshchantments.items;

import dev.shrecked.feshchantments.FeshchantmentsState;
import dev.shrecked.feshchantments.PlayerData;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.ArrayList;

import static dev.shrecked.feshchantments.Feshchantments.UPDATE_ENCHANTMENTS;

public class ForgetScrollItem extends Item {
    public ForgetScrollItem(Item.Settings settings) {
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

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        player.setCurrentHand(hand);
        return TypedActionResult.consume(player.getStackInHand(hand));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity player) {
        if (player instanceof ServerPlayerEntity player1) {
            MinecraftServer server = world.getServer();
            assert server != null;
            PlayerData playerState = FeshchantmentsState.getPlayerState(player, server);

            playerState.enchants.clear();

            PacketByteBuf data = PacketByteBufs.create();
            data.writeMap(
                    playerState.enchants,
                    PacketByteBuf::writeString,
                    PacketByteBuf::writeInt
            );

            System.out.println("Sending enchants: " + playerState.enchants.toString());
            server.execute(() -> ServerPlayNetworking.send(player1, UPDATE_ENCHANTMENTS, data));
        }

        player.playSound(SoundEvents.BLOCK_END_PORTAL_SPAWN, 1.0F, 1.0F);
        if (!(player instanceof PlayerEntity) || !((PlayerEntity)player).getAbilities().creativeMode) {
            stack.decrement(1);
        }
        return stack;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        // https://github.com/SpongePowered/Mixin/issues/387
        // maybe someday this might get resolved, and then I
        // can do something about the eating sound effect.
        //
        // If it does get resolved, add a new field to the
        // enum `UseAction` and then use a mixin to the
        // `LivingEntity.triggerItemUseEffects` function to
        // handle the new field and use a custom sound.
        return UseAction.EAT;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 80;
    }
}
