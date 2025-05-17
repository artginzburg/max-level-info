package art.ginzburg.maxlevelinfo.util;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.OminousBottleAmplifierComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public final class PotionContentsComponentUtil {
  private static final Text NONE_TEXT = Text.translatable("effect.none").formatted(Formatting.GRAY);

  public Iterable<StatusEffectInstance> getPotionEffects(PotionContentsComponent self) {
    try {
      return self.getEffects();
    } catch (Exception e) {
      e.printStackTrace();
      return List.of();
    }
  }

  private static Optional<Potion> getPotionVersion(String potionKey) {
    return Registries.POTION.getOptionalValue(Identifier.ofVanilla(potionKey.toLowerCase()));
  }

  public static void modifyPotionTooltip(Iterable<StatusEffectInstance> effects, Consumer<Text> textConsumer,
      float durationMultiplier, float tickRate, CallbackInfo ci,
      String potionOriginalName, boolean isBadOmen) {
    List<Pair<RegistryEntry<EntityAttribute>, EntityAttributeModifier>> list = Lists
        .<Pair<RegistryEntry<EntityAttribute>, EntityAttributeModifier>>newArrayList();
    ci.cancel();

    Optional<Potion> potionLongVersion = getPotionVersion("long_" + potionOriginalName);
    Optional<Potion> potionStrongVersion = getPotionVersion("strong_" + potionOriginalName);

    boolean isEmpty = true; // `bl` in Yarn mappings

    for (StatusEffectInstance effect : effects) {
      // #region This is taken 1:1 (variable names changed though) from buildTooltip
      // in PotionContentsComponent
      isEmpty = false;
      MutableText effectText = Text.translatable(effect.getTranslationKey());
      RegistryEntry<StatusEffect> registryEntry = effect.getEffectType();
      registryEntry.value().forEachAttributeModifier(effect.getAmplifier(),
          (attribute, modifier) -> list.add(new Pair<>(attribute, modifier)));
      // #endregion

      if (potionLongVersion.isPresent()) {
        effectText.append(Text.literal(isEffectProlonged(effect, potionLongVersion) ? "+" : "-"));
      }

      // #region This is taken 1:1 (variable names changed though) from buildTooltip
      // in PotionContentsComponent
      if (effect.getAmplifier() > 0) {
        effectText = Text.translatable("potion.withAmplifier", effectText,
            Text.translatable("potion.potency." + effect.getAmplifier()));
      }
      // #endregion

      if (isBadOmen) {
        appendBadOmenInfo(effect, effectText);
      }

      if (potionStrongVersion.isPresent() && !isEffectStrong(effect, potionStrongVersion)
          && effect.getAmplifier() == 0) {
        effectText.append(Text.literal(" I"));
      }

      // #region This is taken 1:1 (variable names changed though) from buildTooltip
      // in PotionContentsComponent
      if (!effect.isDurationBelow(20)) {
        effectText = Text.translatable("potion.withDuration", effectText,
            StatusEffectUtil.getDurationText(effect, durationMultiplier, tickRate));
      }

      textConsumer.accept(effectText.formatted(registryEntry.value().getCategory().getFormatting()));
      // #endregion
    }

    // #region This is taken 1:1 (variable names changed though) from buildTooltip
    // in PotionContentsComponent
    if (isEmpty) {
      textConsumer.accept(NONE_TEXT);
    }

    addVanillaTextsAKAWhenApplied(textConsumer, list);
    // #endregion
  }

  /** This is taken 1:1 from buildTooltip in {@link PotionContentsComponent} */
  private static void addVanillaTextsAKAWhenApplied(Consumer<Text> textConsumer,
      List<Pair<RegistryEntry<EntityAttribute>, EntityAttributeModifier>> list) {
    if (!list.isEmpty()) {
      textConsumer.accept(ScreenTexts.EMPTY);
      textConsumer.accept(Text.translatable("potion.whenDrank").formatted(Formatting.DARK_PURPLE));

      for (Pair<RegistryEntry<EntityAttribute>, EntityAttributeModifier> pair : list) {
        EntityAttributeModifier entityAttributeModifier = pair.getSecond();
        double d = entityAttributeModifier.value();
        double e;
        if (entityAttributeModifier.operation() != EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
            && entityAttributeModifier.operation() != EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
          e = entityAttributeModifier.value();
        } else {
          e = entityAttributeModifier.value() * 100.0;
        }

        if (d > 0.0) {
          textConsumer.accept(
              Text.translatable(
                  "attribute.modifier.plus." + entityAttributeModifier.operation().getId(),
                  AttributeModifiersComponent.DECIMAL_FORMAT.format(e),
                  Text.translatable(pair.getFirst().value().getTranslationKey()))
                  .formatted(Formatting.BLUE));
        } else if (d < 0.0) {
          e *= -1.0;
          textConsumer.accept(
              Text.translatable(
                  "attribute.modifier.take." + entityAttributeModifier.operation().getId(),
                  AttributeModifiersComponent.DECIMAL_FORMAT.format(e),
                  Text.translatable(pair.getFirst().value().getTranslationKey()))
                  .formatted(Formatting.RED));
        }
      }
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
