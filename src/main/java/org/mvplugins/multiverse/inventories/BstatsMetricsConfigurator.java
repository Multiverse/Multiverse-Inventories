package org.mvplugins.multiverse.inventories;

import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.bstats.bukkit.Metrics;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;

@Service
final class BstatsMetricsConfigurator {

    private static final int PLUGIN_ID = 7768;
    private final Metrics metrics;

    @Inject
    private BstatsMetricsConfigurator(MultiverseInventories plugin) {
        this.metrics = new Metrics(plugin, PLUGIN_ID);
    }
}
