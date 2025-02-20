package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerTurretBase;
import com.hbm.main.MainRegistry;
import com.hbm.packet.AuxButtonPacket;
import com.hbm.packet.NBTControlPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.tileentity.turret.TileEntityTurretBaseNT;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;

public abstract class GUITurretBase extends GuiInfoContainer {

	protected static ResourceLocation texture = new ResourceLocation(MainRegistry.MODID + ":textures/gui/weapon/gui_turret_base.png");
	protected TileEntityTurretBaseNT turret;
	protected GuiTextField field;
	int index;
	
	public GUITurretBase(InventoryPlayer invPlayer, TileEntityTurretBaseNT tedf) {
		super(new ContainerTurretBase(invPlayer, tedf));
		turret = tedf;
		
		this.xSize = 176;
		this.ySize = 222;
	}
	
	public void initGui() {

		super.initGui();

		Keyboard.enableRepeatEvents(true);
		this.field = new GuiTextField(0, this.fontRenderer, guiLeft + 10, guiTop + 65, 50, 14);
		this.field.setTextColor(-1);
		this.field.setDisabledTextColour(-1);
		this.field.setEnableBackgroundDrawing(false);
		this.field.setMaxStringLength(25);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 45, 16, 52, turret.power, turret.getMaxPower());
		super.renderHoveredToolTip(mouseX, mouseY);
	}

	protected void mouseClicked(int x, int y, int i) throws IOException {
		super.mouseClicked(x, y, i);
		
		boolean flag = x >= this.field.x && x < this.field.x + this.field.width && y >= this.field.y && y < this.field.y + this.field.height;
		this.field.setFocused(flag);

		if(guiLeft + 115 <= x && guiLeft + 115 + 18 > x && guiTop + 26 < y && guiTop + 26 + 18 >= y) {

			mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(turret.getPos().getX(), turret.getPos().getY(), turret.getPos().getZ(), 0, 0));
			return;
		}

		if(guiLeft + 8 <= x && guiLeft + 8 + 10 > x && guiTop + 30 < y && guiTop + 30 + 10 >= y) {

			mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(turret.getPos().getX(), turret.getPos().getY(), turret.getPos().getZ(), 0, 1));
			return;
		}

		if(guiLeft + 22 <= x && guiLeft + 22 + 10 > x && guiTop + 30 < y && guiTop + 30 + 10 >= y) {

			mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(turret.getPos().getX(), turret.getPos().getY(), turret.getPos().getZ(), 0, 2));
			return;
		}

		if(guiLeft + 36 <= x && guiLeft + 36 + 10 > x && guiTop + 30 < y && guiTop + 30 + 10 >= y) {

			mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(turret.getPos().getX(), turret.getPos().getY(), turret.getPos().getZ(), 0, 3));
			return;
		}

		if(guiLeft + 50 <= x && guiLeft + 50 + 10 > x && guiTop + 30 < y && guiTop + 30 + 10 >= y) {

			mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			PacketDispatcher.wrapper.sendToServer(new AuxButtonPacket(turret.getPos().getX(), turret.getPos().getY(), turret.getPos().getZ(), 0, 4));
			return;
		}
		
		int count = getCount();
		
		if(count > 0) {
			
			if(guiLeft + 7 <= x && guiLeft + 7 + 18 > x && guiTop + 80 < y && guiTop + 80 + 18 >= y) {
	
				index--;
				if(index < 0)
					index = count - 1;
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
				return;
			}

			if(guiLeft + 43 <= x && guiLeft + 43 + 18 > x && guiTop + 80 < y && guiTop + 80 + 18 >= y) {
	
				index++;
				index %= count;
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
				return;
			}
		}
		
		if(guiLeft + 7 <= x && guiLeft + 7 + 18 > x && guiTop + 98 < y && guiTop + 98 + 18 >= y) {
			
			mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			
			if(this.field.getText().isEmpty())
				return;
			
			NBTTagCompound data = new NBTTagCompound();
			data.setString("name", this.field.getText());
			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, turret.getPos().getX(), turret.getPos().getY(), turret.getPos().getZ()));
			
			this.field.setText("");
			return;
		}

		if(guiLeft + 43 <= x && guiLeft + 43 + 18 > x && guiTop + 98 < y && guiTop + 98 + 18 >= y) {
			
			mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			
			NBTTagCompound data = new NBTTagCompound();
			data.setInteger("del", this.index);
			PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, turret.getPos().getX(), turret.getPos().getY(), turret.getPos().getZ()));
			return;
		}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.turret.hasCustomInventoryName() ? this.turret.getInventoryName() : I18n.format(this.turret.getInventoryName());
		
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
		
		List<String> names = turret.getWhitelist();
		
		String n = TextFormatting.ITALIC + "None";
		
		while(this.index >= this.getCount())
			this.index--;
		
		if(index < 0)
			index = 0;
		
		if(names != null) {
			n = names.get(index);
		}
		
		String t = this.field.getText();
		
		String cursor = System.currentTimeMillis() % 1000 < 500 ? " " : "||";
		
		if(this.field.isFocused())
			t = t.substring(0, this.field.getCursorPosition()) + cursor + t.substring(this.field.getCursorPosition(), t.length());
		
		double scale = 2;
		
		GL11.glScaled(1D / scale, 1D / scale, 1);
		this.fontRenderer.drawString(n, (int)(12 * scale), (int)(51 * scale), 0x00ff00);
		this.fontRenderer.drawString(t, (int)(12 * scale), (int)(69 * scale), 0x00ff00);
		GL11.glScaled(scale, scale, 1);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int mX, int mY) {
		super.drawDefaultBackground();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(this.getTexture());
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if(guiLeft + 7 <= mX && guiLeft + 7 + 18 > mX && guiTop + 80 < mY && guiTop + 80 + 18 >=mY) {
			drawTexturedModalRect(guiLeft + 7, guiTop + 80, 176, 58, 18, 18);
		}
		if(guiLeft + 43 <= mX && guiLeft + 43 + 18 > mX && guiTop + 80 < mY && guiTop + 80 + 18 >=mY) {
			drawTexturedModalRect(guiLeft + 43, guiTop + 80, 194, 58, 18, 18);
		}
		if(guiLeft + 7 <= mX && guiLeft + 7 + 18 > mX && guiTop + 98 < mY && guiTop + 98 + 18 >=mY) {
			drawTexturedModalRect(guiLeft + 7, guiTop + 98, 176, 76, 18, 18);
		}
		if(guiLeft + 43 <= mX && guiLeft + 43 + 18 > mX && guiTop + 98 < mY && guiTop + 98 + 18 >=mY) {
			drawTexturedModalRect(guiLeft + 43, guiTop + 98, 194, 76, 18, 18);
		}
		
		int i = turret.getPowerScaled(53);
		drawTexturedModalRect(guiLeft + 152, guiTop + 97 - i, 194, 52 - i, 16, i);
		
		if(turret.isOn)
			drawTexturedModalRect(guiLeft + 115, guiTop + 26, 176, 40, 18, 18);
		
		if(turret.targetPlayers)
			drawTexturedModalRect(guiLeft + 8, guiTop + 30, 176, 0, 10, 10);
		
		if(turret.targetAnimals)
			drawTexturedModalRect(guiLeft + 22, guiTop + 30, 176, 10, 10, 10);
		
		if(turret.targetMobs)
			drawTexturedModalRect(guiLeft + 36, guiTop + 30, 176, 20, 10, 10);
		
		if(turret.targetMachines)
			drawTexturedModalRect(guiLeft + 50, guiTop + 30, 176, 30, 10, 10);
		
		int tallies = turret.stattrak;
		
		if(tallies >= 36) {
			
			drawTexturedModalRect(guiLeft + 77, guiTop + 50, 176, 120, 63, 6);
			
		} else {
			
			int steps = (int)Math.ceil(tallies / 5D);
			
			for(int s = 0; s < steps; s++) {
				
				int m = tallies % 5;
				
				if(s < steps - 1 || m == 0) {
					drawTexturedModalRect(guiLeft + 77 + 9 * s, guiTop + 50, 194, 94, 9, 6);
				} else {
					
					drawTexturedModalRect(guiLeft + 77 + 9 * s, guiTop + 50, 176, 94, m * 2, 6);
				}
			}
		}
	}
	
	protected ResourceLocation getTexture() {
		return texture;
	}
	
	private int getCount() {
		
		List<String> names = turret.getWhitelist();
		
		if(names == null)
			return 0;
		
		return names.size();
	}
	
	@Override
	protected void keyTyped(char p_73869_1_, int p_73869_2_) throws IOException {
		
		if(this.field.textboxKeyTyped(p_73869_1_, p_73869_2_)) {
			
		} else {
			super.keyTyped(p_73869_1_, p_73869_2_);
		}
	}
}
