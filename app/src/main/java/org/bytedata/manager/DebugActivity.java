package org.bytedata.manager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import org.bytedata.manager.utils.LayoutHelper;

import java.util.HashMap;
import java.util.Map;

public class DebugActivity extends Activity {

    private String errorMessage = "";

    private static final Map<String, String> exceptionMap = new HashMap<String, String>() {{
        put("StringIndexOutOfBoundsException", "Invalid string operation\n");
        put("IndexOutOfBoundsException", "Invalid list operation\n");
        put("ArithmeticException", "Invalid arithmetical operation\n");
        put("NumberFormatException", "Invalid toNumber block operation\n");
        put("ActivityNotFoundException", "Invalid intent operation\n");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SpannableStringBuilder formattedMessage = new SpannableStringBuilder();
        Intent intent = getIntent();

        if (intent != null) {
            errorMessage = intent.getStringExtra("error");
        }

        if (errorMessage != null && !errorMessage.isEmpty()) {
            String[] split = errorMessage.split("\n");

            String exceptionType = split[0];
            String message = exceptionMap.getOrDefault(exceptionType, "");

            if (message != null && !message.isEmpty()) {
                formattedMessage.append(message);
            }

            for (int i = 1; i < split.length; i++) {
                formattedMessage.append(split[i]);
                formattedMessage.append("\n");
            }
        } else {
            formattedMessage.append("No error message available.");
        }

        setTitle(getTitle() + " Crashed");

        TextView errorView = new TextView(this);
        errorView.setText(formattedMessage);
        errorView.setTextIsSelectable(true);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setPadding(16, 16, 16, 16);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        layoutParams.setMargins(16, 8, 16, 8);

        MaterialButton button = new MaterialButton(this);
        button.setText("Close");
        button.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        button.setLayoutParams(layoutParams);
        button.setOnClickListener(view -> {
            finish();
        });
        linearLayout.addView(button);

        MaterialButton button2 = new MaterialButton(this);
        button2.setText("Send log");
        button2.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        button2.setLayoutParams(layoutParams);
        button2.setOnClickListener(view -> {
            finish();
        });
        linearLayout.addView(button2);

        HorizontalScrollView hscroll = new HorizontalScrollView(this);
        ScrollView vscroll = new ScrollView(this);

        hscroll.addView(vscroll);
        vscroll.addView(errorView);

        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.addView(hscroll);

        frameLayout.addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM));

        setContentView(frameLayout);
    }
}
