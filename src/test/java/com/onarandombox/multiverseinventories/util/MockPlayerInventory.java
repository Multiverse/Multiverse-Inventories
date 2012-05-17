package com.onarandombox.multiverseinventories.util;

import com.onarandombox.multiverseinventories.api.DataStrings;
import com.onarandombox.multiverseinventories.api.DataStrings.ItemWrapper;
import com.onarandombox.multiverseinventories.api.PlayerStats;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

public class MockPlayerInventory implements PlayerInventory {
    
    ItemStack[] armorContents = new ItemStack[PlayerStats.ARMOR_SIZE];
    ItemStack[] inventoryContents = new ItemStack[PlayerStats.INVENTORY_SIZE];

    @Override
    public ItemStack[] getArmorContents() {
        return armorContents;
    }

    @Override
    public ItemStack getHelmet() {
        return armorContents[0];
    }

    @Override
    public ItemStack getChestplate() {
        return armorContents[1];
    }

    @Override
    public ItemStack getLeggings() {
        return armorContents[2];
    }

    @Override
    public ItemStack getBoots() {
        return armorContents[3];
    }

    @Override
    public void setArmorContents(ItemStack[] itemStacks) {
        this.armorContents = itemStacks;
    }

    @Override
    public void setHelmet(ItemStack itemStack) {
        this.armorContents[0] = itemStack;
    }

    @Override
    public void setChestplate(ItemStack itemStack) {
        this.armorContents[1] = itemStack;
    }

    @Override
    public void setLeggings(ItemStack itemStack) {
        this.armorContents[2] = itemStack;
    }

    @Override
    public void setBoots(ItemStack itemStack) {
        this.armorContents[3] = itemStack;
    }

    @Override
    public ItemStack getItemInHand() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setItemInHand(ItemStack itemStack) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getHeldItemSlot() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public HumanEntity getHolder() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getSize() {
        return inventoryContents.length + armorContents.length;
    }

    @Override
    public String getName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ItemStack getItem(int i) {
        if (i >= 0 && i < PlayerStats.INVENTORY_SIZE) {
            return inventoryContents[i];
        } else if (i >= PlayerStats.INVENTORY_SIZE && i < PlayerStats.INVENTORY_SIZE + PlayerStats.ARMOR_SIZE) {
            return armorContents[i - PlayerStats.INVENTORY_SIZE];
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        if (i >= 0 && i < PlayerStats.INVENTORY_SIZE) {
            inventoryContents[i] = itemStack;
        } else if (i >= PlayerStats.INVENTORY_SIZE && i < PlayerStats.INVENTORY_SIZE + PlayerStats.ARMOR_SIZE) {
            armorContents[i - PlayerStats.INVENTORY_SIZE] = itemStack;
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    @Override
    public HashMap<Integer, ItemStack> addItem(ItemStack... itemStacks) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public HashMap<Integer, ItemStack> removeItem(ItemStack... itemStacks) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ItemStack[] getContents() {
        return this.inventoryContents;
    }

    @Override
    public void setContents(ItemStack[] itemStacks) {
        this.inventoryContents = itemStacks;
    }

    @Override
    public boolean contains(int i) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean contains(Material material) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean contains(ItemStack itemStack) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean contains(int i, int i1) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean contains(Material material, int i) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean contains(ItemStack itemStack, int i) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public HashMap<Integer, ? extends ItemStack> all(int i) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public HashMap<Integer, ? extends ItemStack> all(Material material) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public HashMap<Integer, ? extends ItemStack> all(ItemStack itemStack) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int first(int i) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int first(Material material) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int first(ItemStack itemStack) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int firstEmpty() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void remove(int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void remove(Material material) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void remove(ItemStack itemStack) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void clear(int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void clear() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<HumanEntity> getViewers() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getTitle() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public InventoryType getType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ListIterator<ItemStack> iterator() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getMaxStackSize() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setMaxStackSize(int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ListIterator<ItemStack> iterator(int i) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Integer i = 0; i < PlayerStats.INVENTORY_SIZE; i++) {
            if (this.getContents()[i] != null && this.getContents()[i].getTypeId() != 0) {
                if (first) {
                    first = false;
                } else {
                    builder.append(DataStrings.ITEM_DELIMITER);
                }
                builder.append(DataStrings.createEntry(i, ItemWrapper.wrap(this.getContents()[i]).toString()));
            }
        }
        StringBuilder finalString = new StringBuilder();
        finalString.append("{ \"inventoryContents\" : \"").append(builder.toString()).append("\"").append(", \"armorContents\" : \"");
        builder = new StringBuilder();
        first = true;
        for (Integer i = 0; i < PlayerStats.ARMOR_SIZE; i++) {
            if (this.getArmorContents()[i] != null && this.getArmorContents()[i].getTypeId() != 0) {
                if (first) {
                    first = false;
                } else {
                    builder.append(DataStrings.ITEM_DELIMITER);
                }
                builder.append(DataStrings.createEntry(i, ItemWrapper.wrap(this.getArmorContents()[i]).toString()));
            }
        }
        finalString.append(builder.toString()).append("\" }");
        return finalString.toString();
    }
}
