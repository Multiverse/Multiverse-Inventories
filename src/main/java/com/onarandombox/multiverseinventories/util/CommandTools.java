package com.onarandombox.multiverseinventories.util;

import com.onarandombox.MultiverseCore.commandTools.MVCommandManager;
import com.onarandombox.acf.BukkitCommandCompletionContext;
import com.onarandombox.acf.BukkitCommandExecutionContext;
import com.onarandombox.acf.BukkitCommandIssuer;
import com.onarandombox.acf.ConditionContext;
import com.onarandombox.acf.ConditionFailedException;
import com.onarandombox.acf.InvalidCommandArgument;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.commands_acf.AddSharesCommand;
import com.onarandombox.multiverseinventories.commands_acf.AddWorldCommand;
import com.onarandombox.multiverseinventories.commands_acf.CreateGroupCommand;
import com.onarandombox.multiverseinventories.commands_acf.DeleteGroupCommand;
import com.onarandombox.multiverseinventories.commands_acf.GroupCommand;
import com.onarandombox.multiverseinventories.commands_acf.ImportCommand;
import com.onarandombox.multiverseinventories.commands_acf.MigrateCommand;
import com.onarandombox.multiverseinventories.commands_acf.RemoveSharesCommand;
import com.onarandombox.multiverseinventories.commands_acf.RemoveWorldCommand;
import com.onarandombox.multiverseinventories.commands_acf.RootCommand;
import com.onarandombox.multiverseinventories.commands_acf.ToggleCommand;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.locale.Messager;
import com.onarandombox.multiverseinventories.migration.DataImporter;
import com.onarandombox.multiverseinventories.share.Sharable;
import com.onarandombox.multiverseinventories.share.Sharables;
import com.onarandombox.multiverseinventories.share.Shares;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class CommandTools {

    private final MultiverseInventories plugin;
    private final MVCommandManager manager;
    private final Messager messager;

    public CommandTools(MultiverseInventories plugin) {
        this.plugin = plugin;
        this.manager = plugin.getCore().getMVCommandManager();
        this.messager = plugin.getMessager();

        // Completions
        this.manager.getCommandCompletions().registerAsyncCompletion("sharables", this::suggestSharables);
        this.manager.getCommandCompletions().registerAsyncCompletion("optionalSharables", this::suggestOptionalSharables);
        this.manager.getCommandCompletions().registerAsyncCompletion("shares", this::suggestShares);
        this.manager.getCommandCompletions().registerAsyncCompletion("worldGroups", this::suggestWorldGroups);
        this.manager.getCommandCompletions().registerStaticCompletion("invPluginImports", this::suggestInvPluginImports);

        // Contexts
        this.manager.getCommandContexts().registerContext(Sharable.class, this::deriveSharable);
        this.manager.getCommandContexts().registerContext(Shares.class, this::deriveShares);
        this.manager.getCommandContexts().registerContext(WorldGroup.class, this::deriveWorldGroup);
        this.manager.getCommandContexts().registerContext(DataImporter.class, this::deriveDataImporter);

        // Conditions
        this.manager.getCommandConditions().addCondition(Sharable.class, "optional", this::checkIsSharableOptional);
        this.manager.getCommandConditions().addCondition(String.class, "creatableGroupName", this::checkCreatableGroupName);

        //Commands
        this.manager.registerSubModule("mvinv", new RootCommand(this.plugin));
        this.manager.registerCommand(new GroupCommand(this.plugin));
        this.manager.registerCommand(new ToggleCommand(this.plugin));
        this.manager.registerCommand(new CreateGroupCommand(this.plugin));
        this.manager.registerCommand(new DeleteGroupCommand(this.plugin));
        this.manager.registerCommand(new AddWorldCommand(this.plugin));
        this.manager.registerCommand(new RemoveWorldCommand(this.plugin));
        this.manager.registerCommand(new AddSharesCommand(this.plugin));
        this.manager.registerCommand(new RemoveSharesCommand(this.plugin));
        this.manager.registerCommand(new ImportCommand(this.plugin));
        this.manager.registerCommand(new MigrateCommand(this.plugin));
    }

    @NotNull
    private Collection<String> suggestSharables(@NotNull BukkitCommandCompletionContext context) {
        return Sharables.all().stream()
                .unordered()
                .map(share -> share.getNames()[0])
                .collect(Collectors.toList());
    }

    @NotNull
    private Collection<String> suggestOptionalSharables(@NotNull BukkitCommandCompletionContext context) {
        return Sharables.all().stream()
                .unordered()
                .filter(Sharable::isOptional)
                .map(share -> share.getNames()[0])
                .collect(Collectors.toList());
    }

    @NotNull
    private Collection<String> suggestShares(@NotNull BukkitCommandCompletionContext context) {
        return Sharables.getMapKeys();
    }

    @NotNull
    private Collection<String> suggestWorldGroups(@NotNull BukkitCommandCompletionContext context) {
        return this.plugin.getGroupManager().getGroups().stream()
                .unordered()
                .map(WorldGroup::getName)
                .collect(Collectors.toList());
    }

    @NotNull
    private Collection<String> suggestInvPluginImports() {
        return Arrays.asList("MultiInv", "WorldInventories");
    }

    @NotNull
    private Sharable<?> deriveSharable(@NotNull BukkitCommandExecutionContext context) {
        String shareName = context.popFirstArg();
        Sharable<?> targetSharable = Sharables.all().stream()
                .unordered()
                .filter(sharable -> sharable.getNames()[0].equals(shareName))
                .findFirst()
                .orElse(null);

        if (targetSharable == null) {
            this.messager.normal(Message.ERROR_NO_SHARES_SPECIFIED, context.getSender());
            throw new InvalidCommandArgument();
        }

        return targetSharable;
    }

    @NotNull
    private Shares deriveShares(BukkitCommandExecutionContext context) {
        Shares shares = Sharables.lookup(context.popFirstArg());
        if (shares == null) {
            this.messager.normal(Message.ERROR_NO_SHARES_SPECIFIED, context.getSender());
            throw new InvalidCommandArgument();
        }
        return shares;
    }

    @NotNull
    private WorldGroup deriveWorldGroup(@NotNull BukkitCommandExecutionContext context) {
        String groupName = context.popFirstArg();
        WorldGroup group = this.plugin.getGroupManager().getGroup(groupName);
        if (group == null) {
            this.messager.normal(Message.ERROR_NO_GROUP, context.getSender(), groupName);
            throw new InvalidCommandArgument();
        }
        return group;
    }

    @NotNull
    private DataImporter deriveDataImporter(@NotNull BukkitCommandExecutionContext context) {
        String pluginName = context.popFirstArg();
        DataImporter importer;
        if (pluginName.equalsIgnoreCase("MultiInv")) {
            importer = this.plugin.getImportManager().getMultiInvImporter();
        }
        else if (pluginName.equalsIgnoreCase("WorldInventories")) {
            importer = this.plugin.getImportManager().getWorldInventoriesImporter();
        }
        else {
            this.messager.bad(Message.ERROR_UNSUPPORTED_IMPORT, context.getSender(), pluginName);
            throw new InvalidCommandArgument();
        }

        if (importer == null) {
            this.messager.bad(Message.ERROR_PLUGIN_NOT_ENABLED, context.getSender(), pluginName);
            throw new InvalidCommandArgument(false);
        }

        return importer;
    }

    private void checkIsSharableOptional(@NotNull ConditionContext<BukkitCommandIssuer> context,
                                         @NotNull BukkitCommandExecutionContext executionContext,
                                         @NotNull Sharable<?> sharable) {

        if (!sharable.isOptional()) {
            this.messager.normal(Message.NO_OPTIONAL_SHARES, executionContext.getSender(), sharable);
            throw new ConditionFailedException();
        }
    }

    private void checkCreatableGroupName(@NotNull ConditionContext<BukkitCommandIssuer> context,
                                         @NotNull BukkitCommandExecutionContext executionContext,
                                         @NotNull String groupName) {

        if (this.plugin.getGroupManager().getGroup(groupName) != null) {
            this.messager.normal(Message.GROUP_EXISTS, executionContext.getSender(), groupName);
            throw new ConditionFailedException();
        }
    }
}
