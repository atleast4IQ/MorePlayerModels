/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonObject
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.model.geom.PartPose
 *  net.minecraft.client.model.geom.builders.CubeDeformation
 *  net.minecraft.client.model.geom.builders.CubeListBuilder
 *  net.minecraft.client.model.geom.builders.LayerDefinition
 *  net.minecraft.client.model.geom.builders.MeshDefinition
 *  net.minecraft.client.model.geom.builders.PartDefinition
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 */
package noppes.mpm.client.parts;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import noppes.mpm.client.parts.ModelPartWrapper;
import noppes.mpm.client.parts.MpmPartAbstractClient;
import noppes.mpm.client.parts.MpmPartData;
import noppes.mpm.client.parts.MpmPartReader;
import noppes.mpm.mixin.ModelPartMixin;
import noppes.mpm.shared.client.model.Model2DRenderer;
import noppes.mpm.shared.util.NopVector2i;
import noppes.mpm.shared.util.NopVector3f;

public class MpmPartBedrock
extends MpmPartAbstractClient {
    public final Map<ResourceLocation, Model2DRenderer> playerModels = new HashMap<ResourceLocation, Model2DRenderer>();
    private ModelPart model;
    private ModelPartMixin modelMixin;
    public NopVector2i textureSize = NopVector2i.ZERO;

    @Override
    public void render(MpmPartData data, PoseStack mStack, VertexConsumer c, int lightmapUV, AbstractClientPlayer player) {
        mStack.pushPose();
        try {
            if (this.model != null) {
                Map<String, ModelPart> children = this.modelMixin.getChildren();
                this.model.translateAndRotate(mStack);
                float f = 0.0625f;
                mStack.translate(-this.rotatePoint.x * f, -this.rotatePoint.y * f, -this.rotatePoint.z * f);
                mStack.scale(this.scale.x, this.scale.y, this.scale.z);
                int color = 0xFF000000 | data.getColor();
                for (ModelPart modelpart : children.values()) {
                    modelpart.render(noppes.mpm.client.RenderStateScope.copyPose(mStack), c, lightmapUV, OverlayTexture.NO_OVERLAY, color);
                }
            }
        } finally {
            mStack.popPose();
        }
    }

    @Override
    public void load(JsonObject renderData) {
        if (renderData != null && renderData.size() > 0) {
            MeshDefinition meshdefinition = new MeshDefinition();
            PartDefinition root = meshdefinition.getRoot();
            JsonObject ob = renderData.get("minecraft:geometry").getAsJsonArray().get(0).getAsJsonObject();
            JsonObject description = ob.get("description").getAsJsonObject();
            this.textureSize = new NopVector2i(description.get("texture_width").getAsInt(), description.get("texture_height").getAsInt());
            JsonArray bones = ob.get("bones").getAsJsonArray();
            HashMap<String, PartDefinition> namedParts = new HashMap<String, PartDefinition>();
            HashMap<String, NopVector3f> parentPivots = new HashMap<String, NopVector3f>();
            HashMap<String, ModelPartWrapper> defaultPose = new HashMap<String, ModelPartWrapper>();
            for (int i = 0; i < bones.size(); ++i) {
                JsonObject bone = bones.get(i).getAsJsonObject();
                String name = bone.get("name").getAsString();
                String pName = bone.has("parent") ? bone.get("parent").getAsString() : null;
                PartDefinition parent = pName != null && namedParts.containsKey(pName) ? (PartDefinition)namedParts.get(pName) : root;
                NopVector3f ppivot = parentPivots.containsKey(pName) ? (NopVector3f)parentPivots.get(pName) : NopVector3f.ZERO;
                NopVector3f pivot = MpmPartReader.jsonVector3f(bone.get("pivot"));
                parentPivots.put(name, pivot);
                NopVector3f rotation = MpmPartReader.jsonVector3f(bone.get("rotation")).mul((float)Math.PI / 180);
                PartPose pose = PartPose.offsetAndRotation((float)(pivot.x - ppivot.x), (float)(ppivot.y - pivot.y), (float)(pivot.z - ppivot.z), (float)rotation.x, (float)rotation.y, (float)rotation.z);
                PartDefinition partDef = parent.addOrReplaceChild(name, CubeListBuilder.create(), pose);
                defaultPose.put(name, new ModelPartWrapper(name, (ModelPart)null, new NopVector3f(pose.x, pose.y, pose.z), rotation));
                if (bone.has("cubes")) {
                    JsonArray cubes = bone.get("cubes").getAsJsonArray();
                    for (int j = 0; j < cubes.size(); ++j) {
                        CubeListBuilder builder = CubeListBuilder.create();
                        JsonObject cube = cubes.get(j).getAsJsonObject();
                        NopVector2i uv = MpmPartReader.jsonVector2i(cube.get("uv"));
                        boolean mirror = cube.has("mirror") && cube.get("mirror").getAsBoolean();
                        NopVector3f cpivot = MpmPartReader.jsonVector3f(cube.get("pivot"));
                        rotation = MpmPartReader.jsonVector3f(cube.get("rotation")).mul((float)Math.PI / 180);
                        NopVector3f origin = MpmPartReader.jsonVector3f(cube.get("origin"));
                        NopVector3f size = MpmPartReader.jsonVector3f(cube.get("size"));
                        CubeDeformation deform = cube.has("inflate") ? new CubeDeformation(cube.get("inflate").getAsFloat()) : CubeDeformation.NONE;
                        builder.texOffs(uv.x, uv.y).mirror(mirror).addBox(origin.x - cpivot.x, cpivot.y - size.y - origin.y, origin.z - cpivot.z, size.x, size.y, size.z, deform);
                        partDef.addOrReplaceChild("cube_" + name + j, builder, PartPose.offsetAndRotation((float)(cpivot.x - pivot.x), (float)(pivot.y - cpivot.y), (float)(cpivot.z - pivot.z), (float)rotation.x, (float)rotation.y, (float)rotation.z));
                    }
                }
                namedParts.put(name, partDef);
            }
            this.model = LayerDefinition.create((MeshDefinition)meshdefinition, (int)description.get("texture_width").getAsInt(), (int)description.get("texture_height").getAsInt()).bakeRoot();
            this.model.setPos(this.translate.x, this.translate.y, this.translate.z);
            for (Map.Entry entry : defaultPose.entrySet()) {
                ((ModelPartWrapper)entry.getValue()).mcPart = this.getChild(this.model, (String)entry.getKey());
            }
            defaultPose.put(null, new ModelPartWrapper(null, this.model, this.translate, this.rotate));
            this.defaultPose = defaultPose;
            NopVector3f rotation = this.rotate.mul((float)Math.PI / 180);
            this.model.setRotation(rotation.x, rotation.y, rotation.z);
            this.modelMixin = (ModelPartMixin)(Object)this.model;
        }
    }

    private ModelPart getChild(ModelPart root, String name) {
        Map<String, ModelPart> children = ((ModelPartMixin)(Object)root).getChildren();
        if (children.containsKey(name)) {
            return children.get(name);
        }
        for (ModelPart child : children.values()) {
            ModelPart p = this.getChild(child, name);
            if (p == null) continue;
            return p;
        }
        return null;
    }
}
