package com.deplink.boruSmart.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

public class ActionSheetDialog {
    private Context context;
    private Dialog dialog;
    private LinearLayout lLayout_content;
    private List<SheetItem> sheetItemList;

    public ActionSheetDialog(Context context) {
        this.context = context;
    }

    public ActionSheetDialog builder() {
        View view = LayoutInflater.from(context).inflate(
                R.layout.view_actionsheet, null);
        float scale = context.getResources().getDisplayMetrics().density;
        int width = (int) (283 * scale + 0.5f);
        view.setMinimumWidth(width);
        lLayout_content = (LinearLayout) view
                .findViewById(R.id.lLayout_content);
        TextView txt_cancel = (TextView) view.findViewById(R.id.txt_cancel);
        txt_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog = new Dialog(context, R.style.MakeSureDialog);
        dialog.setContentView(view);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.x = 0;
        lp.y = 0;
        dialogWindow.setAttributes(lp);
        return this;
    }

    public ActionSheetDialog setCancelable(boolean cancel) {
        dialog.setCancelable(cancel);
        return this;
    }

    public ActionSheetDialog setCanceledOnTouchOutside(boolean cancel) {
        dialog.setCanceledOnTouchOutside(cancel);
        return this;
    }

    public ActionSheetDialog addSheetItem(String strItem, SheetItemColor color,
                                          OnSheetItemClickListener listener) {
        if (sheetItemList == null) {
            sheetItemList = new ArrayList<>();
        }
        sheetItemList.add(new SheetItem(strItem, color, listener));
        return this;
    }

    private void setSheetItems() {
        if (sheetItemList == null || sheetItemList.size() <= 0) {
            return;
        }
        int size = sheetItemList.size();
        for (int i = 1; i <= size; i++) {
            final int index = i;
            SheetItem sheetItem = sheetItemList.get(i - 1);
            String strItem = sheetItem.name;
            SheetItemColor color = sheetItem.color;
            final OnSheetItemClickListener listener = sheetItem.itemClickListener;
            TextView textView = new TextView(context);
            textView.setText(strItem);
            textView.setTextSize(16);
            textView.setTextColor(R.color.room_type_text);
            textView.setGravity(Gravity.CENTER);
            View view = new View(context);
            view.setBackgroundResource(R.color.line_dirver_color);
            view.setMinimumHeight(1);
            if (size == 1) {
                textView.setBackgroundResource(R.drawable.air_dialog_cancel_button_background);
            } else {
                view.setLayoutParams(new LayoutParams(
                        LayoutParams.MATCH_PARENT, 1));
                if (i == 1) {
                    textView.setBackgroundResource(R.drawable.halfrectangle_top_buttom_background);
                } else if (i < size) {
                    textView.setBackgroundResource(R.drawable.button_delete_background);

                } else {
                    textView.setBackgroundResource(R.drawable.halfrectangle_buttom_button_background);
                }

            }
            if (color == null) {
                textView.setTextColor(Color.parseColor(SheetItemColor.GRAY
                        .getName()));
            } else {
                textView.setTextColor(Color.parseColor(color.getName()));
            }
            float scale = context.getResources().getDisplayMetrics().density;
            int height = (int) (44 * scale + 0.5f);
            textView.setLayoutParams(new LayoutParams(
                    LayoutParams.MATCH_PARENT, height));
            textView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(index);
                    dialog.dismiss();
                }
            });
            lLayout_content.addView(textView);
            if (i == 1) {
                lLayout_content.addView(view);
            } else if (i < size) {
                lLayout_content.addView(view);
            }
        }
    }

    public void show() {
        setSheetItems();
        dialog.show();
    }

    public interface OnSheetItemClickListener {
        void onClick(int which);
    }

    public class SheetItem {
        String name;
        OnSheetItemClickListener itemClickListener;
        SheetItemColor color;

        public SheetItem(String name, SheetItemColor color,
                         OnSheetItemClickListener itemClickListener) {
            this.name = name;
            this.color = color;
            this.itemClickListener = itemClickListener;
        }
    }

    public enum SheetItemColor {
        Blue("#F55555"), GRAY("#333333");
        private String name;
        SheetItemColor(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
