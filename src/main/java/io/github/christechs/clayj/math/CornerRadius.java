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

public class CornerRadius {
    public float topLeft;
    public float topRight;
    public float bottomLeft;
    public float bottomRight;

    public CornerRadius() {
    }

    public CornerRadius(float radius) {
        set(radius, radius, radius, radius);
    }

    public CornerRadius(float topLeft, float topRight, float bottomLeft, float bottomRight) {
        set(topLeft, topRight, bottomLeft, bottomRight);
    }

    public void set(CornerRadius other) {
        set(other.topLeft, other.topRight, other.bottomLeft, other.bottomRight);
    }

    public void set(float topLeft, float topRight, float bottomLeft, float bottomRight) {
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomLeft = bottomLeft;
        this.bottomRight = bottomRight;
    }
}
