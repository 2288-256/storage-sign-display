package com.github.a2288.storage_sign_display.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.RegistryKeys;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Config {
    public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("StorageSignDisplay.json");

    private static final ConfigClassHandler<Config> HANDLER = ConfigClassHandler.createBuilder(Config.class)
            .id(Identifier.of("storage_sign_display", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .appendGsonBuilder(builder -> builder.registerTypeAdapter(
                        new TypeToken<TagKey<Item>>(){}.getType(), 
                        new TagKeyTypeAdapter()
                    ))
                    .setPath(CONFIG_PATH)
                    .build())
            .build();

    @SerialEntry
    public boolean modEnabled = true;
    
    @SerialEntry
    public boolean textureChange = true;
    
    @SerialEntry
    public List<ItemEntry> myListOption = new ArrayList<>(List.of(new ItemEntry("StorageSign")));

    @SerialEntry
    public Map<String, ItemConfig> itemConfigs = new HashMap<>();

    public static class ItemEntry {
        @SerialEntry
        public String id;
        
        @SerialEntry
        public String name;
        
        public ItemEntry() {}
        
        public ItemEntry(String name) {
            this.id = UUID.randomUUID().toString();
            this.name = name;
        }
    }

    public static class ItemConfig {
        @SerialEntry
        public boolean enabled = true;
        
        @SerialEntry
        public int itemNameLineNumber = 1;
        
        @SerialEntry
        public int countLineNumber = 2;
        
        @SerialEntry
        public boolean showCount = true;
        
        @SerialEntry
        public StorageConfigGUI.IdentificationMethod identificationMethod = StorageConfigGUI.IdentificationMethod.ITEM_TAG;
        
        @SerialEntry
        public Item itemId = Items.OAK_SIGN;
        
        @SerialEntry
        public String itemTag = "#minecraft:signs";
        
        public ItemConfig() {}
    }

    public Config() {
        for (ItemEntry entry : myListOption) {
            if (!itemConfigs.containsKey(entry.id)) {
                itemConfigs.put(entry.id, new ItemConfig());
            }
        }
    }

    public static void load() {
        HANDLER.load();
    }
    
    public static void save() {
        HANDLER.save();
    }
    
    public static Config instance() {
        return HANDLER.instance();
    }

    public static class TagKeyTypeAdapter implements JsonSerializer<TagKey<Item>>, JsonDeserializer<TagKey<Item>> {
        @Override
        public TagKey<Item> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            try {
                String tagString = json.getAsString();
                if (tagString.startsWith("#")) {
                    tagString = tagString.substring(1);
                }
                Identifier location = Identifier.of(tagString);
                return TagKey.of(RegistryKeys.ITEM, location);
            } catch (Exception e) {
                return TagKey.of(RegistryKeys.ITEM, Identifier.of("minecraft", "tools"));
            }
        }

        @Override
        public JsonElement serialize(TagKey<Item> src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive("#" + src.id().toString());
        }
    }
}
