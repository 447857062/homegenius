package com.deplink.boruSmart.view.combinationwidget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import deplink.com.smartwirelessrelay.homegenius.EllESDK.R;

/**
 * Created by ${kelijun} on 2018/3/16.
 */
public class TitleLayout extends LinearLayout implements View.OnClickListener {
    private TextView textview_title;
    private TextView textview_edit;
    private ImageView image_setting;
    private ImageView imageview_back;
    private FrameLayout framelayout_back;
    private RelativeLayout layout_root;
    private View view_line_dirver;
    private ReturnImageClickListener returnClickListener;
    private EditTextClickListener editTextClickListener;
    private EditImageClickListener editImageClickListener;
    private  boolean return_image_show;
    private  boolean edit_text_show;
    private  boolean menu_image_show;
    public TitleLayout(Context context) {
        super(context);
    }

    public TitleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.layout_title, this);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleLayout);
        initTyped(typedArray);
    }

    private void initTyped(TypedArray typedArray) {
        textview_title=findViewById(R.id.textview_title);
        textview_edit=findViewById(R.id.textview_edit);
        image_setting=findViewById(R.id.image_setting);
        framelayout_back=findViewById(R.id.framelayout_back);
        layout_root=findViewById(R.id.layout_root);
        imageview_back=findViewById(R.id.imageview_back);
        view_line_dirver=findViewById(R.id.view_line_dirver);
        int backgroundColor = typedArray.getColor(R.styleable.TitleLayout_background_color,R.color.white);
         return_image_show = typedArray.getBoolean(R.styleable.TitleLayout_return_image_show, false);
         edit_text_show = typedArray.getBoolean(R.styleable.TitleLayout_edit_text_show, false);
         menu_image_show = typedArray.getBoolean(R.styleable.TitleLayout_menu_image_show, false);
        String title_text = typedArray.getString(R.styleable.TitleLayout_title_text);
        String edit_text = typedArray.getString(R.styleable.TitleLayout_edit_text);
        int edit_image_src = typedArray.getResourceId(R.styleable.TitleLayout_edit_image_src,0);
        layout_root.setBackgroundResource(backgroundColor);
        if(!return_image_show){
            framelayout_back.setVisibility(View.GONE);
        }else{
            framelayout_back.setOnClickListener(this);
        }
        if(!edit_text_show){
            textview_edit.setVisibility(View.GONE);
        }else{
            textview_edit.setText(edit_text);
            textview_edit.setOnClickListener(this);
        }
        if(!menu_image_show){
            image_setting.setVisibility(View.GONE);
        }else{
            image_setting.setImageResource(edit_image_src);
            image_setting.setOnClickListener(this);
        }
        textview_title.setText(title_text);
        typedArray.recycle();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.framelayout_back:
                if(returnClickListener!=null){
                    returnClickListener.onBackPressed();
                }
                break;
            case R.id.textview_edit:
                if(editTextClickListener!=null){
                    editTextClickListener.onEditTextPressed();
                }
                break;
            case R.id.image_setting:
                if(editImageClickListener!=null){
                    editImageClickListener.onEditImagePressed();
                }
                break;
        }
    }
    public void setTitleText(String titleText){
        textview_title.setText(titleText);
    }
    public void setEditText(String editText){
        if(textview_edit!=null){
            textview_edit.setText(editText);
        }

    }
    public void setTitleTextWhiteColor(){
        if(textview_title!=null){
            textview_title.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
        }

    }
    public void setEditTextWhiteColor(){
        if(textview_edit!=null){
            textview_edit.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
        }

    }
    public void setBackImageResource(int  res){
        if(imageview_back!=null){
            imageview_back.setImageResource(res);
        }

    }
    public void setEditTextVisiable(boolean show){
        if(textview_edit!=null){
            if(show){
                textview_edit.setVisibility(View.VISIBLE);
            }else{
                textview_edit.setVisibility(View.GONE);
            }

        }
    }
    public void setLineDirverVisiable(boolean show){
        if(view_line_dirver!=null){
            if(show){
                view_line_dirver.setVisibility(View.VISIBLE);
            }else{
                view_line_dirver.setVisibility(View.GONE);
            }
        }
    }
    public void setEditImageVisiable(boolean show){
        if(image_setting!=null){
            if(show){
                image_setting.setVisibility(View.VISIBLE);
            }else{
                image_setting.setVisibility(View.GONE);
            }

        }

    }
    public interface ReturnImageClickListener {
        void onBackPressed();
    }
    public interface EditTextClickListener {
        void onEditTextPressed();
    }
    public interface EditImageClickListener {
        void onEditImagePressed();
    }

    public void setEditImageClickListener(EditImageClickListener editImageClickListener) {
        this.editImageClickListener = editImageClickListener;
    }

    public void setEditTextClickListener(EditTextClickListener editTextClickListener) {
        this.editTextClickListener = editTextClickListener;
    }

    public void setReturnClickListener(ReturnImageClickListener returnClickListener) {
        this.returnClickListener = returnClickListener;
    }
}
