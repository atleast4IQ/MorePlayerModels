/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonObject
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.core.Direction
 */
package noppes.mpm.client.parts;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import noppes.mpm.client.parts.ModelPartWrapper;
import noppes.mpm.client.parts.MpmPartAbstractClient;
import noppes.mpm.client.parts.MpmPartData;
import noppes.mpm.client.parts.MpmPartReader;
import noppes.mpm.client.SkinUtil;
import noppes.mpm.shared.client.model.Model2DRenderer;
import noppes.mpm.shared.client.model.ModelPlaneRenderer;
import noppes.mpm.shared.client.model.NopModelPart;
import noppes.mpm.shared.util.NopVector2i;
import noppes.mpm.shared.util.NopVector3f;

public class MpmPartSimple
extends MpmPartAbstractClient {
    private NopModelPart model;
    public NopVector2i textureSize = NopVector2i.ZERO;

    @Override
    public void render(MpmPartData data, PoseStack mStack, VertexConsumer c, int lightmapUV, AbstractClientPlayer player) {
        var previousTexture = Model2DRenderer.textureOverride;
        mStack.pushPose();
        try {
            Model2DRenderer.textureOverride = data.usePlayerSkin ? SkinUtil.getTexture(player) : null;
            if (this.model != null) {
                this.translateAndRotate(mStack);
                this.model.render(mStack, c, lightmapUV, OverlayTexture.NO_OVERLAY, data.color.x, data.color.y, data.color.z, 1.0f);
            }
        } finally {
            Model2DRenderer.textureOverride = previousTexture;
            mStack.popPose();
        }
    }

    public void translateAndRotate(PoseStack pose) {
        pose.scale(this.scale.x, this.scale.y, this.scale.z);
        pose.translate(this.pos.x / 16.0f, this.pos.y / 16.0f, this.pos.z / 16.0f);
        if (this.rot.z != 0.0f) {
            pose.mulPose(Axis.ZP.rotation(this.rot.z));
        }
        if (this.rot.y != 0.0f) {
            pose.mulPose(Axis.YP.rotation(this.rot.y));
        }
        if (this.rot.x != 0.0f) {
            pose.mulPose(Axis.XP.rotation(this.rot.x));
        }
        float f = 0.0625f;
        pose.translate(this.rotatePoint.x * f, this.rotatePoint.y * f, this.rotatePoint.z * f);
    }

    @Override
    public void load(JsonObject renderData) {
        if (renderData != null && renderData.size() > 0) {
            this.textureSize = MpmPartReader.jsonVector2i(renderData.get("texture_size"));
            this.model = new NopModelPart(this.textureSize.x, this.textureSize.y, 0, 0);
            JsonArray parts = renderData.get("parts").getAsJsonArray();
            HashMap<String, NopModelPart> allParts = new HashMap<String, NopModelPart>();
            for (int i = 0; i < parts.size(); ++i) {
                String parent;
                NopModelPart mr;
                JsonObject part = parts.get(i).getAsJsonObject();
                String name = part.has("name") ? part.get("name").getAsString() : UUID.randomUUID().toString();
                NopVector2i texturePosition = MpmPartReader.jsonVector2i(part.get("texture_position"));
                NopVector2i partSize = MpmPartReader.jsonVector2i(part.get("part_size"));
                NopVector3f translate = MpmPartReader.jsonVector3f(part.get("translate"));
                NopVector3f rotate = MpmPartReader.jsonVector3f(part.get("rotate")).mul((float)Math.PI / 180);
                NopVector3f scale = MpmPartReader.jsonVector3fOrOne(part.get("scale"));
                NopVector3f rotatePoint = MpmPartReader.jsonVector3f(part.get("rotate_offset"));
                if (part.has("empty") && part.get("empty").getAsBoolean()) {
                    mr = new NopModelPart(this.textureSize.x, this.textureSize.y, 0, 0);
                    mr.scale = scale;
                } else if (part.has("plane") && part.get("plane").getAsBoolean()) {
                    Direction direction = part.has("direction") ? Direction.valueOf((String)part.get("direction").getAsString().toUpperCase()) : Direction.NORTH;
                    mr = new ModelPlaneRenderer(this.textureSize.x, this.textureSize.y, texturePosition.x, texturePosition.y).mirror(part.has("mirror") && part.get("mirror").getAsBoolean()).addPlane(rotatePoint.x, rotatePoint.y, rotatePoint.z, partSize.x, partSize.y, scale, direction);
                } else {
                    mr = new Model2DRenderer(this.textureSize.x, this.textureSize.y, texturePosition.x, texturePosition.y, partSize.x, partSize.y, this.texture).setScale(scale).setRotationOffset(rotatePoint);
                }
                mr.setRotation(rotate).setPos(translate);
                if (part.has("mirror") && part.get("mirror").getAsBoolean()) {
                    mr.mirror = true;
                }
                this.defaultPose.put(name, new ModelPartWrapper(name, mr, translate, rotate));
                allParts.put(name, mr);
                String string = parent = part.has("parent") ? part.get("parent").getAsString() : null;
                if (allParts.containsKey(parent)) {
                    ((NopModelPart)allParts.get(parent)).addChild(name, mr);
                    continue;
                }
                this.model.addChild(name, mr);
            }
            this.defaultPose.put(null, new ModelPartWrapper(null, this.model, this.translate, this.rotate.mul((float)Math.PI / 180)));
            this.model.setRotation(this.rotate.mul((float)Math.PI / 180));
            this.model.setPos(this.translate);
        }
    }
}
