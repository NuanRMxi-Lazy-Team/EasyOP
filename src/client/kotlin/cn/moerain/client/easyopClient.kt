package cn.moerain.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

class easyopClient : ClientModInitializer {

    companion object {
        lateinit var toggleKey: KeyBinding
    }

    override fun onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.easyop.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                "category.easyop.general"
            )
        )
        HudRenderCallback.EVENT.register(HudRenderer())
    }
}
