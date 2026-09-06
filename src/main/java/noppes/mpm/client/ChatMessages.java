/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DestFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SourceFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.Font$DisplayMode
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderStateShard$CullStateShard
 *  net.minecraft.client.renderer.RenderStateShard$DepthTestStateShard
 *  net.minecraft.client.renderer.RenderStateShard$LightmapStateShard
 *  net.minecraft.client.renderer.RenderStateShard$ShaderStateShard
 *  net.minecraft.client.renderer.RenderStateShard$TransparencyStateShard
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.RenderType$CompositeState
 *  net.minecraft.network.chat.CommonComponents
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.world.entity.player.Player
 *  org.joml.Matrix4f
 *  org.joml.Vector4f
 */
package noppes.mpm.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Player;
import noppes.mpm.MorePlayerModels;
import noppes.mpm.client.TextBlockClient;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class ChatMessages {
    private static Map<String, ChatMessages> users = new Hashtable<String, ChatMessages>();
    protected static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate((GlStateManager.SourceFactor)GlStateManager.SourceFactor.SRC_ALPHA, (GlStateManager.DestFactor)GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, (GlStateManager.SourceFactor)GlStateManager.SourceFactor.ONE, (GlStateManager.DestFactor)GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    private static final RenderStateShard.ShaderStateShard sharder = new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorLightmapShader);
    protected static final RenderType type = RenderType.create((String)"chatbubble", (VertexFormat)DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)false, (boolean)false, (RenderType.CompositeState)RenderType.CompositeState.builder().setCullState(new RenderStateShard.CullStateShard(true)).setLightmapState(new RenderStateShard.LightmapStateShard(true)).setShaderState(sharder).createCompositeState(true));
    protected static final RenderType typeDepth = RenderType.create((String)"chatbubbledepth", (VertexFormat)DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)false, (boolean)true, (RenderType.CompositeState)RenderType.CompositeState.builder().setCullState(new RenderStateShard.CullStateShard(true)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setLightmapState(new RenderStateShard.LightmapStateShard(true)).setDepthTestState(new RenderStateShard.DepthTestStateShard("always", 519)).setShaderState(sharder).createCompositeState(false));
    private Map<Long, TextBlockClient> messages = new TreeMap<Long, TextBlockClient>();
    private int boxLength = 46;
    private float scale = 0.5f;
    private Component lastMessage = CommonComponents.EMPTY;
    private long lastMessageTime = 0L;
    private static Pattern[] patterns = new Pattern[]{Pattern.compile("^<+([a-zA-z0-9_]{2,16})>[:]? (.*)"), Pattern.compile("^\\[.*[\\]]{1,16}[^a-zA-z0-9]?([a-zA-z0-9_]{2,16})[:]? (.*)"), Pattern.compile("^[a-zA-z0-9_]{2,10}[^a-zA-z0-9]([a-zA-z0-9_]{2,16})[:]? (.*)")};

    public void addMessage(Component message) {
        if (!MorePlayerModels.EnableChatBubbles) {
            return;
        }
        long time = System.currentTimeMillis();
        if (message.getString().equals(this.lastMessage.getString()) && this.lastMessageTime + 1000L > time) {
            return;
        }
        TreeMap<Long, TextBlockClient> messages = new TreeMap<Long, TextBlockClient>(this.messages);
        messages.put(time, new TextBlockClient(message, this.boxLength * 4));
        if (messages.size() > 3) {
            messages.remove(messages.keySet().iterator().next());
        }
        this.messages = messages;
        this.lastMessage = message;
        this.lastMessageTime = time;
    }

    public void renderMessages(PoseStack poseStack, MultiBufferSource typeBuffer, float textscale, boolean inRange, int lightmapUV) {
        Map<Long, TextBlockClient> messages = this.getMessages();
        if (messages.isEmpty()) {
            return;
        }
        // The buffered RenderTypes select their shader when the batch is drawn.
        if (inRange) {
            this.render(poseStack, typeBuffer, typeBuffer.getBuffer(typeDepth), textscale, false, lightmapUV);
        }
        this.render(poseStack, typeBuffer, typeBuffer.getBuffer(type), textscale, true, lightmapUV);
    }

    public void render(PoseStack poseStack, MultiBufferSource typeBuffer, VertexConsumer ivertex, float textScale, boolean depth, int lightmapUV) {
        float var14 = 0.02666667f;
        int size = 0;
        for (TextBlockClient block : this.messages.values()) {
            size += block.lines.size();
        }
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        Objects.requireNonNull(font);
        int textYSize = (int)((float)(size * 9) * this.scale);
        poseStack.pushPose();
        try {
            poseStack.translate(0.0f, (float)textYSize * var14, 0.0f);
            poseStack.scale(textScale, textScale, textScale);
            poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
            poseStack.scale(-var14, -var14, var14);
            int black = depth ? -16777216 : -16777216;
            int white = depth ? -1140850689 : 0x44FFFFFF;
            PoseStack.Pose entry = poseStack.last();
            Matrix4f matrix = entry.pose();
            this.drawRect(ivertex, matrix, lightmapUV, -this.boxLength - 2, -2.0f, this.boxLength + 2, textYSize + 1, white, 0.11f);
            this.drawRect(ivertex, matrix, lightmapUV, -this.boxLength - 1, -3.0f, this.boxLength + 1, -2.0f, black, 0.1f);
            this.drawRect(ivertex, matrix, lightmapUV, -this.boxLength - 1, textYSize + 2, -1.0f, textYSize + 1, black, 0.1f);
            this.drawRect(ivertex, matrix, lightmapUV, 3.0f, textYSize + 2, this.boxLength + 1, textYSize + 1, black, 0.1f);
            this.drawRect(ivertex, matrix, lightmapUV, -this.boxLength - 3, -1.0f, -this.boxLength - 2, textYSize, black, 0.1f);
            this.drawRect(ivertex, matrix, lightmapUV, this.boxLength + 3, -1.0f, this.boxLength + 2, textYSize, black, 0.1f);
            this.drawRect(ivertex, matrix, lightmapUV, -this.boxLength - 2, -2.0f, -this.boxLength - 1, -1.0f, black, 0.1f);
            this.drawRect(ivertex, matrix, lightmapUV, this.boxLength + 2, -2.0f, this.boxLength + 1, -1.0f, black, 0.1f);
            this.drawRect(ivertex, matrix, lightmapUV, -this.boxLength - 2, textYSize + 1, -this.boxLength - 1, textYSize, black, 0.1f);
            this.drawRect(ivertex, matrix, lightmapUV, this.boxLength + 2, textYSize + 1, this.boxLength + 1, textYSize, black, 0.1f);
            this.drawRect(ivertex, matrix, lightmapUV, 0.0f, textYSize + 1, 3.0f, textYSize + 4, white, 0.11f);
            this.drawRect(ivertex, matrix, lightmapUV, -1.0f, textYSize + 4, 1.0f, textYSize + 5, white, 0.11f);
            this.drawRect(ivertex, matrix, lightmapUV, -1.0f, textYSize + 1, 0.0f, textYSize + 4, black, 0.1f);
            this.drawRect(ivertex, matrix, lightmapUV, 3.0f, textYSize + 1, 4.0f, textYSize + 3, black, 0.1f);
            this.drawRect(ivertex, matrix, lightmapUV, 2.0f, textYSize + 3, 3.0f, textYSize + 4, black, 0.1f);
            this.drawRect(ivertex, matrix, lightmapUV, 1.0f, textYSize + 4, 2.0f, textYSize + 5, black, 0.1f);
            this.drawRect(ivertex, matrix, lightmapUV, -2.0f, textYSize + 4, -1.0f, textYSize + 5, black, 0.1f);
            this.drawRect(ivertex, matrix, lightmapUV, -2.0f, textYSize + 5, 1.0f, textYSize + 6, black, 0.1f);
            poseStack.scale(this.scale, this.scale, this.scale);
            int index = 0;
            for (TextBlockClient block : this.messages.values()) {
                for (Component chat : block.lines) {
                    float f = -font.width((FormattedText)chat) / 2;
                    Objects.requireNonNull(font);
                    font.drawInBatch(chat, f, (float)(index * 9), black, false, matrix, typeBuffer, depth ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, 0, lightmapUV);
                    ++index;
                }
            }
        } finally {
            poseStack.popPose();
        }
    }

    public void drawRect(VertexConsumer ivertex, Matrix4f matrix, int lightmapUV, float x, float y, float x2, float y2, int color, float z) {
        float j1;
        if (x < x2) {
            j1 = x;
            x = x2;
            x2 = j1;
        }
        if (y < y2) {
            j1 = y;
            y = y2;
            y2 = j1;
        }
        float f1 = (float)(color >> 16 & 0xFF) / 255.0f;
        float f2 = (float)(color >> 8 & 0xFF) / 255.0f;
        float f3 = (float)(color & 0xFF) / 255.0f;
        this.draw(ivertex, matrix, lightmapUV, x, y, z, f1, f2, f3);
        this.draw(ivertex, matrix, lightmapUV, x, y2, z, f1, f2, f3);
        this.draw(ivertex, matrix, lightmapUV, x2, y2, z, f1, f2, f3);
        this.draw(ivertex, matrix, lightmapUV, x2, y, z, f1, f2, f3);
    }

    private void draw(VertexConsumer ivertex, Matrix4f matrix, int lightmapUV, float x, float y, float z, float red, float green, float blue) {
        Vector4f v = matrix.transform(new Vector4f(x, y, z, 1.0f));
        ivertex.addVertex(v.x(), v.y(), v.z()).setColor(red, green, blue, 1.0f).setLight(lightmapUV);
    }

    public static ChatMessages getChatMessages(String username) {
        if (users.containsKey(username)) {
            return users.get(username);
        }
        ChatMessages chat = new ChatMessages();
        users.put(username, chat);
        return chat;
    }

    public static void parseMessage(String toParse) {
        toParse = toParse.replaceAll("\u00a7.", "");
        for (Pattern pattern : patterns) {
            String username;
            Matcher m = pattern.matcher(toParse);
            if (!m.find() || !ChatMessages.validPlayer(username = m.group(1))) continue;
            String message = m.group(2);
            ChatMessages.getChatMessages(username).addMessage((Component)Component.translatable((String)message));
            return;
        }
    }

    public static void test() {
        ChatMessages.test("<Sirnoppes01> :)", "Sirnoppes01: :)");
        ChatMessages.test("<Sirnoppes01> hey", "Sirnoppes01: hey");
        ChatMessages.test("<Sir_noppes> hey", "Sir_noppes: hey");
        ChatMessages.test("<Sirnoppes>: hey", "Sirnoppes: hey");
        ChatMessages.test("[member]Sirnoppes: hey", "Sirnoppes: hey");
        ChatMessages.test("[member]Sirnoppes01: hey", "Sirnoppes01: hey");
        ChatMessages.test("[member]Sir_noppes: hey", "Sir_noppes: hey");
        ChatMessages.test("[member] Sirnoppes: hey", "Sirnoppes: hey");
        ChatMessages.test("[g][member]Sirnoppes: hey", "Sirnoppes: hey");
        ChatMessages.test("[g] [member]Sirnoppes: hey", "Sirnoppes: hey");
        ChatMessages.test("[g] [member]-Sirnoppes: hey", "Sirnoppes: hey");
        ChatMessages.test("[Player755: Teleported Player755 to Player885]", "");
        ChatMessages.test("member Sirnoppes: hey", "Sirnoppes: hey");
        ChatMessages.test("member-Sirnoppes: hey", "Sirnoppes: hey");
        ChatMessages.test("member: Sirnoppes: hey", "");
    }

    private static void test(String toParse, String result) {
        for (Pattern pattern : patterns) {
            Matcher m = pattern.matcher(toParse);
            if (!m.find()) continue;
            String username = m.group(1);
            String message = m.group(2);
            if (message == null || username == null) continue;
            if (result.isEmpty()) {
                System.err.println("failed: " + toParse + " - " + username + ": " + message);
                return;
            }
            if (!(username + ": " + message).equals(result)) continue;
            System.out.println("success: " + toParse);
            return;
        }
        if (result.isEmpty()) {
            System.out.println("success: " + toParse);
        } else {
            System.err.println("failed: " + toParse);
        }
    }

    private static boolean validPlayer(String username) {
        for (Player player : Minecraft.getInstance().level.players()) {
            if (!username.equals(player.getName()) && !username.equals(player.getDisplayName().getString())) continue;
            return true;
        }
        return false;
    }

    private Map<Long, TextBlockClient> getMessages() {
        TreeMap<Long, TextBlockClient> messages = new TreeMap<Long, TextBlockClient>();
        long time = System.currentTimeMillis();
        for (Map.Entry<Long, TextBlockClient> entry : this.messages.entrySet()) {
            if (time > entry.getKey() + 10000L) continue;
            messages.put(entry.getKey(), entry.getValue());
        }
        this.messages = messages;
        return this.messages;
    }

    public boolean hasMessage() {
        return !this.messages.isEmpty();
    }
}
