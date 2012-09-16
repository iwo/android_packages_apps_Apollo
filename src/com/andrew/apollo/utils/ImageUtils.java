package com.andrew.apollo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import com.andrew.apollo.tasks.GetAlbumImageTask;
import com.andrew.apollo.tasks.GetArtistImageTask;

public class ImageUtils {

    public static boolean setArtistImage(String artist, ImageView imageView, Context context) {
        Bitmap bitmap = ImageCache.getInstance().getArtistBitmap(artist);
        if (bitmap == null) {
            imageView.setImageDrawable(null);
            new GetArtistImageTask(artist, imageView, context).execute();
            return false;
        }
        imageView.setImageBitmap(bitmap);
        return true;
    }

    public static boolean setArtistOriginalImage(String artist, ImageView imageView, Context context) {
        return setArtistImage(artist, imageView, context);
    }

    public static boolean setAlbumImage(String artist, String album, ImageView imageView, Context context) {
        Bitmap bitmap = ImageCache.getInstance().getAlbumBitmap(artist, album);
        if (bitmap == null) {
            imageView.setImageDrawable(null);
            new GetAlbumImageTask(artist, album, imageView, context).execute();
            return false;
        }
        imageView.setImageBitmap(bitmap);
        return true;
    }
}
