package com.andrew.apollo.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;
import com.andrew.apollo.lastfm.api.*;
import com.andrew.apollo.utils.ImageCache;

import java.io.File;
import java.util.Iterator;

import static com.andrew.apollo.Constants.LASTFM_API_KEY;

public class GetAlbumImageTask extends GetBitmapTask {

    private final String TAG = "GetArtistImageTask";

    private String artist;

    private String album;

    public GetAlbumImageTask(String artist, String album, OnBitmapReadyListener listener, String tag, Context context) {
        super(listener, tag, context);
        this.artist = artist;
        this.album = album;
    }

    @Override
    protected File getFile(Context context, String extension) {
        String fileName = escapeForFileSystem(album);
        String directory = escapeForFileSystem(artist);

        if (fileName == null || directory == null) {
            Log.e(TAG, "Can't create file name for: " + album + " " + artist);
            return null;
        }
        /*File directoryFile = new File(context.getExternalFilesDir(null), directory);
        return new File(directoryFile, fileName + extension);*/

        return new File(context.getExternalFilesDir(null), directory + "_" + fileName + extension);
    }

    @Override
    protected Bitmap doInBackground(String... ignored) {
        Log.v(TAG, "Get album image for " + artist + " - " + album);
        return super.doInBackground(ignored);
    }

    @Override
    protected String getImageUrl() {
        try {
            Album album = Album.getInfo(artist, this.album, LASTFM_API_KEY);
            if (album == null) {
                Log.w(TAG, "Album not found: " + artist + " - " + this.album);
                return null;
            }
            return album.getImageURL(ImageSize.LARGE); //TODO: ensure that there is an image available in the specified size
        } catch (Exception e) {
            Log.w(TAG, "Error when retrieving album image url", e);
            return null;
        }
    }
}
