package mezz.jei.gui.ingredients;

import java.util.List;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.util.Log;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;

import org.apache.commons.lang3.tuple.Pair;

@SuppressWarnings("deprecation")
public class GuiItemStackFast {
	private static final float RADS = (float) (180.0 / Math.PI);

	private final Matrix4f tempMat = new Matrix4f();
	private final int xPosition;
	private final int yPosition;
	private final int width;
	private final int height;
	private final int padding;

	private ItemStack itemStack;
	private IBakedModel bakedModel;

	public GuiItemStackFast(int xPosition, int yPosition, int padding) {
		this.xPosition = xPosition;
		this.yPosition = yPosition;
		this.padding = padding;
		this.width = 16 + (2 * padding);
		this.height = 16 + (2 * padding);
	}

	public void setItemStack(ItemStack itemStack) {
		this.itemStack = itemStack;
		this.bakedModel = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(itemStack);
	}

	public ItemStack getItemStack() {
		return itemStack;
	}

	public boolean isBuiltInRenderer() {
		return bakedModel != null && bakedModel.isBuiltInRenderer();
	}

	public boolean isGui3d() {
		return bakedModel != null && bakedModel.isGui3d();
	}

	public void clear() {
		this.itemStack = null;
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		return (itemStack != null) && (mouseX >= xPosition) && (mouseY >= yPosition) && (mouseX < xPosition + width) && (mouseY < yPosition + height);
	}

	public void renderItemAndEffectIntoGUI(MatrixTransformer transformer, boolean isGui3d, boolean renderEffect) {
	    if (itemStack == null) {
			return;
		}
	    if(renderEffect && !itemStack.hasEffect()) return;

		if (Config.editModeEnabled) {
			renderEditMode();
		}

		int x = xPosition + padding + 8;
		int y = yPosition + padding + 8;

		Matrix4f transformMat = new Matrix4f();
		transformMat.setIdentity();

		if (isGui3d) {
			tempMat.setIdentity();
			tempMat.setTranslation(new Vector3f(((float) x) / 20f, ((float) y) / 20f, (100.0F + 50f) / -20f));
			transformMat.mul(tempMat);
			tempMat.rotX(210f / RADS);
			transformMat.mul(tempMat);
			tempMat.rotY(-135f / RADS);
			transformMat.mul(tempMat);
		} else {
			tempMat.setIdentity();
			tempMat.setTranslation(new Vector3f(((float) x) / 32f, ((float) y) / 32f, (100.0F + 50f) / -32f));
			transformMat.mul(tempMat);
			tempMat.rotX(180f / RADS);
			transformMat.mul(tempMat);
		}

        handleCameraTransforms(transformMat, this.bakedModel, ItemCameraTransforms.TransformType.GUI);

		tempMat.setIdentity();
		tempMat.setScale(0.5f);
		transformMat.mul(tempMat);

		tempMat.setIdentity();
		tempMat.setTranslation(new Vector3f(-0.5f, -0.5f, -0.5f));
		transformMat.mul(tempMat);

		Matrix3f invTransposeMat = new Matrix3f();
		transformMat.getRotationScale(invTransposeMat);
		invTransposeMat.invert();
		invTransposeMat.transpose();

		transformer.setTransform(transformMat);
		transformer.setInvtranspose(invTransposeMat);
//		ForgeHooksClient.multiplyCurrentGlMatrix(transformMat);

//		transformMat.setIdentity();
//		invTransposeMat.setIdentity();

		if(!renderEffect)
		{
		    renderModel(transformer, bakedModel, itemStack);
		}
		else
		{
		    renderEffect(transformer, bakedModel);
		}
	}

	public void handleCameraTransforms(Matrix4f transformMat, IBakedModel model, ItemCameraTransforms.TransformType cameraTransformType) {
		if (model instanceof IPerspectiveAwareModel) {
			Pair<IBakedModel, Matrix4f> pair = ((IPerspectiveAwareModel) model).handlePerspective(cameraTransformType);

			if (pair.getRight() != null) {
				transformMat.mul(pair.getRight());
			}
		} else {
			applyVanillaTransform(transformMat, model.getItemCameraTransforms().gui);
		}
	}

	public void applyVanillaTransform(Matrix4f transformMat, ItemTransformVec3f transform) {
		if (transform != ItemTransformVec3f.DEFAULT) {
			tempMat.setIdentity();
			tempMat.setTranslation(transform.translation);
			transformMat.mul(tempMat);

			tempMat.rotY(transform.rotation.y);
			transformMat.mul(tempMat);

			tempMat.rotX(transform.rotation.x);
			transformMat.mul(tempMat);

			tempMat.rotZ(transform.rotation.z);
			transformMat.mul(tempMat);

			tempMat.setIdentity();
			tempMat.setM00(transform.scale.x);
			tempMat.setM11(transform.scale.y);
			tempMat.setM22(transform.scale.z);
			transformMat.mul(tempMat);
		}
	}

	private void renderModel(MatrixTransformer transformer, IBakedModel model, ItemStack stack) {
		renderModel(transformer, model, -1, stack);
	}

	@SuppressWarnings("unchecked")
    private void renderModel(MatrixTransformer transformer, IBakedModel model, int color, ItemStack stack) {
	    /*Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.startDrawingQuads();
        worldrenderer.setVertexFormat(DefaultVertexFormats.ITEM);*/
        for (EnumFacing enumfacing : EnumFacing.VALUES) {
			this.renderQuads(transformer, model.getFaceQuads(enumfacing), color, stack);
		}

		this.renderQuads(transformer, model.getGeneralQuads(), color, stack);
		//tessellator.draw();
	}

	private void renderQuads(MatrixTransformer transformer, List<BakedQuad> quads, int color, ItemStack stack) {
		boolean flag = color == -1 && stack != null;
		int auxColor;

		for (BakedQuad bakedquad : quads) {
			auxColor = color;

			if (flag && bakedquad.hasTintIndex()) {
				auxColor = stack.getItem().getColorFromItemStack(stack, bakedquad.getTintIndex());

				if (EntityRenderer.anaglyphEnable) {
					auxColor = TextureUtil.anaglyphColor(auxColor);
				}

				auxColor |= -16777216;
			}
			transformer.setAuxColor(auxColor);
			bakedquad.pipe(transformer);
		}
	}

	private void renderEffect(MatrixTransformer transformer, IBakedModel model) {
		this.renderModel(transformer, model, -8372020);
	}

	private void renderModel(MatrixTransformer transformer, IBakedModel model, int color) {
		this.renderModel(transformer, model, color, null);
	}

	public void renderSlow() {
		if (Config.editModeEnabled) {
			renderEditMode();
		}

		Minecraft minecraft = Minecraft.getMinecraft();
		FontRenderer font = getFontRenderer(minecraft, itemStack);
		RenderItem renderItem = minecraft.getRenderItem();
		renderItem.renderItemAndEffectIntoGUI(itemStack, xPosition + padding, yPosition + padding);
		renderItem.renderItemOverlayIntoGUI(font, itemStack, xPosition + padding, yPosition + padding, null);
	}

	private void renderEditMode() {
		if (Config.isItemOnConfigBlacklist(itemStack, false)) {
			GuiScreen.drawRect(xPosition + padding, yPosition + padding, xPosition + 8 + padding, yPosition + 16 + padding, 0xFFFFFF00);
		}
		if (Config.isItemOnConfigBlacklist(itemStack, true)) {
			GuiScreen.drawRect(xPosition + 8 + padding, yPosition + padding, xPosition + 16 + padding, yPosition + 16 + padding, 0xFFFF0000);
		}
	}

	public FontRenderer getFontRenderer(@Nonnull Minecraft minecraft, @Nonnull ItemStack itemStack) {
		FontRenderer fontRenderer = itemStack.getItem().getFontRenderer(itemStack);
		if (fontRenderer == null) {
			fontRenderer = minecraft.fontRendererObj;
		}
		return fontRenderer;
	}

	public void drawHovered(Minecraft minecraft, int mouseX, int mouseY) {
		try {
			Gui.drawRect(xPosition, yPosition, xPosition + width, yPosition + width, 0x7FFFFFFF);

			renderSlow();

			List<String> tooltip = getTooltip(minecraft, itemStack);
			FontRenderer fontRenderer = getFontRenderer(minecraft, itemStack);
			TooltipRenderer.drawHoveringText(minecraft, tooltip, mouseX, mouseY, fontRenderer);
		} catch (RuntimeException e) {
			Log.error("Exception when rendering tooltip on {}.", itemStack, e);
		}
	}

	@SuppressWarnings("unchecked")
	public static List<String> getTooltip(@Nonnull Minecraft minecraft, @Nonnull ItemStack itemStack) {
		List<String> list = itemStack.getTooltip(minecraft.thePlayer, minecraft.gameSettings.advancedItemTooltips);
		for (int k = 0; k < list.size(); ++k) {
			if (k == 0) {
				list.set(k, itemStack.getRarity().rarityColor + list.get(k));
			} else {
				list.set(k, EnumChatFormatting.GRAY + list.get(k));
			}
		}

		if (Config.editModeEnabled) {
			list.add("");
			list.add(EnumChatFormatting.ITALIC + Translator.translateToLocal("gui.jei.editMode.description"));
			if (Config.isItemOnConfigBlacklist(itemStack, false)) {
				String description = EnumChatFormatting.YELLOW + Translator.translateToLocal("gui.jei.editMode.description.show");
				list.addAll(minecraft.fontRendererObj.listFormattedStringToWidth(description, Constants.MAX_TOOLTIP_WIDTH));
			} else {
				String description = EnumChatFormatting.YELLOW + Translator.translateToLocal("gui.jei.editMode.description.hide");
				list.addAll(minecraft.fontRendererObj.listFormattedStringToWidth(description, Constants.MAX_TOOLTIP_WIDTH));
			}

			Item item = itemStack.getItem();
			if (item.getHasSubtypes()) {
				if (Config.isItemOnConfigBlacklist(itemStack, true)) {
					String description = EnumChatFormatting.RED + Translator.translateToLocal("gui.jei.editMode.description.show.wild");
					list.addAll(minecraft.fontRendererObj.listFormattedStringToWidth(description, Constants.MAX_TOOLTIP_WIDTH));
				} else {
					String description = EnumChatFormatting.RED + Translator.translateToLocal("gui.jei.editMode.description.hide.wild");
					list.addAll(minecraft.fontRendererObj.listFormattedStringToWidth(description, Constants.MAX_TOOLTIP_WIDTH));
				}
			}
		}

		return list;
	}

	public static class MatrixTransformer implements IVertexConsumer
    {
	    private final IVertexConsumer parent;
	    private final Matrix4f transform = new Matrix4f();
	    private final Matrix3f invtranspose = new Matrix3f();;

	    private boolean colored = false;
        private int vertices = 0;

        private final float[] auxColor = new float[]{1, 1, 1, 1};
        private final float[][] buf = new float[3][4];
        private final Vector4f pos = new Vector4f();
        private final Vector3f normal = new Vector3f();

        @Override
        public void setQuadColored()
        {
            colored = true;
        }

        public MatrixTransformer(IVertexConsumer parent)
        {
            this.parent = parent;
        }

        public void setTransform(Matrix4f transform)
        {
            this.transform.set(transform);
        }

        public void setInvtranspose(Matrix3f invtranspose)
        {
            this.invtranspose.set(invtranspose);
        }

        public void setAuxColor(int auxColor)
        {
            // magic BGRA -> RGBA
            this.auxColor[2] = (float)(auxColor & 0xFF) / 0xFF;
            this.auxColor[1] = (float)((auxColor >>> 8) & 0xFF) / 0xFF;
            this.auxColor[0] = (float)((auxColor >>> 16) & 0xFF) / 0xFF;
            this.auxColor[3] = (float)((auxColor >>> 24) & 0xFF) / 0xFF;
        }

        @Override public void put(int element, float... data)
        {
            VertexFormatElement el = DefaultVertexFormats.ITEM.getElement(element);

            if(el.getUsage() == VertexFormatElement.EnumUsage.POSITION)
            {
                pos.set(data);
                if(pos.w == 0) pos.w = 1;
                transform.transform(pos);
                pos.get(buf[0]);
                parent.put(element, buf[0]);
            }
            else if(el.getUsage() == VertexFormatElement.EnumUsage.NORMAL)
            {
                normal.set(data);
                invtranspose.transform(normal);
                normal.normalize();
                normal.get(buf[1]);
                buf[1][3] = 0;
                parent.put(element, buf[1]);
            }
            else if(el.getUsage() == VertexFormatElement.EnumUsage.COLOR)
            {
                System.arraycopy(auxColor, 0, buf[2], 0, buf[2].length);
                if(colored)
                {
                    for(int i = 0; i < 4; i++)
                    {
                        buf[2][i] *= data[i];
                    }
                }
                parent.put(element, buf[2]);
            }
            else
            {
                parent.put(element, data);
            }
            if(element == DefaultVertexFormats.ITEM.getElementCount() - 1)
            {
                vertices++;
                if(vertices == 4)
                {
                    vertices = 0;
                    colored = false;
                }
            }
        }

        public VertexFormat getVertexFormat()
        {
            return parent.getVertexFormat();
        }

        @Override
        public void setQuadTint(int tint)
        {
            parent.setQuadTint(tint);
        }

        @Override
        public void setQuadOrientation(EnumFacing orientation)
        {
            parent.setQuadOrientation(orientation);
        }
    };
}
