package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineArcFurnace;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityMachineArcFurnace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GUIMachineArcFurnace extends GuiInfoContainer {
	
	private static ResourceLocation texture = new ResourceLocation(MainRegistry.MODID + ":textures/gui/gui_arc.png");
	private TileEntityMachineArcFurnace diFurnace;

	public GUIMachineArcFurnace(InventoryPlayer invPlayer, TileEntityMachineArcFurnace tedf) {
		super(new ContainerMachineArcFurnace(invPlayer, tedf));
		diFurnace = tedf;
		
		this.xSize = 176;
		this.ySize = 166;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 8, guiTop + 51 - 34, 16, 34, diFurnace.power, TileEntityMachineArcFurnace.maxPower);
		super.renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.diFurnace.hasCustomInventoryName() ? this.diFurnace.getInventoryName() : I18n.format(this.diFurnace.getInventoryName());
		
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		super.drawDefaultBackground();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		//failsafe TE clone
		//if initial TE invalidates, new TE is fetched
		//if initial ZE is still present, it'll be used instead
		//works so that container packets can still be used
		//efficiency!
		TileEntityMachineArcFurnace fs = null;
		
		if(diFurnace.isInvalid() && diFurnace.getWorld().getTileEntity(diFurnace.getPos()) instanceof TileEntityMachineArcFurnace)
			fs = (TileEntityMachineArcFurnace) diFurnace.getWorld().getTileEntity(diFurnace.getPos());
		else
			fs = diFurnace;
		
		if(fs.hasPower()) {
			int i = (int)diFurnace.getPowerRemainingScaled(34);
			drawTexturedModalRect(guiLeft + 8, guiTop + 51 - i, 176, 67 - i, 16, i);
		}
		
		if(diFurnace.canProcess() && diFurnace.hasPower())
		{
			drawTexturedModalRect(guiLeft + 55, guiTop + 35, 176, 0, 15, 16);
		}
		
		int j1 = fs.getDiFurnaceProgressScaled(24);
		drawTexturedModalRect(guiLeft + 79, guiTop + 34, 176, 16, j1 + 1, 17);
	}
}