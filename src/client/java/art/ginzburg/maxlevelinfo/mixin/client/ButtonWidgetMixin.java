package art.ginzburg.maxlevelinfo.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PressableWidget.class)
public class ButtonWidgetMixin {
  private float animationProgress = 0; // 0 = normal size, 1 = fully scaled
  private boolean wasHovered = false; // Track hover state

  private static final float MAX_SCALE_X = 1.05F; // Maximum button scale
  private static final float MAX_SCALE_Y = 1.2F; // Maximum button scale
  private static final float ANIMATION_SPEED = 0.15F; // Controls animation speed

  private static final ButtonTextures TEXTURES = new ButtonTextures(
      Identifier.ofVanilla("widget/button"),
      Identifier.ofVanilla("widget/button_disabled"),
      Identifier.ofVanilla("widget/button_highlighted"));

  @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
  private void animateButton(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
    PressableWidget self = (PressableWidget) (Object) this;
    boolean isCurrentlyHovered = self.isHovered();

    // Start animation when hovered
    if (self.active) {
      if (isCurrentlyHovered) {
        if (!wasHovered) {
          wasHovered = true; // First time hover detected
        }
        animationProgress = Math.min(animationProgress + delta * ANIMATION_SPEED, 1.0F);
      } else {
        if (wasHovered) {
          wasHovered = false; // First time un-hover detected
        }
        animationProgress = Math.max(animationProgress - delta * ANIMATION_SPEED, 0.0F);
      }
    }

    // Apply easing function (Exponential Ease-Out)
    float easedProgress = easeOutQuart(animationProgress);
    float scaleX = 1.0F + (MAX_SCALE_X - 1.0F) * easedProgress;
    float scaleY = 1.0F + (MAX_SCALE_Y - 1.0F) * easedProgress;

    int originalX = self.getX();
    int originalY = self.getY();
    int width = self.getWidth();
    int height = self.getHeight();

    int scaledWidth = (int) (width * scaleX);
    int scaledHeight = (int) (height * scaleY);
    int offsetX = (scaledWidth - width) / 2;
    int offsetY = (scaledHeight - height) / 2;

    ci.cancel(); // Prevents the original method from running

    float alpha = 1;

    MinecraftClient minecraftClient = MinecraftClient.getInstance();
    context.drawGuiTexture(RenderLayer::getGuiTextured, TEXTURES.get(self.active, self.isSelected()),
        originalX - offsetX, originalY - offsetY, scaledWidth, scaledHeight,
        ColorHelper.getWhite((float) (alpha - (animationProgress / 2))));

    int textColor = self.active ? 16777215 : 10526880;
    self.drawMessage(context, minecraftClient.textRenderer, textColor | MathHelper.ceil(alpha * 255.0F) << 24);
  }

  // Exponential ease-out function
  private float easeOutExpo(float t) {
    return t == 1.0F ? 1.0F : 1.0F - (float) Math.pow(2, -10 * t);
  }

  // Quartic ease-out function (smoother and more balanced)
  private float easeOutQuart(float t) {
    return 1 - (float) Math.pow(1 - t, 4);
  }
}
