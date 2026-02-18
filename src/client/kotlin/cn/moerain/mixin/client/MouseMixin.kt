package cn.moerain.mixin.client

import cn.moerain.client.HudRenderer
import net.minecraft.client.Mouse
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(Mouse::class)
class MouseMixin {

    @Inject(method = ["updateMouse"], at = [At("HEAD")], cancellable = true)
    private fun onUpdateMouse(ci: CallbackInfo) {
        if (HudRenderer.isVisible) {
            ci.cancel()
        }
    }

    @Inject(method = ["onMouseButton"], at = [At("HEAD")], cancellable = true)
    private fun onMouseButton(window: Long, button: Int, action: Int, mods: Int, ci: CallbackInfo) {
        if (HudRenderer.isVisible) {
            // We want to allow the HUD to handle clicks in onHudRender, 
            // but prevent them from reaching the game world.
            // Since we're using GLFW directly in onHudRender, we just cancel the standard handler.
            ci.cancel()
        }
    }

    @Inject(method = ["onMouseScroll"], at = [At("HEAD")], cancellable = true)
    private fun onMouseScroll(window: Long, horizontal: Double, vertical: Double, ci: CallbackInfo) {
        if (HudRenderer.isVisible) {
            HudRenderer.handleScroll(vertical)
            ci.cancel()
        }
    }
}
