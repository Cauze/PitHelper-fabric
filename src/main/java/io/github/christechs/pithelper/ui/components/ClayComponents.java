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

import io.github.christechs.clayj.core.ElementId;
import io.github.christechs.clayj.core.LayoutElementHashMapItem;
import io.github.christechs.clayj.core.ScrollContainerData;
import io.github.christechs.clayj.enums.LayoutAlignmentX;
import io.github.christechs.clayj.enums.LayoutAlignmentY;
import io.github.christechs.clayj.math.CornerRadius;
import io.github.christechs.clayj.math.Vector2;
import io.github.christechs.clayj.util.HashUtil;
import io.github.christechs.pithelper.compat.McCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.util.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static io.github.christechs.clayj.ClayJ.*;
import static io.github.christechs.clayj.enums.AttachToElement.PARENT;
import static io.github.christechs.clayj.enums.FloatingAttachPoint.LEFT_CENTER;
import static io.github.christechs.clayj.enums.FloatingAttachPoint.LEFT_TOP;
import static io.github.christechs.clayj.enums.LayoutAlignmentX.LEFT;
import static io.github.christechs.clayj.enums.SizingType.FIXED;
import static io.github.christechs.clayj.enums.SizingType.GROW;

public class ClayComponents {

    private static final Map<String, ScrollBounds> scrollCache = new HashMap<>();
    public static int mouseDeltaY = 0;
    public static String activeDragElementId = "";
    public static int mouseX = 0, mouseY = 0;
    public static String activeTextInputId = "";
    public static StringBuilder activeText = new StringBuilder();
    public static Consumer<String> activeTextCallback = null;
    public static int cursorPos = 0;
    private static int previousMouseY = 0;
    private static boolean mouseClicked = false;
    private static boolean clickConsumed = false;

    public static void updateInputState(int x, int y, boolean clicked) {
        mouseDeltaY = y - previousMouseY;
        previousMouseY = y;
        mouseX = x;
        mouseY = y;
        mouseClicked = clicked;
        clickConsumed = false;

        if (!McCompat.leftMouseDown()) activeDragElementId = "";
    }

    public static boolean isClicked(String id) {
        if (mouseClicked && !clickConsumed && pointerOver(id)) {
            clickConsumed = true;
            return true;
        }
        return false;
    }

    public static void button(String id, String text, Runnable onClick) {
        boolean hover = pointerOver(id);
        if (isClicked(id)) onClick.run();

        el(decl()
                .id(id)
                .bg(hover ? 100 : 70, 100, 200, 255)
                .radius(new CornerRadius(6))
                .layout(layout()
                        .padding(8, 4)
                        .align(LayoutAlignmentX.CENTER, LayoutAlignmentY.CENTER)),
                () -> text(text, txt().size(1).color(255, 255, 255, 255))
        );
    }

    public static void tab(String id, String label, boolean active, Runnable onClick) {
        boolean hover = pointerOver(id);
        if (isClicked(id)) onClick.run();

        el(decl()
                .id(id)
                .bg(
                        active ? 60 : (hover ? 40 : 30),
                        active ? 120 : (hover ? 45 : 32),
                        active ? 220 : (hover ? 50 : 38),
                        255
                ).radius(new CornerRadius(6))
                .layout(layout()
                        .sizing(GROW, 0, FIXED, 30)
                        .padding(10, 0)
                        .align(LEFT, LayoutAlignmentY.CENTER)),
                () -> text(label, txt().size(1).color(255, 255, 255, 255)));
    }

    public static void scrollbar(String containerId) {
        ScrollContainerData data = new ScrollContainerData();
        ElementId eId = new ElementId();
        HashUtil.hashString(containerId, 0, 0, eId);
        getScrollContainerData(eId, data);

        ScrollBounds bounds = scrollCache.computeIfAbsent(containerId, k -> new ScrollBounds());

        if (data.found && data.scrollContainerDimensions.height > 0) {
            bounds.containerHeight = data.scrollContainerDimensions.height;
            bounds.contentHeight = data.contentDimensions.height;
            if (data.scrollPosition != null) {
                bounds.scrollY = data.scrollPosition.y;
            }
        }

        float trackHeight = bounds.containerHeight;
        float contentHeight = Math.max(bounds.contentHeight, trackHeight);
        float scrollableRange = contentHeight - trackHeight;

        if (scrollableRange <= 2.0f) return;

        String trackId = "ScrollTrack_" + containerId;
        el(decl()
                .id(trackId)
                .bg(20, 20, 25, 255)
                .radius(2)
                .layout(layout()
                        .sizing(FIXED, 4, GROW, 0)),
                () -> renderScrollThumb(containerId, data, bounds, trackHeight, contentHeight, scrollableRange)
        );
    }

    private static void renderScrollThumb(
            String containerId,
            ScrollContainerData data,
            ScrollBounds bounds,
            float trackHeight,
            float contentHeight,
            float scrollableRange) {
        String thumbId = "ScrollThumb_" + containerId;
        String trackId = "ScrollTrack_" + containerId;

        float thumbHeight = Math.max(20.0f, trackHeight * (trackHeight / contentHeight));
        float maxThumbY = trackHeight - thumbHeight;

        boolean isHovered = pointerOver(thumbId) || pointerOver(trackId);
        boolean isActive = activeDragElementId.equals(thumbId);

        if (isHovered && mouseClicked) {
            activeDragElementId = thumbId;
            isActive = true;
        }

        float scrollRatio = -bounds.scrollY / scrollableRange;
        float thumbY = scrollRatio * maxThumbY;

        if (isActive && data.scrollPosition != null) {
            float deltaRatio = (float) mouseDeltaY / maxThumbY;
            data.scrollPosition.y -= deltaRatio * scrollableRange;

            if (data.scrollPosition.y > 0) data.scrollPosition.y = 0;
            if (data.scrollPosition.y < -scrollableRange) data.scrollPosition.y = -scrollableRange;

            thumbY = (-data.scrollPosition.y / scrollableRange) * maxThumbY;
        }

        float finalThumbY = Math.max(0, Math.min(thumbY, maxThumbY));

        el(decl()
                .id(thumbId)
                .bg(
                        isActive || isHovered ? 100 : 80,
                        isActive || isHovered ? 100 : 80,
                        isActive || isHovered ? 110 : 90,
                        255
                )
                .radius(2)
                .floating(
                        PARENT,
                        LEFT_TOP,
                        LEFT_TOP,
                        new Vector2(0, finalThumbY),
                        0
                ).layout(layout()
                        .sizing(GROW, 0, FIXED, thumbHeight)),
                () -> {}
        );
    }

    public static void textInput(String id, String placeholder, String value, float width, Consumer<String> onChange) {
        boolean isFocused = activeTextInputId.equals(id);
        handleTextInputInteraction(id, value, onChange, isFocused);

        String display = value.isEmpty() && !isFocused ? "§8" + placeholder : value;

        el(decl()
                .id(id)
                .bg(
                        isFocused ? 25 : 20,
                        isFocused ? 27 : 22,
                        isFocused ? 31 : 26,
                        255
                )
                .radius(4)
                .layout(layout()
                        .sizing(
                                width > 0 ? FIXED : GROW,
                                width > 0 ? width : 0,
                                FIXED, 24
                        )
                        .padding(8, 0)
                        .align(LEFT, LayoutAlignmentY.CENTER)),
                () -> {
                    text(display, txt().size(1).color(255, 255, 255, 255));
                    if (isFocused && (System.currentTimeMillis() / 500) % 2 == 0) {
                        renderCursor(value);
                    }
                }
        );
    }

    private static void handleTextInputInteraction(String id, String value, Consumer<String> onChange, boolean isFocused) {
        if (isClicked(id)) {
            activeTextInputId = id;
            activeText = new StringBuilder(value);
            activeTextCallback = onChange;

            LayoutElementHashMapItem item = getContext().getHashMapItem(HashUtil.hashString(id, 0, 0));
            if (item != null) {
                float localX = mouseX - item.boundingBox.x - 8;
                int newCursorPos = 0;
                for (int i = 0; i <= value.length(); i++) {
                    int w = Minecraft.getInstance().font.width(value.substring(0, i));
                    if (localX < w + 4) {
                        newCursorPos = i;
                        break;
                    }
                    newCursorPos = i;
                }
                cursorPos = newCursorPos;
            } else {
                cursorPos = value.length();
            }
        } else if (mouseClicked && !pointerOver(id) && isFocused) {
            activeTextInputId = "";
        }

        if (isFocused && !activeText.toString().equals(value)) {
            activeText = new StringBuilder(value);
            cursorPos = Math.min(cursorPos, value.length());
        }
    }

    private static void renderCursor(String value) {
        int safeCursor = Math.min(cursorPos, value.length());
        int cursorOffset = Minecraft.getInstance().font.width(value.substring(0, safeCursor));

        el(decl()
                .bg(255, 255, 255, 255)
                .floating(
                        PARENT,
                        LEFT_CENTER,
                        LEFT_CENTER,
                        new Vector2(cursorOffset + 8, 0),
                        0)
                .layout(layout()
                        .sizing(FIXED, 2, FIXED, 12)),
                () -> {}
        );
    }

    public static void onKeyPressed(KeyEvent event) {
        if (activeTextInputId.isEmpty() || activeTextCallback == null) return;
        int keyCode = event.key();
        boolean ctrl = event.hasControlDown();
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE && cursorPos > 0) {
            activeText.deleteCharAt(cursorPos - 1);
            cursorPos--;
            activeTextCallback.accept(activeText.toString());
        } else if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE && cursorPos < activeText.length()) {
            activeText.deleteCharAt(cursorPos);
            activeTextCallback.accept(activeText.toString());
        } else if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT && cursorPos > 0) {
            cursorPos--;
        } else if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT && cursorPos < activeText.length()) {
            cursorPos++;
        } else if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER) {
            activeTextInputId = "";
            activeTextCallback = null;
        } else if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_V && ctrl) {
            String clipboard = McCompat.clipboard();
            if (clipboard != null && !clipboard.isEmpty()) {
                activeText.insert(cursorPos, clipboard);
                cursorPos += clipboard.length();
                activeTextCallback.accept(activeText.toString());
            }
        } else if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_C && ctrl) {
            McCompat.setClipboard(activeText.toString());
        }
    }

    public static void onCharTyped(char typedChar) {
        if (activeTextInputId.isEmpty() || activeTextCallback == null) return;
        if (StringUtil.isAllowedChatCharacter(typedChar)) {
            activeText.insert(cursorPos, typedChar);
            cursorPos++;
            activeTextCallback.accept(activeText.toString());
        }
    }

    private static class ScrollBounds {
        float containerHeight;
        float contentHeight;
        float scrollY;
    }
}