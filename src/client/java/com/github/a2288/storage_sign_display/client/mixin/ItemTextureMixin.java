package com.github.a2288.storage_sign_display.client.mixin;

import com.github.a2288.storage_sign_display.client.StorageSignDisplayClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemTextureMixin {

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", at = @At("HEAD"), cancellable = true)
    private void onRenderItem(ItemStack itemStack, ModelTransformation.Mode transformationMode, boolean isLeftHanded, MatrixStack transformationMatrices, VertexConsumerProvider vertexConsumerProvider, int lightLevel, int overlayCoords, BakedModel originalModel, CallbackInfo callbackInfo) {
        if (itemStack.getItem() != Items.OAK_SIGN) {
            return;
        }
        
        if (transformationMode != ModelTransformation.Mode.GUI && transformationMode != ModelTransformation.Mode.FIXED) {
            return;
        }
        
        if (!StorageSignDisplayClient.isStorageSign(itemStack)) {
            return;
        }

        ItemStack storageItemToDisplay = StorageSignDisplayClient.getResolvedItem(itemStack);
        
        ItemRenderer currentItemRenderer = (ItemRenderer) (Object) this;
        BakedModel storageItemModel = currentItemRenderer.getModels().getModel(storageItemToDisplay);
        currentItemRenderer.renderItem(storageItemToDisplay, transformationMode, isLeftHanded, transformationMatrices, vertexConsumerProvider, lightLevel, overlayCoords, storageItemModel);
        callbackInfo.cancel();
    }
}
