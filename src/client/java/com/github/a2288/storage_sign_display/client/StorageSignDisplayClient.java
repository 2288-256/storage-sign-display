package com.github.a2288.storage_sign_display.client;

import java.util.HashMap;
import java.util.Map;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class StorageSignDisplayClient implements ClientModInitializer {

    private static final Map<Integer, ItemStack> RESOLVED_ITEM_CACHE = new HashMap<>();
    private static final Map<Integer, Boolean> IS_STORAGE_SIGN_CACHE = new HashMap<>();

    @Override
    public void onInitializeClient() {
    }

    public static boolean isStorageSign(ItemStack stack) {
        if (stack.getItem() != Items.OAK_SIGN) {
            return false;
        }

        int hash = stack.getNbt() != null ? stack.getNbt().hashCode() : 0;

        return IS_STORAGE_SIGN_CACHE.computeIfAbsent(hash, k -> {
            boolean result = stack.hasCustomName() && "StorageSign".equals(stack.getName().getString());
            return result;
        });
    }

    public static ItemStack getResolvedItem(ItemStack stack) {
        if (!isStorageSign(stack)) {
            return stack;
        }

        int hash = stack.getNbt() != null ? stack.getNbt().hashCode() : 0;

        return RESOLVED_ITEM_CACHE.computeIfAbsent(hash, k -> {

            NbtCompound display = stack.getSubNbt("display");
            if (display != null && display.contains("Lore", StorageSignConstants.NBT_LIST_TYPE)) {
                NbtList lore = display.getList("Lore", StorageSignConstants.NBT_STRING_TYPE);
                if (lore.size() >= 1) {
                    try {
                        String itemJson = lore.getString(0);
                        Text itemText = Text.Serializer.fromJson(itemJson);

                        if (itemText != null) {
                            String rawItemName = itemText.getString();
                            String itemName = rawItemName.toLowerCase();
                            Identifier itemId = new Identifier(itemName);

                            if (Registry.ITEM.containsId(itemId)) {
                                ItemStack resolvedItem = new ItemStack(Registry.ITEM.get(itemId));
                                return resolvedItem;
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }

            return new ItemStack(Items.OAK_SIGN);
        });
    }
}