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
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

@Restriction(require = @Condition(value = "minecraft", versionPredicates = ">=1.21.5"))
@Mixin(PotionContentsComponent.class)
public class PotionContentsComponentMixin1_21_5 {
  private final PotionContentsComponentUtil base = new PotionContentsComponentUtil();

  @Inject(method = "appendTooltip", at = @At("HEAD"), cancellable = true)
  private void overrideAppendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type,
      ComponentsAccess components,
      CallbackInfo ci) {
    Optional<RegistryEntry<Potion>> potionOptional = ((PotionContentsComponent) (Object) this).potion();

    if (potionOptional.isEmpty())
      return;

    PotionContentsComponentUtil.modifyPotionTooltip(base.getPotionEffects((PotionContentsComponent) (Object) this),
        textConsumer,
        (Float) components.getOrDefault(DataComponentTypes.POTION_DURATION_SCALE, 1.0F), context.getUpdateTickRate(),
        ci,
        potionOptional.get().value().getBaseName(), false);
  }
}
