package com.github.a2288.storage_sign_display.config;

import com.github.a2288.storage_sign_display.client.StorageSignDisplayClient;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.ItemControllerBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StorageConfigGUI {


    public enum IdentificationMethod {
        ITEM_ID("アイテムID"),
        ITEM_TAG("アイテムTag");

        private final String displayName;

        IdentificationMethod(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public static void save() {
        Config.save();
        StorageSignDisplayClient.onConfigChanged();
    }

    public static Screen make(Screen parent) {
        Config config = Config.instance();

        OptionFlag customFlag = client -> {
            client.setScreen(new RequireCloseSettingScreen(client.currentScreen));
        };

        ListOption<String> itemListOption = ListOption.<String>createBuilder()
                .name(Text.literal("認識させるアイテム名"))
                .description(OptionDescription.of(Text.literal("デフォルトではアイテム名が\n\"Storage Sign\"のみ動作しますが、\n以下にアイテム名を追加することで、その他のアイテム名でも\nこのModの機能を適用することができます。\n\n各アイテム名の詳細設定は上部のタブにある詳細設定で変更可能です。")))
                .state(StateManager.createInstant(
                        config.myListOption.stream().map(entry -> entry.name).collect(Collectors.toList()),
                        () -> config.myListOption.stream().map(entry -> entry.name).collect(Collectors.toList()),
                        newVal -> {
                            config.itemConfigs.keySet().removeIf(key ->
                                    config.myListOption.stream().noneMatch(entry -> entry.id.equals(key)) ||
                                            !newVal.contains(config.myListOption.stream()
                                                    .filter(entry -> entry.id.equals(key))
                                                    .findFirst().map(entry -> entry.name).orElse(""))
                            );
                            List<Config.ItemEntry> newEntries = new ArrayList<>();
                            List<Config.ItemEntry> usedEntries = new ArrayList<>();
                            for (String name : newVal) {
                                if (!name.isEmpty()) {
                                    Config.ItemEntry existingEntry = config.myListOption.stream()
                                            .filter(entry -> entry.name.equals(name) && !usedEntries.contains(entry))
                                            .findFirst()
                                            .orElse(null);
                                    if (existingEntry != null) {
                                        newEntries.add(existingEntry);
                                        usedEntries.add(existingEntry);
                                    } else {
                                        Config.ItemEntry newEntry = new Config.ItemEntry(name);
                                        newEntries.add(newEntry);
                                        config.itemConfigs.put(newEntry.id, new Config.ItemConfig());
                                    }
                                }
                            }
                            config.myListOption = newEntries;
                            StorageSignDisplayClient.onConfigChanged();
                            MinecraftClient.getInstance().setScreen(make(parent));
                        }
                ))
                .controller(StringControllerBuilder::create)
                .initial("StorageSign")
                .build();

        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder()
                .title(Text.literal("Storage Sign Display - 設定"))
                .save(() -> {
                    Config.save();
                    StorageSignDisplayClient.onConfigChanged();
                })
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("全般設定"))
                        .tooltip(Text.literal("このmodの全般設定を行うカテゴリーです"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("一般設定"))
                                .description(OptionDescription.of(Text.literal("Modの有効・無効化などを設定できます")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Modの有効化"))
                                        .description(OptionDescription.of(Text.literal("クリックで有効・無効を切り替えます。")))
                                        .binding(true, () -> config.modEnabled, newVal -> {
                                            config.modEnabled = newVal;
                                            StorageSignDisplayClient.onConfigChanged();
                                        })
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .formatValue(val -> val ? Text.literal("有効") : Text.literal("無効"))
                                                .coloured(true))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("アイテムのテクスチャ変更"))
                                        .description(OptionDescription.of(Text.literal("Storage Signのテクスチャを収納されているアイテムのテクスチャに変更するかを切り替えできます。")))
                                        .binding(true, () -> config.textureChange, newVal -> {
                                            config.textureChange = newVal;
                                            StorageSignDisplayClient.onConfigChanged();
                                        })
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .formatValue(val -> val ? Text.literal("有効") : Text.literal("無効"))
                                                .coloured(true))
                                        .build())
                                .build()
                        )
                        .group(itemListOption)
                        .build());

        if (!config.myListOption.isEmpty() && config.myListOption.stream().anyMatch(entry -> !entry.name.isEmpty())) {
            ConfigCategory.Builder detailCategoryBuilder = ConfigCategory.createBuilder()
                    .name(Text.literal("詳細設定"))
                    .tooltip(Text.literal("各アイテムの詳細設定を行います"));

            for (Config.ItemEntry entry : config.myListOption) {
                if (entry.name.isEmpty()) continue;

                Config.ItemConfig itemConfig = config.itemConfigs.computeIfAbsent(entry.id, k -> new Config.ItemConfig());

                Option<Item> itemIdOption = Option.<Item>createBuilder()
                        .name(Text.literal("対象アイテム（ID）"))
                        .description(OptionDescription.of(Text.literal("対象となる具体的なアイテムを選択します")))
                        .binding(Items.OAK_SIGN, () -> itemConfig.itemId, newVal -> {
                            itemConfig.itemId = newVal;
                            StorageSignDisplayClient.onConfigChanged();
                        })
                        .controller(ItemControllerBuilder::create)
                        .available(itemConfig.identificationMethod == IdentificationMethod.ITEM_ID)
                        .build();

                Option<String> itemTagOption = Option.<String>createBuilder()
                        .name(Text.literal("対象アイテム（タグ）"))
                        .description(OptionDescription.of(
                                Text.literal("アイテムタグを指定します。\n")
                                        .append(Text.literal("例: #minecraft:signs, #minecraft:swords\n"))
                        ))
                        .binding("#minecraft:signs", () -> itemConfig.itemTag, newVal -> {
                            itemConfig.itemTag = newVal;
                            StorageSignDisplayClient.onConfigChanged();
                        })
                        .controller(StringControllerBuilder::create)
                        .available(itemConfig.identificationMethod == IdentificationMethod.ITEM_TAG)
                        .build();

                Option<IdentificationMethod> identificationMethodOption = Option.<IdentificationMethod>createBuilder()
                        .name(Text.literal("アイテム識別方法"))
                        .description(OptionDescription.of(Text.literal("このアイテムを識別する方法を選択します。\\n・アイテムID: minecraft:diamond_swordなどの完全なID\\n・アイテムTag: #minecraft:swordsなどのタグ")))
                        .stateManager(StateManager.createInstant(
                                IdentificationMethod.ITEM_ID,
                                () -> itemConfig.identificationMethod,
                                newVal -> {
                                    itemConfig.identificationMethod = newVal;
                                    itemIdOption.setAvailable(newVal == IdentificationMethod.ITEM_ID);
                                    itemTagOption.setAvailable(newVal == IdentificationMethod.ITEM_TAG);
                                    StorageSignDisplayClient.onConfigChanged();
                                }
                        ))
                        .controller(opt -> EnumControllerBuilder.create(opt)
                                .enumClass(IdentificationMethod.class)
                                .formatValue(val -> Text.literal(val.toString())))
                        .build();
                detailCategoryBuilder.group(OptionGroup.createBuilder()
                        .name(Text.literal(entry.name))
                        .description(OptionDescription.of(Text.literal("アイテム名「" + entry.name + "」の設定")))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("有効化"))
                                .description(OptionDescription.of(Text.literal("アイテム名が" + entry.name + "の場合、表示を変更するかの切り替え")))
                                .binding(true, () -> itemConfig.enabled, newVal -> {
                                    itemConfig.enabled = newVal;
                                    StorageSignDisplayClient.onConfigChanged();
                                })
                                .controller(opt -> BooleanControllerBuilder.create(opt)
                                        .formatValue(val -> val ? Text.literal("有効") : Text.literal("無効"))
                                        .coloured(true))
                                .build())
                        .option(identificationMethodOption)
                        .option(itemIdOption)
                        .option(itemTagOption)
                        .option(Option.<Integer>createBuilder()
                                .name(Text.literal("アイテム名がある行数"))
                                .description(OptionDescription.of(Text.literal("アイテム名が表示される行数を設定します")))
                                .binding(1, () -> itemConfig.itemNameLineNumber, newVal -> {
                                    itemConfig.itemNameLineNumber = newVal;
                                    StorageSignDisplayClient.onConfigChanged();
                                })
                                .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                                        .formatValue(val -> Text.literal(val + " 行目")))
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.literal("アイテムの個数がある行数"))
                                .description(OptionDescription.of(Text.literal("アイテムの個数が表示される行数を設定します")))
                                .binding(2, () -> itemConfig.countLineNumber, newVal -> {
                                    itemConfig.countLineNumber = newVal;
                                    StorageSignDisplayClient.onConfigChanged();
                                })
                                .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                                        .formatValue(val -> Text.literal(val + " 行目")))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("個数表示"))
                                .description(OptionDescription.of(Text.literal("アイテムの個数を表示するかどうか")))
                                .binding(true, () -> itemConfig.showCount, newVal -> {
                                    itemConfig.showCount = newVal;
                                    StorageSignDisplayClient.onConfigChanged();
                                })
                                .controller(opt -> BooleanControllerBuilder.create(opt)
                                        .formatValue(val -> val ? Text.literal("表示") : Text.literal("非表示"))
                                        .coloured(true))
                                .build())
                        .build());
            }
            builder.category(detailCategoryBuilder.build());
        }


        YetAnotherConfigLib yaclLib = builder.build();
        return new CustomYACLScreen(yaclLib, parent);
    }

    private static class RequireCloseSettingScreen extends ConfirmScreen {
        public RequireCloseSettingScreen(Screen parent) {
            super(option -> {
                        if (option) {
                            MinecraftClient.getInstance().setScreen(make(parent));
                        } else MinecraftClient.getInstance().setScreen(parent);
                    },
                    Text.literal("設定画面を一度閉じる必要があります").formatted(Formatting.RED, Formatting.BOLD),
                    Text.literal("このオプションを保存した場合は一度設定画面を閉じる必要があります"),
                    Text.literal("設定画面を閉じる").formatted(Formatting.GREEN),
                    Text.literal("警告を無視して戻る").formatted(Formatting.RED)
            );
        }
    }
}