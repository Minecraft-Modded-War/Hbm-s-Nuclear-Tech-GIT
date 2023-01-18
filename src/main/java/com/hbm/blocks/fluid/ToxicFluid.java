package com.hbm.blocks.fluid;

import com.hbm.main.MainRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

import java.awt.*;

public class ToxicFluid extends Fluid {

	public ToxicFluid(String name){
		super(name, new ResourceLocation(MainRegistry.MODID, "blocks/forgefluid/toxic_still"), new ResourceLocation(MainRegistry.MODID, "blocks/forgefluid/toxic_flowing"), Color.white);
	}
	
}
