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

package io.github.christechs.clayj.core;

import io.github.christechs.clayj.config.ScrollConfigBuilder;
import io.github.christechs.clayj.math.Dimensions;
import io.github.christechs.clayj.math.Vector2;

public class ScrollContainerData {
    public final Dimensions scrollContainerDimensions = new Dimensions();
    public final Dimensions contentDimensions = new Dimensions();
    public Vector2 scrollPosition;
    public ScrollConfigBuilder config;
    public boolean found;

    public void set(ScrollContainerData other) {
        this.scrollContainerDimensions.set(other.scrollContainerDimensions);
        this.contentDimensions.set(other.contentDimensions);

        if (other.scrollPosition != null) {
            if (this.scrollPosition == null) this.scrollPosition = new Vector2();
            this.scrollPosition.set(other.scrollPosition);
        } else {
            this.scrollPosition = null;
        }

        this.config = other.config;
        this.found = other.found;
    }

    public void reset() {
        scrollPosition = null;
        scrollContainerDimensions.width = 0;
        scrollContainerDimensions.height = 0;
        contentDimensions.width = 0;
        contentDimensions.height = 0;
        config = null;
        found = false;
    }
}
