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

package io.github.christechs.pithelper.ui.utils;

import io.github.christechs.clayj.ClayJ;
import io.github.christechs.clayj.enums.LayoutAlignmentY;
import io.github.christechs.clayj.enums.LayoutDirection;
import io.github.christechs.pithelper.ui.components.ClayComponents;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

import static io.github.christechs.clayj.ClayJ.*;
import static io.github.christechs.clayj.enums.LayoutAlignmentX.CENTER;
import static io.github.christechs.clayj.enums.LayoutDirection.LEFT_TO_RIGHT;
import static io.github.christechs.clayj.enums.LayoutDirection.TOP_TO_BOTTOM;
import static io.github.christechs.clayj.enums.SizingType.*;

public class PitUIUtil {

    public static void drawPageNav(int currentPage, int totalPages, String idPrefix, Runnable onPrev, Runnable onNext) {
        if (totalPages > 1) {
            el(decl().layout(layout()
                    .dir(LEFT_TO_RIGHT)
                    .align(CENTER, LayoutAlignmentY.CENTER)
                    .gap(15)
                    .sizing(GROW, 0, FIT, 0)
                    .padding(0, 5)),
                    () -> {
                ClayComponents.button(idPrefix + "_Prev", "< Prev", () -> {
                    if (currentPage > 0) onPrev.run();
                });

                text(
                        "Page " + (currentPage + 1) + " of " + totalPages,
                        txt().size(1).color(200, 200, 200, 255)
                );

                ClayComponents.button(idPrefix + "_Next", "Next >", () -> {
                    if (currentPage < totalPages - 1) onNext.run();
                });
            });
        }
    }

    public static boolean drawItemSlot(String slotId, ItemStack stack) {
        boolean isHovered = stack != null && ClayJ.pointerOver(slotId);

        el(decl().id(slotId)
                .bg(60, 60, 60, 255)
                .radius(2).layout(layout()
                        .sizing(FIXED, 24, FIXED, 24)
                        .align(CENTER, LayoutAlignmentY.CENTER)),
                () -> {
            if (stack != null) {
                el(decl().custom(stack).layout(layout().sizing(FIXED, 16, FIXED, 16)), () -> {});
            }
        });
        return isHovered;
    }

    public static void drawItemGrid(
            ItemStack[] items,
            int currentPage,
            int itemsPerPage,
            int columns,
            String slotIdPrefix,
            Consumer<ItemStack> onHover,
            Consumer<Integer> onClick) {

        int totalItems = items.length;
        int startIdx = currentPage * itemsPerPage;
        int endIdx = Math.min(startIdx + itemsPerPage, totalItems);
        int itemsToDraw = endIdx - startIdx;
        int rows = (int) Math.ceil(itemsToDraw / (double) columns);

        el(decl().layout(layout()
                .dir(TOP_TO_BOTTOM)
                .gap(4)), () -> {
            for (int r = 0; r < rows; r++) {
                int finalR = r;
                el(decl().layout(layout().dir(LEFT_TO_RIGHT).gap(4)), () -> {
                    for (int c = 0; c < columns; c++) {
                        int idx = startIdx + (finalR * columns) + c;
                        if (idx < endIdx) {
                            ItemStack stack = items[idx];

                            int relativeIdx = (finalR * columns) + c;
                            String slotId = slotIdPrefix + relativeIdx;

                            if (drawItemSlot(slotId, stack) && onHover != null) {
                                onHover.accept(stack);
                            }
                            if (onClick != null && ClayComponents.isClicked(slotId)) {
                                onClick.accept(idx);
                            }
                        } else {
                            el(decl().layout(layout().sizing(FIXED, 24, FIXED, 24)), () -> {});
                        }
                    }
                });
            }
        });
    }
}