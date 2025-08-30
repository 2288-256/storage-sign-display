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
    public static final float TEXT_X_OFFSET = 19.75f;
    public static final float TEXT_X_MARGIN = 2.0f;
    public static final float TEXT_Y_OFFSET = 3.0f;
    public static final float TEXT_Y_MARGIN = 2.0f;
    public static final float LINE_SPACING = 9f;
    public static final int WHITE_COLOR = -1; // white

}
