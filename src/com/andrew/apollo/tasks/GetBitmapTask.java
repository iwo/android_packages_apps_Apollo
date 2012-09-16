package com.andrew.apollo.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import com.andrew.apollo.lastfm.api.Artist;
import com.andrew.apollo.lastfm.api.Image;
import com.andrew.apollo.lastfm.api.ImageSize;
import com.andrew.apollo.lastfm.api.PaginatedResult;
import com.andrew.apollo.utils.ImageCache;

import java.io.*;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import static com.andrew.apollo.Constants.LASTFM_API_KEY;

public class GetBitmapTask extends AsyncTask<String, Integer, Bitmap> {

    private final String TAG = "GetBitmapTask";

    private final String EXTENSION_JPG = ".jpg";

    private final String EXTENSION_PNG = ".png";

    private final String EXTENSION_GIF = ".gif";

    private final String[] IMAGE_EXTENSIONS = new String[]{EXTENSION_JPG, EXTENSION_PNG, EXTENSION_GIF};

    private WeakReference<ImageView> imageViewReference;

    private String key;

    private String name;

    private int width;

    private int height;

    private WeakReference<Context> contextReference;

    public GetBitmapTask(String key, String name, ImageView imageView, int width, int height, Context context) {
        this.key = key;
        this.name = name;
        this.width = width;
        this.height = height;
        imageViewReference = new WeakReference<ImageView>(imageView);
        contextReference = new WeakReference<Context>(context);

        imageView.setTag(this);
    }

    @Override
    protected Bitmap doInBackground(String... ignored) {
        Context context = contextReference.get();
        if (context == null) {
            return null;
        }

        boolean found = false;
        File file = null;

        for (String extension : IMAGE_EXTENSIONS) {
            file = getArtistFile(context, name, extension);
            if (file == null) {
                Log.e(TAG, "Can't create file name for: " + name);
                return null;
            }
            if (file.exists()) {
                Log.d(TAG, "Cached file found: " + file.getAbsolutePath());
                found = true;
                break;
            }
        }

        if (!found) {
            String url = getArtistsImageUrl(name);
            Log.d(TAG, "URL received for\"" + name + "\": " + url);
            if (url == null) {
                return null;
            }
            file = getArtistFile(context, name, getExtension(url));
            downloadBitmap(url, file);
            Log.d(TAG, "Downloaded! " + file.exists());
        }

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        if (bitmap == null) {
            Log.e(TAG, "Error decoding bitmap");
            return null;
        }

        Log.d(TAG, "Bitmap decoded! " + bitmap.getWidth() + "x" + bitmap.getHeight());
        return bitmap;
    }

    protected String getArtistsImageUrl(String artistName) {
        try {
            PaginatedResult<Image> artist = Artist.getImages(artistName, 2, 1, LASTFM_API_KEY);
            Iterator<Image> iterator = artist.getPageResults().iterator();
            if (!iterator.hasNext()) {
                return null;
            }
            Image image = iterator.next();
            return image.getImageURL(ImageSize.LARGESQUARE);
        } catch (Exception e) {
            return null;
        }
    }

    protected File getArtistFile(Context context, String artistName, String extension) {
        String fileName = escapeForFileSystem(artistName);
        if (fileName == null) {
            return null;
        }
        return new File(context.getExternalFilesDir(null), fileName);
    }

    protected String escapeForFileSystem(String name) {
        try {
            return URLEncoder.encode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected String getExtension(String url) {
        for (String extension : IMAGE_EXTENSIONS) {
            if (url.endsWith(extension))
                return extension;
        }
        return EXTENSION_JPG;
    }

    private void downloadBitmap(String urlString, File outFile) {

        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;

        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            final InputStream in =
                    new BufferedInputStream(urlConnection.getInputStream());
            out = new BufferedOutputStream(new FileOutputStream(outFile));

            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }

        } catch (final IOException e) {
            Log.e(TAG, "Error in downloadBitmap - " + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (out != null) {
                try {
                    out.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error in downloadBitmap - " + e);
                }
            }
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        ImageView imageView = imageViewReference.get();
        if (bitmap != null) {
            ImageCache.getInstance().setArtistBitmap(name, bitmap);
            if (imageView != null && imageView.getTag() == this) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}
