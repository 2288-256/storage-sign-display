package com.github.a2288.storage_sign_display.client;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.ClickEvent;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import okhttp3.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class UpdateChecker {
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .callTimeout(15, TimeUnit.SECONDS)
            .build();
    private static final Gson GSON = new GsonBuilder().create();

    private final String modId;
    private final String modrinthSlug;
    private final String currentVersion;
    private final Path cacheFile;
    private final long ttlMillis;
    private final boolean enabled;

    private volatile String lastNotifiedVersion = null;

    public UpdateChecker(String modId, String modrinthSlug, String currentVersion, long ttlMillis, boolean enabled) {
        this.modId = modId;
        this.modrinthSlug = modrinthSlug;
        this.currentVersion = currentVersion;
        this.ttlMillis = ttlMillis;
        this.enabled = enabled;
        this.cacheFile = FabricLoader.getInstance().getConfigDir().resolve(modId + "-update-cache.json");
    }

    public void checkAsync() {
        if (!enabled) return;

        try {
            if (Files.exists(cacheFile)) {
                String cached = Files.readString(cacheFile);
                JsonObject obj = JsonParser.parseString(cached).getAsJsonObject();
                long lastChecked = obj.has("lastChecked") ? obj.get("lastChecked").getAsLong() : 0L;
                if (Instant.now().toEpochMilli() - lastChecked < ttlMillis) {
                    if (obj.has("latestVersion")) {
                        String latestCached = obj.get("latestVersion").getAsString();
                        if (isNewerVersion(currentVersion, latestCached) && !latestCached.equals(lastNotifiedVersion)) {
                            notifyOnMainThread(latestCached, obj.has("versionId") ? obj.get("versionId").getAsString() : null);
                        }
                    }
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String url = "https://api.modrinth.com/v2/project/" + modrinthSlug + "/version";
        Request req = new Request.Builder()
                .url(url)
                .header("User-Agent", modId + "-update-checker")
                .build();

        CLIENT.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody body = response.body()) {
                    if (!response.isSuccessful() || body == null) return;
                    JsonArray versions = JsonParser.parseString(body.string()).getAsJsonArray();
                    JsonObject best = null;
                    Instant bestInstant = Instant.EPOCH;

                    for (JsonElement el : versions) {
                        JsonObject v = el.getAsJsonObject();
                        boolean loaderOk = false;
                        if (v.has("loaders")) {
                            for (JsonElement l : v.getAsJsonArray("loaders")) {
                                if ("fabric".equalsIgnoreCase(l.getAsString())) {
                                    loaderOk = true;
                                    break;
                                }
                            }
                        }
                        if (!loaderOk) continue;

                        if (v.has("date_published")) {
                            Instant published = Instant.parse(v.get("date_published").getAsString());
                            if (published.isAfter(bestInstant)) {
                                bestInstant = published;
                                best = v;
                            }
                        }
                    }

                    if (best == null) return;

                    String latestVersionNumber = best.has("version_number") ? best.get("version_number").getAsString() : null;
                    String versionId = best.has("id") ? best.get("id").getAsString() : null;
                    String downloadUrl = null;
                    if (best.has("files")) {
                        JsonArray files = best.getAsJsonArray("files");
                        if (files.size() > 0) {
                            JsonObject f = files.get(0).getAsJsonObject();
                            if (f.has("url")) downloadUrl = f.get("url").getAsString();
                        }
                    }

                    JsonObject cacheOut = new JsonObject();
                    cacheOut.addProperty("lastChecked", Instant.now().toEpochMilli());
                    if (latestVersionNumber != null) cacheOut.addProperty("latestVersion", latestVersionNumber);
                    if (versionId != null) cacheOut.addProperty("versionId", versionId);
                    if (downloadUrl != null) cacheOut.addProperty("downloadUrl", downloadUrl);

                    try {
                        Files.createDirectories(cacheFile.getParent());
                        Files.writeString(cacheFile, GSON.toJson(cacheOut), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (latestVersionNumber != null && isNewerVersion(currentVersion, latestVersionNumber) && !latestVersionNumber.equals(lastNotifiedVersion)) {
                        notifyOnMainThread(latestVersionNumber, versionId);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void notifyOnMainThread(String latestVersion, String versionId) {
        lastNotifiedVersion = latestVersion;

        String versionPage = versionId != null ? ("https://modrinth.com/mod/"+ modrinthSlug +"/version/" + versionId) : ("https://modrinth.com/project/" + modrinthSlug);
        MinecraftClient.getInstance().execute(() -> {
            if (MinecraftClient.getInstance().player == null) return;
            String msg = "[" + modId + "] 新しいバージョン " + latestVersion + " が利用可能です。";
            Text text = Text.literal(msg + " クリックしてダウンロードページを開く").formatted(Formatting.BLUE, Formatting.BOLD)
                    .styled(style -> Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(URI.create(versionPage))));
            MinecraftClient.getInstance().player.sendMessage(text, false);
        });
    }

    private boolean isNewerVersion(String current, String latest) {
        if (current == null || latest == null) return false;
        try {
            String[] a = current.replaceAll("[^0-9.]", "").split("\\.");
            String[] b = latest.replaceAll("[^0-9.]", "").split("\\.");
            int n = Math.max(a.length, b.length);
            for (int i = 0; i < n; i++) {
                int ai = i < a.length && !a[i].isEmpty() ? Integer.parseInt(a[i]) : 0;
                int bi = i < b.length && !b[i].isEmpty() ? Integer.parseInt(b[i]) : 0;
                if (bi > ai) return true;
                if (bi < ai) return false;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}