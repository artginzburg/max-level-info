package art.ginzburg.maxlevelinfo.mixin.client;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.registry.entry.RegistryEntry;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Mixin(PotionContentsComponent.class)
public class PotionContentsComponentMixin {

  @SuppressWarnings("unchecked")
  @Inject(method = "buildTooltip", at = @At("HEAD"), cancellable = true)
  private void modifyPotionTooltip(
      Consumer<Text> textConsumer, float durationMultiplier, float tickRate, CallbackInfo ci) {

    Iterable<StatusEffectInstance> effects = List.of(); // Default empty

    // Fetch the effects by calling getEffects() reflectively
    try {
      Method getEffectsMethod = PotionContentsComponent.class.getDeclaredMethod("getEffects");
      getEffectsMethod.setAccessible(true);

      effects = (Iterable<StatusEffectInstance>) getEffectsMethod.invoke(this); // ✅ Use `this`
    } catch (Exception e) {
      e.printStackTrace();
    }

    List<Pair<RegistryEntry<EntityAttribute>, EntityAttributeModifier>> list = Lists.newArrayList();
    boolean isEmpty = true;

    for (StatusEffectInstance effect : effects) {
      isEmpty = false;
      MutableText effectText = Text.translatable(effect.getTranslationKey());
      RegistryEntry<StatusEffect> registryEntry = effect.getEffectType();

      registryEntry.value().forEachAttributeModifier(effect.getAmplifier(),
          (attribute, modifier) -> list.add(new Pair<>(attribute, modifier)));

      if (effect.getAmplifier() > 0) {
        effectText = Text.translatable("potion.withAmplifier", effectText,
            Text.translatable("potion.potency." + effect.getAmplifier()));
      }

      if (!effect.isDurationBelow(20)) {
        int durationTicks = effect.getDuration();
        Text durationText = StatusEffectUtil.getDurationText(effect, durationMultiplier, tickRate);

        // ✅ Add "+" if duration is more than 3 minutes (3600 ticks)
        if (durationTicks > 3600) {
          effectText = Text.literal(effectText.getString() + "+");
        }

        effectText = Text.translatable("potion.withDuration", effectText, durationText);
      }

      textConsumer.accept(effectText.formatted(registryEntry.value().getCategory().getFormatting()));
    }

    if (isEmpty) {
      textConsumer.accept(Text.translatable("effect.none").formatted(Formatting.GRAY));
    }

    // Cancel the original method so only our modified version runs
    ci.cancel();
  }
}
