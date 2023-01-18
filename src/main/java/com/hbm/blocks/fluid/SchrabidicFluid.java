package com.hbm.blocks.fluid;

import com.hbm.main.MainRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

import java.awt.*;

public class SchrabidicFluid extends Fluid {

	public SchrabidicFluid(String name){
		super(name, new ResourceLocation(MainRegistry.MODID, "blocks/forgefluid/schrabidic_acid_still"), new ResourceLocation(MainRegistry.MODID, "blocks/forgefluid/schrabidic_acid_flowing"), Color.white);
	}
	
}
