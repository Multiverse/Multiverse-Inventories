package com.onarandombox.multiverseinventories.commands_acf;

import com.onarandombox.acf.BaseCommand;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.locale.Messager;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.permissions.Permission;

import java.util.HashSet;
import java.util.Set;

public abstract class InventoriesCommand extends BaseCommand {

    protected final MultiverseInventories plugin;
    protected final Messager messager;
    private final Set<String> permissions;

    protected InventoriesCommand(MultiverseInventories plugin) {
        this.plugin = plugin;
        this.messager = plugin.getMessager();
        this.permissions = new HashSet<>();
    }

    protected void addPermission(Perm perm) {
        addPermission(perm.getPermission());
    }

    protected void addPermission(Permission perm) {
        addPermission(perm.getName());
    }

    protected void addPermission(String perm) {
        permissions.add(perm);
    }

    @Override
    public Set<String> getRequiredPermissions() {
        return permissions;
    }
}
