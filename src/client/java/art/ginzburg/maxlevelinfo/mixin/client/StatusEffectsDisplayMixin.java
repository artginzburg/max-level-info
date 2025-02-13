package art.ginzburg.maxlevelinfo.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import art.ginzburg.maxlevelinfo.util.PotionUtils;

@Mixin(StatusEffectsDisplay.class)
public class StatusEffectsDisplayMixin {

  @Inject(method = "getStatusEffectDescription", at = @At("TAIL"))
  public void overrideGetStatusEffectDescription(StatusEffectInstance statusEffect, CallbackInfoReturnable<Text> cir) {

    int maxAmplifier = PotionUtils.getMaxAmplifierForEffect(statusEffect);
    if (maxAmplifier <= 0 || statusEffect.getAmplifier() == maxAmplifier)
      return;

    List<Text> siblings = cir.getReturnValue().getSiblings();

    if (statusEffect.getAmplifier() == 0) {
      siblings.add(ScreenTexts.SPACE);
      siblings.add(Text.translatable("enchantment.level." + 1));
    }

    siblings.add(ScreenTexts.SPACE);
    siblings.add(Text.literal("/"));
    siblings.add(ScreenTexts.SPACE);
    siblings.add(Text.translatable("enchantment.level." + (maxAmplifier + 1)));
  }
}
