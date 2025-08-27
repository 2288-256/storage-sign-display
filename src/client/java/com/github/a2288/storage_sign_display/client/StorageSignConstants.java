package com.github.a2288.storage_sign_display.client;

/**
 * Constants used throughout the Storage Sign Display mod.
 */
public class StorageSignConstants {
    // ストレージ関連
    public static final long ITEMS_PER_STACK = 64L;
    public static final long STACKS_PER_LARGE_CHEST = 54L;
    public static final long ITEMS_PER_LARGE_CHEST = ITEMS_PER_STACK * STACKS_PER_LARGE_CHEST;
    
    // 表示フォーマット関連
    public static final long MILLION_THRESHOLD = 1_000_000L;
    public static final long THOUSAND_THRESHOLD = 1_000L;
    
    // レンダリング関連
    public static final float FONT_SCALE = 0.5f;
    public static final float Z_OFFSET = 400.0f;
    public static final float Z_SCALE = 1.0f;
    public static final float TEXT_X_OFFSET = 18.0f;
    public static final float TEXT_X_MARGIN = 2.0f;
    public static final float TEXT_Y_OFFSET = 6.0f;
    public static final float TEXT_Y_MARGIN = 2.0f;
    public static final float LINE_SPACING = 7.5f;
    public static final int WHITE_COLOR = 0xFFFFFF; // 16777215
    public static final int LIGHT_LEVEL = 15728880;
    
    // NBT関連
    public static final int NBT_LIST_TYPE = 9;
    public static final int NBT_STRING_TYPE = 8;
    public static final int LORE_ITEM_NAME_INDEX = 0;
    public static final int LORE_ITEM_COUNT_INDEX = 1;
    public static final int MINIMUM_LORE_SIZE = 2;
}
