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

import io.github.christechs.clayj.enums.RenderCommandType;
import io.github.christechs.clayj.math.BoundingBox;

public class RenderCommand {
    public final BoundingBox boundingBox = new BoundingBox();
    public final RenderData renderData = new RenderData();
    public Object userData;
    public int id;
    public short zIndex;
    public RenderCommandType commandType = RenderCommandType.NONE;

    public void set(RenderCommand other) {
        this.boundingBox.set(other.boundingBox);
        this.renderData.set(other.renderData);
        this.userData = other.userData;
        this.id = other.id;
        this.zIndex = other.zIndex;
        this.commandType = other.commandType;
    }

    public void reset() {
        boundingBox.x = 0;
        boundingBox.y = 0;
        boundingBox.width = 0;
        boundingBox.height = 0;
        renderData.reset();
        userData = null;
        id = 0;
        zIndex = 0;
        commandType = RenderCommandType.NONE;
    }
}
