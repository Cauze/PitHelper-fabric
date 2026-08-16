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

import io.github.christechs.pithelper.ui.xml.nodes.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static io.github.christechs.pithelper.PitHelper.PH_LOGGER;

public class ClayXmlEngine {

    private static final Map<String, XmlNode> CACHE = new HashMap<>();

    public static XmlNode load(String resourcePath) {
        if (CACHE.containsKey(resourcePath)) return CACHE.get(resourcePath);
        try {
            Identifier xmlLoc = Identifier.fromNamespaceAndPath("pithelper", resourcePath);
            InputStream stream = Minecraft.getInstance().getResourceManager().open(xmlLoc);
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
            doc.getDocumentElement().normalize();
            XmlNode rootNode = parseNode(doc.getDocumentElement());
            CACHE.put(resourcePath, rootNode);
            return rootNode;
        } catch (Exception e) {
            PH_LOGGER.error("Failed to load XML UI: {}", resourcePath);
            return null;
        }
    }

    private static XmlNode parseNode(Node domNode) {
        if (domNode.getNodeType() != Node.ELEMENT_NODE) return null;
        Element el = (Element) domNode;
        String tag = el.getTagName();

        XmlNode node = createNodeInstance(tag, el);
        if (node != null) {
            NodeList children = el.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                XmlNode child = parseNode(children.item(i));
                if (child != null) node.children.add(child);
            }
        }
        return node;
    }

    private static XmlNode createNodeInstance(String tag, Element el) {
        switch (tag) {
            case "Screen":
            case "Element":
                return new XmlLayoutNode(el);
            case "Text":
                return new XmlTextNode(el);
            case "Button":
                return new XmlButtonNode(el);
            case "Tab":
                return new XmlTabNode(el);
            case "Custom":
                return new XmlCustomNode(el);
            case "Input":
                return new XmlInputNode(el);
            case "Scrollbar":
                return new XmlScrollbarNode(el);
            default:
                return null;
        }
    }
}