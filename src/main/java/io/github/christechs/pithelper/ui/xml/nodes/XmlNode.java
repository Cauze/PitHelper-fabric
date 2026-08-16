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

package io.github.christechs.pithelper.ui.xml.nodes;

import io.github.christechs.clayj.config.ElementDeclBuilder;
import io.github.christechs.clayj.config.LayoutConfigBuilder;
import io.github.christechs.clayj.config.TextConfigBuilder;
import io.github.christechs.clayj.enums.SizingType;
import io.github.christechs.pithelper.ui.xml.XmlContext;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public abstract class XmlNode {
    public String id;
    public String condition;
    public List<XmlNode> children = new ArrayList<>();

    public XmlNode(Element el) {
        this.id = el.getAttribute("id");
        this.condition = el.getAttribute("if");
    }

    protected boolean shouldRender(XmlContext ctx) {
        if (condition == null || condition.isEmpty()) return true;
        return ctx.resolveBoolean(condition);
    }

    public abstract void render(XmlContext ctx);

    protected void applySizing(LayoutConfigBuilder lyt, String w, String h) {
        SizingType typeX = SizingType.FIT;
        float valX = 0;
        SizingType typeY = SizingType.FIT;
        float valY = 0;

        if (w != null) {
            if (w.equals("GROW")) typeX = SizingType.GROW;
            else if (!w.isEmpty() && !w.equals("FIT")) {
                typeX = SizingType.FIXED;
                try {
                    valX = Float.parseFloat(w);
                } catch (Exception ignored) {
                }
            }
        }

        if (h != null) {
            if (h.equals("GROW")) typeY = SizingType.GROW;
            else if (!h.isEmpty() && !h.equals("FIT")) {
                typeY = SizingType.FIXED;
                try {
                    valY = Float.parseFloat(h);
                } catch (Exception ignored) {
                }
            }
        }

        lyt.sizing(typeX, valX, typeY, valY);
    }

    protected void applyColor(ElementDeclBuilder decl, String hex) {
        try {
            int[] rgba = hexToRgba(hex);
            decl.bg(rgba[0], rgba[1], rgba[2], rgba[3]);
        } catch (Exception ignored) {
        }
    }

    protected void applyTextColor(TextConfigBuilder txt, String hex) {
        try {
            int[] rgba = hexToRgba(hex);
            txt.color((float) rgba[0], (float) rgba[1], (float) rgba[2], (float) rgba[3]);
        } catch (Exception ignored) {
        }
    }

    protected int[] hexToRgba(String hex) {
        hex = hex.replace("#", "");
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        int a = hex.length() == 8 ? Integer.parseInt(hex.substring(6, 8), 16) : 255;
        return new int[]{r, g, b, a};
    }
}