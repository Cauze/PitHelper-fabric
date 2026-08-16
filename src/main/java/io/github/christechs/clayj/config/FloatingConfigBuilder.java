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

import io.github.christechs.clayj.enums.AttachToElement;
import io.github.christechs.clayj.enums.FloatingAttachPoint;
import io.github.christechs.clayj.enums.PointerCaptureMode;
import io.github.christechs.clayj.math.Dimensions;
import io.github.christechs.clayj.math.Vector2;

public final class FloatingConfigBuilder implements ConfigBuilder {
    public Vector2 offset = new Vector2();
    public Dimensions expand = new Dimensions();
    public int parentId = 0;
    public short zIndex = 0;
    public FloatingAttachPoint attachElement = FloatingAttachPoint.LEFT_TOP;
    public FloatingAttachPoint attachParent = FloatingAttachPoint.LEFT_TOP;
    public PointerCaptureMode captureMode = PointerCaptureMode.CAPTURE;
    public AttachToElement attachTo = AttachToElement.NONE;

    public void set(FloatingConfigBuilder other) {
        this.offset.set(other.offset);
        this.expand.set(other.expand);
        this.parentId = other.parentId;
        this.zIndex = other.zIndex;
        this.attachElement = other.attachElement;
        this.attachParent = other.attachParent;
        this.captureMode = other.captureMode;
        this.attachTo = other.attachTo;
    }

    public FloatingConfigBuilder offset(float x, float y) {
        this.offset.x = x;
        this.offset.y = y;
        return this;
    }

    public FloatingConfigBuilder expand(float width, float height) {
        this.expand.width = width;
        this.expand.height = height;
        return this;
    }

    public FloatingConfigBuilder parentId(int parentId) {
        this.parentId = parentId;
        return this;
    }

    public FloatingConfigBuilder zIndex(short zIndex) {
        this.zIndex = zIndex;
        return this;
    }

    public FloatingConfigBuilder attach(FloatingAttachPoint element, FloatingAttachPoint parent) {
        this.attachElement = element;
        this.attachParent = parent;
        return this;
    }

    public FloatingConfigBuilder captureMode(PointerCaptureMode mode) {
        this.captureMode = mode;
        return this;
    }

    public FloatingConfigBuilder attachTo(AttachToElement target) {
        this.attachTo = target;
        return this;
    }

    public void reset() {
        this.offset.x = 0;
        this.offset.y = 0;
        this.expand.width = 0;
        this.expand.height = 0;
        this.parentId = 0;
        this.zIndex = 0;
        this.attachElement = FloatingAttachPoint.LEFT_TOP;
        this.attachParent = FloatingAttachPoint.LEFT_TOP;
        this.captureMode = PointerCaptureMode.CAPTURE;
        this.attachTo = AttachToElement.NONE;
    }
}
