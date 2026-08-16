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

import io.github.christechs.clayj.math.BorderWidth;
import io.github.christechs.clayj.math.Color;

public final class BorderConfigBuilder implements ConfigBuilder {
    public Color color = new Color(255, 255, 255, 255);
    public BorderWidth width = new BorderWidth();

    public void set(BorderConfigBuilder other) {
        this.color.set(other.color.r, other.color.g, other.color.b, other.color.a);
        this.width.set(
                other.width.left,
                other.width.right,
                other.width.top,
                other.width.bottom,
                other.width.betweenChildren);
    }

    public BorderConfigBuilder color(Color color) {
        this.color.set(color.r, color.g, color.b, color.a);
        return this;
    }

    public BorderConfigBuilder color(float rgb) {
        this.color.set(rgb, rgb, rgb, this.color.a);
        return this;
    }

    public BorderConfigBuilder color(float r, float g, float b) {
        this.color.set(r, g, b, this.color.a);
        return this;
    }

    public BorderConfigBuilder color(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
        return this;
    }

    public BorderConfigBuilder r(float r) {
        this.color.r = r;
        return this;
    }

    public BorderConfigBuilder g(float g) {
        this.color.g = g;
        return this;
    }

    public BorderConfigBuilder b(float b) {
        this.color.b = b;
        return this;
    }

    public BorderConfigBuilder a(float a) {
        this.color.a = a;
        return this;
    }

    public BorderConfigBuilder width(int all) {
        this.width.left = all;
        this.width.right = all;
        this.width.top = all;
        this.width.bottom = all;
        return this;
    }

    public BorderConfigBuilder width(int left, int right, int top, int bottom) {
        this.width.left = left;
        this.width.right = right;
        this.width.top = top;
        this.width.bottom = bottom;
        return this;
    }

    public BorderConfigBuilder width(int left, int right, int top, int bottom, int betweenChildren) {
        this.width.left = left;
        this.width.right = right;
        this.width.top = top;
        this.width.bottom = bottom;
        this.width.betweenChildren = betweenChildren;
        return this;
    }

    public BorderConfigBuilder left(int left) {
        this.width.left = left;
        return this;
    }

    public BorderConfigBuilder right(int right) {
        this.width.right = right;
        return this;
    }

    public BorderConfigBuilder top(int top) {
        this.width.top = top;
        return this;
    }

    public BorderConfigBuilder bottom(int bottom) {
        this.width.bottom = bottom;
        return this;
    }

    public BorderConfigBuilder betweenChildren(int betweenChildren) {
        this.width.betweenChildren = betweenChildren;
        return this;
    }

    public void reset() {
        this.color.set(255, 255, 255, 255);
        this.width.set(0, 0, 0, 0, 0);
    }
}
