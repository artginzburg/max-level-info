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
import org.spongepowered.asm.mixin.injection.Desc;

import net.minecraft.component.type.OminousBottleAmplifierComponent;
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
  @Inject(target = @Desc( //
      value = "buildTooltip", //
      args = { Consumer.class, float.class, float.class } //
  ), //
      at = @At("HEAD"), cancellable = true //
  )
  private void overrideBuildTooltip(
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

    modifyBasicPotionTooltip(effects, textConsumer, durationMultiplier, tickRate, ci, potionOriginalName, false);
  }

  /**
   * Gets called for non-standard potions, such as Ominous Bottle or Suspicious
   * Stew
   */
  @Inject(target = @Desc(value = "buildTooltip", args = { Iterable.class,
      Consumer.class, float.class, float.class }), at = @At("HEAD"), cancellable = true)
  private static void overrideStaticBuildTooltip(Iterable<StatusEffectInstance> effects,
      Consumer<Text> textConsumer,
      float durationMultiplier, float tickRate, CallbackInfo ci) {

    boolean isBadOmen = false;
    for (StatusEffectInstance effect : effects) {
      if (effect.getTranslationKey().equalsIgnoreCase("effect.minecraft.bad_omen")) {
        isBadOmen = true;
        break;
      }
    }

    if (isBadOmen) {
      ci.cancel();
      modifyBasicPotionTooltip(effects, textConsumer, durationMultiplier, tickRate, ci, "", isBadOmen);
    }
  }

  @SuppressWarnings("unchecked")
  private static void modifyBasicPotionTooltip(Iterable<StatusEffectInstance> effects,
      Consumer<Text> textConsumer,
      float durationMultiplier, float tickRate, CallbackInfo ci, String potionOriginalName, boolean isBadOmen) {

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

    String possiblePotionStrongKey = "STRONG_" +
        potionOriginalName.toUpperCase();

    RegistryEntry<Potion> potionStrongVersion = null;
    try {
      potionStrongVersion = (RegistryEntry<Potion>) Potions.class.getDeclaredField(possiblePotionStrongKey)
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

        effectText.append(Text.literal(isProlonged ? "+" : "-"));
      }

      if (effect.getAmplifier() > 0) {
        effectText = Text.translatable("potion.withAmplifier", effectText,
            Text.translatable("potion.potency." + effect.getAmplifier()));
      }

      if (isBadOmen) {
        if (effect.getAmplifier() == 0) {
          effectText.append(Text.literal(" I"));
        }

        int maxLevel = OminousBottleAmplifierComponent.MAX_VALUE;

        if (effect.getAmplifier() < maxLevel) {
          effectText.append(Text.literal(" / ")).append(Text.translatable("potion.potency." + maxLevel));
        }
      }

      if (potionStrongVersion != null) {
        boolean isStrong = false;
        for (StatusEffectInstance potionStrongEffect : potionStrongVersion.value().getEffects()) {
          if (potionStrongEffect.getEffectType() == effect.getEffectType()
              && potionStrongEffect.getAmplifier() == effect.getAmplifier()) {
            isStrong = true;
            break;
          }
        }

        if (!isStrong && effect.getAmplifier() == 0) {
          effectText.append(Text.literal(" I"));
        }
      }

      if (!effect.isDurationBelow(20)) {
        Text durationText = StatusEffectUtil.getDurationText(effect, durationMultiplier, tickRate);

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
