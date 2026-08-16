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

import io.github.christechs.clayj.math.BoundingBox;
import io.github.christechs.clayj.math.Dimensions;
import io.github.christechs.clayj.math.Vector2;

public class ScrollContainerDataInternal {
    public final BoundingBox boundingBox = new BoundingBox();
    public final Dimensions contentSize = new Dimensions();
    public final Vector2 scrollOrigin = new Vector2();
    public final Vector2 pointerOrigin = new Vector2();
    public final Vector2 scrollMomentum = new Vector2();
    public final Vector2 scrollPosition = new Vector2();
    public final Vector2 previousDelta = new Vector2();
    public LayoutElement layoutElement;
    public float momentumTime = 0f;
    public int elementId = 0;
    public boolean openThisFrame = false;
    public boolean pointerScrollActive = false;

    public void reset() {
        layoutElement = null;
        boundingBox.x = 0;
        boundingBox.y = 0;
        boundingBox.width = 0;
        boundingBox.height = 0;
        contentSize.width = 0;
        contentSize.height = 0;
        scrollOrigin.x = 0;
        scrollOrigin.y = 0;
        pointerOrigin.x = 0;
        pointerOrigin.y = 0;
        scrollMomentum.x = 0;
        scrollMomentum.y = 0;
        scrollPosition.x = 0;
        scrollPosition.y = 0;
        previousDelta.x = 0;
        previousDelta.y = 0;
        momentumTime = 0f;
        elementId = 0;
        openThisFrame = false;
        pointerScrollActive = false;
    }
}
