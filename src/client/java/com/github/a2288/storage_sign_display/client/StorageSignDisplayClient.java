package com.github.a2288.storage_sign_display.client;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class StorageSignDisplayClient implements ClientModInitializer {

    private static final Map<Integer, ItemStack> RESOLVED_ITEM_CACHE = new ConcurrentHashMap<>();
    private static final Map<Integer, Boolean> IS_STORAGE_SIGN_CACHE = new ConcurrentHashMap<>();

    @Override
    public void onInitializeClient() {
    }

    public static boolean isStorageSign(ItemStack stack) {
        if (stack.getItem() != Items.OAK_SIGN) {
            return false;
        }

        int hash = stack.hashCode();

        return IS_STORAGE_SIGN_CACHE.computeIfAbsent(hash, k -> {
            Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
            return customName != null && "StorageSign".equals(customName.getString());
        });
    }

    public static ItemStack getResolvedItem(ItemStack stack) {
        if (!isStorageSign(stack)) {
            return stack;
        }

        int hash = stack.hashCode();

        return RESOLVED_ITEM_CACHE.computeIfAbsent(hash, k -> {
            LoreComponent loreComponent = stack.get(DataComponentTypes.LORE);
            if (loreComponent != null) {
                List<Text> loreLines = loreComponent.lines();
                if (!loreLines.isEmpty()) {
                    try {
                        Text itemText = loreLines.get(0);
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
}