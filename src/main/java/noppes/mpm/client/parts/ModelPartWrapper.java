/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.geom.ModelPart
 */
package noppes.mpm.client.parts;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import noppes.mpm.client.parts.AnimationContainer;
import noppes.mpm.constants.EnumAnimation;
import noppes.mpm.shared.client.model.NopModelPart;
import noppes.mpm.shared.util.NopVector3f;

public class ModelPartWrapper {
    public final String name;
    protected ModelPart mcPart = null;
    protected NopModelPart mpmPart = null;
    public final NopVector3f oriPos;
    public final NopVector3f oriRot;
    public Map<EnumAnimation, AnimationContainer> animations = new HashMap<EnumAnimation, AnimationContainer>();

    public ModelPartWrapper(String name, ModelPart mcPart, NopVector3f oriPos, NopVector3f oriRot) {
        this.name = name;
        this.mcPart = mcPart;
        this.oriRot = oriRot;
        this.oriPos = oriPos;
    }

    public ModelPartWrapper(String name, NopModelPart mpmPart, NopVector3f oriPos, NopVector3f oriRot) {
        this.name = name;
        this.mpmPart = mpmPart;
        this.oriRot = oriRot;
        this.oriPos = oriPos;
    }

    public NopVector3f getPos() {
        if (this.mcPart != null) {
            return new NopVector3f(this.mcPart.x, this.mcPart.y, this.mcPart.z);
        }
        return new NopVector3f(this.mpmPart.x, this.mpmPart.y, this.mpmPart.z);
    }

    public void setPos(NopVector3f pos) {
        if (this.mcPart != null) {
            this.mcPart.setPos(pos.x, pos.y, pos.z);
        } else {
            this.mpmPart.setPos(pos.x, pos.y, pos.z);
        }
    }

    public NopVector3f getRot() {
        if (this.mcPart != null) {
            return new NopVector3f(this.mcPart.xRot, this.mcPart.yRot, this.mcPart.zRot);
        }
        return new NopVector3f(this.mpmPart.xRot, this.mpmPart.yRot, this.mpmPart.zRot);
    }

    public void setRot(NopVector3f rot) {
        if (this.mcPart != null) {
            this.mcPart.setRotation(rot.x, rot.y, rot.z);
        } else {
            this.mpmPart.setRotation(rot);
        }
    }

    public boolean isVisible() {
        return this.mcPart != null ? this.mcPart.visible : this.mpmPart.visible;
    }

    public void setVisible(boolean b) {
        if (this.mcPart != null) {
            this.mcPart.visible = b;
        } else {
            this.mpmPart.visible = b;
        }
    }
}

