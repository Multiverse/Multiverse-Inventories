package com.onarandombox.multiverseinventories.command.tools;

import java.util.Arrays;

import com.onarandombox.acf.BukkitCommandExecutionContext;
import com.onarandombox.acf.CommandContexts;
import com.onarandombox.acf.InvalidCommandArgument;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.locale.Messager;
import com.onarandombox.multiverseinventories.migration.DataImporter;
import com.onarandombox.multiverseinventories.share.Sharable;
import com.onarandombox.multiverseinventories.share.Sharables;
import com.onarandombox.multiverseinventories.share.Shares;

public class MVInvCommandContexts {
    public static void init(MultiverseInventories plugin) {
        new MVInvCommandContexts(plugin);
    }

    private final MultiverseInventories plugin;
    private final Messager messager;

    private MVInvCommandContexts(MultiverseInventories plugin) {
        this.plugin = plugin;
        this.messager = plugin.getMessager();
        CommandContexts<BukkitCommandExecutionContext> commandContexts = plugin.getCore().getMVCommandManager().getCommandContexts();
        commandContexts.registerContext(DataImporter.class, this::parseDataImporter);
        commandContexts.registerContext(Sharable.class, this::parseSharable);
        commandContexts.registerContext(Shares.class, this::parseShares);
        commandContexts.registerContext(WorldGroup.class, this::parseWorldGroup);
    }

    private DataImporter parseDataImporter(BukkitCommandExecutionContext context) {
        String pluginName = context.popFirstArg();
        DataImporter importer;

        if (pluginName.equalsIgnoreCase("MultiInv")) {
            importer = this.plugin.getImportManager().getMultiInvImporter();
        } else if (pluginName.equalsIgnoreCase("WorldInventories")) {
            importer = this.plugin.getImportManager().getWorldInventoriesImporter();
        } else {
            throw new InvalidCommandArgument(messager.getMessage(Message.ERROR_UNSUPPORTED_IMPORT, pluginName));
        }

        if (importer == null) {
            throw new InvalidCommandArgument(messager.getMessage(Message.ERROR_PLUGIN_NOT_ENABLED, pluginName), false);
        }

        return importer;
    }

    private Sharable<?> parseSharable(BukkitCommandExecutionContext context) {
        String sharableName = context.popFirstArg();
        Sharable<?> targetSharable = Sharables.all().stream()
                .filter(sharable -> sharable.getNames().length > 0)
                .filter(sharable -> sharable.getNames()[0].equals(sharableName))
                .findFirst()
                .orElse(null);

        if (targetSharable != null) {
            return targetSharable;
        }
        if (context.isOptional()) {
            return null;
        }
        throw new InvalidCommandArgument(messager.getMessage(Message.ERROR_NO_SHARES_SPECIFIED));
    }

    private Shares parseShares(BukkitCommandExecutionContext context) {
        Shares shares = Sharables.lookup(context.popFirstArg());
        if (shares != null) {
            return shares;
        }
        if (context.isOptional()) {
            return null;
        }
        throw new InvalidCommandArgument(messager.getMessage(Message.ERROR_NO_SHARES_SPECIFIED));
    }

    private WorldGroup parseWorldGroup(BukkitCommandExecutionContext context) {
        String groupName = context.popFirstArg();
        WorldGroup group = this.plugin.getGroupManager().getGroup(groupName);
        if (group != null) {
            return group;
        }
        if (context.isOptional()) {
            return null;
        }
        throw new InvalidCommandArgument(messager.getMessage(Message.ERROR_NO_GROUP, groupName));
    }
}
