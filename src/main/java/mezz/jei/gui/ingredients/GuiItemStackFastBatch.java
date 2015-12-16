package mezz.jei.gui.ingredients;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.pipeline.LightUtil.ItemConsumer;
import net.minecraftforge.client.model.pipeline.WorldRendererConsumer;
import mezz.jei.gui.Focus;
import mezz.jei.gui.ingredients.GuiItemStackFast.MatrixTransformer;
import mezz.jei.util.ItemStackElement;

public class GuiItemStackFastBatch {
    private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
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

	private void renderSimpleItems(@Nullable GuiItemStackFast hovered, boolean effect)
	{
	 // 3d Items
        GlStateManager.pushMatrix();
        {
            GlStateManager.enableLighting();
            GlStateManager.scale(20.0F, 20.0F, -20.0F);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            worldrenderer.startDrawingQuads();
            worldrenderer.setVertexFormat(DefaultVertexFormats.ITEM);
            MatrixTransformer transformer = new MatrixTransformer(new WorldRendererConsumer(worldrenderer));
            for (GuiItemStackFast guiItemStack : renderItems3d) {
                if (hovered != guiItemStack) {
                    guiItemStack.renderItemAndEffectIntoGUI(transformer, true, effect);
                }
            }
            tessellator.draw();
        }
        GlStateManager.popMatrix();

        // 2d Items
        GlStateManager.pushMatrix();
        {
            GlStateManager.disableLighting();
            GlStateManager.scale(32.0F, 32.0F, -32.0F);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            worldrenderer.startDrawingQuads();
            worldrenderer.setVertexFormat(DefaultVertexFormats.ITEM);
            MatrixTransformer transformer = new MatrixTransformer(new WorldRendererConsumer(worldrenderer));
            for (GuiItemStackFast guiItemStack : renderItems2d) {
                if (hovered != guiItemStack) {
                    guiItemStack.renderItemAndEffectIntoGUI(transformer, false, effect);
                }
            }
            tessellator.draw();
        }
        GlStateManager.popMatrix();

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

        renderSimpleItems(hovered, false);
		// effects
		
		GlStateManager.depthMask(false);
        GlStateManager.depthFunc(514);
        GlStateManager.blendFunc(768, 1);
        textureManager.bindTexture(RES_ITEM_GLINT);
        GlStateManager.matrixMode(5890);

        GlStateManager.pushMatrix();
        GlStateManager.scale(8.0F, 8.0F, 8.0F);
        float f = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
        GlStateManager.translate(f, 0.0F, 0.0F);
        GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);

        GlStateManager.matrixMode(5888);
        renderSimpleItems(hovered, true);
        GlStateManager.matrixMode(5890);

        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.scale(8.0F, 8.0F, 8.0F);
        float f1 = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F / 8.0F;
        GlStateManager.translate(-f1, 0.0F, 0.0F);
        GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);

        GlStateManager.matrixMode(5888);
        renderSimpleItems(hovered, true);
        GlStateManager.matrixMode(5890);

        GlStateManager.popMatrix();

        GlStateManager.matrixMode(5888);
        GlStateManager.blendFunc(770, 771);
        GlStateManager.depthFunc(515);
        GlStateManager.depthMask(true);
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
