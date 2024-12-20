
package moe.nea.firmament.features.texturepack

import com.google.gson.JsonObject
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import moe.nea.firmament.features.texturepack.predicates.AndPredicate
import moe.nea.firmament.features.texturepack.predicates.DisplayNamePredicate
import moe.nea.firmament.features.texturepack.predicates.ExtraAttributesPredicate
import moe.nea.firmament.features.texturepack.predicates.ItemPredicate
import moe.nea.firmament.features.texturepack.predicates.LorePredicate
import moe.nea.firmament.features.texturepack.predicates.NotPredicate
import moe.nea.firmament.features.texturepack.predicates.OrPredicate
import moe.nea.firmament.features.texturepack.predicates.PetPredicate
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

object CustomModelOverrideParser {
    object FirmamentRootPredicateSerializer : KSerializer<FirmamentModelPredicate> {
        val delegateSerializer = kotlinx.serialization.json.JsonObject.serializer()
        override val descriptor: SerialDescriptor
            get() = SerialDescriptor("FirmamentModelRootPredicate", delegateSerializer.descriptor)

        override fun deserialize(decoder: Decoder): FirmamentModelPredicate {
            val json = decoder.decodeSerializableValue(delegateSerializer).intoGson() as JsonObject
            return AndPredicate(parsePredicates(json).toTypedArray())
        }

        override fun serialize(encoder: Encoder, value: FirmamentModelPredicate) {
            TODO("Cannot serialize firmament predicates")
        }
    }

    val predicateParsers = mutableMapOf<Identifier, FirmamentModelPredicateParser>()


    fun registerPredicateParser(name: String, parser: FirmamentModelPredicateParser) {
        predicateParsers[Identifier.of("firmament", name)] = parser
    }

    init {
        registerPredicateParser("display_name", DisplayNamePredicate.Parser)
        registerPredicateParser("lore", LorePredicate.Parser)
        registerPredicateParser("all", AndPredicate.Parser)
        registerPredicateParser("any", OrPredicate.Parser)
        registerPredicateParser("not", NotPredicate.Parser)
        registerPredicateParser("item", ItemPredicate.Parser)
        registerPredicateParser("extra_attributes", ExtraAttributesPredicate.Parser)
        registerPredicateParser("pet", PetPredicate.Parser)
    }

    private val neverPredicate = listOf(
        object : FirmamentModelPredicate {
            override fun test(stack: ItemStack): Boolean {
                return false
            }
        }
    )

    fun parsePredicates(predicates: JsonObject): List<FirmamentModelPredicate> {
        val parsedPredicates = mutableListOf<FirmamentModelPredicate>()
        for (predicateName in predicates.keySet()) {
            if (!predicateName.startsWith("firmament:")) continue
            val identifier = Identifier.of(predicateName)
            val parser = predicateParsers[identifier] ?: return neverPredicate
            val parsedPredicate = parser.parse(predicates[predicateName]) ?: return neverPredicate
            parsedPredicates.add(parsedPredicate)
        }
        return parsedPredicates
    }

    @JvmStatic
    fun parseCustomModelOverrides(jsonObject: JsonObject): Array<FirmamentModelPredicate>? {
        val predicates = (jsonObject["predicate"] as? JsonObject) ?: return null
        val parsedPredicates = parsePredicates(predicates)
        if (parsedPredicates.isEmpty())
            return null
        return parsedPredicates.toTypedArray()
    }
}
