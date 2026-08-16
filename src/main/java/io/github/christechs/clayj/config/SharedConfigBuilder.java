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

import io.github.christechs.clayj.math.Color;
import io.github.christechs.clayj.math.CornerRadius;

public final class SharedConfigBuilder implements ConfigBuilder {
    public Color backgroundColor;
    public CornerRadius cornerRadius;
    public Object userData;

    public void set(SharedConfigBuilder other) {
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

        this.userData = other.userData;
    }

    public SharedConfigBuilder bg(Color color) {
        this.backgroundColor = color;
        return this;
    }

    public SharedConfigBuilder radius(CornerRadius radius) {
        this.cornerRadius = radius;
        return this;
    }

    public SharedConfigBuilder userData(Object data) {
        this.userData = data;
        return this;
    }

    public void reset() {
        this.backgroundColor = null;
        this.cornerRadius = null;
        this.userData = null;
    }
}
