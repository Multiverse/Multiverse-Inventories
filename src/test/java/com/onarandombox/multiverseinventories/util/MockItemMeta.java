/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2019.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/
package com.onarandombox.multiverseinventories.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class MockItemMeta {

    private static Map<ItemMeta, MockItemMeta> itemMetaData = new HashMap<>();

    public static ItemFactory mockItemFactory() {
        ItemFactory itemFactory = mock(ItemFactory.class);

        doAnswer(invocationOnMock -> MockItemMeta.mockItemMeta(invocationOnMock.getArgument(0)))
                .when(itemFactory).getItemMeta(any(Material.class));

        return itemFactory;
    }

    private static ItemMeta mockItemMeta(Material type) {
        ItemMeta itemMeta = mock(ItemMeta.class);

        MockItemMeta mockItemMeta = new MockItemMeta(type);
        itemMetaData.put(itemMeta, mockItemMeta);

        return itemMeta;
    }

    private final Material type;

    private MockItemMeta(Material type) {
        this.type = type;
    }
}
