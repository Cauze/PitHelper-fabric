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

package io.github.christechs.pithelper.ui.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class XmlContext {
    private final Map<String, Supplier<String>> stringVars = new HashMap<>();
    private final Map<String, Supplier<Boolean>> booleanVars = new HashMap<>();
    private final Map<String, Runnable> callbacks = new HashMap<>();
    private final Map<String, Runnable> customRenderers = new HashMap<>();

    private final Map<String, Consumer<String>> stringSetters = new HashMap<>();

    public void bindString(String key, Supplier<String> supplier) {
        stringVars.put(key, supplier);
    }

    public void bindBoolean(String key, Supplier<Boolean> supplier) {
        booleanVars.put(key, supplier);
    }

    public void bindAction(String key, Runnable action) {
        callbacks.put(key, action);
    }

    public void bindCustomRenderer(String id, Runnable renderer) {
        customRenderers.put(id, renderer);
    }

    public void bindInput(String key, Supplier<String> getter, Consumer<String> setter) {
        stringVars.put(key, getter);
        stringSetters.put(key, setter);
    }

    public String resolveString(String input) {
        if (input == null || input.isEmpty()) return input;

        if (input.contains("${")) {
            String result = input;
            for (Map.Entry<String, Supplier<String>> entry : stringVars.entrySet()) {
                String token = "${" + entry.getKey() + "}";
                if (result.contains(token)) {
                    String val = entry.getValue().get();
                    result = result.replace(token, val == null ? "" : val);
                }
            }
            result = result.replaceAll("\\$\\{[^}]+\\}", "");
            return result;
        }

        return input;
    }

    public boolean resolveBoolean(String input) {
        boolean invert = false;
        if (input != null && input.startsWith("!")) {
            invert = true;
            input = input.substring(1);
        }

        if (input != null && input.startsWith("${") && input.endsWith("}")) {
            String key = input.substring(2, input.length() - 1);
            boolean result = booleanVars.containsKey(key) ? booleanVars.get(key).get() : false;
            return invert != result;
        }
        return invert != Boolean.parseBoolean(input);
    }

    public Runnable getCallback(String key) {
        return callbacks.getOrDefault(key, () -> {
        });
    }

    public Runnable getCustomRenderer(String id) {
        return customRenderers.get(id);
    }

    public Consumer<String> getStringSetter(String key) {
        return stringSetters.getOrDefault(key, val -> {
        });
    }
}
