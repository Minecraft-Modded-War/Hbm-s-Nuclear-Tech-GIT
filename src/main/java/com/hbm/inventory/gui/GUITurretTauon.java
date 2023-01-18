package com.hbm.inventory.gui;

import com.hbm.main.MainRegistry;
import com.hbm.tileentity.turret.TileEntityTurretBaseNT;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUITurretTauon extends GUITurretBase {
	
	private static ResourceLocation texture = new ResourceLocation(MainRegistry.MODID + ":textures/gui/weapon/gui_turret_tau.png");

	public GUITurretTauon(InventoryPlayer invPlayer, TileEntityTurretBaseNT tedf) {
		super(invPlayer, tedf);
	}
	
	protected ResourceLocation getTexture() {
		return texture;
	}
}
