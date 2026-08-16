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

package io.github.christechs.clayj.math;

public class Sizing {
    public final SizingAxis width = new SizingAxis();
    public final SizingAxis height = new SizingAxis();

    public Sizing() {
    }

    public Sizing(SizingAxis width, SizingAxis height) {
        set(width, height);
    }

    public void set(Sizing other) {
        this.width.set(other.width);
        this.height.set(other.height);
    }

    public void set(SizingAxis width, SizingAxis height) {
        this.width.set(width);
        this.height.set(height);
    }

    public SizingAxis sizingAxis(boolean xAxis) {
        return xAxis ? width : height;
    }

    public Dimensions clamp(Dimensions d) {
        d.height = clampHeight(d.height);
        d.width = clampWidth(d.width);
        return d;
    }

    public float clampWidth(float w) {
        return Math.max(width.minMax.min, Math.min(width.minMax.max, w));
    }

    public float clampHeight(float h) {
        return Math.max(height.minMax.min, Math.min(height.minMax.max, h));
    }
}
