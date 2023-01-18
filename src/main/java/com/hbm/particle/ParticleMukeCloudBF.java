package com.hbm.particle;

import com.hbm.main.MainRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ParticleMukeCloudBF extends ParticleMukeCloud {

	private static final ResourceLocation texture = new ResourceLocation(MainRegistry.MODID + ":textures/particle/explosion_bf.png");
	
	public ParticleMukeCloudBF(World world, double x, double y, double z, double mx, double my, double mz) {
		super(world, x, y, z, mx, my, mz);
	}
	
	public ResourceLocation getTexture() {
		return texture;
	}
}
