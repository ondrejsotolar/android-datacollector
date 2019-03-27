package cz.muni.irtis.datacollector.metrics.service;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

public class ScreenshotSaver {
    public static String processImage(final byte[] png, final Context context) {
        Random rand = new Random();
        int n = rand.nextInt(10000) + 1;

        File output=new File(context.getExternalFilesDir(null),"screenshot_" + n  + ".png");
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
            return output.getAbsolutePath();
        }
        catch (Exception e) {
            Log.e("ScreenshotSaver", "Exception writing out screenshot", e);
        }
        throw new IllegalStateException("Screenshot not saved!");
    }

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
