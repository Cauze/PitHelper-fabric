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

package io.github.christechs.config;

import java.util.ArrayList;
import java.util.List;

public class ConfigCategoryData {
    public final String id;
    public final String name;
    public final List<ConfigElement> elements = new ArrayList<>();

    public ConfigCategoryData(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void add(ConfigElement element) {
        this.elements.add(element);
    }
}
