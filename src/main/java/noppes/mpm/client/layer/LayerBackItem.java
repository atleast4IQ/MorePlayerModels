/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.block.model.ItemTransform
 *  net.minecraft.client.renderer.entity.LivingEntityRenderer
 *  net.minecraft.client.renderer.entity.player.PlayerRenderer
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.SwordItem
 */
package noppes.mpm.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import noppes.mpm.MorePlayerModels;
import noppes.mpm.client.layer.LayerInterface;

public class LayerBackItem
extends LayerInterface {
    public LayerBackItem(PlayerRenderer render) {
        super(render);
    }

    @Override
    public void render(PoseStack mStack, MultiBufferSource typeBuffer, int lightmapUV, float limbSwing, float limbSwingAmount, float partialTicks, float age, float netHeadYaw, float headPitch) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemStack itemstack = this.playerdata.backItem;
        if (!MorePlayerModels.EnableBackItem || itemstack.isEmpty() || ItemStack.isSameItem((ItemStack)itemstack, (ItemStack)this.player.getInventory().getSelected())) {
            return;
        }
        Item item = itemstack.getItem();
        if (item instanceof BlockItem) {
            return;
        }
        this.base.body.translateAndRotate(mStack);
        mStack.translate(0.0f, 0.36f, 0.14f);
        mStack.mulPose(Axis.XP.rotationDegrees(180.0f));
        if (item instanceof SwordItem) {
            mStack.mulPose(Axis.XN.rotationDegrees(180.0f));
        }
        BakedModel model = minecraft.getItemRenderer().getItemModelShaper().getItemModel(itemstack);
        ItemTransform p_175034_1_ = model.getTransforms().thirdPersonRightHand;
        mStack.scale(p_175034_1_.scale.x(), p_175034_1_.scale.y(), p_175034_1_.scale.z());
        minecraft.getItemRenderer().renderStatic((LivingEntity)this.player, itemstack, ItemDisplayContext.NONE, false, noppes.mpm.client.RenderStateScope.copyPose(mStack), typeBuffer, this.player.level(), lightmapUV, LivingEntityRenderer.getOverlayCoords((LivingEntity)this.player, (float)0.0f), this.player.getId() + ItemDisplayContext.NONE.ordinal());
    }

    @Override
    public void rotate(float limbSwing, float limbSwingAmount, float partialTicks, float age, float netHeadYaw, float headPitch) {
    }
}

