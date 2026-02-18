package cn.moerain

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.Uuids
import net.minecraft.world.GameRules
import java.util.UUID

class easyop : ModInitializer {

    companion object {
        const val MOD_ID = "easyop"
        val KICK_PLAYER_PACKET_ID = Identifier.of(MOD_ID, "kick_player")
        val UPDATE_RULE_PACKET_ID = Identifier.of(MOD_ID, "update_rule")
        val SET_TIME_PACKET_ID = Identifier.of(MOD_ID, "set_time")
        val TELEPORT_PACKET_ID = Identifier.of(MOD_ID, "teleport")
        val LOCATE_PACKET_ID = Identifier.of(MOD_ID, "locate")
    }

    override fun onInitialize() {
        PayloadTypeRegistry.playC2S().register(KickPlayerPayload.ID, KickPlayerPayload.CODEC)
        PayloadTypeRegistry.playC2S().register(UpdateRulePayload.ID, UpdateRulePayload.CODEC)
        PayloadTypeRegistry.playC2S().register(SetTimePayload.ID, SetTimePayload.CODEC)
        PayloadTypeRegistry.playC2S().register(TeleportPayload.ID, TeleportPayload.CODEC)
        PayloadTypeRegistry.playC2S().register(LocatePayload.ID, LocatePayload.CODEC)

        ServerPlayNetworking.registerGlobalReceiver(KickPlayerPayload.ID) { payload, context ->
            val player = context.player()
            val server = player.server
            if (server != null && player.hasPermissionLevel(2)) {
                val targetPlayer = server.playerManager.getPlayer(payload.uuid)
                if (targetPlayer != null) {
                    targetPlayer.networkHandler.disconnect(Text.translatable("message.easyop.kicked_by_admin"))
                    player.sendMessage(Text.translatable("message.easyop.kick_success", targetPlayer.name.string), false)
                } else {
                    player.sendMessage(Text.translatable("message.easyop.player_not_found"), false)
                }
            } else {
                player.sendMessage(Text.translatable("message.easyop.no_permission"), false)
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(UpdateRulePayload.ID) { payload, context ->
            val player = context.player()
            val server = player.server
            if (server != null && player.hasPermissionLevel(2)) {
                val rules = server.gameRules
                var ruleUpdated = false
                rules.accept(object : GameRules.Visitor {
                    override fun <T : GameRules.Rule<T>> visit(key: GameRules.Key<T>, type: GameRules.Type<T>) {
                        if (key.name == payload.ruleName) {
                            val rule = rules.get(key)
                            if (rule is GameRules.BooleanRule) {
                                val newValue = if (payload.value == "true" || payload.value == "false") {
                                    payload.value.toBoolean()
                                } else {
                                    !rule.get()
                                }
                                rule.set(newValue, server)
                                ruleUpdated = true
                            } else if (rule is GameRules.IntRule) {
                                rule.set(payload.value.toIntOrNull() ?: 0, server)
                                ruleUpdated = true
                            }
                        }
                    }
                })

                if (ruleUpdated) {
                    player.sendMessage(Text.translatable("message.easyop.rule_updated", payload.ruleName, payload.value), false)
                } else {
                    player.sendMessage(Text.translatable("message.easyop.rule_not_found", payload.ruleName), false)
                }
            } else {
                player.sendMessage(Text.translatable("message.easyop.no_permission"), false)
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(SetTimePayload.ID) { payload, context ->
            val player = context.player()
            val server = player.server
            if (server != null && player.hasPermissionLevel(2)) {
                val world = player.getWorld()
                if (world is ServerWorld) {
                    world.timeOfDay = payload.time
                    player.sendMessage(Text.translatable("message.easyop.time_set", payload.time), false)
                }
            } else {
                player.sendMessage(Text.translatable("message.easyop.no_permission"), false)
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(TeleportPayload.ID) { payload, context ->
            val player = context.player()
            val server = player.server
            if (server != null && player.hasPermissionLevel(2)) {
                val targetPlayer = server.playerManager.getPlayer(payload.uuid)
                if (targetPlayer != null && player is ServerPlayerEntity) {
                    val targetWorld = targetPlayer.getWorld()
                    if (targetWorld is ServerWorld) {
                        player.teleport(targetWorld, targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(), emptySet(), player.getYaw(), player.getPitch(), true)
                        player.sendMessage(Text.translatable("message.easyop.teleport_success", targetPlayer.name.string), false)
                    }
                } else {
                    player.sendMessage(Text.translatable("message.easyop.player_not_found"), false)
                }
            } else {
                player.sendMessage(Text.translatable("message.easyop.no_permission"), false)
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(LocatePayload.ID) { payload, context ->
            val player = context.player()
            val server = player.server
            if (server != null && player.hasPermissionLevel(2)) {
                val world = player.getWorld() as? ServerWorld ?: return@registerGlobalReceiver
                
                val registryKey = when (payload.type) {
                    "biome" -> RegistryKeys.BIOME
                    "poi" -> RegistryKeys.POINT_OF_INTEREST_TYPE
                    else -> RegistryKeys.STRUCTURE
                }

                val registry = world.registryManager.getOrThrow(registryKey)
                val entry = registry.getEntry(Identifier.of(payload.id))
                
                if (entry.isPresent) {
                    val pos = if (payload.type == "biome" || payload.type == "poi") {
                        null // Biome and POI locating is not fully implemented for this version yet
                    } else {
                        val structureEntry = entry.get() as net.minecraft.registry.entry.RegistryEntry<net.minecraft.world.gen.structure.Structure>
                        world.chunkManager.chunkGenerator.locateStructure(
                            world,
                            net.minecraft.registry.entry.RegistryEntryList.of(structureEntry),
                            player.blockPos,
                            100,
                            false
                        )?.first
                    }

                    if (pos != null) {
                        player.sendMessage(Text.translatable("message.easyop.locate_success", payload.id, pos.x, pos.y, pos.z), false)
                    } else {
                        player.sendMessage(Text.translatable("message.easyop.locate_failed"), false)
                    }
                } else {
                    player.sendMessage(Text.translatable("message.easyop.structure_not_found", payload.id), false)
                }
            } else {
                player.sendMessage(Text.translatable("message.easyop.no_permission"), false)
            }
        }
    }

    data class UpdateRulePayload(val ruleName: String, val value: String) : CustomPayload {
        companion object {
            val ID: CustomPayload.Id<UpdateRulePayload> = CustomPayload.Id(UPDATE_RULE_PACKET_ID)
            val CODEC: PacketCodec<RegistryByteBuf, UpdateRulePayload> = PacketCodec.tuple(
                PacketCodecs.STRING, UpdateRulePayload::ruleName,
                PacketCodecs.STRING, UpdateRulePayload::value,
                ::UpdateRulePayload
            )
        }
        override fun getId(): CustomPayload.Id<out CustomPayload> = ID
    }

    data class KickPlayerPayload(val uuid: UUID) : CustomPayload {
        companion object {
            val ID: CustomPayload.Id<KickPlayerPayload> = CustomPayload.Id(KICK_PLAYER_PACKET_ID)
            private val UUID_CODEC: PacketCodec<RegistryByteBuf, UUID> = Uuids.PACKET_CODEC.cast()
            val CODEC: PacketCodec<RegistryByteBuf, KickPlayerPayload> = PacketCodec.tuple(
                UUID_CODEC, KickPlayerPayload::uuid,
                ::KickPlayerPayload
            )
        }
        override fun getId(): CustomPayload.Id<out CustomPayload> = ID
    }

    data class SetTimePayload(val time: Long) : CustomPayload {
        companion object {
            val ID: CustomPayload.Id<SetTimePayload> = CustomPayload.Id(SET_TIME_PACKET_ID)
            val CODEC: PacketCodec<RegistryByteBuf, SetTimePayload> = PacketCodec.tuple(
                PacketCodecs.VAR_LONG, SetTimePayload::time,
                ::SetTimePayload
            )
        }
        override fun getId(): CustomPayload.Id<out CustomPayload> = ID
    }

    data class TeleportPayload(val uuid: UUID) : CustomPayload {
        companion object {
            val ID: CustomPayload.Id<TeleportPayload> = CustomPayload.Id(TELEPORT_PACKET_ID)
            private val UUID_CODEC: PacketCodec<RegistryByteBuf, UUID> = Uuids.PACKET_CODEC.cast()
            val CODEC: PacketCodec<RegistryByteBuf, TeleportPayload> = PacketCodec.tuple(
                UUID_CODEC, TeleportPayload::uuid,
                ::TeleportPayload
            )
        }
        override fun getId(): CustomPayload.Id<out CustomPayload> = ID
    }

    data class LocatePayload(val type: String, val id: String) : CustomPayload {
        companion object {
            val ID: CustomPayload.Id<LocatePayload> = CustomPayload.Id(LOCATE_PACKET_ID)
            val CODEC: PacketCodec<RegistryByteBuf, LocatePayload> = PacketCodec.tuple(
                PacketCodecs.STRING, LocatePayload::type,
                PacketCodecs.STRING, LocatePayload::id,
                ::LocatePayload
            )
        }
        override fun getId(): CustomPayload.Id<out CustomPayload> = ID
    }
}
