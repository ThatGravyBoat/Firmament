package moe.nea.firmament.repo

import com.mojang.serialization.Dynamic
import io.github.cottonmc.cotton.gui.client.CottonHud
import io.github.moulberry.repo.IReloadable
import io.github.moulberry.repo.NEURepository
import io.github.moulberry.repo.data.NEUItem
import java.io.PrintWriter
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.io.path.absolutePathString
import kotlin.io.path.writer
import net.minecraft.SharedConstants
import net.minecraft.client.resource.language.I18n
import net.minecraft.datafixer.Schemas
import net.minecraft.datafixer.TypeReferences
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.text.Text
import moe.nea.firmament.Firmament
import moe.nea.firmament.util.LegacyTagParser
import moe.nea.firmament.util.appendLore
import moe.nea.firmament.util.skyblockId

object ItemCache : IReloadable {
    val dfuLog = Path.of("logs/dfulog.txt")
    private val cache: MutableMap<String, ItemStack> = ConcurrentHashMap()
    private val df = Schemas.getFixer()
    private val dfuHandle = PrintWriter(dfuLog.writer())
    var isFlawless = true
        private set

    private fun NEUItem.get10809CompoundTag(): NbtCompound = NbtCompound().apply {
        put("tag", LegacyTagParser.parse(nbttag))
        putString("id", minecraftItemId)
        putByte("Count", 1)
        putShort("Damage", damage.toShort())
    }

    private fun NbtCompound.transformFrom10809ToModern(): NbtCompound? =
        try {
            df.update(
                TypeReferences.ITEM_STACK,
                Dynamic(NbtOps.INSTANCE, this),
                -1,
                SharedConstants.getGameVersion().saveVersion.id
            ).value as NbtCompound
        } catch (e: Exception) {
            if (isFlawless)
                Firmament.logger.error("Failed to run data fixer an item. Check ${dfuLog.absolutePathString()} for more information")
            isFlawless = false
            e.printStackTrace(dfuHandle)
            null
        }

    fun brokenItemStack(neuItem: NEUItem?): ItemStack {
        return ItemStack(Items.PAINTING).apply {
            setCustomName(Text.literal(neuItem?.displayName ?: "null"))
            appendLore(listOf(Text.translatable("firmament.repo.brokenitem", neuItem?.skyblockItemId)))
        }
    }

    private fun NEUItem.asItemStackNow(): ItemStack {
        try {
            val oldItemTag = get10809CompoundTag()
            val modernItemTag = oldItemTag.transformFrom10809ToModern()
                ?: return brokenItemStack(this)
            val itemInstance = ItemStack.fromNbt(modernItemTag)
            if (itemInstance.nbt?.contains("Enchantments") == true) {
                itemInstance.enchantments.add(NbtCompound())
            }
            return itemInstance
        } catch (e: Exception) {
            e.printStackTrace()
            return brokenItemStack(this)
        }
    }

    fun NEUItem?.asItemStack(): ItemStack {
        if (this == null) return brokenItemStack(null)
        var s = cache[this.skyblockItemId]
        if (s == null) {
            s = asItemStackNow()
            cache[this.skyblockItemId] = s
        }
        return s
    }

    fun NEUItem.getIdentifier() = skyblockId.identifier


    var job: Job? = null

    override fun reload(repository: NEURepository) {
        val j = job
        if (j != null && j.isActive) {
            j.cancel()
        }
        cache.clear()
        isFlawless = true

        job = Firmament.coroutineScope.launch {
            val items = repository.items?.items
            if (items == null) {
                CottonHud.remove(RepoManager.progressBar)
                return@launch
            }
            val recacheItems = I18n.translate("firmament.repo.cache")
            RepoManager.progressBar.reportProgress(recacheItems, 0, items.size)
            CottonHud.add(RepoManager.progressBar)
            var i = 0
            items.values.forEach {
                it.asItemStack() // Rebuild cache
                RepoManager.progressBar.reportProgress(recacheItems, i++, items.size)
            }
            CottonHud.remove(RepoManager.progressBar)
        }
    }
}