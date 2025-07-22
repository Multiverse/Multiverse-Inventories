package org.mvplugins.multiverse.inventories.listeners;

import org.jvnet.hk2.annotations.Contract;
import org.mvplugins.multiverse.core.dynamiclistener.DynamicListener;
import org.mvplugins.multiverse.inventories.view.ReadOnlyInventoryHolder;

@Contract
public sealed interface MVInvListener extends DynamicListener permits InventoryViewListener, MVEventsListener, RespawnListener, ShareHandleListener, SpawnChangeListener {
}
