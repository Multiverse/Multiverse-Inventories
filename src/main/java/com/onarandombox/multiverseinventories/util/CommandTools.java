package com.onarandombox.multiverseinventories.util;

import com.onarandombox.MultiverseCore.commandTools.MVCommandManager;
import com.onarandombox.acf.BukkitCommandCompletionContext;
import com.onarandombox.acf.BukkitCommandExecutionContext;
import com.onarandombox.acf.BukkitCommandIssuer;
import com.onarandombox.acf.ConditionContext;
import com.onarandombox.acf.ConditionFailedException;
import com.onarandombox.acf.InvalidCommandArgument;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.commands_acf.GroupCommand;
import com.onarandombox.multiverseinventories.commands_acf.ToggleCommand;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.locale.Messager;
import com.onarandombox.multiverseinventories.share.Sharable;
import com.onarandombox.multiverseinventories.share.Sharables;
import com.onarandombox.multiverseinventories.share.Shares;
import org.jetbrains.annotations.NotNull;

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

        // Contexts
        this.manager.getCommandContexts().registerContext(Sharable.class, this::deriveSharable);
        this.manager.getCommandContexts().registerContext(Shares.class, this::deriveShares);

        // Conditions
        this.manager.getCommandConditions().addCondition(Sharable.class, "optional", this::checkIsSharableOptional);

        //Commands
        this.manager.registerCommand(new GroupCommand(this.plugin));
        this.manager.registerCommand(new ToggleCommand(this.plugin));
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
    private Sharable<?> deriveSharable(BukkitCommandExecutionContext context) {
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

    private void checkIsSharableOptional(@NotNull ConditionContext<BukkitCommandIssuer> context,
                                         @NotNull BukkitCommandExecutionContext executionContext,
                                         @NotNull Sharable<?> sharable) {

        if (!sharable.isOptional()) {
            this.messager.normal(Message.NO_OPTIONAL_SHARES, executionContext.getSender(), sharable);
            throw new ConditionFailedException();
        }
    }
}
