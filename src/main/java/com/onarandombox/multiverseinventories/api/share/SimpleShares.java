package com.onarandombox.multiverseinventories.api.share;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Simple implementation of Shares.
 */
public class SimpleShares {

    private EnumSet<DefaultSharable> sharing = EnumSet.noneOf(DefaultSharable.class);

    public SimpleShares(DefaultSharable... sharables) {
        for (DefaultSharable sharable : sharables) {
            this.sharing.add(sharable);
        }
    }

    private SimpleShares(EnumSet<DefaultSharable> sharables) {
        this.sharing = sharables;
    }

    private SimpleShares(List sharesList) {
        for (Object shareStringObj : sharesList) {
            String shareString = shareStringObj.toString();
            DefaultSharable sharable = DefaultSharable.lookup(shareString);
            if (sharable != null) {
                this.sharing.add(sharable);
            } else {
                if (shareString.equals("*") || shareString.equalsIgnoreCase("all")
                        || shareString.equalsIgnoreCase("everything")) {
                    this.sharing = DefaultSharable.all();
                    break;
                }
            }
        }
    }



}

