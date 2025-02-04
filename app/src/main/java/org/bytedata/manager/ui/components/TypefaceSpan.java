package org.bytedata.manager.ui.editor.components;

/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

import org.bytedata.manager.utils.AndroidUtilities;


public class TypefaceSpan extends MetricAffectingSpan {

    private final Typeface typeface;
    private int textSize;
    private int color;
    private int colorKey = -1;

    public TypefaceSpan(Typeface tf) {
        typeface = tf;
    }

    public TypefaceSpan(Typeface tf, int size) {
        typeface = tf;
        textSize = size;
    }

    public TypefaceSpan(Typeface tf, int size, int color) {
        typeface = tf;
        if (size > 0) {
            textSize = size;
        }
        this.colorKey = colorKey;
        color = color;
    }

    public Typeface getTypeface() {
        return typeface;
    }

    public void setColor(int value) {
        color = value;
    }

    public boolean isMono() {
        return typeface == Typeface.MONOSPACE;
    }

    public boolean isBold() {
        return typeface == AndroidUtilities.getTypeface("fonts/rmedium.ttf");
    }

    public boolean isItalic() {
        return typeface == AndroidUtilities.getTypeface("fonts/ritalic.ttf");
    }

    @Override
    public void updateMeasureState(TextPaint p) {
        if (typeface != null) {
            p.setTypeface(typeface);
        }
        if (textSize != 0) {
            p.setTextSize(textSize);
        }
        p.setFlags(p.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        if (typeface != null) {
            tp.setTypeface(typeface);
        }
        if (textSize != 0) {
            tp.setTextSize(textSize);
        }
        if (color != 0) {
            tp.setColor(color);
        }
        tp.setFlags(tp.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }
}
