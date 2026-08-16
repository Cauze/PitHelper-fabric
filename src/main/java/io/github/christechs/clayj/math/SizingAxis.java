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

import io.github.christechs.clayj.enums.SizingType;

public class SizingAxis {
    public final SizingMinMax minMax = new SizingMinMax();
    public float percent;
    public SizingType type;

    public SizingAxis() {
    }

    public SizingAxis(SizingType type) {
        this.type = type;
    }

    public SizingAxis(SizingType type, float percent) {
        this.type = type;
        this.percent = percent;
    }

    public SizingAxis(SizingType type, float min, float max) {
        this.type = type;
        this.minMax.min = min;
        this.minMax.max = max;
    }

    public void set(SizingAxis sizingAxis) {
        this.minMax.set(sizingAxis.minMax.min, sizingAxis.minMax.max);
        this.percent = sizingAxis.percent;
        this.type = sizingAxis.type;
    }
}
