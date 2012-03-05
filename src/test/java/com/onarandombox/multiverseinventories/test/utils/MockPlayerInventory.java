package com.onarandombox.multiverseinventories.test.utils;

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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ItemStack getChestplate() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ItemStack getLeggings() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ItemStack getBoots() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setArmorContents(ItemStack[] itemStacks) {
        this.armorContents = itemStacks;
    }

    @Override
    public void setHelmet(ItemStack itemStack) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setChestplate(ItemStack itemStack) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setLeggings(ItemStack itemStack) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setBoots(ItemStack itemStack) {
        //To change body of implemented methods use File | Settings | File Templates.
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
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ItemStack getItem(int i) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        //To change body of implemented methods use File | Settings | File Templates.
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
}
