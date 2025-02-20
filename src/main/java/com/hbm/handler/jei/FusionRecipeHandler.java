package com.hbm.handler.jei;

import com.hbm.handler.jei.JeiRecipes.FusionRecipe;

import com.hbm.main.MainRegistry;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.util.ResourceLocation;

public class FusionRecipeHandler implements IRecipeCategory<FusionRecipe> {

	public static ResourceLocation gui_rl = new ResourceLocation(MainRegistry.MODID + ":textures/gui/gui_nei_fusion.png");
	
	protected final IDrawable background;
	
	public FusionRecipeHandler(IGuiHelper help) {
		background = help.createDrawable(gui_rl, 33, 33, 109, 19);
	}
	
	@Override
	public String getUid() {
		return JEIConfig.FUSION_BYPRODUCT;
	}

	@Override
	public String getTitle() {
		return "Fusion Reactor";
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
	public void setRecipe(IRecipeLayout recipeLayout, FusionRecipe recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		
		guiItemStacks.init(0, true, 1, 1);
		guiItemStacks.init(1, false, 91, 1);
		
		guiItemStacks.set(ingredients);
	}

}
