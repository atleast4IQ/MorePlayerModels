/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.Resource
 */
package noppes.mpm.client.parts;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import noppes.mpm.LogWriter;
import noppes.mpm.ModelEyeData;
import noppes.mpm.client.parts.AnimationContainer;
import noppes.mpm.client.parts.MpmPart;
import noppes.mpm.client.parts.MpmPartBedrock;
import noppes.mpm.client.parts.MpmPartEyes;
import noppes.mpm.client.parts.MpmPartSimple;
import noppes.mpm.constants.BodyPart;
import noppes.mpm.constants.PartBehaviorType;
import noppes.mpm.constants.PartRenderType;
import noppes.mpm.shared.client.AssetsFinder;
import noppes.mpm.shared.util.NopVector2i;
import noppes.mpm.shared.util.NopVector3f;
import noppes.mpm.shared.util.NopVector3i;
import noppes.mpm.util.MpmException;

public class MpmPartReader {
    public static Map<ResourceLocation, MpmPart> PARTS = new HashMap<ResourceLocation, MpmPart>();
    public static Map<String, List<AnimationContainer>> ANIMATIONS = new HashMap<String, List<AnimationContainer>>();

    public static void reload() {
        HashMap<String, List<AnimationContainer>> mapA = new HashMap<String, List<AnimationContainer>>();
        List<ResourceLocation> list = AssetsFinder.find("animations", ".json");
        for (ResourceLocation loc : list) {
            try (Reader reader = Minecraft.getInstance().getResourceManager().getResource(loc).orElseThrow().openAsReader()) {
                JsonElement document = JsonParser.parseReader(reader);
                if (!MpmAnimationFormat.isMpm(document)) {
                    continue;
                }
                JsonObject root = document.getAsJsonObject();
                mapA.put(loc.getPath().substring(11, loc.getPath().length() - 5), MpmPart.loadAnimations(root));
            }
            catch (Exception throwable) {
                LogWriter.error("Error in " + loc.toString(), throwable);
            }
        }
        ANIMATIONS = mapA;
        HashMap<ResourceLocation, MpmPart> map = new HashMap<ResourceLocation, MpmPart>();
        list = AssetsFinder.find("parts", ".json");
        for (ResourceLocation resourceLocation : list) {
            MpmPart part = MpmPartReader.loadPart(resourceLocation);
            if (part == null) continue;
            map.put(resourceLocation, part);
        }
        PARTS = map;
        PARTS.put(ModelEyeData.RESOURCE, new MpmPartEyes(0, ModelEyeData.RESOURCE));
        PARTS.put(ModelEyeData.RESOURCE_RIGHT, new MpmPartEyes(1, ModelEyeData.RESOURCE_RIGHT));
        PARTS.put(ModelEyeData.RESOURCE_LEFT, new MpmPartEyes(2, ModelEyeData.RESOURCE_LEFT));
        for (Map.Entry entry : PARTS.entrySet()) {
            if (((MpmPart)entry.getValue()).parentId == null || PARTS.containsKey(((MpmPart)entry.getValue()).parentId)) continue;
            LogWriter.error("Error in " + ((ResourceLocation)entry.getKey()).toString() + " - Unable to find parent " + ((MpmPart)entry.getValue()).parentId);
            MpmPartReader.Notify(Component.literal((String)("Error in " + ((ResourceLocation)entry.getKey()).toString() + " - Unable to find parent " + ((MpmPart)entry.getValue()).parentId)));
        }
    }

    private static MpmPart loadPart(ResourceLocation location) {
        try (Reader reader = Minecraft.getInstance().getResourceManager().getResource(location).orElseThrow().openAsReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            PartRenderType renderType = PartRenderType.valueOf(MpmPartReader.getRequiredString(root, "render_type").toUpperCase());
            MpmPart part = new MpmPart();
            if (renderType == PartRenderType.BEDROCK) {
                part = new MpmPartBedrock();
            }
            if (renderType == PartRenderType.SIMPLE) {
                part = new MpmPartSimple();
            }
            part.isEnabled = !root.has("enabled") || root.get("enabled").getAsBoolean();
            part.id = location;
            part.name = MpmPartReader.getRequiredString(root, "name");
            part.texture = root.has("texture") ? ResourceLocation.parse(root.get("texture").getAsString()) : null;
            part.menu = MpmPartReader.getRequiredString(root, "menu");
            part.author = MpmPartReader.getRequiredString(root, "author");
            part.translate = MpmPartReader.jsonVector3f(root.get("translate"));
            part.scale = MpmPartReader.jsonVector3fOrOne(root.get("scale"));
            part.rotatePoint = MpmPartReader.jsonVector3f(root.get("rotate_offset"));
            part.rotate = MpmPartReader.jsonVector3f(root.get("rotate"));
            part.previewRotation = root.get("preview_rotation").getAsInt();
            part.hiddenParts = MpmPartReader.jsonEnumList(BodyPart.class, root.get("hidden_parts"));
            part.disableCustomTextures = root.has("disable_custom_textures") && root.get("disable_custom_textures").getAsBoolean();
            part.defaultUsePlayerSkins = root.has("default_use_player_skins") && root.get("default_use_player_skins").getAsBoolean();
            part.renderType = renderType;
            part.bodyPart = BodyPart.valueOf(MpmPartReader.getRequiredString(root, "body_part").toUpperCase());
            part.load(root.has("render_data") ? root.get("render_data").getAsJsonObject() : null);
            if (root.has("parent")) {
                part.parentId = ResourceLocation.parse(root.get("parent").getAsString());
            }
            PartBehaviorType partBehaviorType = part.animationType = root.has("animation_type") ? PartBehaviorType.valueOf(root.get("animation_type").getAsString().toUpperCase()) : PartBehaviorType.NONE;
            if (root.has("animation_inherit")) {
                String inpart = root.get("animation_inherit").getAsString().toLowerCase();
                if (!ANIMATIONS.containsKey(inpart)) {
                    throw new MpmException("Unknown animation inherit: " + inpart, new Object[0]);
                }
                part.load(ANIMATIONS.get(inpart), part);
            }
            if (root.has("animation_data")) {
                part.load(MpmPart.loadAnimations(root.get("animation_data").getAsJsonObject()), part);
            }
            return part;
        }
        catch (Exception e) {
            LogWriter.error("Error in " + location.toString(), e);
            MpmPartReader.Notify(Component.literal((String)("Error in " + location + " - " + e.getMessage())));
            return null;
        }
    }

    public static String getRequiredString(JsonObject root, String part) {
        if (!root.has(part)) {
            throw new MpmException("Can't fine " + part, new Object[0]);
        }
        return root.get(part).getAsString();
    }

    public static <T extends Enum> List<T> jsonEnumList(Class<T> type, JsonElement el) {
        ArrayList<T> list = new ArrayList<T>();
        if (el == null || !el.isJsonArray()) {
            return list;
        }
        JsonArray arr = el.getAsJsonArray();
        for (int i = 0; i < arr.size(); ++i) {
            list.add(type.cast(Enum.valueOf(type, arr.get(i).getAsString().toUpperCase())));
        }
        return list;
    }

    public static NopVector2i jsonVector2i(JsonElement el) {
        if (el == null || !el.isJsonArray()) {
            return NopVector2i.ZERO;
        }
        JsonArray arr = el.getAsJsonArray();
        int[] r = new int[arr.size()];
        for (int i = 0; i < arr.size(); ++i) {
            r[i] = arr.get(i).getAsInt();
        }
        return new NopVector2i(r);
    }

    public static NopVector3i jsonVector3i(JsonElement el) {
        if (el == null || !el.isJsonArray()) {
            return NopVector3i.ZERO;
        }
        JsonArray arr = el.getAsJsonArray();
        int[] r = new int[arr.size()];
        for (int i = 0; i < arr.size(); ++i) {
            r[i] = arr.get(i).getAsInt();
        }
        return new NopVector3i(r);
    }

    public static NopVector3f jsonVector3f(JsonElement el) {
        if (el == null) {
            return NopVector3f.ZERO;
        }
        JsonArray arr = el.getAsJsonArray();
        float[] r = new float[arr.size()];
        for (int i = 0; i < arr.size(); ++i) {
            r[i] = arr.get(i).getAsFloat();
        }
        return new NopVector3f(r);
    }

    public static NopVector3f jsonVector3fOrOne(JsonElement el) {
        if (el == null) {
            return NopVector3f.ONE;
        }
        JsonArray arr = el.getAsJsonArray();
        float[] r = new float[arr.size()];
        for (int i = 0; i < arr.size(); ++i) {
            r[i] = arr.get(i).getAsFloat();
        }
        return new NopVector3f(r);
    }

    public static void Notify(MutableComponent message) {
        MutableComponent chatcomponenttranslation = message.withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC});
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.shouldInformAdmins()) {
            Minecraft.getInstance().player.sendSystemMessage((Component)chatcomponenttranslation);
        }
    }
}
