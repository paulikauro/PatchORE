package org.openredstone.patchore.patch

import de.tr7zw.nbtapi.NBT
import de.tr7zw.nbtapi.NBTType
import de.tr7zw.nbtapi.iface.ReadableNBT
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SpawnEggMeta
import org.openredstone.patchore.PatchORE
import org.openredstone.patchore.sendInfo

class SpawnEggsPatch : Listener {
    @EventHandler
    fun onSpawnEggDispense(event: BlockDispenseEvent) {
        if (!event.item.isSpawnEgg) {
            return
        }

        if (isLegal(event.item)) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler
    fun onSpawnEggUse(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) {
            return
        }

        val item = event.item ?: return
        if (!item.isSpawnEgg) {
            return
        }

        if (isLegal(item)) {
            return
        }

        event.player.sendInfo("Yeah, no. Enjoy your fixed egg.")

        event.isCancelled = true
        val replacement = ItemStack(item.type)

        if (event.player.inventory.itemInMainHand.isSpawnEgg) {
            event.player.inventory.setItemInMainHand(replacement)
        } else {
            event.player.inventory.setItemInOffHand(replacement)
        }
    }

    private fun isLegal(item: ItemStack): Boolean = NBT.getComponents<Boolean>(item) { nbt ->
        val entityCompound = nbt.getCompound("minecraft:entity_data") ?: return@getComponents true
        val entityId = entityCompound.get<String>("id")?.removePrefix("minecraft:") ?: return@getComponents false
        val entitySection =
            PatchORE.config.getConfigurationSection("spawneggs.entity.$entityId") ?: return@getComponents false
        if (entitySection.getBoolean("block", false)) return@getComponents false

        for (subKey in entitySection.getKeys(false)) {
            val formattedKey = getCamelCase(subKey)

            when (entityCompound.getType(formattedKey)) {
                NBTType.NBTTagInt -> {
                    val value = entityCompound.getInteger(formattedKey)
                    if (value > entitySection.getInt(subKey)) {
                        return@getComponents false
                    }
                }

                NBTType.NBTTagString -> {
                    val value = entityCompound.getString(formattedKey)
                    if (value == entitySection.getString(subKey)) {
                        return@getComponents false
                    }
                }

                else -> {}
            }
        }
        true
    }

    private inline fun <reified T> ReadableNBT.get(key: String): T? = getOrNull(key, T::class.java)

    private fun getCamelCase(underScore: String): String {
        val camel = Regex("_(.)").replace(underScore) { it.groupValues[1].uppercase() }
        return camel.replaceFirstChar { it.uppercase() }
    }

    private val ItemStack.isSpawnEgg get() = hasItemMeta() && itemMeta is SpawnEggMeta
}
