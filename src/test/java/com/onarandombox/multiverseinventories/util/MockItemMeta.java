/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2019.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/
package com.onarandombox.multiverseinventories.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockItemMeta {

    private static final Map<ItemMeta, MockItemMeta> itemMetaData = new HashMap<>();

    public static ItemFactory mockItemFactory() {
        ItemFactory itemFactory = mock(ItemFactory.class);

        when(itemFactory.equals(any(), any())).thenReturn(true);

        doAnswer(invocation -> {
            Material material = invocation.getArgument(0);
            switch (material) {
                case WRITTEN_BOOK:
                    return mockItemMeta(material, BookMeta.class);
                case LEATHER_BOOTS:
                    return mockItemMeta(material, LeatherArmorMeta.class);
                default:
                    return mockItemMeta(material, ItemMeta.class);
            }

        }).when(itemFactory).getItemMeta(any(Material.class));

        doReturn(true).when(itemFactory).isApplicable(any(ItemMeta.class), any(ItemStack.class));
        doReturn(true).when(itemFactory).isApplicable(any(ItemMeta.class), any(Material.class));

        doAnswer(invocation -> invocation.getArgument(0)).when(itemFactory).asMetaFor(any(ItemMeta.class), any(ItemStack.class));
        doAnswer(invocation -> invocation.getArgument(0)).when(itemFactory).asMetaFor(any(ItemMeta.class), any(Material.class));

        doAnswer(invocation -> invocation.getArgument(1)).when(itemFactory).updateMaterial(any(ItemMeta.class), any(Material.class));

        return itemFactory;
    }

    private static <T extends ItemMeta> T mockItemMeta(Material type, Class<T> itemMetaClass) {
        Map<String, Object> data = new HashMap<>();

        T itemMeta = mock(itemMetaClass, invocation -> {
            String methodName = invocation.getMethod().getName();
            if (methodName.startsWith("set")) {
                if (invocation.getArguments().length > 1) {
                    data.put(methodName.substring(3), Arrays.asList(invocation.getArguments()));
                } else {
                    data.put(methodName.substring(3), invocation.getArguments()[0]);
                }
            } else if (methodName.startsWith("get")) {
                return data.get(methodName.substring(3));
            }

            return null;
        });

        when(itemMeta.toString()).thenAnswer(i -> data.toString());

        when(itemMeta.serialize()).thenAnswer(i -> data);

        when(itemMeta.clone()).thenReturn(itemMeta);

        MockItemMeta mockItemMeta = new MockItemMeta(type);
        itemMetaData.put(itemMeta, mockItemMeta);

        return itemMeta;
    }

    private final Material type;

    private MockItemMeta(Material type) {
        this.type = type;
    }
}
