package ram.talia.hexal.common.network

import at.petrak.hexcasting.api.utils.asByteArray
import at.petrak.hexcasting.common.msgs.IMessage
import io.netty.buffer.ByteBuf
import net.minecraft.nbt.NbtIo
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import ram.talia.hexal.api.HexalAPI
import ram.talia.hexal.api.everbook.Everbook
import ram.talia.hexal.xplat.IXplatAbstractions
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

data class MsgSendEverbookC2S(val everbook: Everbook) : IMessage {
	override fun serialize(buf: FriendlyByteBuf) {
		val out : ByteArrayOutputStream = ByteArrayOutputStream()
		val bookNbt = everbook.serialiseToNBT()
		NbtIo.writeCompressed(bookNbt, out)
		val bytes = out.toByteArray()
		HexalAPI.LOGGER.info("Serialized everbook data of size ${bookNbt.sizeInBytes()} compressed to size ${bytes.size}")
		buf.writeByteArray(bytes)
	}

	override fun getFabricId() = ID

	fun handle(server: MinecraftServer, sender: ServerPlayer) {
		server.execute {
			IXplatAbstractions.INSTANCE.setFullEverbook(sender, everbook.filterIotasIllegalInterworld(server.overworld()))
		}
	}

	companion object {
		@JvmField
		val ID: ResourceLocation = HexalAPI.modLoc("sendever")

		@JvmStatic
		fun deserialise(buffer: ByteBuf): MsgSendEverbookC2S {
			val buf = FriendlyByteBuf(buffer)
			val bytes = buf.readByteArray()
			val inStream = ByteArrayInputStream(bytes)
			val decompressed = NbtIo.readCompressed(inStream)
			HexalAPI.LOGGER.info("Deserializing everbook data of size ${bytes.size} decompressed to size ${decompressed.sizeInBytes()}")
			return MsgSendEverbookC2S(Everbook.fromNbt(decompressed))
		}
	}
}