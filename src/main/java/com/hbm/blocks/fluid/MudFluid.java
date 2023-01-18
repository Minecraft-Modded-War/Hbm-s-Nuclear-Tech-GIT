package com.hbm.blocks.fluid;

import com.hbm.main.MainRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

import java.awt.*;

public class MudFluid extends Fluid {

	public MudFluid(){
		super("mud_fluid", new ResourceLocation(MainRegistry.MODID, "blocks/forgefluid/mud_still"), new ResourceLocation(MainRegistry.MODID, "blocks/forgefluid/mud_flowing"), Color.white);
	}

}
