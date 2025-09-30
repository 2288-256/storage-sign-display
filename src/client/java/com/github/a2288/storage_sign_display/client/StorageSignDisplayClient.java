package com.github.a2288.storage_sign_display.client;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.a2288.storage_sign_display.config.Config;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageSignDisplayClient implements ClientModInitializer {

    private static final Map<Integer, ItemStack> RESOLVED_ITEM_CACHE = new ConcurrentHashMap<>();
    private static final Map<Integer, Boolean> IS_STORAGE_SIGN_CACHE = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(StorageSignDisplayClient.class);

    @Override
    public void onInitializeClient() {
        Config.load();
    }

    public static boolean isStorageSign(ItemStack stack) {
        if (stack.getItem() != Items.OAK_SIGN) {
            return false;
        }

        int hash = stack.hashCode();

        return IS_STORAGE_SIGN_CACHE.computeIfAbsent(hash, k -> {
            Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
            if (customName == null) {
                return false;
            }
            
            String itemName = customName.getString();
            Config config = Config.instance();
            

            return config.myListOption.stream().anyMatch(entry -> {
                Config.ItemConfig itemConfig = config.itemConfigs.get(entry.id);
                return itemConfig != null && itemConfig.enabled && entry.name.equals(itemName);
            });
        });
    }

    public static ItemStack getResolvedItem(ItemStack stack) {
        if (!isStorageSign(stack)) {
            return stack;
        }

        int hash = stack.hashCode();

        return RESOLVED_ITEM_CACHE.computeIfAbsent(hash, k -> {
            Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
            if (customName == null) {
                return new ItemStack(Items.OAK_SIGN);
            }
            
            String itemName = customName.getString();
            Config config = Config.instance();
            

            Config.ItemEntry matchingEntry = config.myListOption.stream()
                .filter(entry -> entry.name.equals(itemName))
                .findFirst()
                .orElse(null);
                
            if (matchingEntry == null) {
                return new ItemStack(Items.OAK_SIGN);
            }
            
            Config.ItemConfig itemConfig = config.itemConfigs.get(matchingEntry.id);
            if (itemConfig == null || !itemConfig.enabled) {
                return new ItemStack(Items.OAK_SIGN);
            }
            
            LoreComponent loreComponent = stack.get(DataComponentTypes.LORE);
            if (loreComponent != null) {
                List<Text> loreLines = loreComponent.lines();
                int itemNameLineIndex = itemConfig.itemNameLineNumber - 1;
                
                if (loreLines.size() > itemNameLineIndex && itemNameLineIndex >= 0) {
                    try {
                        Text itemText = loreLines.get(itemNameLineIndex);
                        String rawItemName = itemText.getString();

                        Identifier itemId = Identifier.tryParse(rawItemName.toLowerCase());

                        if (itemId != null && Registries.ITEM.containsId(itemId)) {
                            return new ItemStack(Registries.ITEM.get(itemId));
                        }

                    } catch (Exception e) {

                    }
                }
            }

            return new ItemStack(Items.OAK_SIGN);
        });
    }

    public static String getItemCountText(ItemStack stack) {
        if (!isStorageSign(stack)) {
            return null;
        }
        
        Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (customName == null) {
            return null;
        }
        
        String itemName = customName.getString();
        Config config = Config.instance();
        

        Config.ItemEntry matchingEntry = config.myListOption.stream()
            .filter(entry -> entry.name.equals(itemName))
            .findFirst()
            .orElse(null);
            
        if (matchingEntry == null) {
            return null;
        }
        
        Config.ItemConfig itemConfig = config.itemConfigs.get(matchingEntry.id);
        if (itemConfig == null || !itemConfig.enabled || !itemConfig.showCount) {
            return null;
        }
        
        LoreComponent loreComponent = stack.get(DataComponentTypes.LORE);
        if (loreComponent != null) {
            List<Text> loreLines = loreComponent.lines();
            int countLineIndex = itemConfig.countLineNumber - 1;
            
            if (loreLines.size() > countLineIndex && countLineIndex >= 0) {
                return loreLines.get(countLineIndex).getString();
            }
        }
        
        return null;
    }

    public static void onConfigChanged() {
        RESOLVED_ITEM_CACHE.clear();
        IS_STORAGE_SIGN_CACHE.clear();
    }
}