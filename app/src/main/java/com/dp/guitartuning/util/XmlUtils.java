package com.dp.guitartuning.util;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

public final class XmlUtils {

    private XmlUtils() {
    }

    /**
     * 设置 radius。
     */
    @BindingAdapter("radius")
    public static void setRadius(View view, float radiusDp) {
        float radiusPx = view.getContext().getResources().getDisplayMetrics().density * radiusDp;

        if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            Drawable drawable = imageView.getDrawable();
            if (drawable != null) {
                Glide.with(view.getContext())
                        .load(drawable)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners((int) radiusPx)))
                        .into(imageView);
            }
            return;
        }

        if (view instanceof CardView) {
            ((CardView) view).setRadius(radiusPx);
            return;
        }

        Drawable background = view.getBackground();
        if (background instanceof GradientDrawable) {
            ((GradientDrawable) background).setCornerRadius(radiusPx);
            return;
        }

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(radiusPx);
        if (background instanceof ColorDrawable) {
            gradientDrawable.setColor(((ColorDrawable) background).getColor());
        }
        view.setBackground(gradientDrawable);
    }
}
