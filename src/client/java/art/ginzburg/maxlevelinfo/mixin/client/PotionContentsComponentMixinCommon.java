package art.ginzburg.maxlevelinfo.mixin.client;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import art.ginzburg.maxlevelinfo.util.PotionContentsComponentUtil;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;

@Mixin(PotionContentsComponent.class)
public class PotionContentsComponentMixinCommon {

  @Inject(method = "buildTooltip(Ljava/lang/Iterable;Ljava/util/function/Consumer;FF)V", at = @At("HEAD"), cancellable = true)
  private static void overrideStaticBuildTooltip(Iterable<StatusEffectInstance> effects, Consumer<Text> textConsumer,
      float durationMultiplier, float tickRate, CallbackInfo ci) {
    if (effects.iterator().hasNext()
        && effects.iterator().next().getTranslationKey().equalsIgnoreCase("effect.minecraft.bad_omen"))
      PotionContentsComponentUtil.modifyPotionTooltip(effects, textConsumer, durationMultiplier, tickRate, ci, "",
          true);
  }
}
