package com.mega.scenemode.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.mega.scenemode.R;

import mega.log.MLog;

public class ConfirmDialog extends DialogFragment implements View.OnClickListener {
    public @Nullable Button positiveButton;
    public @Nullable Button negativeButton;
    public @Nullable TextView title;
    public @Nullable TextView hint;
    public @Nullable String hintText;
    public @Nullable String titleText;
    public @Nullable String positiveText;
    public @Nullable String negativeText;
    public int titleVisible;
    public int positiveVisible;
    public int negativeVisible;
    public @Nullable
    OnClickListener clickListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MLog.d("onCreate()");
        setStyle(STYLE_NORMAL, R.style.AppTheme_translucent);
    }

    @Override
    public @NonNull View onCreateView(@NonNull LayoutInflater inflater,
          @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        MLog.d("onCreateView()");
        View view = inflater.inflate(R.layout.confirm_dialog, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(false);
        }
        initView(view);
        initListener();
        return view;
    }

    public void initView(@NonNull View view) {
        positiveButton = view.findViewById(R.id.dialog_positive);
        negativeButton = view.findViewById(R.id.dialog_negative);
        title = view.findViewById(R.id.dialog_title);
        hint = view.findViewById(R.id.dialog_message);
        if (positiveButton != null) {
            positiveButton.setVisibility(positiveVisible);
            if (positiveText != null) {
                positiveButton.setText(positiveText);
            }
        }
        if (negativeButton != null) {
            negativeButton.setVisibility(negativeVisible);
            if (negativeText != null) {
                negativeButton.setText(negativeText);
            }
        }

        if (hint != null && hintText != null) {
            hint.setText(hintText);
        }
        if (title != null) {
            title.setVisibility(titleVisible);
            if (titleText != null) {
                title.setText(titleText);
            }
        }
    }

    public void initListener() {
        if (negativeButton != null) {
            negativeButton.setOnClickListener(this);
        }

        if (positiveButton != null) {
            positiveButton.setOnClickListener(this);
        }
    }

    public void setTitle(@Nullable String title) {
        if (this.title != null) {
            this.title.setText(title);
        } else {
            titleText = title;
        }
    }

    public void setHint(@Nullable String hint) {
        if (this.hint != null) {
            this.hint.setText(hint);
        } else {
            hintText = hint;
        }
    }

    public void setPositiveText(@Nullable String hint) {
        if (positiveButton != null) {
            positiveButton.setText(hint);
        } else {
            positiveText = hint;
        }
    }

    public void setNegativeText(@Nullable String hint) {
        if (negativeButton != null) {
            negativeButton.setText(hint);
        } else {
            negativeText = hint;
        }
    }

    public void setTitleVisible(int visible) {
        if (title != null) {
            title.setVisibility(visible);
        } else {
            titleVisible = visible;
        }
    }

    public void setPositiveVisible(int visible) {
        if (positiveButton != null) {
            positiveButton.setVisibility(visible);
        } else {
            positiveVisible = visible;
        }
    }

    public void setNegativeVisible(int visible) {
        if (negativeButton != null) {
            negativeButton.setVisibility(visible);
        } else {
            negativeVisible = visible;
        }
    }

    public interface OnClickListener {
        void onPositiveClick();

        void onNegativeClick();
    }

    @Override
    public void onClick(@NonNull View v) {
        int id = v.getId();
        if (id == R.id.dialog_positive) {
            MLog.d("onPositiveClick()");
            if (clickListener != null) {
                clickListener.onPositiveClick();
            }
        } else if (id == R.id.dialog_negative) {
            MLog.d("onNegativeClick()");
            if (clickListener != null) {
                clickListener.onNegativeClick();
            }
        }
        dismiss();
    }

    public void setOnClickListener(@NonNull OnClickListener listener) {
        clickListener = listener;
    }
}