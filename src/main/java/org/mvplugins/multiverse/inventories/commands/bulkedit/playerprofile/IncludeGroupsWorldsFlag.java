package org.mvplugins.multiverse.inventories.commands.bulkedit.playerprofile;

import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.flag.CommandFlag;
import org.mvplugins.multiverse.core.command.flag.CommandFlagsManager;
import org.mvplugins.multiverse.core.command.flag.FlagBuilder;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;

@Service
final class IncludeGroupsWorldsFlag extends FlagBuilder {
    static final String NAME = "mvinvincludegroupsworlds";

    @Inject
    private IncludeGroupsWorldsFlag(CommandFlagsManager flagsManager) {
        super(NAME, flagsManager);
    }

    final CommandFlag includeGroupsWorlds = flag(CommandFlag.builder("--include-groups-worlds")
            .addAlias("-i")
            .build());
}
