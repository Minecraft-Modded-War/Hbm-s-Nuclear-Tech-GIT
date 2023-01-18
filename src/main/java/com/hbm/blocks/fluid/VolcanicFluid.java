package com.hbm.blocks.fluid;

import com.hbm.main.MainRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

import java.awt.*;

public class VolcanicFluid extends Fluid {

	public VolcanicFluid() {
		super("volcanic_lava_fluid", new ResourceLocation(MainRegistry.MODID, "blocks/forgefluid/volcanic_lava_still"), new ResourceLocation(MainRegistry.MODID, "blocks/forgefluid/volcanic_lava_flowing"), Color.white);
	}
}
