package moe.nea.firmament.compat.rei

import io.github.moulberry.repo.data.NEUIngredient
import java.util.stream.Stream
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer
import me.shedaniel.rei.api.common.entry.EntrySerializer
import me.shedaniel.rei.api.common.entry.EntryStack
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext
import me.shedaniel.rei.api.common.entry.type.EntryDefinition
import me.shedaniel.rei.api.common.entry.type.EntryType
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes
import net.minecraft.item.ItemStack
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import moe.nea.firmament.compat.rei.FirmamentReiPlugin.Companion.asItemEntry
import moe.nea.firmament.repo.PetData
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.petData
import moe.nea.firmament.util.skyBlockId

object SBItemEntryDefinition : EntryDefinition<SBItemStack> {
	override fun equals(o1: SBItemStack, o2: SBItemStack, context: ComparisonContext): Boolean {
		return o1.skyblockId == o2.skyblockId && o1.getStackSize() == o2.getStackSize()
	}

	override fun cheatsAs(entry: EntryStack<SBItemStack>?, value: SBItemStack): ItemStack {
		return value.asCopiedItemStack()
	}

	override fun getValueType(): Class<SBItemStack> = SBItemStack::class.java
	override fun getType(): EntryType<SBItemStack> = EntryType.deferred(FirmamentReiPlugin.SKYBLOCK_ITEM_TYPE_ID)

	override fun getRenderer(): EntryRenderer<SBItemStack> = NEUItemEntryRenderer

	override fun getSerializer(): EntrySerializer<SBItemStack> {
		return NEUItemEntrySerializer
	}

	override fun getTagsFor(entry: EntryStack<SBItemStack>?, value: SBItemStack?): Stream<out TagKey<*>>? {
		return Stream.empty()
	}

	override fun asFormattedText(entry: EntryStack<SBItemStack>, value: SBItemStack): Text {
		return VanillaEntryTypes.ITEM.definition.asFormattedText(entry.asItemEntry(), value.asImmutableItemStack())
	}

	override fun hash(entry: EntryStack<SBItemStack>, value: SBItemStack, context: ComparisonContext): Long {
		// Repo items are immutable, and get replaced entirely when loaded from disk
		return value.skyblockId.hashCode() * 31L
	}

	override fun wildcard(entry: EntryStack<SBItemStack>?, value: SBItemStack): SBItemStack {
		return value.copy(stackSize = 1, petData = RepoManager.getPotentialStubPetData(value.skyblockId),
		                  stars = 0, extraLore = listOf())
	}

	override fun normalize(entry: EntryStack<SBItemStack>?, value: SBItemStack): SBItemStack {
		return wildcard(entry, value)
	}

	override fun copy(entry: EntryStack<SBItemStack>?, value: SBItemStack): SBItemStack {
		return value
	}

	override fun isEmpty(entry: EntryStack<SBItemStack>?, value: SBItemStack): Boolean {
		return value.getStackSize() == 0
	}

	override fun getIdentifier(entry: EntryStack<SBItemStack>?, value: SBItemStack): Identifier {
		return value.skyblockId.identifier
	}

	fun getEntry(sbItemStack: SBItemStack): EntryStack<SBItemStack> =
		EntryStack.of(this, sbItemStack)

	fun getEntry(skyblockId: SkyblockId, count: Int = 1): EntryStack<SBItemStack> =
		getEntry(SBItemStack(skyblockId, count))

	fun getEntry(ingredient: NEUIngredient): EntryStack<SBItemStack> =
		getEntry(SkyblockId(ingredient.itemId), count = ingredient.amount.toInt())

	fun getEntry(stack: ItemStack): EntryStack<SBItemStack> =
		getEntry(
			SBItemStack(
				stack.skyBlockId ?: SkyblockId.NULL,
				RepoManager.getNEUItem(stack.skyBlockId ?: SkyblockId.NULL),
				stack.count,
				petData = stack.petData?.let { PetData.fromHypixel(it) }
			)
		)
}