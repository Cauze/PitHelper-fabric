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

package io.github.christechs.clayj;

import io.github.christechs.clayj.core.RenderCommand;
import io.github.christechs.clayj.enums.ClayJError;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class LayoutResults implements Iterable<RenderCommand> {

    private static final RenderCommand FALLBACK_COMMAND = new RenderCommand();

    private final RenderCommand[] commands;
    private final int length;

    LayoutResults(RenderCommand[] commands, int length) {
        this.commands = commands;
        this.length = length;
    }

    public int length() {
        return length;
    }

    public RenderCommand get(int index) {
        if (index < 0 || index >= length) {
            ClayJContext context = ClayJ.getContext();
            if (context != null && context.errorHandler != null) {
                context.errorHandler.handleError(ClayJError.INTERNAL_ERROR);
            }
            return FALLBACK_COMMAND;
        }
        return commands[index];
    }

    public boolean isEmpty() {
        return length == 0;
    }

    @Override
    public Iterator<RenderCommand> iterator() {
        return new Iterator<RenderCommand>() {
            private int cursor = 0;

            @Override
            public boolean hasNext() {
                return cursor < length;
            }

            @Override
            public RenderCommand next() {
                if (!hasNext()) throw new NoSuchElementException();
                return commands[cursor++];
            }
        };
    }

    @Override
    public String toString() {
        return "LayoutResults{length=" + length + "}";
    }
}