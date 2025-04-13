package org.mvplugins.multiverse.inventories;

import org.mvplugins.multiverse.core.module.MultiverseModuleBinder;
import org.mvplugins.multiverse.external.glassfish.hk2.utilities.binding.ScopedBindingBuilder;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;

final class MultiverseInventoriesPluginBinder extends MultiverseModuleBinder<MultiverseInventories> {

    MultiverseInventoriesPluginBinder(@NotNull MultiverseInventories plugin) {
        super(plugin);
    }

    @Override
    protected ScopedBindingBuilder<MultiverseInventories> bindPluginClass
            (ScopedBindingBuilder<MultiverseInventories> bindingBuilder) {
        return super.bindPluginClass(bindingBuilder).to(MultiverseInventories.class);
    }
}
