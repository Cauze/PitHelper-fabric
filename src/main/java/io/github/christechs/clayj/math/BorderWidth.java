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

public class BorderWidth {
    public int left;
    public int right;
    public int top;
    public int bottom;
    public int betweenChildren;

    public BorderWidth() {
    }

    public BorderWidth(int left, int right, int top, int bottom, int betweenChildren) {
        set(left, right, top, bottom, betweenChildren);
    }

    public void set(BorderWidth other) {
        set(other.left, other.right, other.top, other.bottom, other.betweenChildren);
    }

    public void set(int left, int right, int top, int bottom, int betweenChildren) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
        this.betweenChildren = betweenChildren;
    }
}
