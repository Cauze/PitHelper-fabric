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

public class XmlTabNode extends XmlNode {
    String text, active, onClick;

    public XmlTabNode(Element el) {
        super(el);
        text = el.getAttribute("text");
        active = el.getAttribute("active");
        onClick = el.getAttribute("onClick");
    }

    @Override
    public void render(XmlContext ctx) {
        if (!shouldRender(ctx)) return;
        ClayComponents.tab(
                ctx.resolveString(id),
                ctx.resolveString(text),
                ctx.resolveBoolean(active),
                ctx.getCallback(onClick));
    }
}
