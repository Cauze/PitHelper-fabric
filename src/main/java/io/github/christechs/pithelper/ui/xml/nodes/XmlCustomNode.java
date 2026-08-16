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

import io.github.christechs.pithelper.ui.xml.XmlContext;
import org.w3c.dom.Element;

public class XmlCustomNode extends XmlNode {

    public XmlCustomNode(Element el) {
        super(el);
    }

    @Override
    public void render(XmlContext ctx) {
        if (!shouldRender(ctx)) return;
        String resolvedId = ctx.resolveString(id);
        Runnable r = ctx.getCustomRenderer(resolvedId);
        if (r != null) r.run();
    }
}
