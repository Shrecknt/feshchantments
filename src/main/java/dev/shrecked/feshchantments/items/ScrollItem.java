package dev.shrecked.feshchantments.items;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.FoodComponents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.ArrayList;

public class ScrollItem extends Item {
    public ScrollItem(Item.Settings settings) {
        super(settings.food(new FoodComponent(
                0,
                0,
                false,
                true,
                false,
                new ArrayList<>()
        )));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        player.playSound(SoundEvents.BLOCK_WOOL_BREAK, 1.0F, 1.0F);

        ItemStack itemStack = player.getStackInHand(hand);
        player.setCurrentHand(hand);
        return TypedActionResult.consume(itemStack);

        // return TypedActionResult.success(playerEntity.getStackInHand(hand));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity player) {
        player.playSound(SoundEvents.BLOCK_END_PORTAL_SPAWN, 1.0F, 1.0F);
        if (!(player instanceof PlayerEntity) || !((PlayerEntity)player).getAbilities().creativeMode) {
            stack.decrement(1);
        }
        return stack;
    }
}