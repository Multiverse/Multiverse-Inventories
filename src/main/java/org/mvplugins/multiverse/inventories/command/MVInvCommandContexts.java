package org.mvplugins.multiverse.inventories.command;

import com.google.common.base.Strings;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.BukkitCommandExecutionContext;
import org.mvplugins.multiverse.external.acf.commands.CommandContexts;
import org.mvplugins.multiverse.external.acf.commands.InvalidCommandArgument;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.share.Sharable;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.share.Shares;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

@Service
public final class MVInvCommandContexts {

    private final WorldGroupManager worldGroupManager;

    @Inject
    private MVInvCommandContexts(@NotNull MVCommandManager commandManager, @NotNull WorldGroupManager worldGroupManager) {
        this.worldGroupManager = worldGroupManager;

        CommandContexts<BukkitCommandExecutionContext> commandContexts = commandManager.getCommandContexts();
        commandContexts.registerContext(Sharable.class, this::parseSharable);
        commandContexts.registerContext(Shares.class, this::parseShares);
        commandContexts.registerContext(WorldGroup.class, this::parseWorldGroup);
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
        throw new InvalidCommandArgument(MVInvi18n.ERROR_NOSHARESSPECIFIED);
    }

    private Shares parseShares(BukkitCommandExecutionContext context) {
        String shareStrings = context.popFirstArg();
        if (Strings.isNullOrEmpty(shareStrings)) {
            throw new InvalidCommandArgument(MVInvi18n.ERROR_NOSHARESSPECIFIED);
        }

        String[] shareNames = shareStrings.split(",");
        Shares newShares = Sharables.noneOf();
        Shares negativeShares = Sharables.noneOf();
        for (String shareName : shareNames) {
            if (shareName.startsWith("-")) {
                shareName = shareName.substring(1);
                Option.of(Sharables.lookup(shareName))
                        .peek(shares -> negativeShares.setSharing(shares, true));
                continue;
            }
            Option.of(Sharables.lookup(shareName))
                    .peek(shares -> newShares.setSharing(shares, true));
        }

        newShares.setSharing(negativeShares, false);
        if (newShares.isEmpty()) {
            throw new InvalidCommandArgument(MVInvi18n.ERROR_NOSHARESSPECIFIED);
        }

        return newShares;
    }

    private WorldGroup parseWorldGroup(BukkitCommandExecutionContext context) {
        String groupName = context.popFirstArg();
        WorldGroup group = worldGroupManager.getGroup(groupName);
        if (group != null) {
            return group;
        }
        if (context.isOptional()) {
            return null;
        }
        throw new InvalidCommandArgument(MVInvi18n.ERROR_NOGROUP);
    }
}
