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
import io.github.christechs.clayj.enums.LayoutAlignmentX;
import io.github.christechs.clayj.enums.LayoutAlignmentY;
import io.github.christechs.clayj.enums.LayoutDirection;
import io.github.christechs.clayj.math.CornerRadius;
import io.github.christechs.pithelper.ui.xml.XmlContext;
import org.w3c.dom.Element;

import static io.github.christechs.clayj.ClayJ.*;

public class XmlLayoutNode extends XmlNode {
    String widthStr, heightStr, bg, dir, align, padding, gap, radius, scroll;

    public XmlLayoutNode(Element el) {
        super(el);
        widthStr = el.getAttribute("width");
        heightStr = el.getAttribute("height");
        bg = el.getAttribute("bg");
        dir = el.getAttribute("dir");
        align = el.getAttribute("align");
        padding = el.getAttribute("padding");
        gap = el.getAttribute("gap");
        radius = el.getAttribute("radius");
        scroll = el.getAttribute("scroll");
    }

    @Override
    public void render(XmlContext ctx) {
        if (!shouldRender(ctx)) return;

        ElementDeclBuilder declaration = decl();

        String resolvedId = ctx.resolveString(id);
        if (resolvedId != null && !resolvedId.trim().isEmpty()) {
            declaration.id(resolvedId);
        }

        String rBg = ctx.resolveString(bg);
        if (rBg != null && !rBg.isEmpty()) applyColor(declaration, rBg);

        String rRadius = ctx.resolveString(radius);
        if (rRadius != null && !rRadius.isEmpty()) {
            String[] r = rRadius.split(",");
            try {
                if (r.length == 1) declaration.radius(Integer.parseInt(r[0].trim()));
                else if (r.length == 4)
                    declaration.radius(new CornerRadius(Float.parseFloat(r[0].trim()), Float.parseFloat(r[1].trim()), Float.parseFloat(r[2].trim()), Float.parseFloat(r[3].trim())));
            } catch (Exception ignored) {
            }
        }

        String rScroll = ctx.resolveString(scroll);
        if (rScroll != null && !rScroll.isEmpty()) {
            String[] s = rScroll.split(",");
            if (s.length == 2) declaration.scroll(Boolean.parseBoolean(s[0].trim()), Boolean.parseBoolean(s[1].trim()));
        }

        LayoutConfigBuilder lyt = layout();
        applySizing(lyt, ctx.resolveString(widthStr), ctx.resolveString(heightStr));

        String rDir = ctx.resolveString(dir);
        if (rDir != null && !rDir.isEmpty()) {
            try {
                lyt.dir(LayoutDirection.valueOf(rDir));
            } catch (Exception ignored) {
            }
        }

        String rGap = ctx.resolveString(gap);
        if (rGap != null && !rGap.isEmpty()) {
            try {
                lyt.gap(Integer.parseInt(rGap));
            } catch (Exception ignored) {
            }
        }

        String rPadding = ctx.resolveString(padding);
        if (rPadding != null && !rPadding.isEmpty()) {
            String[] p = rPadding.split(",");
            try {
                if (p.length == 1) lyt.padding(Integer.parseInt(p[0].trim()), Integer.parseInt(p[0].trim()));
                else lyt.padding(Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()));
            } catch (Exception ignored) {
            }
        }

        String rAlign = ctx.resolveString(align);
        if (rAlign != null && !rAlign.isEmpty()) {
            String[] a = rAlign.split(",");
            try {
                lyt.align(LayoutAlignmentX.valueOf(a[0].trim()), LayoutAlignmentY.valueOf(a[1].trim()));
            } catch (Exception ignored) {
            }
        }

        declaration.layout(lyt);
        el(declaration, () -> {
            for (XmlNode child : children) child.render(ctx);
        });
    }
}