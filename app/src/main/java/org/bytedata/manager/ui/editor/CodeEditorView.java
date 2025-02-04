package org.bytedata.manager.ui.editor;

import android.content.Context;
import android.graphics.Canvas;
import android.os.TransactionTooLargeException;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.I18nConfig;
import io.github.rosemoe.sora.R;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.component.EditorTextActionWindow;

public class CodeEditorView extends CodeEditor {

    private static final String LOG_TAG = "CodeEditorView";

    public CodeEditorView(Context context) {
        this(context, null);
    }

    public CodeEditorView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.codeEditorStyle);
    }

    public CodeEditorView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CodeEditorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        replaceComponent(EditorTextActionWindow.class, new CuatomEditorTextActionWindow(this));
    }

    protected String getSelectedText() {
        if (getCursor().isSelected()) {
            return getSelectedText(getText(), getCursor().getLeft(), getCursor().getRight());
        }
        return null;
    }

    protected String getSelectedText(@NonNull CharSequence text, int start, int end) {
        if (end < start) {
            return null;
        }
        if (end - start > getProps().clipboardTextLengthLimit) {
            Toast.makeText(getContext(), I18nConfig.getResourceId(R.string.sora_editor_clip_text_length_too_large), Toast.LENGTH_SHORT).show();
            return null;
        }
        try {
            var clip = (text instanceof Content) ? ((Content) text).substring(start, end) : text.subSequence(start, end).toString();
            return clip;
        } catch (RuntimeException e) {
            if (e.getCause() instanceof TransactionTooLargeException) {
                Toast.makeText(getContext(), I18nConfig.getResourceId(R.string.sora_editor_clip_text_length_too_large), Toast.LENGTH_SHORT).show();
            } else {
                Log.w(LOG_TAG, e);
                Toast.makeText(getContext(), e.getClass().toString(), Toast.LENGTH_SHORT).show();
            }
        }
        return null;
    }

    public void insert(int line, int column, String string) {
        getText().insert(line, column, string);
    }

    public void delete(int startLine, int startColumn, int endLine, int endColumn) {
        getText().delete(startLine, startColumn, endLine, endColumn);
    }

    public void delete(int startIndex, int endIndex) {
        getText().delete(startIndex, endIndex);
    }

    public void replace(int line, int column, int endLine, int endColumn, String string) {
        getText().replace(line, column, endLine, endColumn, string);
    }

    public void setSelectionRegion(int startIndex, int endIndex) {
        CharPosition start = getText().getIndexer().getCharPosition(startIndex);
        CharPosition end = getText().getIndexer().getCharPosition(endIndex);
        super.setSelectionRegion(start.getLine(),
                start.getColumn(),
                end.getLine(),
                end.getColumn());
    }

    public void beginBatchEdit() {
        getText().beginBatchEdit();
    }

    public void endBatchEdit() {
        getText().endBatchEdit();
    }

    public Content getContent() {
        return (Content) getText();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
