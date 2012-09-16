package com.andrew.apollo.utils;

import android.graphics.Bitmap;

import java.util.HashMap;
import java.util.Map;

public class ImageCache {

    private Map<String, Bitmap> naiveCache = new HashMap<String, Bitmap>();
    private static ImageCache instance;

    public static ImageCache getInstance() {
        if (instance == null) {
            instance = new ImageCache();
        }
        return instance;
    }

    private ImageCache() {
    }

    public Bitmap getArtistBitmap(String artistName)
    {
        return naiveCache.get(artistName);
    }

    public void setArtistBitmap(String name, Bitmap bitmap) {
        naiveCache.put(name, bitmap);
    }

    public Bitmap getAlbumBitmap(String artistName, String albumName)
    {
        return naiveCache.get(artistName + " :: " + albumName);
    }

    public void setAlbumBitmap(String artistName, String albumName, Bitmap bitmap) {
        naiveCache.put(artistName + " :: " + albumName, bitmap);
    }
}
