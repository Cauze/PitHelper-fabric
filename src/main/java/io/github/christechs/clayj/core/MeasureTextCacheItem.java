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

import io.github.christechs.clayj.math.Dimensions;

public class MeasureTextCacheItem {
    public final Dimensions unwrappedDimensions = new Dimensions();
    public int measureWordsStartIndex = 0;
    public boolean containsNewlines = false;

    public int id = 0;
    public int nextIndex = 0;
    public int generation = 0;

    public void set(MeasureTextCacheItem other) {
        this.unwrappedDimensions.set(other.unwrappedDimensions);
        this.measureWordsStartIndex = other.measureWordsStartIndex;
        this.containsNewlines = other.containsNewlines;
        this.id = other.id;
        this.nextIndex = other.nextIndex;
        this.generation = other.generation;
    }

    public void reset() {
        unwrappedDimensions.width = 0;
        unwrappedDimensions.height = 0;
        measureWordsStartIndex = 0;
        containsNewlines = false;
        id = 0;
        nextIndex = 0;
        generation = 0;
    }
}
