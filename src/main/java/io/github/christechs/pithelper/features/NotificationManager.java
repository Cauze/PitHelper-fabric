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

package io.github.christechs.pithelper.features;

import io.github.christechs.pithelper.data.PitEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager {

    public static final List<VisualNotif> activeNotifications = new CopyOnWriteArrayList<>();

    public static void add(PitEvent event, String fallbackText, long durationMs) {
        activeNotifications.removeIf(n -> n.event != null && n.event.timestamp == event.timestamp);
        activeNotifications.add(new VisualNotif(event, fallbackText, durationMs));
    }

    public static void tick() {
        long now = System.currentTimeMillis();
        activeNotifications.removeIf(n -> now > n.expireTime);
    }

    public static class VisualNotif {
        public final PitEvent event;
        public final String fallbackText;
        public final long expireTime;

        public VisualNotif(PitEvent event, String fallbackText, long durationMs) {
            this.event = event;
            this.fallbackText = fallbackText;
            this.expireTime = System.currentTimeMillis() + durationMs;
        }
    }
}
