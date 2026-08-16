/*
 * PitHelper - A Hypixel The Pit Helper Mod.
 * Copyright (C) 2026 Christian Steenkamp
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.christechs.pithelper.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EventFetcher {

    private static final String DATA_URL = "https://raw.githubusercontent.com/BrookeAFK/brookeafk-api/refs/heads/main/events.js";
    public static volatile List<PitEvent> allRawEvents = null;
    public static volatile List<PitEvent> cachedEvents = null;
    public static volatile PitEvent activeEvent = null;
    public static volatile boolean isFetching = false;
    public static volatile String errorMessage = null;

    public static void fetchAsync() {
        if (isFetching || allRawEvents != null) return;
        isFetching = true;
        errorMessage = null;

        CompletableFuture.runAsync(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(DATA_URL).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "PitHelper-Mod");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("HTTP " + conn.getResponseCode());
                }

                StringBuilder sb = new StringBuilder();
                try (Reader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                    char[] buffer = new char[4096];
                    int read;
                    while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
                        sb.append(buffer, 0, read);
                    }
                }

                String json = sb.toString().trim();

                int start = json.indexOf('[');
                int end = json.lastIndexOf(']');
                if (start != -1 && end != -1 && end >= start) {
                    json = json.substring(start, end + 1);
                } else {
                    throw new RuntimeException("Malformed data structure.");
                }

                Type listType = new TypeToken<ArrayList<PitEvent>>() {
                }.getType();
                List<PitEvent> rawEvents = new Gson().fromJson(json, listType);

                if (rawEvents != null && !rawEvents.isEmpty()) {
                    for (PitEvent e : rawEvents) {
                        e.eventType = PitEventType.fromName(e.event);
                    }
                    allRawEvents = rawEvents;
                    updateState();
                } else {
                    throw new RuntimeException("Empty or invalid event data.");
                }

            } catch (Exception ex) {
                errorMessage = "Fetch Failed: " + ex.getMessage();
            } finally {
                isFetching = false;
            }
        });
    }

    public static void updateState() {
        List<PitEvent> raw = allRawEvents;
        if (raw == null) return;

        long now = System.currentTimeMillis();
        List<PitEvent> upcoming = new ArrayList<>();
        PitEvent currentActive = null;

        for (PitEvent e : raw) {
            long actualStart = e.timestamp + e.eventType.startOffset;
            long actualEnd = actualStart + e.eventType.duration;

            if (now >= actualStart && now <= actualEnd) {
                if (currentActive == null || actualEnd < (currentActive.timestamp + currentActive.eventType.startOffset + currentActive.eventType.duration)) {
                    currentActive = e;
                }
            } else if (now < actualStart) {
                upcoming.add(e);
            }
        }

        upcoming.sort(Comparator.comparingLong(e -> e.timestamp + e.eventType.startOffset));

        cachedEvents = upcoming;
        activeEvent = currentActive;
    }
}