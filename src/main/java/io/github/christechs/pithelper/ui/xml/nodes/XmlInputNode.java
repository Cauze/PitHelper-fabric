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

import io.github.christechs.pithelper.ui.components.ClayComponents;
import io.github.christechs.pithelper.ui.xml.XmlContext;
import org.w3c.dom.Element;

public class XmlInputNode extends XmlNode {
    String placeholder, bind, widthStr;

    public XmlInputNode(Element el) {
        super(el);
        placeholder = el.getAttribute("placeholder");
        bind = el.getAttribute("bind");
        widthStr = el.getAttribute("width");
    }

    @Override
    public void render(XmlContext ctx) {
        if (!shouldRender(ctx)) return;

        float width = -1;
        String rw = ctx.resolveString(widthStr);
        if (rw != null && !rw.isEmpty()) {
            try {
                width = Float.parseFloat(rw);
            } catch (Exception ignored) {
            }
        }

        String varName = bind.replace("${", "").replace("}", "");

        ClayComponents.textInput(
                ctx.resolveString(id),
                ctx.resolveString(placeholder),
                ctx.resolveString("${" + varName + "}"),
                width,
                ctx.getStringSetter(varName));
    }
}
