package com.hbm.render.util;

import com.hbm.main.MainRegistry;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class GaugeUtil {

	public static enum Gauge {

		ROUND_SMALL(new ResourceLocation(MainRegistry.MODID + ":textures/gui/gauges/small_round.png"), 18, 18, 13),
		ROUND_LARGE(new ResourceLocation(MainRegistry.MODID + ":textures/gui/gauges/large_round.png"), 36, 36, 13),
		BOW_SMALL(new ResourceLocation(MainRegistry.MODID + ":textures/gui/gauges/small_bow.png"), 18, 18, 13),
		BOW_LARGE(new ResourceLocation(MainRegistry.MODID + ":textures/gui/gauges/large_bow.png"), 36, 36, 13),
		WIDE_SMALL(new ResourceLocation(MainRegistry.MODID + ":textures/gui/gauges/small_wide.png"), 18, 12, 7),
		WIDE_LARGE(new ResourceLocation(MainRegistry.MODID + ":textures/gui/gauges/large_wide.png"), 36, 24, 11),
		BAR_SMALL(new ResourceLocation(MainRegistry.MODID + ":textures/gui/gauges/small_bar.png"), 36, 12, 16);

		ResourceLocation texture;
		int width;
		int height;
		int count;

		private Gauge(ResourceLocation texture, int width, int height, int count) {
			this.texture = texture;
			this.width = width;
			this.height = height;
			this.count = count;
		}
	}

	/**
	 * 
	 * @param gauge The gauge enum to use
	 * @param x The x coord in the GUI (left)
	 * @param y The y coord in the GUI (top)
	 * @param z The z-level (from GUI.zLevel)
	 * @param progress Double from 0-1 how far the gauge has progressed
	 */
	public static void renderGauge(Gauge gauge, double x, double y, double z, double progress) {

		Minecraft.getMinecraft().renderEngine.bindTexture(gauge.texture);

		int frameNum = (int) Math.round((gauge.count - 1) * progress);
		double singleFrame = 1D / (double)gauge.count;
		double frameOffset = singleFrame * frameNum;

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buf.pos(x, 				 y + gauge.height, 	z).tex(0, 	frameOffset + singleFrame).endVertex();
		buf.pos(x + gauge.width, y + gauge.height,  z).tex(1, 	frameOffset + singleFrame).endVertex();
		buf.pos(x + gauge.width, y, 				z).tex(1, 	frameOffset).endVertex();
		buf.pos(x, 				 y, 				z).tex(0, 	frameOffset).endVertex();
		tess.draw();
	}

}