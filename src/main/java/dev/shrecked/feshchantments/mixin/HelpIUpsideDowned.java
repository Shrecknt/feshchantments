package dev.shrecked.feshchantments.mixin;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class HelpIUpsideDowned {
    @Inject(at = @At("RETURN"), method = "shouldFlipUpsideDown", cancellable = true)
    private static void shouldFlipUpsideDown(LivingEntity entity, CallbackInfoReturnable<Boolean> ci) {
        if (entity instanceof PlayerEntity || entity.hasCustomName()) {
            String name = Formatting.strip(entity.getName().getString());
            if (name == null) return;
            if (name.equalsIgnoreCase("Shrecknt")
                    || name.equalsIgnoreCase("Gildfesh")) {
                ci.setReturnValue(true);
            }
        }
    }
}
