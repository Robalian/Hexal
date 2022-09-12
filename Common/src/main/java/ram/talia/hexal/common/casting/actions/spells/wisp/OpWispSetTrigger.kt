package ram.talia.hexal.common.casting.actions.spells.wisp

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapNoSpellCircle
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.spell.casting.MixinCastingContextInterface
import ram.talia.hexal.api.spell.casting.triggers.WispTriggerRegistry

class OpWispSetTrigger(private val triggerType: WispTriggerRegistry.WispTriggerType<*>) : ConstManaOperator {
	override val argc = triggerType.argc

	override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
		@Suppress("CAST_NEVER_SUCCEEDS")
		val mCast = ctx as? MixinCastingContextInterface

		if (mCast == null || mCast.wisp == null)
			throw MishapNoSpellCircle()

		HexalAPI.LOGGER.info("Setting ${mCast.wisp} trigger to $triggerType")

		mCast.wisp.setTrigger(triggerType.makeFromArgs(mCast.wisp, args, ctx))

		return listOf()
	}
}