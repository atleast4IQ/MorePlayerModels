package noppes.mpm.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import noppes.mpm.ModelData;
import noppes.mpm.mixin.EntityMixin;

/** Call-local snapshots: renderer instances and models may be recursively reused. */
public final class RenderStateScope implements AutoCloseable {
    private final LivingEntity entity;
    private final float body, oldBody, head, oldHead, pitch, oldPitch, eye;
    private final ModelData previousData = ClientProxy.data;
    private final PlayerModel<?> previousModel = ClientProxy.playerModel;
    private final List<PartState> parts;
    private final EntityModel<?> model;
    private final float attackTime;
    private final boolean riding, young;
    private final boolean crouching;
    private final float swimAmount;
    private final net.minecraft.client.model.HumanoidModel.ArmPose leftArmPose, rightArmPose;

    public RenderStateScope(LivingEntity entity, EntityModel<?> model) {
        this.entity = entity;
        body = entity.yBodyRot; oldBody = entity.yBodyRotO;
        head = entity.yHeadRot; oldHead = entity.yHeadRotO;
        pitch = entity.getXRot(); oldPitch = entity.xRotO; eye = entity.getEyeHeight();
        this.model = model;
        attackTime = model.attackTime; riding = model.riding; young = model.young;
        parts = model instanceof PlayerModel<?> player ? snapshot(player) : List.of();
        if (model instanceof PlayerModel<?> player) {
            crouching = player.crouching; swimAmount = player.swimAmount;
            leftArmPose = player.leftArmPose; rightArmPose = player.rightArmPose;
        } else {
            crouching = false; swimAmount = 0; leftArmPose = rightArmPose = null;
        }
        if (entity instanceof net.minecraft.world.entity.player.Player player && model instanceof PlayerModel<?> playerModel) {
            ClientProxy.data = ModelData.get(player);
            ClientProxy.playerModel = playerModel;
        } else {
            ClientProxy.data = null;
            ClientProxy.playerModel = null;
        }
    }

    public static List<PartState> snapshot(PlayerModel<?> model) {
        return java.util.stream.Stream.of(model.head, model.body, model.leftArm, model.rightArm,
                model.leftLeg, model.rightLeg, model.hat, model.jacket, model.leftSleeve,
                model.rightSleeve, model.leftPants, model.rightPants)
                .flatMap(ModelPart::getAllParts).distinct().map(PartState::new).toList();
    }

    /** Vanilla's deprecated runAsFancy does not restore graphics mode when rendering throws. */
    public static void runAsFancy(Runnable renderer) {
        var option = net.minecraft.client.Minecraft.getInstance().options.graphicsMode();
        var previous = option.get();
        boolean change = net.minecraft.client.Minecraft.useShaderTransparency();
        try {
            if (change) option.set(net.minecraft.client.GraphicsStatus.FANCY);
            renderer.run();
        } finally {
            if (change) option.set(previous);
        }
    }

    public static void renderIsolated(PoseStack source, java.util.function.Consumer<PoseStack> renderer) {
        renderer.accept(copyPose(source));
    }

    /** No push/pop on the caller's stack, including when a foreign renderer throws. */
    public static PoseStack copyPose(PoseStack source) {
        PoseStack copy = new PoseStack();
        copy.mulPose(source.last().pose());
        copy.last().normal().set(source.last().normal());
        return copy;
    }

    @Override
    public void close() {
        parts.forEach(PartState::restore);
        model.attackTime = attackTime; model.riding = riding; model.young = young;
        if (model instanceof PlayerModel<?> player) {
            player.crouching = crouching; player.swimAmount = swimAmount;
            player.leftArmPose = leftArmPose; player.rightArmPose = rightArmPose;
        }
        entity.yBodyRot = body; entity.yBodyRotO = oldBody;
        entity.yHeadRot = head; entity.yHeadRotO = oldHead;
        entity.setXRot(pitch); entity.xRotO = oldPitch;
        ((EntityMixin)entity).setEyeHeight(eye);
        ClientProxy.data = previousData;
        ClientProxy.playerModel = previousModel;
    }

    public record PartState(ModelPart part, float x, float y, float z, float rx, float ry, float rz,
                            float sx, float sy, float sz, boolean visible, boolean skipDraw) {
        public PartState(ModelPart p) {
            this(p, p.x, p.y, p.z, p.xRot, p.yRot, p.zRot, p.xScale, p.yScale, p.zScale, p.visible, p.skipDraw);
        }
        public void restore() {
            part.x = x; part.y = y; part.z = z;
            part.xRot = rx; part.yRot = ry; part.zRot = rz;
            part.xScale = sx; part.yScale = sy; part.zScale = sz;
            part.visible = visible; part.skipDraw = skipDraw;
        }
    }
}
