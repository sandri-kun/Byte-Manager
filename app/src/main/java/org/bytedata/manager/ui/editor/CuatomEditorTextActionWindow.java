package org.bytedata.manager.ui.editor;

import android.annotation.SuppressLint;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;

import org.bytedata.manager.R;
import org.bytedata.manager.utils.AndroidUtilities;

import io.github.rosemoe.sora.widget.component.EditorTextActionWindow;

public class CuatomEditorTextActionWindow extends EditorTextActionWindow {

    private final CodeEditorView editor;

    private final ImageButton selectAll;
    private final ImageButton pasteBtn;
    private final ImageButton copyBtn;
    private final ImageButton cutBtn;
    private final ImageButton longSelectBtn;

    private final ImageButton searchBtn;
    private final ImageButton translateBtn;

    public CuatomEditorTextActionWindow(CodeEditorView editor) {
        super(editor);
        this.editor = editor;

        @SuppressLint("InflateParams")
        View root = getView();

        selectAll = root.findViewById(R.id.panel_btn_select_all);
        cutBtn = root.findViewById(R.id.panel_btn_cut);
        copyBtn = root.findViewById(R.id.panel_btn_copy);
        longSelectBtn = root.findViewById(R.id.panel_btn_long_select);
        pasteBtn = root.findViewById(R.id.panel_btn_paste);
        searchBtn = root.findViewById(R.id.panel_btn_search);
        translateBtn = root.findViewById(R.id.panel_btn_translate);

        translateBtn.setOnClickListener(this);
        searchBtn.setOnClickListener(this);

        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(5 * editor.getDpUnit());
        gd.setColor(0xD9424242);
        root.setBackground(gd);
    }

    @Override
    public void displayWindow() {
        updateBtnState();
        super.displayWindow();
        setSize(getView().getMeasuredWidth(), getHeight());
    }

    private void updateBtnState() {
        searchBtn.setVisibility(editor.getCursor().isSelected() ? View.VISIBLE : View.GONE);
        translateBtn.setVisibility(editor.getCursor().isSelected() ? View.VISIBLE : View.GONE);

        /*selectAll.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.MULTIPLY));
        cutBtn.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.MULTIPLY));
        copyBtn.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.MULTIPLY));
        longSelectBtn.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.MULTIPLY));
        pasteBtn.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.MULTIPLY));
        searchBtn.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.MULTIPLY));
        translateBtn.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.MULTIPLY));*/
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.panel_btn_translate) {
            AndroidUtilities.showToast("translate");
        } else if (id == R.id.panel_btn_search) {
            if (!TextUtils.isEmpty(editor.getSelectedText())) {
                AndroidUtilities.showToast(editor.getSelectedText());
            }
            AndroidUtilities.showToast("null");
        }
        super.onClick(view);
    }
}