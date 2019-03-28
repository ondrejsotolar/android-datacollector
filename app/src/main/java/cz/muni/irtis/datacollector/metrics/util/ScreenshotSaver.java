package cz.muni.irtis.datacollector.metrics.util;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;
import java.util.UUID;

public class ScreenshotSaver {
    /**
     * Save PNG image to local storage & return absolute path.
     * File has 64 char long random UUID name.
     * @param png image
     * @param context context
     * @return absolute path to file
     */
    public static String processImage(final byte[] png, final Context context) {
        String fileName = UUID.randomUUID().toString() + ".png";
        File output = new File(context.getExternalFilesDir(null), fileName);
        try {
            FileOutputStream fos = new FileOutputStream(output);
            fos.write(png);
            fos.flush();
            fos.getFD().sync();
            fos.close();

            String absolutePath = output.getAbsolutePath();
            // check if saved properly
            MediaScannerConnection.scanFile(context,
                    new String[] {absolutePath},
                    new String[] {"image/png"},
                    null);

            Log.d("INFO: ", absolutePath);
            return absolutePath;
        }
        catch (Exception e) {
            Log.e("ScreenshotSaver", "Exception writing out screenshot", e);
        }
        throw new IllegalStateException("Screenshot not saved!");
    }

    // TODO: delete
    public static String processImage_Threaded(final byte[] png, final String dir, final Context context) {
        final String[] absolutePath = new String[1];
        new Thread() {
            @Override
            public void run() {
                Random rand = new Random();
                int n = rand.nextInt(10000) + 1;

                File output=new File(dir,"screenshot_" + n  + ".png");

                try {
                    FileOutputStream fos=new FileOutputStream(output);

                    fos.write(png);
                    fos.flush();
                    fos.getFD().sync();
                    fos.close();

                    MediaScannerConnection.scanFile(context,
                            new String[] {output.getAbsolutePath()},
                            new String[] {"image/png"},
                            null);

                    Log.i("INFO: ", output.getAbsolutePath());
                    absolutePath[0] = output.getAbsolutePath();
                }
                catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "Exception writing out screenshot", e);
                }
            }
        }.start();
        return absolutePath[0];
    }
}
