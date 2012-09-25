package com.andrew.apollo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import com.andrew.apollo.tasks.GetAlbumImageTask;
import com.andrew.apollo.tasks.GetArtistImageTask;

public class ImageUtils {

    private static ImageProvider imageProvider = new ImageProvider();

    public static void setArtistImage(String artist, ImageView imageView, Context context) {
        imageProvider.setArtistImage(artist, imageView, context);
    }

    public static void setArtistOriginalImage(String artist, ImageView imageView, Context context) {
        setArtistImage(artist, imageView, context);
    }

    public static void setAlbumImage(String artist, String album, ImageView imageView, Context context) {
        imageProvider.setAlbumImage(artist, album, imageView, context);
    }
}
