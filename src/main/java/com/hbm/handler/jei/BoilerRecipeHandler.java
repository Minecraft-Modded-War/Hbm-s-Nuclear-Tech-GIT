package com.hbm.handler.jei;

import com.hbm.handler.jei.JeiRecipes.BoilerRecipe;

import com.hbm.main.MainRegistry;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.IDrawableAnimated.StartDirection;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class BoilerRecipeHandler implements IRecipeCategory<BoilerRecipe> {

	public static final ResourceLocation gui_rl = new ResourceLocation(MainRegistry.MODID, "textures/gui/gui_nei_boiler.png");
	
	protected final IDrawable background;
	protected final IDrawableStatic tempStatic;
	protected final IDrawableAnimated tempAnimated;
	
	public BoilerRecipeHandler(IGuiHelper help) {
		background = help.createDrawable(gui_rl, 33, 33, 109, 19);
		tempStatic = help.createDrawable(gui_rl, 0, 86, 6, 16);
		tempAnimated = help.createAnimatedDrawable(tempStatic, 240, StartDirection.BOTTOM, false);
	}
	
	@Override
	public String getUid() {
		return JEIConfig.BOILER;
	}

	@Override
	public String getTitle() {
		return "Boiler";
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
		tempAnimated.draw(minecraft, 52, 2);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BoilerRecipe recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		
		guiItemStacks.init(0, true, 1, 1);
		guiItemStacks.init(1, false, 91, 1);
		
		guiItemStacks.set(ingredients);
	}

}
