package com.onarandombox.multiverseinventories.locale;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An enum containing all messages/strings used by Multiverse.
 */
public enum MultiverseMessage {
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

    // Errors
    ERROR_CONFIG_LOAD("Encountered an error while loading the configuration file.  Disabling..."),
    ERROR_DATA_LOAD("Encountered an error while loading the data file.  Disabling..."),
    ERROR_NO_GROUP("There is no group with the name: %1"),
    ERROR_NO_WORLD_PROFILE("There is no world profile for the world: %1"),
    ERROR_PLUGIN_NOT_ENABLED("%1 is not enabled so you may not import data from it!"),
    ERROR_UNSUPPORTED_IMPORT("Sorry, '%1' is not supported for importing."),

    // Group Conflicts
    CONFLICT_RESULTS("Conflict found for groups: '%1' and '%2' because they both share: '%3' for the world(s): '%4'"),
    CONFLICT_CHECKING("Checking for conflicts in groups..."),
    CONFLICT_FOUND("Conflicts have been found... If these are not resolved, you may experience problems with your data."),
    CONFLICT_NOT_FOUND("No group conflicts found!"),

    //// Commands
    // Info Command
    INFO_WORLD("&b--- Info for world: %1 ---"),
    INFO_WORLD_INFO("&1Groups:&f %1"),
    INFO_GROUP("&b--- Info for group: %1 ---"),
    INFO_GROUP_INFO("&1Worlds:&f %1", "&1Shares:&f %2");
    // BEGIN CHECKSTYLE-SUPPRESSION: Javadoc

    private final List<String> def;

    MultiverseMessage(String def, String... extra) {
        this.def = new ArrayList<String>();
        this.def.add(def);
        this.def.addAll(Arrays.asList(extra));
    }

    /**
     * @return This {@link MultiverseMessage}'s default-message
     */
    public List<String> getDefault() {
        return def;
    }

}
