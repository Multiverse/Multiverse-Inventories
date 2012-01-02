package com.onarandombox.multiverseprofiles.locale;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An enum containing all messages/strings used by Multiverse.
 */
public enum MultiverseMessage {
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

    //// Commands
    // List Command
    LIST_NAME("World Listing"),
    LIST_DESC("Displays a listing of all worlds that you can enter."),
    LIST_TITLE("Multiverse World List"),
    LIST_NO_MATCH("No worlds matched your filter:");

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
