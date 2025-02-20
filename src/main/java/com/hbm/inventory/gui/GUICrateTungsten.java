package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerCrateTungsten;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityCrateTungsten;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GUICrateTungsten extends GuiContainer {

	private static ResourceLocation texture = new ResourceLocation(MainRegistry.MODID + ":textures/gui/gui_crate_tungsten.png");
	private static ResourceLocation texture_hot = new ResourceLocation(MainRegistry.MODID + ":textures/gui/gui_crate_tungsten_hot.png");
	private TileEntityCrateTungsten diFurnace;
	
	public GUICrateTungsten(InventoryPlayer invPlayer, TileEntityCrateTungsten tedf) {
		super(new ContainerCrateTungsten(invPlayer, tedf));
		diFurnace = tedf;
		
		this.xSize = 176;
		this.ySize = 168;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String title = I18n.format("container.crateTungsten");
		this.fontRenderer.drawString(title, this.xSize / 2 - this.fontRenderer.getStringWidth(title) / 2, 6, 0xffffff);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 0xffffff);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks){
		super.drawScreen(mouseX, mouseY, partialTicks);
		super.renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		if(diFurnace.heatTimer == 0)
			Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		else
			Minecraft.getMinecraft().getTextureManager().bindTexture(texture_hot);
		
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}
}
