package com.andrew.apollo.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;
import com.andrew.apollo.lastfm.api.Artist;
import com.andrew.apollo.lastfm.api.Image;
import com.andrew.apollo.lastfm.api.ImageSize;
import com.andrew.apollo.lastfm.api.PaginatedResult;
import com.andrew.apollo.utils.ImageCache;

import java.io.File;
import java.util.Iterator;

import static com.andrew.apollo.Constants.LASTFM_API_KEY;

public class GetArtistImageTask extends GetBitmapTask {

    private final String TAG = "GetArtistImageTask";

    private String artist;

    public GetArtistImageTask(String artist, ImageView imageView, Context context) {
        super(imageView, context);
        this.artist = artist;
    }

    @Override
    protected File getFile(Context context, String extension) {
        String fileName = escapeForFileSystem(artist);
        if (fileName == null) {
            Log.e(TAG, "Can't create file name for: " + artist);
            return null;
        }
        return new File(context.getExternalFilesDir(null), fileName);
    }

    @Override
    protected String getImageUrl() {
        try {
            PaginatedResult<Image> images = Artist.getImages(this.artist, 2, 1, LASTFM_API_KEY);
            Iterator<Image> iterator = images.getPageResults().iterator();
            if (!iterator.hasNext()) {
                Log.e(TAG, "Error when retrieving artist image url for \"" + artist + "\" - empty result");
                return null;
            }
            Image image = iterator.next();
            return image.getImageURL(ImageSize.LARGESQUARE); //TODO: ensure that there is an image available in the specified size
        } catch (Exception e) {
            Log.e(TAG, "Error when retrieving artist image url for \"" + artist + "\"", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        ImageCache.getInstance().setArtistBitmap(artist, bitmap);
    }
}
