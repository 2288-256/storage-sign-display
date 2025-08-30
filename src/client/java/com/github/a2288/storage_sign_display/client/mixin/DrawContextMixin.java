package com.github.a2288.storage_sign_display.client.mixin;

import com.github.a2288.storage_sign_display.client.StorageSignDisplayClient;
import com.github.a2288.storage_sign_display.client.StorageSignConstants;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.joml.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {

    protected DrawContextMixin(Matrix3x2fStack matrices) {
        this.matrices = matrices;
    }

    @Shadow public abstract void drawText(TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow);
    @Shadow private Matrix3x2fStack matrices;

    @Inject(
            method = "drawStackOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void renderCustomCountAfterOverlay(TextRenderer textRenderer, ItemStack stack, int x, int y, String stackCountText, CallbackInfo ci) {

        this.matrices.pushMatrix();
        if (!StorageSignDisplayClient.isStorageSign(stack)) {
            this.matrices.popMatrix();
            return;
        }

        float xPosition = (float) (x + StorageSignConstants.TEXT_X_OFFSET - StorageSignConstants.TEXT_X_MARGIN / StorageSignConstants.FONT_SCALE);
        float yPosition = (float) (y + StorageSignConstants.TEXT_Y_OFFSET + StorageSignConstants.TEXT_Y_MARGIN / StorageSignConstants.FONT_SCALE);
        this.matrices.translate(xPosition,yPosition);
        this.matrices.scale(StorageSignConstants.FONT_SCALE,StorageSignConstants.FONT_SCALE);
        String formattedDisplayText = "";
        try {
            LoreComponent loreComponent = stack.get(DataComponentTypes.LORE);
            if (loreComponent != null) {
                if (loreComponent.lines().size() > 1) {
                    String rawItemCountText = loreComponent.lines().get(1).getString();
                    long totalItemCount = Long.parseLong(rawItemCountText);
                    formattedDisplayText = formatItemCount(totalItemCount);
                } else {
                    System.err.println("Error parsing item count from lore");
                    formattedDisplayText = "ERR";
                }
            }
        } catch (Exception e) {
            formattedDisplayText = "ERR";
        }
        renderGuiQuad(textRenderer,formattedDisplayText);
        this.matrices.popMatrix();
        ci.cancel();
    }

    private String formatItemCount(long totalItemCount) {
        if (totalItemCount < 0) return "Invalid";
        if (totalItemCount == 0) return "0";

        long numLargeChests = totalItemCount / StorageSignConstants.ITEMS_PER_LARGE_CHEST;
        long remainderAfterLargeChests = totalItemCount % StorageSignConstants.ITEMS_PER_LARGE_CHEST;
        long numStacks = remainderAfterLargeChests / StorageSignConstants.ITEMS_PER_STACK;
        long numIndividualItems = remainderAfterLargeChests % StorageSignConstants.ITEMS_PER_STACK;

        StringBuilder stacksAndItemsLine = new StringBuilder();
        if (numStacks > 0) stacksAndItemsLine.append(numStacks).append("s");
        if (numIndividualItems > 0) stacksAndItemsLine.append(numIndividualItems);

        StringBuilder largeChestsLine = new StringBuilder();
        if (numLargeChests > 0) {
            if (numLargeChests >= StorageSignConstants.MILLION_THRESHOLD) {
                largeChestsLine.append(String.format("%.1fM", numLargeChests / (double)StorageSignConstants.MILLION_THRESHOLD)).append("LC");
            } else if (numLargeChests >= StorageSignConstants.THOUSAND_THRESHOLD) {
                largeChestsLine.append(String.format("%.1fK", numLargeChests / (double)StorageSignConstants.THOUSAND_THRESHOLD)).append("LC");
            } else {
                largeChestsLine.append(numLargeChests).append("LC");
            }
        }

        if (!stacksAndItemsLine.isEmpty() && !largeChestsLine.isEmpty()) {
            return largeChestsLine + "\n" + stacksAndItemsLine;
        } else if (!largeChestsLine.isEmpty()) {
            return largeChestsLine.toString();
        } else if (!stacksAndItemsLine.isEmpty()) {
            return stacksAndItemsLine.toString();
        } else {
            return "0";
        }
    }

    private void renderGuiQuad( TextRenderer textRenderer, String displayText) {
        if (displayText != null && !displayText.isEmpty()) {
            String[] textLines = displayText.split("\n");
                for (int lineIndex = 0; lineIndex < textLines.length; lineIndex++) {
                    String currentLine = textLines[lineIndex];
                    if (!currentLine.isEmpty()) {
                        if (textLines.length == 1) {
                            int leftAlignedX = -textRenderer.getWidth(currentLine);
                            drawText(textRenderer, Text.of(currentLine), leftAlignedX, (int) StorageSignConstants.LINE_SPACING, StorageSignConstants.WHITE_COLOR, true);
                            continue;
                        }
                        drawText(textRenderer, Text.of(currentLine), -textRenderer.getWidth(currentLine), (int) (lineIndex * StorageSignConstants.LINE_SPACING), StorageSignConstants.WHITE_COLOR, true);
                    }
                }
            }

        }
    

    /**
     * DrawContextがアイテムを描画する直前に、描画対象のItemStack (`stack`) を改変します。
     * このprivateメソッドは、DrawContextにおける全てのアイテム描画の最終地点です。
     *
     * @param originalStack 元々描画されるはずだったItemStack
     * @return 実際に描画するItemStack
     */
    @ModifyVariable(
            method = "drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;III)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private ItemStack modifyRenderedItemStack(ItemStack originalStack) {
        if (originalStack.isOf(Items.OAK_SIGN) && StorageSignDisplayClient.isStorageSign(originalStack)) {

            ItemStack itemToDisplay = StorageSignDisplayClient.getResolvedItem(originalStack);

            if (itemToDisplay != null && !itemToDisplay.isEmpty()) {
                return itemToDisplay;
            }
        }

        return originalStack;
    }
}