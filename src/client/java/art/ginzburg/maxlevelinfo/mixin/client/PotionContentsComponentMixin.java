package art.ginzburg.maxlevelinfo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.component.type.OminousBottleAmplifierComponent;
import net.minecraft.component.type.PotionContentsComponent;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;

import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(PotionContentsComponent.class)
public class PotionContentsComponentMixin {
  private static final Text NONE_TEXT = Text.translatable("effect.none").formatted(Formatting.GRAY);

  @Inject(method = "buildTooltip(Ljava/util/function/Consumer;FF)V", at = @At("HEAD"), cancellable = true)
  private void overrideBuildTooltip(Consumer<Text> textConsumer, float durationMultiplier, float tickRate,
      CallbackInfo ci) {
    Optional<RegistryEntry<Potion>> potionOptional = ((PotionContentsComponent) (Object) this).potion();

    if (potionOptional.isEmpty())
      return;

    modifyPotionTooltip(getPotionEffects(), textConsumer, durationMultiplier, tickRate, ci,
        potionOptional.get().value().getBaseName(), false);
  }

  @Inject(method = "buildTooltip(Ljava/lang/Iterable;Ljava/util/function/Consumer;FF)V", at = @At("HEAD"), cancellable = true)
  private static void overrideStaticBuildTooltip(Iterable<StatusEffectInstance> effects, Consumer<Text> textConsumer,
      float durationMultiplier, float tickRate, CallbackInfo ci) {
    if (effects.iterator().hasNext()
        && effects.iterator().next().getTranslationKey().equalsIgnoreCase("effect.minecraft.bad_omen"))
      modifyPotionTooltip(effects, textConsumer, durationMultiplier, tickRate, ci, "", true);
  }

  private Iterable<StatusEffectInstance> getPotionEffects() {
    try {
      PotionContentsComponent self = (PotionContentsComponent) (Object) this;
      return self.getEffects();
    } catch (Exception e) {
      e.printStackTrace();
      return List.of();
    }
  }

  private static Optional<Potion> getPotionVersion(String potionKey) {
    return Registries.POTION.getOptionalValue(Identifier.ofVanilla(potionKey.toLowerCase()));
  }

  private static void modifyPotionTooltip(Iterable<StatusEffectInstance> effects, Consumer<Text> textConsumer,
      float durationMultiplier, float tickRate, CallbackInfo ci,
      String potionOriginalName, boolean isBadOmen) {
    ci.cancel();

    Optional<Potion> potionLongVersion = getPotionVersion("long_" + potionOriginalName);
    Optional<Potion> potionStrongVersion = getPotionVersion("strong_" + potionOriginalName);

    // `bl` in Yarn mappings
    boolean isEmpty = true;

    for (StatusEffectInstance effect : effects) {
      isEmpty = false;
      MutableText effectText = Text.translatable(effect.getTranslationKey());
      RegistryEntry<StatusEffect> registryEntry = effect.getEffectType();

      if (potionLongVersion.isPresent()) {
        effectText.append(Text.literal(isEffectProlonged(effect, potionLongVersion) ? "+" : "-"));
      }

      if (effect.getAmplifier() > 0) {
        effectText = Text.translatable("potion.withAmplifier", effectText,
            Text.translatable("potion.potency." + effect.getAmplifier()));
      }

      if (isBadOmen) {
        appendBadOmenInfo(effect, effectText);
      }

      if (potionStrongVersion.isPresent() && !isEffectStrong(effect, potionStrongVersion)
          && effect.getAmplifier() == 0) {
        effectText.append(Text.literal(" I"));
      }

      if (!effect.isDurationBelow(20)) {
        effectText = Text.translatable("potion.withDuration", effectText,
            StatusEffectUtil.getDurationText(effect, durationMultiplier, tickRate));
      }

      textConsumer.accept(effectText.formatted(registryEntry.value().getCategory().getFormatting()));
    }

    if (isEmpty) {
      textConsumer.accept(NONE_TEXT);
    }

  }

  private static boolean isEffectProlonged(StatusEffectInstance effect, Optional<Potion> potionLongVersion) {
    return potionLongVersion.get().getEffects().stream()
        .anyMatch(e -> e.getEffectType() == effect.getEffectType() && e.getDuration() == effect.getDuration());
  }

  private static boolean isEffectStrong(StatusEffectInstance effect, Optional<Potion> potionStrongVersion) {
    return potionStrongVersion.get().getEffects().stream()
        .anyMatch(e -> e.getEffectType() == effect.getEffectType() && e.getAmplifier() == effect.getAmplifier());
  }

  private static void appendBadOmenInfo(StatusEffectInstance effect, MutableText effectText) {
    if (effect.getAmplifier() == 0) {
      effectText.append(Text.literal(" I"));
    }

    int maxLevel = OminousBottleAmplifierComponent.MAX_VALUE;
    if (effect.getAmplifier() < maxLevel) {
      effectText.append(Text.literal(" / ")).append(Text.translatable("potion.potency." + maxLevel));
    }
  }
}
