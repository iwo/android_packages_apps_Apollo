package com.andrew.apollo.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.*;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public abstract class GetBitmapTask extends AsyncTask<String, Integer, Bitmap> {

    private final String TAG = "GetBitmapTask";

    private final String EXTENSION_JPG = ".jpg";
    private final String EXTENSION_PNG = ".png";
    private final String EXTENSION_GIF = ".gif";

    private final String[] IMAGE_EXTENSIONS = new String[]{EXTENSION_JPG, EXTENSION_PNG, EXTENSION_GIF};

    private WeakReference<OnBitmapReadyListener> listenerReference;

    private WeakReference<Context> contextReference;

    private String tag;

    public GetBitmapTask(OnBitmapReadyListener listener, String tag, Context context) {
        listenerReference = new WeakReference<OnBitmapReadyListener>(listener);
        contextReference = new WeakReference<Context>(context);
        this.tag = tag;
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
            file = getFile(context, extension);
            if (file == null) {
                return null;
            }
            if (file.exists()) {
                Log.d(TAG, "Cached file found: " + file.getAbsolutePath());
                found = true;
                break;
            }
        }

        if (!found) {
            String url = getImageUrl();
            if (url == null || url.isEmpty()) {
                Log.w(TAG, "No URL received");
                return null;
            }
            file = getFile(context, getExtension(url));
            File dir = file.getParentFile();
            if (!dir.exists() && !dir.mkdirs())
            {
                Log.e(TAG, "Can't create parent directory");
                return null;
            }
            Log.v(TAG, "Downloading " + url + " to " + file.getAbsolutePath());
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

    protected abstract String getImageUrl();

    protected abstract File getFile(Context context, String extension);

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
        super.onPostExecute(bitmap);
        OnBitmapReadyListener listener = listenerReference.get();
        if (bitmap != null) {
            if (listener != null) {
                listener.bitmapReady(bitmap, tag);
            }
        }
    }

    public interface OnBitmapReadyListener {
        public void bitmapReady(Bitmap bitmap, String tag);
    }
}
