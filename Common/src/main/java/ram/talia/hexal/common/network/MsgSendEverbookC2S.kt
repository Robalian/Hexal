package ram.talia.hexal.common.network

import at.petrak.hexcasting.common.msgs.IMessage
import io.netty.buffer.ByteBuf
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.config.HexalConfig
import ram.talia.hexal.api.everbook.Everbook
import ram.talia.hexal.api.nbt.decompressToNBT
import ram.talia.hexal.api.nbt.toCompressedBytes
import ram.talia.hexal.xplat.IXplatAbstractions

data class MsgSendEverbookC2S(val everbook: Everbook) : IMessage {
	private var wasTooBig = false
	override fun serialize(buf: FriendlyByteBuf) {
		val bookNbt = everbook.serialiseToNBT()
		val bytes = bookNbt.toCompressedBytes()
		HexalAPI.LOGGER.info("Serialized everbook data of size ${bookNbt.sizeInBytes()} compressed to size ${bytes.size}")
		buf.writeUUID(everbook.uuid)
		buf.writeByteArray(bytes)
	}

	override fun getFabricId() = ID

	fun handle(server: MinecraftServer, sender: ServerPlayer) {
		server.execute {
			IXplatAbstractions.INSTANCE.setFullEverbook(sender, everbook.filterIotasIllegalInterworld(server.overworld()))
			if (wasTooBig){
				sender.sendSystemMessage(Component.translatable("hexal.everbook.sizewarning", HexalConfig.server.everbookMaxSize))
			}
		}
	}

	companion object {
		@JvmField
		val ID: ResourceLocation = HexalAPI.modLoc("sendever")

		@JvmStatic
		fun deserialise(buffer: ByteBuf): MsgSendEverbookC2S {
			val buf = FriendlyByteBuf(buffer)
			val uuid = buf.readUUID()
			val bytes = buf.readByteArray()
			val decompressed = bytes.decompressToNBT(HexalConfig.server.everbookMaxSize)
			if (decompressed.isEmpty){
				val packet = MsgSendEverbookC2S(Everbook(uuid, mutableMapOf(), listOf()))
				packet.wasTooBig = true
				return packet
			}
			HexalAPI.LOGGER.info("Deserializing everbook data of size ${bytes.size} decompressed to size ${decompressed.sizeInBytes()}")
			return MsgSendEverbookC2S(Everbook.fromNbt(decompressed))
		}
	}
}