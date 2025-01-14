package art.ginzburg.maxlevelinfo.mixin.client;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {

  @Inject(method = "getName", at = @At("TAIL"), cancellable = true)
  private static void overrideGetName(RegistryEntry<Enchantment> enchantment, int level,
      CallbackInfoReturnable<Text> cir) {

    int maxLevel = enchantment.value().getMaxLevel();

    if (maxLevel == 1 || maxLevel == level)
      return;

    MutableText appendix = Text.literal(" / ").append(Text.translatable("enchantment.level." + maxLevel));

    Texts.setStyleIfAbsent(appendix,
        Style.EMPTY.withColor(Formatting.DARK_GRAY));

    Text customName = cir.getReturnValue().copy()
        .append(appendix);

    cir.setReturnValue(customName);
  }
}
