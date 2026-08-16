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

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ConfigGroup extends ConfigElement {
    public final List<ConfigElement> elements = new ArrayList<>();
    public boolean expanded = false;
    public ConfigOption<Boolean> masterToggle;
    public Identifier icon;

    public ConfigGroup(
            String id,
            String name,
            String description,
            Identifier icon,
            ConfigOption<Boolean> masterToggle) {
        super(id, name, description);
        this.icon = icon;
        this.masterToggle = masterToggle;
    }

    public <T extends ConfigElement> T add(T element) {
        this.elements.add(element);
        return element;
    }
}
