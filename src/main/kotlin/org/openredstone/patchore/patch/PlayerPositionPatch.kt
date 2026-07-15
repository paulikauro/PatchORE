package org.openredstone.patchore.patch

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.openredstone.patchore.sendInfo
import kotlin.math.abs

class PlayerPositionPatch(plugin: JavaPlugin) :
    PacketAdapter(
        plugin, ListenerPriority.HIGHEST,
        PacketType.Play.Client.POSITION,
        PacketType.Play.Client.POSITION_LOOK
    ) {

    override fun onPacketReceiving(event: PacketEvent) {
        if (event.packetType != PacketType.Play.Client.POSITION_LOOK &&
            event.packetType != PacketType.Play.Client.POSITION
        ) {
            return
        }

        val doubles = event.packet.doubles
        val x = doubles.read(0)
        val y = doubles.read(1)
        val z = doubles.read(2)

        if (isValid(x) && isValid(y) && isValid(z)) {
            return
        }

        event.isCancelled = true
        plugin.server.scheduler.runTask(plugin, Runnable { fixPlayer(event.player) })
    }

    private fun fixPlayer(player: Player) {
        player.teleport(player.world.spawnLocation)
        player.sendInfo("You were sent back to spawn due to an invalid location.")
    }

    private companion object {
        fun isValid(d: Double): Boolean = d.isFinite() && abs(d) < 3.0E7
    }
}
