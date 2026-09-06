/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.Tesselator
 *  com.mojang.blaze3d.vertex.VertexBuffer
 *  com.mojang.blaze3d.vertex.VertexBuffer$Usage
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.Resource
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 */
package noppes.mpm.shared.client.model;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import noppes.mpm.shared.client.ImageDownloadAlt;
import noppes.mpm.shared.client.ResourceDownloader;
import noppes.mpm.shared.client.model.NopModelPart;
import noppes.mpm.shared.client.model.util.CustomRenderStates;
import noppes.mpm.shared.client.model.util.Polygon;
import noppes.mpm.shared.client.model.util.Vertex;
import noppes.mpm.shared.util.NopVector2i;
import noppes.mpm.shared.util.NopVector3f;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

public class Model2DRenderer
extends NopModelPart {
    private final float x1;
    private final float x2;
    private final float y1;
    private final float y2;
    private final int width;
    private final int height;
    private final NopVector2i texPos;
    private float rotationOffsetX;
    private float rotationOffsetY;
    private float rotationOffsetZ;
    public static ResourceLocation textureOverride = null;
    private ResourceLocation location;
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private float thickness = 1.0f;
    private final Map<ResourceLocation, Polygon[]> compiled = new HashMap<ResourceLocation, Polygon[]>();
    VertexBuffer cache;

    public Model2DRenderer(int texWidth, int texHeight, int x, int y, int width, int height, ResourceLocation location) {
        super(texWidth, texHeight, 0, 0);
        this.width = width;
        this.height = height;
        this.texPos = new NopVector2i(x, y);
        this.setTexSize(texWidth, texHeight);
        this.location = location;
        this.x1 = (float)x / (float)texWidth;
        this.y1 = (float)y / (float)texHeight;
        this.x2 = ((float)x + (float)width) / (float)texWidth;
        this.y2 = ((float)y + (float)height) / (float)texHeight;
        this.init(location);
    }

    public Polygon[] init(ResourceLocation location) {
        float f9;
        float f8;
        float f7;
        int k;
        BufferedImage image;
        Polygon[] polygons;
        block34: {
            polygons = this.compiled.get(location);
            if (polygons != null || location == null || location.toString().isEmpty()) {
                return polygons;
            }
            if (ResourceDownloader.contains(location)) {
                return null;
            }
            image = null;
            try {
                Resource resource = (Resource)Minecraft.getInstance().getResourceManager().getResource(location).get();
                try (var input = resource.open()) {
                    image = ImageIO.read(input);
                }
            }
            catch (Exception e) {
                AbstractTexture text = Minecraft.getInstance().getTextureManager().getTexture(location, null);
                if (text == null || !(text instanceof ImageDownloadAlt)) break block34;
                try (FileInputStream input2 = new FileInputStream(((ImageDownloadAlt)text).cacheFile);){
                    image = ImageIO.read(input2);
                }
                catch (Exception input2) {
                    // empty catch block
                }
            }
        }
        int scaleW = 1;
        int scaleH = 1;
        if (image != null) {
            scaleW = Math.max(1, (int)((float)image.getWidth() / this.xTexSize));
            scaleH = Math.max(1, (int)((float)image.getHeight() / this.yTexSize));
        }
        int width = this.width * scaleW;
        int height = this.height * scaleH;
        NopVector2i texPos = this.texPos.mul(scaleW, scaleH);
        polygons = new Polygon[6];
        polygons[0] = new Polygon(new Vector3f(0.0f, 0.0f, 1.0f), new Vertex(0.0f, 0.0f, 0.0f, this.x1, this.y2), new Vertex(1.0f, 0.0f, 0.0f, this.x2, this.y2), new Vertex(1.0f, 1.0f, 0.0f, this.x2, this.y1), new Vertex(0.0f, 1.0f, 0.0f, this.x1, this.y1));
        polygons[1] = new Polygon(new Vector3f(0.0f, 0.0f, -1.0f), new Vertex(0.0f, 1.0f, -0.0625f, this.x1, this.y1), new Vertex(1.0f, 1.0f, -0.0625f, this.x2, this.y1), new Vertex(1.0f, 0.0f, -0.0625f, this.x2, this.y2), new Vertex(0.0f, 0.0f, -0.0625f, this.x1, this.y2));
        ArrayList<Vertex> list = new ArrayList<Vertex>();
        ArrayList<Vertex> list2 = new ArrayList<Vertex>();
        for (k = 0; k < width; ++k) {
            f7 = (float)k / (float)width;
            f8 = this.x1 + (this.x2 - this.x1) * f7 - 0.5f * (this.x1 - this.x2) / (float)width;
            f9 = f7 + 1.0f / (float)width;
            boolean left = false;
            boolean right = false;
            if (image == null) {
                left = true;
                right = true;
            } else {
                try {
                    for (int n = 0; n < height; ++n) {
                        if ((image.getRGB(texPos.x + k, texPos.y + n) >> 24 & 0xFF) < 128) continue;
                        if (k + 1 < width && (image.getRGB(texPos.x + k + 1, texPos.y + n) >> 24 & 0xFF) < 128) {
                            right = true;
                        } else if (k + 1 == width) {
                            right = true;
                        }
                        if (k > 0 && (image.getRGB(texPos.x + k - 1, texPos.y + n) >> 24 & 0xFF) < 128) {
                            left = true;
                            continue;
                        }
                        if (k != 0) continue;
                        left = true;
                    }
                }
                catch (Exception n) {
                    // empty catch block
                }
            }
            if (left) {
                list.add(new Vertex(f7, 0.0f, -0.0625f, f8, this.y2));
                list.add(new Vertex(f7, 0.0f, 0.0f, f8, this.y2));
                list.add(new Vertex(f7, 1.0f, 0.0f, f8, this.y1));
                list.add(new Vertex(f7, 1.0f, -0.0625f, f8, this.y1));
            }
            if (!right) continue;
            list2.add(new Vertex(f9, 1.0f, -0.0625f, f8, this.y1));
            list2.add(new Vertex(f9, 1.0f, 0.0f, f8, this.y1));
            list2.add(new Vertex(f9, 0.0f, 0.0f, f8, this.y2));
            list2.add(new Vertex(f9, 0.0f, -0.0625f, f8, this.y2));
        }
        polygons[2] = new Polygon(new Vector3f(-1.0f, 0.0f, 0.0f), list.toArray(new Vertex[0]));
        polygons[3] = new Polygon(new Vector3f(1.0f, 0.0f, 0.0f), list2.toArray(new Vertex[0]));
        list = new ArrayList();
        list2 = new ArrayList();
        for (k = 0; k < height; ++k) {
            f7 = (float)k / (float)height;
            f8 = this.y2 + (this.y1 - this.y2) * f7 - 0.5f * (this.y2 - this.y1) / (float)height;
            f9 = f7 + 1.0f / (float)height;
            boolean top = false;
            boolean bottom = false;
            if (image == null) {
                top = true;
                bottom = true;
            } else {
                try {
                    for (int n = 0; n < width; ++n) {
                        int m = height - k - 1;
                        if ((image.getRGB(texPos.x + n, texPos.y + m) >> 24 & 0xFF) < 128) continue;
                        if (m > 0 && (image.getRGB(texPos.x + n, texPos.y + m - 1) >> 24 & 0xFF) < 128) {
                            top = true;
                        } else if (m == 0) {
                            top = true;
                        }
                        if (m + 1 < height && (image.getRGB(texPos.x + n, texPos.y + m + 1) >> 24 & 0xFF) < 128) {
                            bottom = true;
                            continue;
                        }
                        if (m + 1 != height) continue;
                        bottom = true;
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (bottom) {
                list2.add(new Vertex(1.0f, f7, 0.0f, this.x2, f8));
                list2.add(new Vertex(0.0f, f7, 0.0f, this.x1, f8));
                list2.add(new Vertex(0.0f, f7, -0.0625f, this.x1, f8));
                list2.add(new Vertex(1.0f, f7, -0.0625f, this.x2, f8));
            }
            if (!top) continue;
            list.add(new Vertex(0.0f, f9, 0.0f, this.x1, f8));
            list.add(new Vertex(1.0f, f9, 0.0f, this.x2, f8));
            list.add(new Vertex(1.0f, f9, -0.0625f, this.x2, f8));
            list.add(new Vertex(0.0f, f9, -0.0625f, this.x1, f8));
        }
        polygons[4] = new Polygon(new Vector3f(0.0f, 1.0f, 0.0f), list.toArray(new Vertex[0]));
        polygons[5] = new Polygon(new Vector3f(0.0f, -1.0f, 0.0f), list2.toArray(new Vertex[0]));
        this.compiled.put(location, polygons);
        return polygons;
    }

    @Override
    public void render(PoseStack mstack, VertexConsumer builder, int light, int overlay, float red, float green, float blue, float alpha) {
        this.render(textureOverride != null ? textureOverride : this.location, mstack, builder, light, overlay, red, green, blue, alpha);
    }

    public void render(ResourceLocation location, PoseStack mstack, VertexConsumer builder, int light, int overlay, float red, float green, float blue, float alpha) {
        if (!this.visible || location == null || location.toString().isEmpty()) {
            return;
        }
        mstack.pushPose();
        try {
            this.translateAndRotate(mstack);
            float f = 0.0625f;
            mstack.translate(this.rotationOffsetX * f, this.rotationOffsetY * f, this.rotationOffsetZ * f);
            mstack.scale(this.scaleX * (float)this.width / (float)this.height, this.scaleY, this.thickness);
            mstack.mulPose(Axis.XP.rotationDegrees(180.0f));
            if (this.mirror) {
                mstack.translate(0.0f, 0.0f, -1.0f * f);
                mstack.mulPose(Axis.YP.rotationDegrees(180.0f));
            }
            this.renderModel(location, mstack.last().normal(), mstack.last().pose(), builder, light, overlay, red, green, blue, alpha);
        } finally {
            mstack.popPose();
        }
    }

    public void render(ResourceLocation resource, PoseStack mstack, int light, int overlay, float red, float green, float blue, float alpha) {
        if (!this.visible || resource == null) {
            return;
        }
        var previousShader = RenderSystem.getShader();
        int previousTexture = RenderSystem.getShaderTexture(0);
        Matrix4f previousTextureMatrix = new Matrix4f(RenderSystem.getTextureMatrix());
        try {
            RenderSystem.setShader(() -> CustomRenderStates.posTexNormalShader);
            RenderSystem.setShaderTexture((int)0, (ResourceLocation)resource);
            RenderSystem.setTextureMatrix((Matrix4f)new Matrix4f().translation((float)this.texPos.x, (float)this.texPos.y, 0.0f));
            mstack.pushPose();
            try {
                this.translateAndRotate(mstack);
                float f = 0.0625f;
                mstack.translate(this.rotationOffsetX * f, this.rotationOffsetY * f, this.rotationOffsetZ * f);
                mstack.scale(this.scaleX * (float)this.width / (float)this.height, this.scaleY, this.thickness);
                mstack.mulPose(Axis.XP.rotationDegrees(180.0f));
                if (this.mirror) {
                    mstack.translate(0.0f, 0.0f, -1.0f * f);
                    mstack.mulPose(Axis.YP.rotationDegrees(180.0f));
                }
                PoseStack.Pose entry = mstack.last();
                Matrix4f matrix = entry.pose();
                this.cache.drawWithShader(matrix, new Matrix4f(), RenderSystem.getShader());
            } finally {
                mstack.popPose();
            }
        } finally {
            RenderSystem.setShader(() -> previousShader);
            RenderSystem.setShaderTexture(0, previousTexture);
            RenderSystem.setTextureMatrix(previousTextureMatrix);
        }
    }

    public void renderModel(ResourceLocation resource, Matrix3f matrix3f, Matrix4f matrix4f, VertexConsumer builder, int light, int overlay, float red, float green, float blue, float alpha) {
        Polygon[] polygons = this.init(resource);
        if (polygons == null) {
            return;
        }
        for (int i = 0; i < polygons.length; ++i) {
            Polygon p = polygons[i];
            Vector3f vector3f = matrix3f.transform(new Vector3f((Vector3fc)p.normal));
            float nX = vector3f.x();
            float nY = vector3f.y();
            float nZ = vector3f.z();
            for (int j = 0; j < p.vertexes.length; ++j) {
                Vertex vec = p.vertexes[j];
                Vector4f vector4f = matrix4f.transform(new Vector4f((Vector3fc)vec.pos, 1.0f));
                builder.addVertex(vector4f.x(), vector4f.y(), vector4f.z());
                builder.setColor(red, green, blue, alpha);
                builder.setUv(vec.texCoords.x, vec.texCoords.y);
                builder.setOverlay(overlay);
                builder.setLight(light);
                builder.setNormal(nX, nY, nZ);
            }
        }
    }

    private void addVertex(VertexConsumer builder, Matrix4f matrix, float x, float y, float z, float red, float green, float blue, float alpha, float texU, float texV, int overlayUV, int lightmapUV, float normalX, float normalY, float normalZ) {
        Vector4f v = matrix.transform(new Vector4f(x, y, z, 1.0f));
        builder.addVertex(v.x(), v.y(), v.z());
        builder.setColor(red, green, blue, alpha);
        builder.setUv(texU, texV);
        builder.setOverlay(overlayUV);
        builder.setLight(lightmapUV);
        builder.setNormal(normalX, normalY, normalZ);
    }

    public Model2DRenderer setRotationOffset(float x, float y, float z) {
        this.rotationOffsetX = x;
        this.rotationOffsetY = y;
        this.rotationOffsetZ = z;
        return this;
    }

    public Model2DRenderer setRotationOffset(NopVector3f scale) {
        this.rotationOffsetX = scale.x;
        this.rotationOffsetY = scale.y;
        this.rotationOffsetZ = scale.z;
        return this;
    }

    public void setScale(float scale) {
        this.scaleX = scale;
        this.scaleY = scale;
    }

    public void setScale(float x, float y) {
        this.scaleX = x;
        this.scaleY = y;
    }

    public Model2DRenderer setScale(NopVector3f scale) {
        this.scaleX = scale.x;
        this.scaleY = scale.y;
        this.thickness = scale.z;
        return this;
    }

    public void setThickness(float thickness) {
        this.thickness = thickness;
    }
}
