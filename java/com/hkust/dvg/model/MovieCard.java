package com.hkust.dvg.model;

import android.graphics.drawable.Drawable;

import com.google.android.glass.app.Card.ImageLayout;

public class MovieCard {

    private String text;
    private String footerText;
    private ImageLayout imgLayout;
    private Drawable[] images;

    public MovieCard() {
    }

    public MovieCard(String text, String footerText,
                     ImageLayout imgLayout, Drawable[] images) {
        this.text = text;
        this.footerText = footerText;
        this.imgLayout = imgLayout;
        this.images = images;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFooterText() {
        return footerText;
    }

    public void setFooterText(String footerText) {
        this.footerText = footerText;
    }

    public ImageLayout getImgLayout() {
        return imgLayout;
    }

    public void setImgLayout(ImageLayout imgLayout) {
        this.imgLayout = imgLayout;
    }

    public Drawable[] getImages() {
        return images;
    }

    public void setImages(Drawable[] images) {
        this.images = images;
    }

}
