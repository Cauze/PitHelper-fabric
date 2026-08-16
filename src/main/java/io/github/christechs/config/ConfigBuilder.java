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

public class ConfigBuilder {

    public static ConfigGroup group(
            String id,
            String name,
            String description,
            Identifier icon,
            ConfigOption<Boolean> masterToggle) {
        return new ConfigGroup(id, name, description, icon, masterToggle);
    }

    public static ConfigButton button(
            String id, String name, String description, String buttonLabel, Runnable action) {
        return new ConfigButton(id, name, description, buttonLabel, action);
    }

    public static ConfigDisplay display(
            String id, String name, String description, java.util.function.Supplier<String> value) {
        return new ConfigDisplay(id, name, description, value);
    }

    public static ConfigOption<Boolean> virtual(
            String id,
            String name,
            String desc,
            java.util.function.Supplier<Boolean> getter,
            java.util.function.Consumer<Boolean> setter) {
        return new ConfigOption<>(id, name, desc, getter, setter);
    }

    public static ConfigOption<Integer> virtualInt(
            String id,
            String name,
            String desc,
            java.util.function.Supplier<Integer> getter,
            java.util.function.Consumer<Integer> setter) {
        return new ConfigOption<>(id, name, desc, getter, setter);
    }
}
