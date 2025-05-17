package art.ginzburg.maxlevelinfo.mixin.client;

import java.util.Optional;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import art.ginzburg.maxlevelinfo.util.PotionContentsComponentUtil;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

@Restriction(require = @Condition(value = "minecraft", versionPredicates = "<1.21.5"))
@Mixin(PotionContentsComponent.class)
public class PotionContentsComponentMixin1_21_2 {
  private final PotionContentsComponentUtil base = new PotionContentsComponentUtil();

  @Inject(method = "method_47372(Ljava/util/function/Consumer;FF)V", at = @At("HEAD"), cancellable = true)
  private void overrideBuildTooltip(Consumer<Text> textConsumer, float durationMultiplier, float tickRate,
      CallbackInfo ci) {
    Optional<RegistryEntry<Potion>> potionOptional = ((PotionContentsComponent) (Object) this).potion();

    if (potionOptional.isEmpty())
      return;

    PotionContentsComponentUtil.modifyPotionTooltip(base.getPotionEffects((PotionContentsComponent) (Object) this),
        textConsumer, durationMultiplier,
        tickRate, ci,
        potionOptional.get().value().getBaseName(), false);
  }
}
