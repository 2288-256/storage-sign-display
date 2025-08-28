package com.github.a2288.storage_sign_display.client.mixin;

import com.github.a2288.storage_sign_display.client.StorageSignDisplayClient;
import com.github.a2288.storage_sign_display.client.StorageSignConstants;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ItemRenderer.class)
public abstract class ItemSizeDisplayMixin {

    @Inject(method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("TAIL"))
    private void renderCustomCount(TextRenderer textRenderer, ItemStack itemStack, int xPosition, int yPosition, String countLabel, CallbackInfo callbackInfo) {
        if (!StorageSignDisplayClient.isStorageSign(itemStack)) {
            return;
        }

        NbtCompound displayNbt = itemStack.getSubNbt("display");
        if (displayNbt != null && displayNbt.contains("Lore", StorageSignConstants.NBT_LIST_TYPE)) {
            NbtList loreList = displayNbt.getList("Lore", StorageSignConstants.NBT_STRING_TYPE);
            if (loreList.size() >= StorageSignConstants.MINIMUM_LORE_SIZE) {
                String rawItemCountText;
                try {
                    String itemCountJson = loreList.getString(StorageSignConstants.LORE_ITEM_COUNT_INDEX);
                    Text parsedText = Text.Serializer.fromJson(itemCountJson);
                    rawItemCountText = (parsedText != null ? parsedText.getString() : "");
                } catch (Exception parseException) {
                    rawItemCountText = "";
                }

                String formattedDisplayText;
                try {
                    long totalItemCount = Long.parseLong(rawItemCountText);
                    formattedDisplayText = formatItemCount(totalItemCount);
                } catch (NumberFormatException numberFormatException) {
                    formattedDisplayText = rawItemCountText;
                }

                if (!formattedDisplayText.isEmpty()) {
                    if (!itemStack.isEmpty()) {
                        MatrixStack transformationMatrices = new MatrixStack();
                        renderGuiQuad(transformationMatrices, textRenderer, formattedDisplayText, xPosition, yPosition);
                    }
                }
            }
        }
    }

    private String formatItemCount(long totalItemCount) {
        if (totalItemCount < 0) return "Invalid count";
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
        
        if (stacksAndItemsLine.length() > 0 && largeChestsLine.length() > 0) {
            return largeChestsLine.toString() + "\n" + stacksAndItemsLine.toString();
        } else if (largeChestsLine.length() > 0) {
            return largeChestsLine.toString();
        } else if (stacksAndItemsLine.length() > 0) {
            return stacksAndItemsLine.toString();
        } else {
            return "0";
        }
    }

    private void renderGuiQuad(MatrixStack transformationMatrices, TextRenderer textRenderer, String displayText, int xPosition, int yPosition) {
        if (displayText != null && !displayText.isEmpty()) {
            String[] textLines = displayText.split("\n");
            
            transformationMatrices.push();
            transformationMatrices.translate(0.0D, 0.0D, StorageSignConstants.Z_OFFSET);
            transformationMatrices.scale(StorageSignConstants.FONT_SCALE, StorageSignConstants.FONT_SCALE, StorageSignConstants.Z_SCALE);
            
            VertexConsumerProvider.Immediate immediateVertexConsumer = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            
            for (int lineIndex = 0; lineIndex < textLines.length; lineIndex++) {
                String currentLine = textLines[lineIndex];
                if (!currentLine.isEmpty()) {
                    float textXPosition = (float)((xPosition + StorageSignConstants.TEXT_X_OFFSET - StorageSignConstants.TEXT_X_MARGIN) / StorageSignConstants.FONT_SCALE - textRenderer.getWidth(currentLine));
                    float textYPosition = (float)((yPosition + StorageSignConstants.TEXT_Y_OFFSET + StorageSignConstants.TEXT_Y_MARGIN) / StorageSignConstants.FONT_SCALE + (lineIndex * StorageSignConstants.LINE_SPACING));
                    textRenderer.draw(currentLine, textXPosition, textYPosition, StorageSignConstants.WHITE_COLOR, true, transformationMatrices.peek().getPositionMatrix(), immediateVertexConsumer, false, 0, StorageSignConstants.LIGHT_LEVEL);
                }
            }
            
            immediateVertexConsumer.draw();
            transformationMatrices.pop();
        }
    }
}
