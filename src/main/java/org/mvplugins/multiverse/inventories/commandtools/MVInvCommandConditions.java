package org.mvplugins.multiverse.inventories.commandtools;

import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.BukkitCommandExecutionContext;
import org.mvplugins.multiverse.external.acf.commands.BukkitCommandIssuer;
import org.mvplugins.multiverse.external.acf.commands.BukkitConditionContext;
import org.mvplugins.multiverse.external.acf.commands.CommandConditions;
import org.mvplugins.multiverse.external.acf.commands.ConditionContext;
import org.mvplugins.multiverse.external.acf.commands.ConditionFailedException;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.share.Sharable;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

@Service
public final class MVInvCommandConditions {

    private final WorldGroupManager worldGroupManager;

    @Inject
    private MVInvCommandConditions(@NotNull MVCommandManager commandManager, @NotNull WorldGroupManager worldGroupManager) {
        this.worldGroupManager = worldGroupManager;

        CommandConditions<BukkitCommandIssuer, BukkitCommandExecutionContext, BukkitConditionContext> commandConditions
                = commandManager.getCommandConditions();
        commandConditions.addCondition(Sharable.class, "optionalSharable", this::checkOptionalSharable);
        commandConditions.addCondition(String.class, "newWorldGroupName", this::checkNewWorldGroupName);
    }

    private void checkOptionalSharable(ConditionContext<BukkitCommandIssuer> context,
                                       BukkitCommandExecutionContext executionContext,
                                       Sharable<?> sharable) {
        if (sharable == null || !sharable.isOptional()) {
            throw new ConditionFailedException(MVInvi18n.TOGGLE_NOOPTIONALSHARES);
        }
    }

    private void checkNewWorldGroupName(ConditionContext<BukkitCommandIssuer> context,
                                        BukkitCommandExecutionContext executionContext,
                                        String worldGroupName) {
        if (worldGroupManager.getGroup(worldGroupName) != null) {
            throw new ConditionFailedException(MVInvi18n.GROUP_EXISTS, "{group}", worldGroupName);
        }
    }
}
