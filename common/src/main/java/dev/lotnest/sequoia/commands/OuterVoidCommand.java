/*
 * Copyright © sequoia-mod 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package dev.lotnest.sequoia.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.wynntils.utils.mc.McUtils;
import dev.lotnest.sequoia.SequoiaMod;
import dev.lotnest.sequoia.core.components.Managers;
import dev.lotnest.sequoia.core.consumers.command.Command;
import dev.lotnest.sequoia.features.OuterVoidTrackerFeature;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class OuterVoidCommand extends Command {
    private static final String ITEM_NAME_ARGUMENT = "itemName";

    private static final String FEATURE_DISABLED_I18N_KEY = "sequoia.command.outerVoid.featureDisabled";

    @Override
    public String getCommandName() {
        return "outerVoid";
    }

    @Override
    public List<String> getAliases() {
        return List.of("ov");
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommandBuilder(
            LiteralArgumentBuilder<CommandSourceStack> base) {
        return base.then(Commands.literal("addNeededItem")
                        .then(Commands.argument("itemAmount", IntegerArgumentType.integer(1))
                                .then(Commands.argument(ITEM_NAME_ARGUMENT, StringArgumentType.greedyString())
                                        .suggests((context, builder) -> {
                                            OuterVoidTrackerFeature outerVoidTrackerFeature =
                                                    Managers.Feature.getFeatureInstance(OuterVoidTrackerFeature.class);
                                            if (!outerVoidTrackerFeature.isEnabled()) {
                                                return Suggestions.empty();
                                            }
                                            return SharedSuggestionProvider.suggest(
                                                    outerVoidTrackerFeature.getItemNames(), builder);
                                        })
                                        .executes(this::addNeededItem))))
                .then(Commands.literal("removeNeededItem")
                        .then(Commands.argument(ITEM_NAME_ARGUMENT, StringArgumentType.greedyString())
                                .suggests((context, builder) -> {
                                    OuterVoidTrackerFeature outerVoidTrackerFeature =
                                            Managers.Feature.getFeatureInstance(OuterVoidTrackerFeature.class);
                                    if (!outerVoidTrackerFeature.isEnabled()) {
                                        return Suggestions.empty();
                                    }
                                    return SharedSuggestionProvider.suggest(
                                            outerVoidTrackerFeature.getNeededItems(), builder);
                                })
                                .executes(this::removeNeededItem)))
                .then(Commands.literal("clearNeededItems").executes(this::clearNeededItems))
                .executes(this::syntaxError);
    }

    private int addNeededItem(CommandContext<CommandSourceStack> context) {
        OuterVoidTrackerFeature outerVoidTrackerFeature =
                Managers.Feature.getFeatureInstance(OuterVoidTrackerFeature.class);
        if (!outerVoidTrackerFeature.isEnabled()) {
            McUtils.sendMessageToClient(SequoiaMod.prefix(Component.translatable(FEATURE_DISABLED_I18N_KEY)));
            return 1;
        }

        String itemName = context.getArgument(ITEM_NAME_ARGUMENT, String.class);
        int itemAmount = context.getArgument("itemAmount", Integer.class);

        if (!outerVoidTrackerFeature.itemNameExists(itemName)) {
            McUtils.sendMessageToClient(
                    SequoiaMod.prefix(Component.translatable("sequoia.command.outerVoid.itemNameNotExists", itemName)));
            return 1;
        }

        if (outerVoidTrackerFeature.hasNeededItem(itemName)) {
            outerVoidTrackerFeature.addNeededItem(itemName, itemAmount);
            McUtils.sendMessageToClient(SequoiaMod.prefix(
                    Component.translatable("sequoia.command.outerVoid.addedMoreNeededItem", itemAmount, itemName)));
            return 1;
        }

        outerVoidTrackerFeature.addNeededItem(itemName, itemAmount);
        McUtils.sendMessageToClient(SequoiaMod.prefix(
                Component.translatable("sequoia.command.outerVoid.addedNeededItem", itemAmount, itemName)));

        return 1;
    }

    private int removeNeededItem(CommandContext<CommandSourceStack> context) {
        OuterVoidTrackerFeature outerVoidTrackerFeature =
                Managers.Feature.getFeatureInstance(OuterVoidTrackerFeature.class);
        if (!outerVoidTrackerFeature.isEnabled()) {
            McUtils.sendMessageToClient(SequoiaMod.prefix(Component.translatable(FEATURE_DISABLED_I18N_KEY)));
            return 1;
        }

        String itemName = context.getArgument(ITEM_NAME_ARGUMENT, String.class);
        if (!outerVoidTrackerFeature.hasNeededItem(itemName)) {
            McUtils.sendMessageToClient(
                    SequoiaMod.prefix(Component.translatable("sequoia.command.outerVoid.notInNeededItems", itemName)));
            return 1;
        }

        outerVoidTrackerFeature.removeNeededItem(itemName);
        McUtils.sendMessageToClient(
                SequoiaMod.prefix(Component.translatable("sequoia.command.outerVoid.removedNeededItem", itemName)));

        return 1;
    }

    private int clearNeededItems(CommandContext<CommandSourceStack> context) {
        OuterVoidTrackerFeature outerVoidTrackerFeature =
                Managers.Feature.getFeatureInstance(OuterVoidTrackerFeature.class);
        if (!outerVoidTrackerFeature.isEnabled()) {
            McUtils.sendMessageToClient(SequoiaMod.prefix(Component.translatable(FEATURE_DISABLED_I18N_KEY)));
            return 1;
        }

        if (outerVoidTrackerFeature.getNeededItems().isEmpty()) {
            McUtils.sendMessageToClient(
                    SequoiaMod.prefix(Component.translatable("sequoia.command.outerVoid.noNeededItems")));
            return 1;
        }

        outerVoidTrackerFeature.clearNeededItems();
        McUtils.sendMessageToClient(
                SequoiaMod.prefix(Component.translatable("sequoia.command.outerVoid.clearedNeededItems")));

        return 1;
    }
}
