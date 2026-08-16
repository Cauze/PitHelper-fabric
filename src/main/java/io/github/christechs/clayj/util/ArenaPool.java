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

package io.github.christechs.clayj.util;

import io.github.christechs.clayj.ClayJ;
import io.github.christechs.clayj.ClayJContext;
import io.github.christechs.clayj.enums.ClayJError;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ArenaPool<T> {
    private final T[] pool;
    private final Consumer<T> resetAction;
    private final T fallbackInstance;
    private int cursor = 0;

    @SuppressWarnings("unchecked")
    public ArenaPool(int capacity, Supplier<T> factory, Consumer<T> resetAction) {
        this.pool = (T[]) new Object[capacity];
        this.resetAction = resetAction;
        for (int i = 0; i < capacity; i++) {
            this.pool[i] = factory.get();
        }
        this.fallbackInstance = factory.get();
    }

    public ArenaPool(int capacity, Supplier<T> factory) {
        this(capacity, factory, null);
    }

    public T take() {
        if (cursor >= pool.length) {
            ClayJContext context = ClayJ.getContext();
            if (context != null && context.errorHandler != null) {
                context.errorHandler.handleError(ClayJError.ARENA_CAPACITY_EXCEEDED);
            }
            if (resetAction != null) {
                resetAction.accept(fallbackInstance);
            }
            return fallbackInstance;
        }

        T item = pool[cursor++];

        if (resetAction != null) {
            resetAction.accept(item);
        }

        return item;
    }

    public void reset() {
        cursor = 0;
    }

    public int capacity() {
        return pool.length;
    }
}