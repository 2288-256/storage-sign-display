package com.github.a2288.storage_sign_display.config;

import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.mixin.TabNavigationBarAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class CustomYACLScreen extends YACLScreen {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomYACLScreen.class);
    
    public CustomYACLScreen(YetAnotherConfigLib config, Screen parent) {
        super(config, parent);
    }
    
    @Override
    protected void init() {
        super.init();

        if (tabNavigationBar != null) {
            wrapTabNavigationForPendingChangesCheck();
        }
    }

    private void wrapTabNavigationForPendingChangesCheck() {
        hookTabManagerForPendingChanges();
    }

    private void hookTabManagerForPendingChanges() {
    }
    
    private int lastTabIndex = -1;
    private boolean preventTabChange = false;
    
    @Override
    public void tick() {
        super.tick();
        if (tabNavigationBar instanceof TabNavigationBarAccessor accessor) {
            int currentTabIndex = accessor.yacl$getTabs().indexOf(Objects.requireNonNull(tabManager.getCurrentTab()));
            if (lastTabIndex != -1 && lastTabIndex != currentTabIndex && !preventTabChange && pendingChanges()) {
                client = MinecraftClient.getInstance();
                MinecraftClient.getInstance().setScreen(new RequireSaveScreen(client.currentScreen));
                return;
            }
            if (!preventTabChange) {
                lastTabIndex = currentTabIndex;
            }
        }
    }
    private class RequireSaveScreen extends ConfirmScreen {
        public RequireSaveScreen(Screen parent) {
            super(option -> {
                        if (option) {
                            finishOrSave();
                        }
                        else {
                            preventTabChange = true;
                            try {
                                if (tabNavigationBar instanceof TabNavigationBarAccessor accessor) {
                                    var tabs = accessor.yacl$getTabs();
                                    if (lastTabIndex >= 0 && lastTabIndex < tabs.size()) {
                                        tabManager.setCurrentTab(tabs.get(lastTabIndex), false);
                                    }
                                }
                            } catch (Exception e) {
                                LOGGER.error("タブの復元中にエラーが発生しました: ", e);
                            } finally {
                                preventTabChange = false;
                            }
                            MinecraftClient.getInstance().setScreen(parent);
                        }
                    },
                    Text.literal("移動する前に保存する必要があります。").formatted(Formatting.RED, Formatting.BOLD),
                    Text.literal("YetAnotherConfigLibの仕様上、タブを変更する前に設定を保存する必要があります。"),
                    Text.literal("設定内容を保存する").formatted(Formatting.GREEN),
                    Text.literal("保存せずに戻る").formatted(Formatting.RED)
            );
        }
    }
}