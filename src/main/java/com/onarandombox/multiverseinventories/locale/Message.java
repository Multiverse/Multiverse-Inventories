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
    ERROR_NO_WORLD_PROFILE("&6There is no world player for the world: &f%1"),
    ERROR_PLUGIN_NOT_ENABLED("&f%1 &6is not enabled so you may not import data from it!"),
    ERROR_UNSUPPORTED_IMPORT("&6Sorry, ''&f%1&6'' is not supported for importing."),
    ERROR_NO_SHARES_SPECIFIED("&cYou did not specify any valid shares!"),

    // Group Conflicts
    CONFLICT_RESULTS("Conflict found for groups: '%1' and '%2' because they both share: '%3' for the world(s): '%4'"),
    CONFLICT_CHECKING("Checking for conflicts in groups..."),
    CONFLICT_FOUND("Conflicts have been found... If these are not resolved, you may experience problems with your data."),
    CONFLICT_NOT_FOUND("No group conflicts found!"),

    //// Commands
    // Info Command
    INFO_WORLD("&b===[ Info for world: &6%1&b ]==="),
    INFO_WORLD_INFO("&6Groups:&f %1"),
    INFO_GROUP("&b===[ Info for group: &6%1&b ]==="),
    INFO_GROUP_INFO("&6Worlds:&f %1", "&bShares:&f %2"),
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
    TELEPORTING("Teleporting to this group's spawn..."),
    TELEPORTED_BY("You were teleported by: %1"),
    TELEPORT_CONSOLE_ERROR("From the console, you must provide a PLAYER"),
    // DebugCommand
    INVALID_DEBUG("&fInvalid debug level.  Please use number 0-3.  &b(3 being many many messages!)"),
    DEBUG_SET("Debug mode is %1");

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

