package cn.moerain.client

import cn.moerain.easyop
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

class HudRenderer : HudRenderCallback {

    private val ruleIds = listOf(
        "commandBlockOutput",
        "commandBlocksEnabled",
        "doDaylightCycle",
        "doEntityDrops",
        "doFireTick",
        "doImmediateRespawn",
        "doInsomnia",
        "doLimitedCrafting",
        "doMobLoot",
        "doMobSpawning",
        "doTileDrops",
        "doWeatherCycle",
        "drowningDamage",
        "fallDamage",
        "fireDamage",
        "freezeDamage",
        "functionCommandLimit",
        "keepInventory",
        "locatorBar",
        "maxCommandChainLength",
        "mobGriefing",
        "naturalRegeneration",
        "playersSleepingPercentage",
        "projectilesCanBreakBlocks",
        "pvp",
        "randomTickSpeed",
        "recipesUnlock",
        "respawnBlocksExplode",
        "sendCommandFeedback",
        "showBorderEffect",
        "showCoordinates",
        "showDaysPlayed",
        "showDeathMessages",
        "showRecipeMessages",
        "showTags",
        "spawnRadius",
        "tntExplodes"
    )

    private val intRuleIds = setOf(
        "functionCommandLimit",
        "maxCommandChainLength",
        "playersSleepingPercentage",
        "randomTickSpeed",
        "spawnRadius"
    )

    enum class Tab(val titleKey: String) {
        FAST_SETTINGS("tab.easyop.fast_settings"),
        PLAYER_LIST("tab.easyop.player_list"),
        RULES("tab.easyop.game_rules")
    }

    companion object {
        var isVisible = false
            private set
        
        fun toggleVisibility() {
            val client = MinecraftClient.getInstance()
            isVisible = !isVisible
            if (isVisible) {
                client.mouse.unlockCursor()
            } else {
                client.mouse.lockCursor()
            }
        }

        fun forceHide() {
            isVisible = false
        }

        private var instance: HudRenderer? = null

        fun handleScroll(vertical: Double) {
            instance?.let { renderer ->
                if (renderer.currentTab == Tab.RULES) {
                    val client = MinecraftClient.getInstance()
                    val maxScroll = (renderer.ruleIds.size * renderer.ruleRowHeight - (client.window.scaledHeight - renderer.padding * 4)).coerceAtLeast(0)
                    renderer.scrollOffset = (renderer.scrollOffset - vertical * 10).toInt().coerceIn(0, maxScroll)
                }
            }
        }
    }

    init {
        instance = this
    }

    private var currentTab = Tab.FAST_SETTINGS
    private var lastAltState = false
    private var lastClickTime = 0L

    // Sidebar dimensions
    private val sidebarWidth = 120
    private val padding = 10
    private val buttonHeight = 20
    private var scrollOffset = 0
    private val ruleRowHeight = 22
    private var selectedRule: String? = null
    private var inputBuffer = ""

    override fun onHudRender(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        val client = MinecraftClient.getInstance()
        
        if (client.currentScreen == null) {
            while (easyopClient.toggleKey.wasPressed()) {
                toggleVisibility()
            }
        } else {
            if (isVisible) {
                forceHide()
            }
        }

        if (!isVisible) return

        if (selectedRule != null && client.currentScreen == null) {
            // Very basic keyboard input for integer rules
            for (i in GLFW.GLFW_KEY_0..GLFW.GLFW_KEY_9) {
                if (GLFW.glfwGetKey(client.window.handle, i) == GLFW.GLFW_PRESS) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastClickTime > 150) {
                        inputBuffer += (i - GLFW.GLFW_KEY_0).toString()
                        lastClickTime = currentTime
                    }
                }
            }
            if (GLFW.glfwGetKey(client.window.handle, GLFW.GLFW_KEY_BACKSPACE) == GLFW.GLFW_PRESS) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime > 150 && inputBuffer.isNotEmpty()) {
                    inputBuffer = inputBuffer.substring(0, inputBuffer.length - 1)
                    lastClickTime = currentTime
                }
            }
            if (GLFW.glfwGetKey(client.window.handle, GLFW.GLFW_KEY_ENTER) == GLFW.GLFW_PRESS) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime > 200) {
                    if (inputBuffer.isNotEmpty()) {
                        ClientPlayNetworking.send(easyop.UpdateRulePayload(selectedRule!!, inputBuffer))
                    }
                    selectedRule = null
                    lastClickTime = currentTime
                }
            }
        }

        val screenWidth = client.window.scaledWidth
        val screenHeight = client.window.scaledHeight

        // Background
        drawContext.fill(0, 0, screenWidth, screenHeight, 0x80000000.toInt())

        // Sidebar
        val mouseX = (client.mouse.x * screenWidth / client.window.width).toInt()
        val mouseY = (client.mouse.y * screenHeight / client.window.height).toInt()

        Tab.entries.forEachIndexed { index, tab ->
            val y = padding + index * (buttonHeight + 5)
            val isHovered = mouseX in padding until (padding + sidebarWidth) && 
                            mouseY in y until (y + buttonHeight)
            
            val color = if (isHovered) 0xFFA0FFA0.toInt() else 0xFFFFFFFF.toInt()
            drawContext.drawText(client.textRenderer, Text.translatable(tab.titleKey), padding, y + (buttonHeight - 8) / 2, color, false)
            
            if (isHovered && GLFW.glfwGetMouseButton(client.window.handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime > 200) {
                    currentTab = tab
                    lastClickTime = currentTime
                }
            }
        }

        // Content Area
        val contentX = sidebarWidth + padding * 2
        drawContent(drawContext, contentX, padding, mouseX, mouseY)
    }

    private fun drawContent(drawContext: DrawContext, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        val client = MinecraftClient.getInstance()
        when (currentTab) {
            Tab.FAST_SETTINGS -> {
                var currentY = y
                drawContext.drawText(client.textRenderer, Text.translatable("gui.easyop.time_control"), x, currentY, 0xFFFFFFFF.toInt(), false)
                currentY += 15
                
                val times = listOf(
                    "gui.easyop.time.sunrise" to 0L,
                    "gui.easyop.time.day" to 1000L,
                    "gui.easyop.time.noon" to 6000L,
                    "gui.easyop.time.sunset" to 12000L,
                    "gui.easyop.time.night" to 13000L,
                    "gui.easyop.time.midnight" to 18000L
                )

                times.forEachIndexed { index, pair ->
                    val bx = x + (index % 3) * 85
                    val by = currentY + (index / 3) * 25
                    drawButton(drawContext, bx, by, 80, 20, Text.translatable(pair.first), mouseX, mouseY) {
                        ClientPlayNetworking.send(easyop.SetTimePayload(pair.second))
                    }
                }
                
                currentY += 55
                drawContext.drawText(client.textRenderer, Text.translatable("gui.easyop.locate_structure"), x, currentY, 0xFFFFFFFF.toInt(), false)
                currentY += 15
                
                val structures = listOf(
                    Triple("structure", "minecraft:village", "village"),
                    Triple("structure", "minecraft:bastion_remnant", "bastion_remnant"),
                    Triple("structure", "minecraft:fortress", "fortress"),
                    Triple("structure", "minecraft:stronghold", "stronghold"),
                    Triple("structure", "minecraft:mansion", "mansion"),
                    Triple("structure", "minecraft:monument", "monument"),
                    Triple("biome", "minecraft:desert", "desert"),
                    Triple("poi", "minecraft:librarian", "librarian")
                )

                structures.forEachIndexed { index, triple ->
                    val bx = x + (index % 3) * 85
                    val by = currentY + (index / 3) * 25
                    drawButton(drawContext, bx, by, 80, 20, Text.translatable("gui.easyop.locate." + triple.third), mouseX, mouseY) {
                        ClientPlayNetworking.send(easyop.LocatePayload(triple.first, triple.second))
                    }
                }
                
                currentY += 55
                drawContext.drawText(client.textRenderer, Text.translatable("gui.easyop.mob_spawning"), x, currentY, 0xFFFFFFFF.toInt(), false)
                currentY += 15
                val btnWidth = 60
                drawButton(drawContext, x, currentY, btnWidth, 20, Text.literal("TRUE"), mouseX, mouseY) {
                    ClientPlayNetworking.send(easyop.UpdateRulePayload("doMobSpawning", "true"))
                }
                drawButton(drawContext, x + btnWidth + 5, currentY, btnWidth, 20, Text.literal("FALSE"), mouseX, mouseY) {
                    ClientPlayNetworking.send(easyop.UpdateRulePayload("doMobSpawning", "false"))
                }
            }
            Tab.PLAYER_LIST -> {
                drawContext.drawText(client.textRenderer, Text.translatable("gui.easyop.online_players"), x, y, 0xFFFFFFFF.toInt(), false)
                val players = client.networkHandler?.playerList ?: emptyList()
                players.forEachIndexed { index, entry ->
                    val playerY = y + 20 + index * 12
                    val name = entry.profile.name
                    
                    drawContext.drawText(client.textRenderer, Text.literal(name), x, playerY, 0xFFFFFFFF.toInt(), false)

                    // Kick button
                    drawButton(drawContext, x + 160, playerY - 2, 40, 10, Text.translatable("gui.easyop.kick"), mouseX, mouseY) {
                        ClientPlayNetworking.send(easyop.KickPlayerPayload(entry.profile.id))
                    }
                    
                    // TP button
                    drawButton(drawContext, x + 205, playerY - 2, 40, 10, Text.translatable("gui.easyop.tp"), mouseX, mouseY) {
                        ClientPlayNetworking.send(easyop.TeleportPayload(entry.profile.id))
                    }
                }
            }
            Tab.RULES -> {
                val world = client.world
                if (world == null) {
                    drawContext.drawText(client.textRenderer, Text.translatable("gui.easyop.rules_not_available"), x, y, 0xFFFFFFFF.toInt(), false)
                    return
                }

                val viewHeight = client.window.scaledHeight - padding * 4
                val totalContentHeight = ruleIds.size * ruleRowHeight
                val maxScroll = (totalContentHeight - viewHeight).coerceAtLeast(0)

                // Scrollbar background
                if (totalContentHeight > viewHeight) {
                    val scrollbarX = client.window.scaledWidth - padding
                    val scrollbarWidth = 4
                    drawContext.fill(scrollbarX, padding * 2, scrollbarX + scrollbarWidth, client.window.scaledHeight - padding * 2, 0x40FFFFFF.toInt())
                    
                    val thumbHeight = (viewHeight.toFloat() / totalContentHeight * viewHeight).coerceAtLeast(10f).toInt()
                    val thumbY = (padding * 2 + (scrollOffset.toFloat() / maxScroll * (viewHeight - thumbHeight))).toInt()
                    drawContext.fill(scrollbarX, thumbY, scrollbarX + scrollbarWidth, thumbY + thumbHeight, 0xFFFFFFFF.toInt())
                }

                ruleIds.forEachIndexed { index, ruleName ->
                    val rowY = y + 20 + index * ruleRowHeight - scrollOffset
                    
                    if (rowY < y + 15 || rowY > client.window.scaledHeight - padding * 2) return@forEachIndexed

                    // Draw Description
                    val descriptionKey = "gamerule.easyop.$ruleName"
                    drawContext.drawText(client.textRenderer, Text.translatable(descriptionKey), x, rowY + 5, 0xFFFFFFFF.toInt(), false)

                    // Draw Control
                    val controlX = x + 250
                    val controlWidth = 80
                    
                    val isBoolean = ruleName !in intRuleIds

                    if (isBoolean) {
                        val btnWidth = (controlWidth - 5) / 2
                        // TRUE button
                        drawButton(drawContext, controlX, rowY, btnWidth, buttonHeight, Text.literal("TRUE"), mouseX, mouseY) {
                            ClientPlayNetworking.send(easyop.UpdateRulePayload(ruleName, "true"))
                        }
                        // FALSE button
                        drawButton(drawContext, controlX + btnWidth + 5, rowY, btnWidth, buttonHeight, Text.literal("FALSE"), mouseX, mouseY) {
                            ClientPlayNetworking.send(easyop.UpdateRulePayload(ruleName, "false"))
                        }
                    } else {
                        val text = if (selectedRule == ruleName) inputBuffer else "EDIT"
                        
                        drawButton(drawContext, controlX, rowY, controlWidth, buttonHeight, Text.literal(text), mouseX, mouseY) {
                            selectedRule = ruleName
                            inputBuffer = ""
                        }
                    }
                }
            }
        }
    }

    private fun drawButton(drawContext: DrawContext, x: Int, y: Int, width: Int, height: Int, text: Text, mouseX: Int, mouseY: Int, onClick: () -> Unit) {
        val client = MinecraftClient.getInstance()
        val isHovered = mouseX in x until (x + width) && mouseY in y until (y + height)
        val color = if (isHovered) 0xFFA0FFA0.toInt() else 0xFFFFFFFF.toInt()
        
        drawContext.fill(x, y, x + width, y + height, 0x40FFFFFF.toInt())
        drawContext.drawCenteredTextWithShadow(client.textRenderer, text, x + width / 2, y + (height - 8) / 2, color)

        if (isHovered && GLFW.glfwGetMouseButton(client.window.handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > 200) {
                onClick()
                lastClickTime = currentTime
            }
        }
    }
}
