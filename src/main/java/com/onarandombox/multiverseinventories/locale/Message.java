package com.onarandombox.multiverseinventories.locale;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An enum containing all messages/strings used by Multiverse.
 */
public enum Message {
    // BEGIN CHECKSTYLE-SUPPRESSION: Javadoc
    TEST_STRING("a test-string from the enum"),

    // Generic Strings
    GENERIC_SORRY("Sorry..."),
    GENERIC_PAGE("Page"),
    GENERIC_OF("of"),
    GENERIC_UNLOADED("UNLOADED"),
    GENERIC_PLUGIN_DISABLED("This plugin is Disabled!"),
    GENERIC_ERROR("[Error]"),
    GENERIC_SUCCESS("[Success]"),
    GENERIC_INFO("[Info]"),
    GENERIC_HELP("[Help]"),
    GENERIC_COMMAND_NO_PERMISSION("You do not have permission to %1. (%2)"),
    GENERIC_THE_CONSOLE("the console"),
    GENERIC_NOT_LOGGED_IN("%1 is not logged on right now!"),
    GENERIC_OFF("OFF"),

    // Errors
    ERROR_CONFIG_LOAD("Encountered an error while loading the configuration file.  Disabling..."),
    ERROR_DATA_LOAD("Encountered an error while loading the data file.  Disabling..."),
    ERROR_NO_GROUP("&6There is no group with the name: &f%1"),
    ERROR_NO_WORLD("&6There is no world with the name: &f%1"),
    ERROR_PLAYERDATA_IMPORT("&6There was an error while importing the playerdata."),
    ERROR_NO_WORLD_PROFILE("&6There is no profile container for the world: &f%1"),
    ERROR_PLUGIN_NOT_ENABLED("&f%1 &6is not enabled so you may not import data from it!"),
    ERROR_UNSUPPORTED_IMPORT("&6Sorry, ''&f%1&6'' is not supported for importing."),
    ERROR_NO_SHARES_SPECIFIED("&cYou did not specify any valid shares!"),

    // Group Conflicts
    CONFLICT_RESULTS("Conflict found for groups: '%1' and '%2' because they both share: '%3' for the world(s): '%4'"),
    CONFLICT_CHECKING("Checking for conflicts in groups..."),
    CONFLICT_FOUND("Conflicts have been found... If these are not resolved, you may experience problems with your data."),
    CONFLICT_NOT_FOUND("No group conflicts found!"),

    //// Commands
    NON_CONVERSABLE("You are not allowed to access conversations (remote console?)"),
    INVALID_PROMPT_OPTION("&cThat is not a valid option! Type &f##&c to stop working on groups."),
    // Info Command
    INFO_ZERO_ARG("You may only use the no argument version of this command in game!"),
    INFO_WORLD("&b===[ Info for world: &6%1&b ]==="),
    INFO_WORLD_INFO("&6Groups:&f %1"),
    INFO_GROUP("&b===[ Info for group: &6%1&b ]==="),
    INFO_GROUPS_INFO("&6Worlds:&f %1", "&bShares:&f %2"),
    // Group Command
    GROUP_COMMAND_PROMPT("&6What would you like to do? &fCreate&6, &fImport&6 (which will create a group as well), &fEdit &6or &fDelete&6. Enter &f##&6 at any time to cancel."),
    GROUP_CREATE_PROMPT("&6Please name your new group: "),
    GROUP_EDIT_PROMPT("&6Edit which group? %1"),
    GROUP_DELETE_PROMPT("&6Delete which group? %1"),
    GROUP_MODIFY_PROMPT("&6Which would you like to change for &e%1&6? &fWorlds &6or &fShares&6. Enter &f##&6 to finish."),
    GROUP_WORLDS_PROMPT("&6Enter the name of a world to add to group &f%1&6 or enter &f@&6 to continue. To remove a world, precede the name with the minus symbol. (ex: &f-worldname&6). Current worlds: %2"),
    VANILLA_IMPORT_PROMPT("&6Enter the name of the world you would like to import inventory data from. Worlds: %1"),
    GROUP_SHARES_PROMPT("&6Enter &fall&6 or a specific share to add to group &f%1&6 or enter &f@&6 to continue. To remove shares, precede the name with the minus symbol (ex: &f-inventory&6). Current shares: %2"),
    GROUP_INVALID_NAME("&cThat name is not valid!  May only contain letters, numbers, and underscores."),
    GROUP_EXISTS("&cThat group already exists! (&f%1&c)"),
    GROUP_REMOVED("&2Removed group: &f%1"),
    GROUP_WORLDS_EMPTY("&cYou may not have a group with no worlds, please add worlds or type &f##&c to cancel."),
    GROUP_CREATION_COMPLETE("&2You created a new group!"),
    GROUP_UPDATED("&2Group has been updated!"),
    // List Command
    LIST_GROUPS("&b===[ Group List ]===", "&6Groups:&f %1"),
    // Reload Command
    RELOAD_COMPLETE("&b===[ Reload Complete! ]==="),
    // AddWorld Command
    WORLD_ADDED("&6World:&f %1 &6added to Group: &f%2"),
    WORLD_ALREADY_EXISTS("&6World:&f %1 &6already part of Group: &f%2"),
    // RemoveWorld Command
    WORLD_REMOVED("&6World:&f %1 &6removed from Group: &f%2"),
    WORLD_NOT_IN_GROUP("&6World:&f %1 &6is not part of Group: &f%2"),
    // AddShares Command
    NOW_SHARING("&6Group: &f%1 &6is now sharing: &f%2"),
    // Spawn Command
    TELEPORTING("Teleporting to this world group's spawn..."),
    TELEPORTED_BY("You were teleported by: %1"),
    TELEPORT_CONSOLE_ERROR("From the console, you must provide a PLAYER"),
    // DebugCommand
    INVALID_DEBUG("&fInvalid debug level. Please use number 0-3.  &b(3 being many many messages!)"),
    DEBUG_SET("Debug mode is %1"),
    // Toggle Command
    NOW_USING_OPTIONAL("&f%1 &6will now be considered when player's change world."),
    NOW_NOT_USING_OPTIONAL("&f%1 &6will no longer be considered when player's change world."),
    NO_OPTIONAL_SHARES("&f%1 &6is not an optional share!"),
    // Migrate Command
    MIGRATE_FAILED("Failed to migrate data from %1 to %2! Check logs for error details."),
    MIGRATE_SUCCESSFUL("Migrated data from %1 to %2!");

    // BEGIN CHECKSTYLE-SUPPRESSION: Javadoc

    private final List<String> def;

    Message(String def, String... extra) {
        this.def = new ArrayList<String>();
        this.def.add(def);
        this.def.addAll(Arrays.asList(extra));
    }

    /**
     * @return This {@link Message}'s default-message
     */
    public List<String> getDefault() {
        return def;
    }

}

