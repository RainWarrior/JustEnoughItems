package mezz.jei.gui.ingredients;

import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.gui.IGuiFluidStackGroup;

public class GuiFluidStackGroup extends GuiIngredientGroup<FluidStack, GuiIngredient<FluidStack>> implements IGuiFluidStackGroup {
	private static final FluidStackHelper helper = new FluidStackHelper();

	@Override
	public void init(int slotIndex, boolean input, int xPosition, int yPosition, int width, int height, int capacityMb) {
		FluidStackRenderer renderer = new FluidStackRenderer(capacityMb);
		GuiIngredient<FluidStack> guiIngredient = new GuiIngredient<>(renderer, helper, input, xPosition, yPosition, width, height, 0);
		guiIngredients.put(slotIndex, guiIngredient);
	}
}
