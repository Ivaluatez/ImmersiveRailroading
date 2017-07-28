package cam72cam.immersiverailroading.net;

import cam72cam.immersiverailroading.entity.EntityMoveableRollingStock;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/*
 * Movable rolling stock sync packet
 */
public class MRSSyncPacket implements IMessage {
	private int entityID;
	private float rotationYaw;
	private float frontYaw;
	private float rearYaw;
	private double posX;
	private double posY;
	private double posZ;
	private double motionX;
	private double motionY;
	private double motionZ;

	public MRSSyncPacket() {
		// Reflect constructor
	}

	public MRSSyncPacket(EntityMoveableRollingStock mrs) {
		// Should we sync dimension ID as well?
		this.entityID = mrs.getEntityId();
		this.rotationYaw = mrs.rotationYaw;
		this.frontYaw = mrs.frontYaw;
		this.rearYaw = mrs.rearYaw;
		this.posX = mrs.posX;
		this.posY = mrs.posY;
		this.posZ = mrs.posZ;
		this.motionX = mrs.motionX;
		this.motionY = mrs.motionY;
		this.motionZ = mrs.motionZ;
	}

	public void applyTo(EntityMoveableRollingStock mrs) {
		mrs.rotationYaw = this.rotationYaw;
		mrs.frontYaw = this.frontYaw;
		mrs.rearYaw = this.rearYaw;
		mrs.posX = this.posX;
		mrs.posY = this.posY;
		mrs.posZ = this.posZ;
		mrs.motionX = this.motionX;
		mrs.motionY = this.motionY;
		mrs.motionZ = this.motionZ;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		entityID = buf.readInt();
		rotationYaw = buf.readFloat();
		frontYaw = buf.readFloat();
		rearYaw = buf.readFloat();
		posX = buf.readDouble();
		posY = buf.readDouble();
		posZ = buf.readDouble();
		motionX = buf.readDouble();
		motionY = buf.readDouble();
		motionZ = buf.readDouble();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(entityID);
		buf.writeFloat(rotationYaw);
		buf.writeFloat(frontYaw);
		buf.writeFloat(rearYaw);
		buf.writeDouble(posX);
		buf.writeDouble(posY);
		buf.writeDouble(posZ);
		buf.writeDouble(motionX);
		buf.writeDouble(motionY);
		buf.writeDouble(motionZ);
	}

	public static class Handler implements IMessageHandler<MRSSyncPacket, IMessage> {
		@Override
		public IMessage onMessage(MRSSyncPacket message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}

		private void handle(MRSSyncPacket message, MessageContext ctx) {
			EntityMoveableRollingStock entity = (EntityMoveableRollingStock) Minecraft.getMinecraft().world.getEntityByID(message.entityID);
			if (entity == null) {
				return;
			}

			message.applyTo(entity);
		}
	}
}