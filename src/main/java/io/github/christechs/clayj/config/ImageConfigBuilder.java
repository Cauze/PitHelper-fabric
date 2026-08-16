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

import io.github.christechs.clayj.math.Dimensions;

public final class ImageConfigBuilder implements ConfigBuilder {
    public Object imageData;
    public Dimensions sourceDimensions = new Dimensions();

    public ImageConfigBuilder data(Object data) {
        this.imageData = data;
        return this;
    }

    public ImageConfigBuilder sourceDim(float width, float height) {
        this.sourceDimensions.width = width;
        this.sourceDimensions.height = height;
        return this;
    }

    public void set(ImageConfigBuilder other) {
        set(other.imageData, other.sourceDimensions);
    }

    public void set(Object imageData, Dimensions sourceDimensions) {
        this.imageData = imageData;
        this.sourceDimensions.set(sourceDimensions);
    }

    public void reset() {
        this.imageData = null;
        this.sourceDimensions.width = 0;
        this.sourceDimensions.height = 0;
    }
}
