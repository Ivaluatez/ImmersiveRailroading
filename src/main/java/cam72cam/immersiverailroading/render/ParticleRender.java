package cam72cam.immersiverailroading.render;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.entity.EntitySmokeParticle;
import cam72cam.immersiverailroading.util.GLBoolTracker;
import cam72cam.immersiverailroading.util.VecUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public class ParticleRender extends Render<EntitySmokeParticle> {

	private int textureID;
	private GLSLShader shader;

	public ParticleRender(RenderManager renderManager) {
		super(renderManager);
		ResourceLocation imageLoc = new ResourceLocation("immersiverailroading:particles/smoke.png");

		InputStream input;
		BufferedImage image;
		try {
			input = ImmersiveRailroading.proxy.getResourceStream(imageLoc);
			image = TextureUtil.readBufferedImage(input);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error loading particle texture");
		}
		int size = image.getWidth();

		int[] pixels = new int[size * size];
		image.getRGB(0, 0, size, size, pixels, 0, size);

		ByteBuffer buffer = BufferUtils.createByteBuffer(size * size * 4);
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				int pixel = pixels[y * size + x];
				buffer.put((byte) ((pixel >> 16) & 0xFF));
				buffer.put((byte) ((pixel >> 8) & 0xFF));
				buffer.put((byte) ((pixel >> 0) & 0xFF));
				buffer.put((byte) ((pixel >> 24) & 0xFF));
			}
		}
		buffer.flip();

		textureID = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
		TextureUtil.allocateTexture(textureID, size, size);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, size, size, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
		
		shader = new GLSLShader("smoke_vert.c", "smoke_frag.c");
	}

	@Override
	public boolean shouldRender(EntitySmokeParticle entity, ICamera camera, double camX, double camY, double camZ) {
		return true;
	}

	@Override
	public void doRender(EntitySmokeParticle particle, double x, double y, double z, float entityYaw, float partialTicks) {
		GL11.glPushMatrix();
		{
			float darken = 0.9f-particle.darken*0.9f;
			float alpha = (particle.lifespan - particle.ticksExisted - partialTicks)/(float)particle.lifespan * (0.2f + particle.thickness * 2);
			
			shader.bind();
			shader.paramFloat("ALPHA", alpha);
			shader.paramFloat("DARKEN", darken, darken, darken);

			GLBoolTracker light = new GLBoolTracker(GL11.GL_LIGHTING, false);
			GLBoolTracker cull = new GLBoolTracker(GL11.GL_CULL_FACE, false);
			GLBoolTracker tex = new GLBoolTracker(GL11.GL_TEXTURE_2D, false);
			GLBoolTracker blend = new GLBoolTracker(GL11.GL_BLEND, true);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			//GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

			GL11.glColor4f(0.3f, 0.3f, 0.3f, 0.5f);

			// Move to specified position
			GlStateManager.translate(x, y, z);
			GlStateManager.translate(0, partialTicks * 0.2, 0);

			GL11.glRotated(180 - Minecraft.getMinecraft().player.rotationYaw, 0, 1, 0);

			int te = 4;
			// double rad = Math.sqrt(particle.ticksExisted/160.0);
			for (int i = 0; i < te; i++) {
				if (i == 2) {
					continue;
				}
				Vec3d offset = VecUtil.fromYaw((particle.ticksExisted + partialTicks) / 40.0 * particle.size, i * 360.0f / te);
				GL11.glPushMatrix();
				{
					GL11.glTranslated(offset.x, 0, offset.z);
					double scale = (particle.ticksExisted + partialTicks) / 20.0 * particle.size * (particle.motionY*8);
					GL11.glScaled(scale, scale, scale);
					// GL11.glCallList(this.dl);
					
					GL11.glRotated(particle.rot, 0, 0, 1);
					GL11.glTranslated(0.5, 0, 0);
					GL11.glRotated(-particle.rot, 0, 0, 1);

					double angle = particle.ticksExisted + partialTicks;
					if (i == 1) {
						GL11.glRotated(angle, 0, 0, 1);	
					}
					if (i == 3) {
						GL11.glRotated(-angle, 0, 0, 1);	
					}
	
					GL11.glBegin(GL11.GL_QUADS);
					{
						GL11.glTexCoord2d(0, 0);
						GL11.glVertex3d(-1, -1, 0);
						GL11.glTexCoord2d(0, 1);
						GL11.glVertex3d(-1, 1, 0);
						GL11.glTexCoord2d(1, 1);
						GL11.glVertex3d(1, 1, 0);
						GL11.glTexCoord2d(1, 0);
						GL11.glVertex3d(1, -1, 0);
					}
					GL11.glEnd();
				}
				GL11.glPopMatrix();
				// GL11.glTranslated(0, 0.02, 0);
			}

			blend.restore();
			tex.restore();
			cull.restore();
			light.restore();
			
			shader.unbind();
		}
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntitySmokeParticle entity) {
		return null;
	}
}