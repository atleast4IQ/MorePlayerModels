/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.resources.ResourceLocation
 */
package noppes.mpm.client.parts;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import noppes.mpm.client.parts.ModelPartWrapper;
import noppes.mpm.client.parts.MpmPart;
import noppes.mpm.client.parts.MpmPartData;
import noppes.mpm.client.SkinUtil;
import noppes.mpm.shared.util.NopVector3f;

public abstract class MpmPartAbstractClient
extends MpmPart {
    public NopVector3f pos = NopVector3f.ZERO;
    public NopVector3f rot = NopVector3f.ZERO;
    protected Map<String, ModelPartWrapper> defaultPose = new HashMap<String, ModelPartWrapper>();

    public PartScope saveRenderState() {
        return new PartScope(this);
    }

    public static final class PartScope implements AutoCloseable {
        private final MpmPartAbstractClient part;
        private final NopVector3f pos, rot;
        private final java.util.List<Runnable> restores;

        private PartScope(MpmPartAbstractClient part) {
            this.part = part;
            pos = part.pos;
            rot = part.rot;
            restores = part.defaultPose.values().stream().map(wrapper -> {
                NopVector3f position = wrapper.getPos();
                NopVector3f rotation = wrapper.getRot();
                boolean visible = wrapper.isVisible();
                return (Runnable)() -> {
                    wrapper.setPos(position);
                    wrapper.setRot(rotation);
                    wrapper.setVisible(visible);
                };
            }).toList();
        }

        @Override
        public void close() {
            part.pos = pos;
            part.rot = rot;
            restores.forEach(Runnable::run);
        }
    }

    public void render(MpmPartData data, PoseStack mStack, MultiBufferSource typeBuffer, int lightmapUV, AbstractClientPlayer player) {
        VertexConsumer c = typeBuffer.getBuffer(RenderType.entityTranslucent(data.usePlayerSkin ? SkinUtil.getTexture(player) : data.getTexture()));
        this.render(data, mStack, c, lightmapUV, player);
    }

    public void render(MpmPartData data, PoseStack mStack, VertexConsumer c, int lightmapUV, AbstractClientPlayer player) {
    }

    @Override
    public final ModelPartWrapper getPart(String name) {
        return this.defaultPose.get(name);
    }
}
