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

public class Padding {
    public int left;
    public int right;
    public int top;
    public int bottom;

    public Padding() {
    }

    public Padding(int left, int right, int top, int bottom) {
        set(left, right, top, bottom);
    }

    public Padding(int padding) {
        set(padding, padding, padding, padding);
    }

    public void set(Padding other) {
        set(other.left, other.right, other.top, other.bottom);
    }

    public void set(int left, int right, int top, int bottom) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    public int vertical() {
        return top + bottom;
    }

    public int horizontal() {
        return left + right;
    }

    public float sizeAxis(boolean xAxis) {
        return xAxis ? (float) horizontal() : (float) vertical();
    }
}
