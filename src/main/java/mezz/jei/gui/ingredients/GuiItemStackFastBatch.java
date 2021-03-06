package mezz.jei.gui.ingredients;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;

import mezz.jei.gui.Focus;
import mezz.jei.util.ItemStackElement;

public class GuiItemStackFastBatch {
	private final List<GuiItemStackFast> renderItemsAll = new ArrayList<>();
	private final List<GuiItemStackFast> renderItemsBuiltIn = new ArrayList<>();
	private final List<GuiItemStackFast> renderItems2d = new ArrayList<>();
	private final List<GuiItemStackFast> renderItems3d = new ArrayList<>();

	public void clear() {
		renderItemsAll.clear();
		renderItemsBuiltIn.clear();
		renderItems2d.clear();
		renderItems3d.clear();
	}

	public void add(GuiItemStackFast guiItemStack) {
		renderItemsAll.add(guiItemStack);
	}

	public void set(int i, List<ItemStackElement> itemList) {
		renderItemsBuiltIn.clear();
		renderItems2d.clear();
		renderItems3d.clear();

		for (GuiItemStackFast guiItemStack : renderItemsAll) {
			if (i >= itemList.size()) {
				guiItemStack.clear();
			} else {
				ItemStack stack = itemList.get(i).getItemStack();
				guiItemStack.setItemStack(stack);
				if (guiItemStack.isBuiltInRenderer()) {
					renderItemsBuiltIn.add(guiItemStack);
				} else if (guiItemStack.isGui3d()) {
					renderItems3d.add(guiItemStack);
				} else {
					renderItems2d.add(guiItemStack);
				}
			}
			i++;
		}
	}

	@Nullable
	public Focus getFocusUnderMouse(int mouseX, int mouseY) {
		for (GuiItemStackFast guiItemStack : renderItemsAll) {
			if (guiItemStack.isMouseOver(mouseX, mouseY)) {
				return new Focus(guiItemStack.getItemStack());
			}
		}
		return null;
	}

	/** renders all ItemStacks and returns hovered gui item stack for later render pass */
	@Nullable
	public GuiItemStackFast render(@Nullable GuiItemStackFast hovered, @Nonnull Minecraft minecraft, boolean isMouseOver, int mouseX, int mouseY) {
		if (isMouseOver && hovered == null) {
			for (GuiItemStackFast guiItemStack : renderItemsAll) {
				if (guiItemStack.isMouseOver(mouseX, mouseY)) {
					hovered = guiItemStack;
					break;
				}
			}
		}

		RenderHelper.enableGUIStandardItemLighting();

		RenderItem renderItem = minecraft.getRenderItem();
		TextureManager textureManager = minecraft.getTextureManager();
		renderItem.zLevel += 50.0F;

		textureManager.bindTexture(TextureMap.locationBlocksTexture);
		textureManager.getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		// 3d Items
		GlStateManager.pushMatrix();
		{
			GlStateManager.enableLighting();
			GlStateManager.scale(20.0F, 20.0F, -20.0F);
			for (GuiItemStackFast guiItemStack : renderItems3d) {
				if (hovered != guiItemStack) {
					guiItemStack.renderItemAndEffectIntoGUI(true);
				}
			}
		}
		GlStateManager.popMatrix();

		// 2d Items
		GlStateManager.pushMatrix();
		{
			GlStateManager.disableLighting();
			GlStateManager.scale(32.0F, 32.0F, -32.0F);
			for (GuiItemStackFast guiItemStack : renderItems2d) {
				if (hovered != guiItemStack) {
					guiItemStack.renderItemAndEffectIntoGUI(false);
				}
			}
		}
		GlStateManager.popMatrix();

		GlStateManager.disableAlpha();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();

		textureManager.bindTexture(TextureMap.locationBlocksTexture);
		textureManager.getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();

		renderItem.zLevel -= 50.0F;

		// built-in render Items
		for (GuiItemStackFast guiItemStack : renderItemsBuiltIn) {
			if (hovered != guiItemStack) {
				guiItemStack.renderSlow();
			}
		}

		RenderHelper.disableStandardItemLighting();

		return hovered;
	}
}
