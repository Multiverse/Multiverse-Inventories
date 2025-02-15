package org.mvplugins.multiverse.inventories.util;

import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.locale.message.MessageReplacement;
import org.mvplugins.multiverse.external.acf.locales.MessageKey;
import org.mvplugins.multiverse.external.acf.locales.MessageKeyProvider;

public enum MVInvi18n implements MessageKeyProvider {
    TEST_STRING,

    GENERIC_SORRY,
    GENERIC_PAGE,
    GENERIC_OF,
    GENERIC_UNLOADED,
    GENERIC_PLUGINDISABLED,
    GENERIC_ERROR,
    GENERIC_SUCCESS,
    GENERIC_INFO,
    GENERIC_HELP,
    GENERIC_COMMANDNOPERMISION,
    GENERIC_THECONSOLE,
    GENERIC_NOTLOGGEDIN,
    GENERIC_OFF,

    ERROR_CONFIGLOAD,
    ERROR_DATALOAD,
    ERROR_NOGROUP,
    ERROR_NOWORLD,
    ERROR_NOWORLDPROFILE,
    ERROR_NOSHARESSPECIFIED,

    CONFLICT_RESULTS,
    CONFLICT_CHECKING,
    CONFLICT_FOUND,
    CONFLICT_NOTFOUND,

    INFO_WORLD,
    INFO_WORLD_INFO,
    INFO_GROUP,
    INFO_GROUP_INFO,
    INFO_GROUP_INFOSHARES,
    INFO_GROUP_INFONEGATIVESHARES,
    INFO_ZEROARG,

    LIST_GROUPS,
    LIST_GROUPS_INFO,

    RELOAD_COMPLETE,

    ADDWORLD_WORLDADDED,
    ADDWORLD_WORLDALREADYEXISTS,

    REMOVEWORLD_WORLDREMOVED,
    REMOVEWORLD_WORLDNOTINGROUP,

    SHARES_NOWSHARING,

    SPAWN_TELEPORTING,
    SPAWN_TELEPORTEDBY,
    SPAWN_TELEPORTCONSOLEERROR,

    DEBUG_INVALIDDEBUG,
    DEBUG_SET,

    TOGGLE_NOWUSINGOPTIONAL,
    TOGGLE_NOWNOTUSINGOPTIONAL,
    TOGGLE_NOOPTIONALSHARES,

    GROUP_COMMANDPROMPT,
    GROUP_CREATEPROMPT,
    GROUP_EDITPROMPT,
    GROUP_DELETEPROMPT,
    GROUP_MODIFYPROMPT,
    GROUP_WORLDSPROMPT,
    GROUP_SHARESPROMPT,
    GROUP_INVALIDNAME,
    GROUP_EXISTS,
    GROUP_REMOVED,
    GROUP_WORLDSEMPTY,
    GROUP_CREATIONCOMPLETE,
    GROUP_UPDATED,
    GROUP_NONCONVERSABLE,
    GROUP_INVALIDOPTION,

    IMPORT_PLUGINNOTENABLED,
    IMPORT_UNSUPPORTEDPLUGIN,
    IMPORT_CONFIRMPROMPT,
    IMPORT_SUCCESS,
    IMPORT_FAILED,

    DELETEGROUP_CONFIRMPROMPT,
    DELETEGROUP_SUCCESS,
    ;

    private final MessageKey key = MessageKey.of("mv-inventories." + this.name().replace('_', '.')
            .toLowerCase());

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageKey getMessageKey() {
        return this.key;
    }

    /**
     * Creates a message with non-localized message fallback and replacements
     * @param nonLocalizedMessage   The non-localized message
     * @param replacements          The replacements
     *
     * @return A new localizable Message
     */
    @NotNull
    public Message bundle(@NotNull String nonLocalizedMessage, @NotNull MessageReplacement... replacements) {
        return Message.of(this, nonLocalizedMessage, replacements);
    }
}
