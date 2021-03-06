package mezz.jei.plugins.vanilla;

import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerWorkbench;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.plugins.vanilla.brewing.BrewingRecipeCategory;
import mezz.jei.plugins.vanilla.brewing.BrewingRecipeHandler;
import mezz.jei.plugins.vanilla.brewing.BrewingRecipeMaker;
import mezz.jei.plugins.vanilla.crafting.CraftingRecipeCategory;
import mezz.jei.plugins.vanilla.crafting.CraftingRecipeMaker;
import mezz.jei.plugins.vanilla.crafting.ShapedOreRecipeHandler;
import mezz.jei.plugins.vanilla.crafting.ShapedRecipesHandler;
import mezz.jei.plugins.vanilla.crafting.ShapelessOreRecipeHandler;
import mezz.jei.plugins.vanilla.crafting.ShapelessRecipesHandler;
import mezz.jei.plugins.vanilla.furnace.FuelRecipeHandler;
import mezz.jei.plugins.vanilla.furnace.FuelRecipeMaker;
import mezz.jei.plugins.vanilla.furnace.FurnaceFuelCategory;
import mezz.jei.plugins.vanilla.furnace.FurnaceSmeltingCategory;
import mezz.jei.plugins.vanilla.furnace.SmeltingRecipeHandler;
import mezz.jei.plugins.vanilla.furnace.SmeltingRecipeMaker;

@JEIPlugin
public class VanillaPlugin implements IModPlugin {

	@Override
	public boolean isModLoaded() {
		return true;
	}

	@Override
	public void register(IModRegistry registry) {
		registry.addRecipeCategories(
				new CraftingRecipeCategory(),
				new FurnaceFuelCategory(),
				new FurnaceSmeltingCategory(),
				new BrewingRecipeCategory()
		);

		registry.addRecipeHandlers(
				new ShapedOreRecipeHandler(),
				new ShapedRecipesHandler(),
				new ShapelessOreRecipeHandler(),
				new ShapelessRecipesHandler(),
				new FuelRecipeHandler(),
				new SmeltingRecipeHandler(),
				new BrewingRecipeHandler()
		);

		registry.addBasicRecipeTransferHelper(ContainerWorkbench.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36);
		registry.addBasicRecipeTransferHelper(ContainerFurnace.class, VanillaRecipeCategoryUid.SMELTING, 0, 1, 1, 36);
		registry.addBasicRecipeTransferHelper(ContainerFurnace.class, VanillaRecipeCategoryUid.FUEL, 1, 1, 1, 36);
		registry.addBasicRecipeTransferHelper(ContainerBrewingStand.class, VanillaRecipeCategoryUid.BREWING, 0, 4, 4, 36);

		registry.addRecipes(CraftingRecipeMaker.getCraftingRecipes());
		registry.addRecipes(SmeltingRecipeMaker.getFurnaceRecipes());
		registry.addRecipes(FuelRecipeMaker.getFuelRecipes());
		registry.addRecipes(BrewingRecipeMaker.getBrewingRecipes());
	}
}
