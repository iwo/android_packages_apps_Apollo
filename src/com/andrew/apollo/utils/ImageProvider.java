package com.andrew.apollo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;
import com.andrew.apollo.R;
import com.andrew.apollo.tasks.GetAlbumImageTask;
import com.andrew.apollo.tasks.GetArtistImageTask;
import com.andrew.apollo.tasks.GetBitmapTask;

import java.util.*;

public class ImageProvider implements GetBitmapTask.OnBitmapReadyListener {

    private Map<String, Bitmap> memCache = new HashMap<String, Bitmap>();

    private Map<String, Set<ImageView>> pendingImagesMap = new HashMap<String, Set<ImageView>>();

    public ImageProvider() {

    }

    public void setArtistImage(String artist, ImageView imageView, Context context) {
        String tag = getArtistTag(artist);
        if (!setCachedBitmap(tag, imageView)) {
            asyncLoad(tag, imageView, new GetArtistImageTask(artist, this, tag, context));
        }
    }

    public void setAlbumImage(String artist, String album, ImageView imageView, Context context) {
        String tag = getAlbumTag(artist, album);
        if (!setCachedBitmap(tag, imageView)) {
            asyncLoad(tag, imageView, new GetAlbumImageTask(artist, album, this, tag, context));
        }
    }

    private boolean setCachedBitmap(String tag, ImageView imageView) {
        if (!memCache.containsKey(tag))
            return false;
        Bitmap bitmap = memCache.get(tag);
        imageView.setTag(tag);
        imageView.setImageBitmap(bitmap);
        return true;
    }

    private void setLoadedBitmap(String tag, ImageView imageView, Bitmap bitmap) {
        if (!tag.equals(imageView.getTag()))
            return;

        final TransitionDrawable transition = new TransitionDrawable(new Drawable[]{
                new ColorDrawable(android.R.color.transparent),
                new BitmapDrawable(imageView.getResources(), bitmap)
        });

        imageView.setImageDrawable(transition);
        final int duration = imageView.getResources().getInteger(R.integer.image_fade_in_duration);
        transition.startTransition(duration);
    }

    private void asyncLoad(String tag, ImageView imageView, GetBitmapTask task) {
        Set<ImageView> pendingImages = pendingImagesMap.get(tag);
        if (pendingImages == null) {
            pendingImages = Collections.newSetFromMap(new WeakHashMap<ImageView, Boolean>()); // create weak set
            pendingImagesMap.put(tag, pendingImages);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        pendingImages.add(imageView);
        imageView.setTag(tag);
        imageView.setImageDrawable(null);
    }

    private String getArtistTag(String artist) {
        return artist;
    }

    private String getAlbumTag(String artist, String album) {
        return artist + " - " + album;
    }

    @Override
    public void bitmapReady(Bitmap bitmap, String tag) {
        memCache.put(tag, bitmap);
        Set<ImageView> pendingImages = pendingImagesMap.get(tag);
        if (pendingImages != null) {
            pendingImages.remove(tag);
            for (ImageView imageView : pendingImages) {
                setLoadedBitmap(tag, imageView, bitmap);
            }
        }
    }
}
