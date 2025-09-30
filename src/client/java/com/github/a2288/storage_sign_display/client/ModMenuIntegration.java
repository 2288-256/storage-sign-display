package com.github.a2288.storage_sign_display.client;

import com.github.a2288.storage_sign_display.config.StorageConfigGUI;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return StorageConfigGUI::make;
    }
}