/*
 * ClayJ is licensed under the zlib/libpng license.
 * Copyright (c) 2026 Christian Steenkamp
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from the
 * use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not
 * claim that you wrote the original software. If you use this software in a
 * product, an acknowledgment in the product documentation would be
 * appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not
 * be misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package io.github.christechs.clayj.config;

import io.github.christechs.clayj.ClayJ;
import io.github.christechs.clayj.core.ElementId;
import io.github.christechs.clayj.enums.AttachToElement;
import io.github.christechs.clayj.enums.FloatingAttachPoint;
import io.github.christechs.clayj.math.Color;
import io.github.christechs.clayj.math.CornerRadius;
import io.github.christechs.clayj.math.Vector2;
import io.github.christechs.clayj.util.HashUtil;

public final class ElementDeclBuilder implements ConfigBuilder {
    public ElementId id = new ElementId();
    public LayoutConfigBuilder layout;
    public Color backgroundColor;
    public CornerRadius cornerRadius;
    public ImageConfigBuilder image;
    public FloatingConfigBuilder floating;
    public ScrollConfigBuilder scroll;
    public BorderConfigBuilder border;
    public Object userData;
    public CustomConfigBuilder custom;

    public ElementDeclBuilder() {
    }

    public void set(ElementDeclBuilder other) {
        this.id.set(other.id);
        this.layout = other.layout;

        if (other.backgroundColor != null) {
            if (this.backgroundColor == null) this.backgroundColor = new Color();
            this.backgroundColor.set(other.backgroundColor);
        } else {
            this.backgroundColor = null;
        }

        if (other.cornerRadius != null) {
            if (this.cornerRadius == null) this.cornerRadius = new CornerRadius();
            this.cornerRadius.set(other.cornerRadius);
        } else {
            this.cornerRadius = null;
        }

        this.image = other.image;
        this.floating = other.floating;
        this.scroll = other.scroll;
        this.border = other.border;
        this.userData = other.userData;
        this.custom = other.custom;
    }

    public ElementDeclBuilder id(CharSequence idString) {
        HashUtil.hashString(idString, 0, 0, this.id);
        return this;
    }

    public ElementDeclBuilder id(ElementId id) {
        this.id = id;
        return this;
    }

    public ElementDeclBuilder layout(LayoutConfigBuilder layout) {
        this.layout = layout;
        return this;
    }

    public ElementDeclBuilder bg(Color color) {
        if (this.backgroundColor == null) this.backgroundColor = new Color();
        this.backgroundColor.set(color.r, color.g, color.b, color.a);
        return this;
    }

    public ElementDeclBuilder bg(int rgb) {
        if (backgroundColor == null) backgroundColor = new Color();
        this.backgroundColor.set(rgb, rgb, rgb, 255);
        return this;
    }

    public ElementDeclBuilder bg(int r, int g, int b) {
        if (backgroundColor == null) backgroundColor = new Color();
        this.backgroundColor.set(r, g, b, 255);
        return this;
    }

    public ElementDeclBuilder bg(int r, int g, int b, int a) {
        if (backgroundColor == null) backgroundColor = new Color();
        this.backgroundColor.set(r, g, b, a);
        return this;
    }

    public ElementDeclBuilder radius(CornerRadius cornerRadius) {
        this.cornerRadius = cornerRadius;
        return this;
    }

    public ElementDeclBuilder radius(int radius) {
        if (this.cornerRadius == null) {
            this.cornerRadius = new CornerRadius();
        }
        this.cornerRadius.set(radius, radius, radius, radius);
        return this;
    }

    public ElementDeclBuilder image(ImageConfigBuilder image) {
        this.image = image;
        return this;
    }

    public ElementDeclBuilder floating(FloatingConfigBuilder floating) {
        this.floating = floating;
        return this;
    }

    public ElementDeclBuilder floating(
            AttachToElement attachTo,
            FloatingAttachPoint attachElement,
            FloatingAttachPoint attachParent,
            Vector2 offset,
            int zIndex) {
        return floating(attachTo, 0, attachElement, attachParent, offset, zIndex);
    }

    public ElementDeclBuilder floating(
            AttachToElement attachTo,
            int parentId,
            FloatingAttachPoint attachElement,
            FloatingAttachPoint attachParent,
            Vector2 offset,
            int zIndex) {
        if (this.floating == null) {
            this.floating = new FloatingConfigBuilder();
        }
        this.floating.attachTo = attachTo;
        this.floating.parentId = parentId;
        this.floating.attachElement = attachElement;
        this.floating.attachParent = attachParent;
        this.floating.offset.x = offset.x;
        this.floating.offset.y = offset.y;
        this.floating.zIndex = (short) zIndex;
        return this;
    }

    public ElementDeclBuilder scroll(ScrollConfigBuilder scroll) {
        this.scroll = scroll;
        return this;
    }

    public ElementDeclBuilder scroll(boolean horizontal, boolean vertical) {
        if (this.scroll == null) {
            this.scroll = new ScrollConfigBuilder();
        }
        this.scroll.horizontal = horizontal;
        this.scroll.vertical = vertical;
        return this;
    }

    public ElementDeclBuilder border(BorderConfigBuilder border) {
        this.border = border;
        return this;
    }

    public ElementDeclBuilder border(
            Color color, int left, int right, int top, int bottom, int between) {
        if (this.border == null) {
            this.border = new BorderConfigBuilder();
        }
        this.border.color = color;
        this.border.width.left = left;
        this.border.width.right = right;
        this.border.width.top = top;
        this.border.width.bottom = bottom;
        this.border.width.betweenChildren = between;
        return this;
    }

    public ElementDeclBuilder userData(Object userData) {
        this.userData = userData;
        return this;
    }

    public ElementDeclBuilder custom(Object customData) {
        if (this.custom == null) this.custom = ClayJ.getContext().transientCustoms.take();
        this.custom.customData = customData;
        return this;
    }

    public void reset() {
        this.id.reset();
        this.layout = null;
        this.backgroundColor = null;
        this.cornerRadius = null;
        this.image = null;
        this.floating = null;
        this.scroll = null;
        this.border = null;
        this.userData = null;
        this.custom = null;
    }
}
