package com.onarandombox.multiverseinventories.util;

import org.bukkit.Server;
import org.bukkit.command.SimpleCommandMap;

public abstract class DummyCraftServer implements Server {
    public abstract SimpleCommandMap getCommandMap();
}
