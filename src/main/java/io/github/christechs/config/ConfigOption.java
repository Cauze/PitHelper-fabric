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

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConfigOption<T> extends ConfigElement {
    private final Supplier<T> getter;
    private final Consumer<T> setter;
    public double min = Double.MIN_VALUE;
    public double max = Double.MAX_VALUE;

    public ConfigOption(
            String id, String name, String description, Supplier<T> getter, Consumer<T> setter) {
        super(id, name, description);
        this.getter = getter;
        this.setter = setter;
    }

    public T get() {
        return getter.get();
    }

    public void set(T value) {
        setter.accept(value);
    }

    public ConfigOption<T> setBounds(double min, double max) {
        this.min = min;
        this.max = max;
        return this;
    }

    public ConfigOption<T> setHidden(boolean hidden) {
        this.hidden = hidden;
        return this;
    }
}
