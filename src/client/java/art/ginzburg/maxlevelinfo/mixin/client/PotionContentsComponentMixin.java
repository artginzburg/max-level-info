package art.ginzburg.maxlevelinfo.mixin.client;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
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
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
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

    // Access the PotionContentsComponent's potion field
    PotionContentsComponent potionContentsComponent = (PotionContentsComponent) (Object) this;

    // Retrieve the Optional<RegistryEntry<Potion>> and unwrap it safely
    Optional<RegistryEntry<Potion>> potionOptional = potionContentsComponent.potion();

    if (potionOptional.isEmpty()) {
      return; // No potion, exit early
    }

    // Get the actual Potion from the RegistryEntry
    RegistryEntry<Potion> potionRegistryEntry = potionOptional.get();
    Potion potionOriginal = potionRegistryEntry.value();

    String potionOriginalName = potionOriginal.getBaseName();
    String possiblePotionLongKey = "LONG_" + potionOriginalName.toUpperCase(); // "LONG_NIGHT_VISION"

    // Fetch the potion from the Potions class
    RegistryEntry<Potion> potionLongVersion = null;
    try {
      // Use reflection to get the potion object from Potions class
      potionLongVersion = (RegistryEntry<Potion>) Potions.class.getDeclaredField(possiblePotionLongKey)
          .get(null);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      // No matching field in Potions class, or failed to access it
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
        Text durationText = StatusEffectUtil.getDurationText(effect, durationMultiplier, tickRate);

        // If we have a potion, check its effects
        if (potionLongVersion != null) {
          boolean isProlonged = false;
          for (StatusEffectInstance potionLongEffect : potionLongVersion.value().getEffects()) {
            if (potionLongEffect.getEffectType() == effect.getEffectType()
                && potionLongEffect.getDuration() == effect.getDuration()) {
              isProlonged = true;
              break; // No need to check more effects
            }
          }

          if (isProlonged) {
            effectText.append(Text.literal("+"));
          }
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
