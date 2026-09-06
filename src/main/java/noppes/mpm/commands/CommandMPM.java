/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.suggestion.SuggestionProvider
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  net.minecraft.commands.CommandBuildContext
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.commands.SharedSuggestionProvider
 *  net.minecraft.commands.arguments.CompoundTagArgument
 *  net.minecraft.commands.arguments.EntityArgument
 *  net.minecraft.commands.arguments.ResourceArgument
 *  net.minecraft.commands.arguments.ResourceLocationArgument
 *  net.minecraft.commands.synchronization.SuggestionProviders
 *  net.minecraft.core.Holder$Reference
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.MobCategory
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.neoforge.registries.ForgeRegistries
 *  net.neoforged.neoforge.server.command.EnumArgument
 */
package noppes.mpm.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.server.command.EnumArgument;
import noppes.mpm.ModelData;
import noppes.mpm.constants.EnumAnimation;
import noppes.mpm.packets.Packets;
import noppes.mpm.packets.client.PacketAnimationStart;
import noppes.mpm.packets.client.PacketPlayerDataSend;
import noppes.mpm.util.NoppesStringUtils;

public class CommandMPM {
    /** Vanilla operator level for commands that change other players' MPM data. */
    private static final int ADMIN_PERMISSION_LEVEL = 2;
    private static List<String> entities;
    private static ArgumentType<EnumAnimation> animationArgumentType;
    public static final SuggestionProvider<CommandSourceStack> ENTITIES;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildAspect) {
        entities = new ArrayList<String>();
        for (EntityType ent : BuiltInRegistries.ENTITY_TYPE) {
            if (ent.getCategory() == MobCategory.MISC) continue;
            entities.add(BuiltInRegistries.ENTITY_TYPE.getKey(ent).toString());
        }
        entities.add("clear");
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("mpm");
        CommandMPM.getSubCommands(command.requires(source -> source.hasPermission(ADMIN_PERMISSION_LEVEL)), buildAspect);
        dispatcher.register(command);
    }

    private static void getSubCommands(LiteralArgumentBuilder<CommandSourceStack> command, CommandBuildContext buildAspect) {
        command.then(Commands.literal((String)"url").then(Commands.argument((String)"targets", (ArgumentType)EntityArgument.players()).then(Commands.argument((String)"url", (ArgumentType)StringArgumentType.greedyString()).executes(context -> {
            Collection<ServerPlayer> players = CommandMPM.getPlayers((CommandContext<CommandSourceStack>)context);
            String url = StringArgumentType.getString((CommandContext)context, (String)"url");
            if (url.equalsIgnoreCase("clear")) {
                url = "";
            }
            for (ServerPlayer player : players) {
                ModelData data = ModelData.get((Player)player);
                if (data.url.equals(url)) continue;
                data.url = url;
                Packets.sendNearby((Entity)player, new PacketPlayerDataSend(player.getUUID(), data.writeToNBT()));
            }
            return players.size();
        }))));
        command.then(Commands.literal((String)"entity").then(Commands.argument((String)"targets", (ArgumentType)EntityArgument.players()).then(((RequiredArgumentBuilder)Commands.argument((String)"entity", (ArgumentType)ResourceLocationArgument.id()).suggests(ENTITIES).executes(context -> {
            Collection<ServerPlayer> players = CommandMPM.getPlayers((CommandContext<CommandSourceStack>)context);
            return CommandMPM.setEntity((CommandContext<CommandSourceStack>)context, buildAspect, players, new CompoundTag());
        })).then(Commands.argument((String)"nbt", (ArgumentType)CompoundTagArgument.compoundTag()).executes(context -> {
            Collection<ServerPlayer> players = CommandMPM.getPlayers((CommandContext<CommandSourceStack>)context);
            return CommandMPM.setEntity((CommandContext<CommandSourceStack>)context, buildAspect, players, CompoundTagArgument.getCompoundTag((CommandContext)context, (String)"nbt"));
        })))));
        command.then(Commands.literal((String)"name").then(Commands.argument((String)"targets", (ArgumentType)EntityArgument.players()).then(Commands.argument((String)"name", (ArgumentType)StringArgumentType.greedyString()).executes(context -> {
            Collection<ServerPlayer> players = CommandMPM.getPlayers((CommandContext<CommandSourceStack>)context);
            String name = StringArgumentType.getString((CommandContext)context, (String)"name");
            if (name.equalsIgnoreCase("clear")) {
                name = "";
            }
            for (ServerPlayer player : players) {
                ModelData data = ModelData.get((Player)player);
                if (data.displayName.equals(name)) continue;
                data.displayName = name;
                Packets.sendNearby((Entity)player, new PacketPlayerDataSend(player.getUUID(), data.writeToNBT()));
                player.refreshDisplayName();
            }
            return players.size();
        }))));
        command.then(Commands.literal((String)"sendmodel").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument((String)"targets", (ArgumentType)EntityArgument.players()).executes(context -> {
            ModelData fromData = ModelData.get((Player)((CommandSourceStack)context.getSource()).getPlayerOrException());
            Collection<ServerPlayer> players = CommandMPM.getPlayers((CommandContext<CommandSourceStack>)context);
            return CommandMPM.sendModel(players, fromData);
        })).then(Commands.literal((String)"clear").executes(context -> {
            Collection<ServerPlayer> players = CommandMPM.getPlayers((CommandContext<CommandSourceStack>)context);
            return CommandMPM.sendModel(players, new ModelData());
        }))).then(Commands.argument((String)"from", (ArgumentType)EntityArgument.player()).executes(context -> {
            ModelData fromData = ModelData.get((Player)EntityArgument.getPlayer((CommandContext)context, (String)"from"));
            Collection<ServerPlayer> players = CommandMPM.getPlayers((CommandContext<CommandSourceStack>)context);
            return CommandMPM.sendModel(players, fromData);
        }))));
        command.then(Commands.literal((String)"animation").then(Commands.argument((String)"targets", (ArgumentType)EntityArgument.players()).then(Commands.argument((String)"animation", animationArgumentType).executes(context -> {
            Collection<ServerPlayer> players = CommandMPM.getPlayers((CommandContext<CommandSourceStack>)context);
            EnumAnimation animation = (EnumAnimation)((Object)((Object)context.getArgument("animation", EnumAnimation.class)));
            for (ServerPlayer player : players) {
                ModelData data = ModelData.get((Player)player);
                if (data.animation == animation) {
                    data.setAnimation(EnumAnimation.NONE);
                } else {
                    data.setAnimation(animation);
                }
                Packets.sendNearby((Entity)player, new PacketAnimationStart(player.getUUID(), data.animation));
            }
            return players.size();
        }))));
        command.then(Commands.literal((String)"scale").then(((RequiredArgumentBuilder)Commands.argument((String)"targets", (ArgumentType)EntityArgument.players()).then(Commands.argument((String)"all", (ArgumentType)StringArgumentType.word()).executes(context -> {
            Collection<ServerPlayer> players = CommandMPM.getPlayers((CommandContext<CommandSourceStack>)context);
            Scale scale = Scale.Parse(StringArgumentType.getString((CommandContext)context, (String)"all"));
            for (ServerPlayer player : players) {
                ModelData data = ModelData.get((Player)player);
                data.head.setScale(scale.scaleX, scale.scaleY, scale.scaleZ);
                data.body.setScale(scale.scaleX, scale.scaleY, scale.scaleZ);
                data.arm1.setScale(scale.scaleX, scale.scaleY, scale.scaleZ);
                data.arm2.setScale(scale.scaleX, scale.scaleY, scale.scaleZ);
                data.leg1.setScale(scale.scaleX, scale.scaleY, scale.scaleZ);
                data.leg2.setScale(scale.scaleX, scale.scaleY, scale.scaleZ);
                Packets.sendNearby((Entity)player, new PacketPlayerDataSend(player.getUUID(), data.writeToNBT()));
            }
            return players.size();
        }))).then(Commands.argument((String)"head", (ArgumentType)StringArgumentType.word()).then(Commands.argument((String)"body", (ArgumentType)StringArgumentType.word()).then(Commands.argument((String)"arms", (ArgumentType)StringArgumentType.word()).then(Commands.argument((String)"legs", (ArgumentType)StringArgumentType.word()).executes(context -> {
            Collection<ServerPlayer> players = CommandMPM.getPlayers((CommandContext<CommandSourceStack>)context);
            Scale head = Scale.Parse(StringArgumentType.getString((CommandContext)context, (String)"head"));
            Scale body = Scale.Parse(StringArgumentType.getString((CommandContext)context, (String)"body"));
            Scale arms = Scale.Parse(StringArgumentType.getString((CommandContext)context, (String)"arms"));
            Scale legs = Scale.Parse(StringArgumentType.getString((CommandContext)context, (String)"legs"));
            for (ServerPlayer player : players) {
                ModelData data = ModelData.get((Player)player);
                data.head.setScale(head.scaleX, head.scaleY, head.scaleZ);
                data.body.setScale(body.scaleX, body.scaleY, body.scaleZ);
                data.arm1.setScale(arms.scaleX, arms.scaleY, arms.scaleZ);
                data.arm2.setScale(arms.scaleX, arms.scaleY, arms.scaleZ);
                data.leg1.setScale(legs.scaleX, legs.scaleY, legs.scaleZ);
                data.leg2.setScale(legs.scaleX, legs.scaleY, legs.scaleZ);
                Packets.sendNearby((Entity)player, new PacketPlayerDataSend(player.getUUID(), data.writeToNBT()));
            }
            return players.size();
        })))))));
    }

    private static Collection<ServerPlayer> getPlayers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return EntityArgument.getPlayers(context, (String)"targets");
    }

    private static int sendModel(Collection<ServerPlayer> players, ModelData fromData) {
        CompoundTag compound = fromData.writeToNBT();
        for (ServerPlayer player : players) {
            ModelData data = ModelData.get((Player)player);
            data.readFromNBT(compound);
            data.save();
            Packets.sendNearby((Entity)player, new PacketPlayerDataSend(player.getUUID(), data.writeToNBT()));
        }
        return players.size();
    }

    private static int setEntity(CommandContext<CommandSourceStack> context, CommandBuildContext buildAspect, Collection<ServerPlayer> players, CompoundTag extra) throws CommandSyntaxException {
        ResourceLocation resource = (ResourceLocation)context.getArgument("entity", ResourceLocation.class);
        if (!resource.toString().equalsIgnoreCase("minecraft:clear")) {
            Holder.Reference ref = ResourceArgument.resource((CommandBuildContext)buildAspect, (ResourceKey)Registries.ENTITY_TYPE).parse(new StringReader(resource.toString()));
            resource = BuiltInRegistries.ENTITY_TYPE.getKey((EntityType)ref.value());
        } else {
            resource = null;
        }
        for (ServerPlayer player : players) {
            ModelData data = ModelData.get((Player)player);
            if (NoppesStringUtils.areEqual(data.getEntityName(), resource) && data.extra.equals((Object)extra)) continue;
            data.setEntity(resource);
            data.extra = extra;
            Packets.sendNearby((Entity)player, new PacketPlayerDataSend(player.getUUID(), data.writeToNBT()));
        }
        return players.size();
    }

    static {
        animationArgumentType = EnumArgument.enumArgument(EnumAnimation.class);
        ENTITIES = SuggestionProviders.register((ResourceLocation)ResourceLocation.parse("entities"), (context, builder) -> SharedSuggestionProvider.suggest(entities.stream(), (SuggestionsBuilder)builder));
    }

    static class Scale {
        float scaleX;
        float scaleY;
        float scaleZ;

        Scale() {
        }

        private static Scale Parse(String s) throws NumberFormatException {
            Scale scale = new Scale();
            if (s.contains(",")) {
                String[] split = s.split(",");
                if (split.length != 3) {
                    throw new NumberFormatException("Not enough args given");
                }
                scale.scaleX = Float.parseFloat(split[0]);
                scale.scaleY = Float.parseFloat(split[1]);
                scale.scaleZ = Float.parseFloat(split[2]);
            } else {
                scale.scaleY = scale.scaleX = Float.parseFloat(s);
                scale.scaleZ = scale.scaleX;
            }
            return scale;
        }
    }
}
