package com.onarandombox.multiverseinventories;

import org.mvplugins.multiverse.core.api.MVPlugin;
import org.mvplugins.multiverse.core.inject.binder.JavaPluginBinder;
import org.mvplugins.multiverse.external.glassfish.hk2.utilities.binding.ScopedBindingBuilder;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;

public class MultiverseInventoriesPluginBinder extends JavaPluginBinder<MultiverseInventories> {

    protected MultiverseInventoriesPluginBinder(@NotNull MultiverseInventories plugin) {
        super(plugin);
    }

    @Override
    protected ScopedBindingBuilder<MultiverseInventories> bindPluginClass
            (ScopedBindingBuilder<MultiverseInventories> bindingBuilder) {
        return super.bindPluginClass(bindingBuilder).to(MVPlugin.class).to(MultiverseInventories.class);
    }
}
