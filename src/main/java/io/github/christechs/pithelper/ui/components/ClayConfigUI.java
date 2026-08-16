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

package io.github.christechs.pithelper.ui.components;

import io.github.christechs.clayj.config.ImageConfigBuilder;
import io.github.christechs.clayj.enums.LayoutAlignmentX;
import io.github.christechs.clayj.enums.LayoutAlignmentY;
import io.github.christechs.clayj.math.CornerRadius;
import io.github.christechs.config.*;

import static io.github.christechs.clayj.ClayJ.*;
import static io.github.christechs.clayj.enums.LayoutAlignmentX.LEFT;
import static io.github.christechs.clayj.enums.LayoutAlignmentX.RIGHT;
import static io.github.christechs.clayj.enums.LayoutDirection.LEFT_TO_RIGHT;
import static io.github.christechs.clayj.enums.LayoutDirection.TOP_TO_BOTTOM;
import static io.github.christechs.clayj.enums.SizingType.*;

public class ClayConfigUI {

    public static boolean elementMatches(ConfigElement element, String query) {
        if (query == null || query.trim().isEmpty()) return true;
        if (element.hidden) return false;

        String q = query.toLowerCase();
        if (element.name.toLowerCase().contains(q)) return true;
        if (element.description != null && element.description.toLowerCase().contains(q)) return true;

        if (element instanceof ConfigGroup) {
            for (ConfigElement child : ((ConfigGroup) element).elements) {
                if (elementMatches(child, q)) return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static void drawConfigElement(ConfigElement element, String query) {
        if (element.hidden || !elementMatches(element, query)) return;

        if (element instanceof ConfigOption) {

            ConfigOption<?> opt = (ConfigOption<?>) element;

            if (opt.get() instanceof Boolean)
                configToggle((ConfigOption<Boolean>) opt);
            else if (opt.get() instanceof Integer)
                configSpinner((ConfigOption<Integer>) opt);
            else if (opt.get() instanceof String)
                configTextInput((ConfigOption<String>) opt);

        } else if (element instanceof ConfigGroup) {

            drawConfigGroup((ConfigGroup) element, query);

        } else if (element instanceof ConfigButton) {

            ConfigButton btn = (ConfigButton) element;

            el(decl().layout(layout().dir(TOP_TO_BOTTOM).gap(4)), () -> {

                text(btn.name, txt().size(1).color(200, 200, 200, 255));

                if (btn.description != null && !btn.description.isEmpty()) {
                    text(btn.description, txt().size(1).color(150, 150, 150, 255));
                }

                ClayComponents.button("CfgBtn_" + btn.id, btn.buttonLabel, btn.action);
            });
        } else if (element instanceof ConfigDisplay) {
            ConfigDisplay disp = (ConfigDisplay) element;
            el(decl()
                    .bg(35, 38, 45, 255)
                    .radius(6)
                    .layout(layout()
                            .padding(10, 10)),
                    () -> text(
                            disp.name + ": §e" + disp.displayValue.get(),
                            txt().size(1).color(255, 255, 255, 255)
                    )
            );
        }
    }

    public static void drawConfigGroup(ConfigGroup group, String query) {
        boolean isSearching = query != null && !query.trim().isEmpty();
        if (isSearching) group.expanded = true;

        el(decl()
                .id("Grp_" + group.id)
                .bg(40, 45, 55, 255)
                .radius(6)
                .layout(layout()
                        .sizing(GROW, 0, FIT, 0)
                        .dir(TOP_TO_BOTTOM)
                        .padding(10, 10)
                        .gap(10)),
                () -> {
                    renderGroupHeader(group);
                    if (group.expanded) renderGroupBody(group, query);
                }
        );
    }

    private static void renderGroupHeader(ConfigGroup group) {
        el(decl().layout(layout()
                .sizing(GROW, 0, FIXED, 24)
                .dir(LEFT_TO_RIGHT)
                .align(LEFT, LayoutAlignmentY.CENTER)
                .gap(10)),
                () -> {
                    ClayComponents.button(
                            "Btn_Toggle_" + group.id,
                            group.expanded ? "▼" : "▶",
                            () -> group.expanded = !group.expanded
                    );

                    if (group.icon != null) {
                        el(decl()
                                .image(new ImageConfigBuilder()
                                        .data(group.icon)
                                        .sourceDim(16, 16)
                                ).layout(layout()
                                        .sizing(FIXED, 16, FIXED, 16)),
                                () -> {});
                    }

                    if (group.masterToggle != null) {
                        configToggle(group.masterToggle);
                    } else {
                        text(group.name, txt().size(1).color(255, 255, 255, 255));
                    }
                });
    }

    private static void renderGroupBody(ConfigGroup group, String query) {
        if (group.description != null && !group.description.isEmpty()) {
            text(group.description, txt().size(1).color(180, 180, 180, 255));
        }

        el(decl().layout(layout().dir(TOP_TO_BOTTOM).gap(8).padding(15, 0)), () -> {
            boolean groupExplicitMatch = group.name.toLowerCase().contains(query == null ? "" : query.toLowerCase());
            for (ConfigElement child : group.elements) {
                drawConfigElement(child, groupExplicitMatch ? "" : query);
            }
        });
    }

    public static void configTextInput(ConfigOption<String> option) {
        el(decl().layout(layout().dir(TOP_TO_BOTTOM).gap(4)), () -> {
            text(option.name, txt().size(1).color(200, 200, 200, 255));
            ClayComponents.textInput("Txt_" + option.id, option.name, option.get(), 200, option::set);
        });
    }

    public static void configToggle(ConfigOption<Boolean> option) {
        String id = "Tgl_" + option.id;
        boolean value = option.get();

        el(decl().layout(layout()
                .dir(LEFT_TO_RIGHT)
                .gap(10)
                .align(LEFT, LayoutAlignmentY.CENTER)),
                () -> {
                if (ClayComponents.isClicked(id))
                    option.set(!value);
                el(decl()
                        .id(id)
                        .bg(
                                value ? 60 : 100,
                                value ? 180 : 100,
                                value ? 80 : 100, 255
                        ).radius(new CornerRadius(8))
                        .layout(layout()
                                .sizing(FIXED, 32, FIXED, 16)
                                .padding(2, 0)
                                .align(value ? RIGHT : LEFT, LayoutAlignmentY.CENTER)),
                        () -> el(decl()
                                .bg(255, 255, 255, 255)
                                .radius(new CornerRadius(6))
                                .layout(layout()
                                        .sizing(FIXED, 12, FIXED, 12)),
                                () -> {})
                );

                el(decl().layout(layout()
                        .dir(TOP_TO_BOTTOM)),
                        () -> {
                    text(
                            option.name,
                            txt().size(1)
                                    .color(255, 255, 255, 255)
                    );
                    if (option.description != null && !option.description.isEmpty()) {
                        text(option.description, txt().size(1).color(150, 150, 150, 255));
                    }
                }
                );
        }
        );
    }

    public static void configSpinner(ConfigOption<Integer> option) {
        String id = "Spn_" + option.id;
        int value = option.get();

        el(decl().layout(layout()
                .dir(LEFT_TO_RIGHT)
                .gap(8)
                .align(LEFT, LayoutAlignmentY.CENTER)),
                () -> {
            text(
                    option.name + ":",
                    txt().size(1)
                            .color(200, 200, 200, 255)
            );

            ClayComponents.button(
                    id + "_down",
                    "-",
                    () -> {
                        if (value > option.min)
                            option.set(value - 1);
                    }
                    );
            el(decl().layout(layout()
                    .sizing(FIXED, 20, FIXED, 20)
                    .align(LayoutAlignmentX.CENTER, LayoutAlignmentY.CENTER)),
                    () -> text(String.valueOf(value), txt().size(1)));

            ClayComponents.button(id + "_up", "+", () -> {
                if (value < option.max) option.set(value + 1);
            }
            );
        }
        );
    }
}