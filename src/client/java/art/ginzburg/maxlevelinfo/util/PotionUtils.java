package art.ginzburg.maxlevelinfo.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.component.type.OminousBottleAmplifierComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;

public class PotionUtils {
  private static final Map<StatusEffectKey, Integer> effectToMaxAmplifierCache = new HashMap<>();
  private static boolean cacheInitialized = false;

  private static void initializeCache() {
    if (cacheInitialized)
      return;
    cacheInitialized = true;

    for (Potion potion : Registries.POTION) {
      RegistryEntry<Potion> potionEntry = Registries.POTION.getEntry(potion);
      if (potionEntry == null)
        continue;

      for (StatusEffectInstance effectInstance : potion.getEffects()) {
        StatusEffectKey key = new StatusEffectKey(effectInstance);

        effectToMaxAmplifierCache.merge(key, effectInstance.getAmplifier(), Math::max);
      }
    }

    effectToMaxAmplifierCache.put(new StatusEffectKey(StatusEffects.BAD_OMEN),
        OminousBottleAmplifierComponent.MAX_VALUE);
  }

  public static int getMaxAmplifierForEffect(StatusEffectInstance effect) {
    initializeCache();
    return effectToMaxAmplifierCache.getOrDefault(new StatusEffectKey(effect), -1);
  }

  private static class StatusEffectKey {
    private final String effectId;

    public StatusEffectKey(StatusEffectInstance effectInstance) {
      this.effectId = effectInstance.getEffectType().getIdAsString();
    }

    public StatusEffectKey(RegistryEntry<StatusEffect> effect) {
      this.effectId = effect.getIdAsString();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (!(obj instanceof StatusEffectKey other))
        return false;
      return effectId.equalsIgnoreCase(other.effectId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(effectId);
    }
  }
}
