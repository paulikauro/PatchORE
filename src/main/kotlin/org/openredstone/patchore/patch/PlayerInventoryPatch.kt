package org.openredstone.patchore.patch

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.openredstone.patchore.PatchORE
import org.openredstone.patchore.sendInfo

class PlayerInventoryPatch(plugin: JavaPlugin) :
    PacketAdapter(plugin, ListenerPriority.HIGHEST, PacketType.Play.Client.ENTITY_NBT_QUERY) {

    private val maxNbtBytes = PatchORE.config.getInt("nbt.max_size_bytes")

    override fun onPacketReceiving(event: PacketEvent) {
        if (event.packetType != PacketType.Play.Client.ENTITY_NBT_QUERY) {
            return
        }

        val doubles = event.packet.doubles
        if (doubles.read(0) < maxNbtBytes) {
            return
        }

        event.isCancelled = true
        plugin.server.scheduler.runTask(plugin, Runnable { clearNBT(event.player) })
    }

    private fun clearNBT(player: Player) {
        player.inventory.clear()
        player.sendInfo("Your inventory was cleared due to an NBT overload.")
    }
}
