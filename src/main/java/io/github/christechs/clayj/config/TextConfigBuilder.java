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

import io.github.christechs.clayj.enums.TextAlignment;
import io.github.christechs.clayj.enums.TextWrapMode;
import io.github.christechs.clayj.math.Color;

public final class TextConfigBuilder implements ConfigBuilder {
    public Color textColor = new Color(255, 255, 255, 255);
    public int fontId = 0;
    public int fontSize = 16;
    public int letterSpacing = 0;
    public int lineHeight = 0;
    public TextWrapMode wrapMode = TextWrapMode.WORDS;
    public TextAlignment textAlignment = TextAlignment.LEFT;
    public boolean hashStringContents = true;

    public void set(TextConfigBuilder other) {
        this.textColor.set(other.textColor);
        this.fontId = other.fontId;
        this.fontSize = other.fontSize;
        this.letterSpacing = other.letterSpacing;
        this.lineHeight = other.lineHeight;
        this.wrapMode = other.wrapMode;
        this.textAlignment = other.textAlignment;
        this.hashStringContents = other.hashStringContents;
    }

    public TextConfigBuilder color(Color textColor) {
        this.textColor.set(textColor.r, textColor.g, textColor.b, textColor.a);
        return this;
    }

    public TextConfigBuilder color(float rgb) {
        this.textColor.set(rgb, rgb, rgb, this.textColor.a);
        return this;
    }

    public TextConfigBuilder color(float r, float g, float b) {
        this.textColor.set(r, g, b, this.textColor.a);
        return this;
    }

    public TextConfigBuilder color(float r, float g, float b, float a) {
        this.textColor.set(r, g, b, a);
        return this;
    }

    public TextConfigBuilder r(float r) {
        this.textColor.r = r;
        return this;
    }

    public TextConfigBuilder g(float g) {
        this.textColor.g = g;
        return this;
    }

    public TextConfigBuilder b(float b) {
        this.textColor.b = b;
        return this;
    }

    public TextConfigBuilder a(float a) {
        this.textColor.a = a;
        return this;
    }

    public TextConfigBuilder fontId(int fontId) {
        this.fontId = fontId;
        return this;
    }

    public TextConfigBuilder size(int fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public TextConfigBuilder letterSpacing(int letterSpacing) {
        this.letterSpacing = letterSpacing;
        return this;
    }

    public TextConfigBuilder lineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
        return this;
    }

    public TextConfigBuilder wrap(TextWrapMode wrapMode) {
        this.wrapMode = wrapMode;
        return this;
    }

    public TextConfigBuilder align(TextAlignment alignment) {
        this.textAlignment = alignment;
        return this;
    }

    public void reset() {
        this.textColor.set(255, 255, 255, 255);
        this.fontId = 0;
        this.fontSize = 16;
        this.letterSpacing = 0;
        this.lineHeight = 0;
        this.wrapMode = TextWrapMode.WORDS;
        this.textAlignment = TextAlignment.LEFT;
        this.hashStringContents = true;
    }
}
