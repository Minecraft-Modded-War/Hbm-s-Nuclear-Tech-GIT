package com.hbm.handler.jei;

import com.hbm.inventory.DFCRecipes.DFCRecipe;

import com.hbm.main.MainRegistry;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class DFCRecipeHandler implements IRecipeCategory<DFCRecipe> {

	public static final ResourceLocation gui_rl = new ResourceLocation(MainRegistry.MODID, "textures/gui/jei/gui_nei_dfc.png");
	
	protected final IDrawable background;
	
	public DFCRecipeHandler(IGuiHelper help) {
		background = help.createDrawable(gui_rl, 0, 0, 134, 52);
	}
	
	@Override
	public String getUid() {
		return JEIConfig.DFC;
	}

	@Override
	public String getTitle() {
		return "DFC Crate Recipe";
	}

	@Override
	public String getModName() {
		return MainRegistry.MODID;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}
	
	@Override
	public void drawExtras(Minecraft minecraft) {
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, DFCRecipe recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		
		guiItemStacks.init(0, true, 59, 30);
		guiItemStacks.init(1, false, 95, 30);
		
		guiItemStacks.set(ingredients);
	}
}
