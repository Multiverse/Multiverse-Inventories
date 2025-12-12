package org.mvplugins.multiverse.inventories.command;

import com.google.common.base.Strings;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.core.utils.REPatterns;
import org.mvplugins.multiverse.external.acf.commands.BukkitCommandExecutionContext;
import org.mvplugins.multiverse.external.acf.commands.CommandContexts;
import org.mvplugins.multiverse.external.acf.commands.InvalidCommandArgument;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.profile.PlayerNamesMapper;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.profile.key.ContainerKey;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileType;
import org.mvplugins.multiverse.inventories.profile.key.ProfileTypes;
import org.mvplugins.multiverse.inventories.share.Sharable;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.share.Shares;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public final class MVInvCommandContexts {

    private final WorldGroupManager worldGroupManager;
    private final PlayerNamesMapper playerNamesMapper;
    private final InventoriesConfig config;
    private final ProfileDataSource profileDataSource;

    @Inject
    private MVInvCommandContexts(
            @NotNull MVCommandManager commandManager,
            @NotNull WorldGroupManager worldGroupManager,
            @NotNull PlayerNamesMapper playerNamesMapper,
            @NotNull InventoriesConfig config,
            @NotNull ProfileDataSource profileDataSource
    ) {
        this.worldGroupManager = worldGroupManager;
        this.playerNamesMapper = playerNamesMapper;
        this.config = config;
        this.profileDataSource = profileDataSource;

        CommandContexts<BukkitCommandExecutionContext> commandContexts = commandManager.getCommandContexts();
        commandContexts.registerContext(ContainerKey.class, this::parseContainerKey);
        commandContexts.registerContext(ContainerKey[].class, this::parseContainerKeyArray);
        commandContexts.registerContext(GlobalProfileKey.class, this::parseGlobalProfileKey);
        commandContexts.registerContext(GlobalProfileKey[].class, this::parseGlobalProfileKeyArray);
        commandContexts.registerIssuerAwareContext(ProfileType.class, this::parseProfileType);
        commandContexts.registerIssuerAwareContext(ProfileType[].class, this::parseProfileTypeArray);
        commandContexts.registerContext(Sharable.class, this::parseSharable);
        commandContexts.registerContext(Shares.class, this::parseShares);
        commandContexts.registerContext(WorldGroup.class, this::parseWorldGroup);
    }

    private ProfileType parseProfileType(BukkitCommandExecutionContext context) {
        if (!config.getEnableGamemodeShareHandling()) {
            return ProfileTypes.getDefault();
        }
        String profileType = context.popFirstArg();
        return ProfileTypes.forName(profileType)
                .getOrElseThrow(() -> new InvalidCommandArgument("Invalid profile type: " + profileType));
    }

    private ContainerKey parseContainerKey(BukkitCommandExecutionContext context) {
        String input = context.popFirstArg();
        String[] keyValueSplit = REPatterns.EQUALS.split(input, 2);
        if (keyValueSplit.length != 2) {
            throw new InvalidCommandArgument("Invalid world/group format: " + input + ". Expected format: type=name");
        }
        ContainerType containerType = Try.of(() -> ContainerType.valueOf(keyValueSplit[0].toUpperCase()))
                .getOrElseThrow(() -> new InvalidCommandArgument("Unknown container type: " + keyValueSplit[0]));
        String dataName = keyValueSplit[1];
        List<String> availableDataNames = profileDataSource.listContainerDataNames(containerType);
        if (!availableDataNames.contains(dataName)) {
            throw new InvalidCommandArgument("The " + keyValueSplit[0] + " name " + dataName + " does not have any data.");
        }
        return ContainerKey.create(containerType, dataName);
    }

    private ContainerKey[] parseContainerKeyArray(BukkitCommandExecutionContext context) {
        String keyStrings = context.popFirstArg();
        if (keyStrings.equals("@all")) {
            return Arrays.stream(ContainerType.values()).flatMap(containerType ->
                    profileDataSource.listContainerDataNames(containerType)
                            .stream()
                            .map(containerName -> ContainerKey.create(containerType, containerName)))
                    .toArray(ContainerKey[]::new);
        }
        List<ContainerKey> containerKeys = new ArrayList<>();
        String[] typesSplit = REPatterns.SEMICOLON.split(keyStrings);
        for (String typeSplit : typesSplit) {
            String[] keyValueSplit = REPatterns.EQUALS.split(typeSplit, 2);
            if (keyValueSplit.length != 2) {
                throw new InvalidCommandArgument("Invalid worlds/groups format: " + typeSplit + ". Expected format: type=name1,name2;type2=name3");
            }
            ContainerType containerType = ContainerType.valueOf(keyValueSplit[0].toUpperCase());
            String[] dataNameSplit = REPatterns.COMMA.split(keyValueSplit[1]);
            List<String> availableDataNames = profileDataSource.listContainerDataNames(containerType);
            for (String dataName : dataNameSplit) {
                if (availableDataNames.contains(dataName)) {
                    containerKeys.add(ContainerKey.create(containerType, dataName));
                }
            }
        }
        return containerKeys.toArray(new ContainerKey[0]);
    }

    private GlobalProfileKey parseGlobalProfileKey(BukkitCommandExecutionContext context) {
        String keyString = context.popFirstArg();
        // todo: UUID parsing
        return playerNamesMapper.getKey(keyString)
                .getOrElseThrow(() -> new InvalidCommandArgument("Unknown player name: " + keyString));
    }

    private GlobalProfileKey[] parseGlobalProfileKeyArray(BukkitCommandExecutionContext context) {
        String keyStrings = context.popFirstArg();
        if (Objects.equals(keyStrings, "@all")) {
            return playerNamesMapper.getKeys().toArray(GlobalProfileKey[]::new);
        }
        // todo: UUID parsing
        String[] profileNames = REPatterns.COMMA.split(keyStrings);
        return Arrays.stream(profileNames)
                .map(playerNamesMapper::getKey)
                .filter(Option::isDefined)
                .map(Option::get)
                .toArray(GlobalProfileKey[]::new);
    }

    private ProfileType[] parseProfileTypeArray(BukkitCommandExecutionContext context) {
        String keyStrings = context.getFirstArg();
        if (keyStrings == null) {
            return ProfileTypes.getTypes().toArray(ProfileType[]::new);
        }
        if (Objects.equals(keyStrings, "@all")) {
            context.popFirstArg();
            return ProfileTypes.getTypes().toArray(ProfileType[]::new);
        }
        String[] profileNames = REPatterns.COMMA.split(keyStrings);
        List<ProfileType> list = Arrays.stream(profileNames)
                .map(ProfileTypes::forName)
                .filter(Option::isDefined)
                .map(Option::get)
                .toList();
        if (list.isEmpty()) {
            return ProfileTypes.getTypes().toArray(ProfileType[]::new);
        }

        context.popFirstArg();
        return list.toArray(new ProfileType[0]);
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
