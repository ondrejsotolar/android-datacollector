package cz.muni.irtis.datacollector.metrics.util.screenshot;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

class ScreenshotSaver {
    private static DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("yyyyMMddHHmmssSSS")
            .toFormatter();

    /**
     * Save PNG image to local storage & return absolute path.
     * File has 64 char long random UUID name.
     * @param png image
     * @param context context
     * @return absolute path to file
     */
    static String processImage(final byte[] png, final Context context) {
        String fileName = formatter.print(DateTime.now()) + ".png";
        File output = new File(context.getExternalFilesDir(null), fileName);
        String absolutePath = null;
        try {
            FileOutputStream fos = new FileOutputStream(output);
            fos.write(png);
            fos.flush();
            fos.getFD().sync();
            fos.close();

            absolutePath = output.getAbsolutePath();
            Log.d("INFO: ", absolutePath);
        }
        catch (Exception e) {
            Log.e("ScreenshotSaver", "Exception writing out screenshot", e);
        }
        return absolutePath;
    }

    // TODO: delete
    public static String processImage_Threaded(final byte[] png, File externalFilesDir) {
        final String fileName = formatter.print(DateTime.now()) + ".png";
        final File output = new File(externalFilesDir, fileName);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    FileOutputStream fos=new FileOutputStream(output);
                    fos.write(png);
                    fos.flush();
                    fos.getFD().sync();
                    fos.close();
                    Log.i("INFO: ", output.getAbsolutePath());
                }
                catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "Exception writing out screenshot", e);
                }
                return null;
            }
        }.execute();
        return output.getAbsolutePath();
    }
}
