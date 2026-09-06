package noppes.mpm.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import java.util.List;
import java.util.Map;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RenderStateScopeTest {
    @Test void isolatedCallsPreserveCallerForNormalCanceledNestedAndExceptionalRenders() {
        PoseStack caller = new PoseStack();
        caller.translate(2, 3, 4);
        Matrix4f before = new Matrix4f(caller.last().pose());
        RenderStateScope.renderIsolated(caller, render -> {}); // canceled before preparation
        RenderStateScope.renderIsolated(caller, render -> {
            render.translate(0, -1, 0);
            RenderStateScope.renderIsolated(render, nested -> nested.translate(9, 0, 0));
            assertEquals(2, render.last().pose().m30());
        });
        assertThrows(IllegalStateException.class, () -> RenderStateScope.renderIsolated(caller, render -> {
            render.translate(0, -1, 0);
            RenderStateScope.renderIsolated(render, nested -> {
                nested.pushPose(); // Foreign renderer fails before its cleanup.
                throw new IllegalStateException("render failed");
            });
        }));
        assertTrue(caller.clear());
        assertEquals(before, caller.last().pose());
    }

    @Test void copyPreservesScaledNormalBehavior() {
        PoseStack caller = new PoseStack();
        caller.scale(2, 3, 4);
        PoseStack copy = RenderStateScope.copyPose(caller);
        Vector3f normal = new Vector3f(1, 1, 1).normalize();
        assertEquals(caller.last().transformNormal(normal, new Vector3f()),
                copy.last().transformNormal(normal, new Vector3f()));
    }

    @Test void nestedPartSnapshotsRestoreAllTransformsAndVisibility() {
        ModelPart part = new ModelPart(List.of(), Map.of());
        var outer = new RenderStateScope.PartState(part);
        part.x = 7; part.yRot = 2; part.xScale = 3; part.visible = false;
        var inner = new RenderStateScope.PartState(part);
        part.x = 20; part.yRot = 4; part.xScale = 8; part.visible = true; part.skipDraw = true;
        inner.restore();
        assertEquals(7, part.x); assertEquals(2, part.yRot); assertEquals(3, part.xScale);
        assertFalse(part.visible); assertFalse(part.skipDraw);
        outer.restore();
        assertEquals(0, part.x); assertEquals(0, part.yRot); assertEquals(1, part.xScale);
        assertTrue(part.visible);
    }
}
