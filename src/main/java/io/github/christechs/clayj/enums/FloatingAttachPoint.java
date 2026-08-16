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

package io.github.christechs.clayj.enums;

public enum FloatingAttachPoint {
    LEFT_TOP,
    LEFT_CENTER,
    LEFT_BOTTOM,
    CENTER_TOP,
    CENTER_CENTER,
    CENTER_BOTTOM,
    RIGHT_TOP,
    RIGHT_CENTER,
    RIGHT_BOTTOM;

    public boolean attachLeft() {
        return this == LEFT_TOP || this == LEFT_CENTER || this == LEFT_BOTTOM;
    }

    public boolean attachRight() {
        return this == RIGHT_TOP || this == RIGHT_CENTER || this == RIGHT_BOTTOM;
    }

    public boolean attachHorizontalCenter() {
        return this == CENTER_TOP || this == CENTER_CENTER || this == CENTER_BOTTOM;
    }

    public boolean attachTop() {
        return this == LEFT_TOP || this == CENTER_TOP || this == RIGHT_TOP;
    }

    public boolean attachBottom() {
        return this == LEFT_BOTTOM || this == CENTER_BOTTOM || this == RIGHT_BOTTOM;
    }

    public boolean attachVerticalCenter() {
        return this == LEFT_CENTER || this == CENTER_CENTER || this == RIGHT_CENTER;
    }
}
