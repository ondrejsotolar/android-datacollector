package cz.muni.irtis.datacollector.metrics.util.screenshot;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;
import android.view.Display;
import android.view.Surface;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Date;

import cz.muni.irtis.datacollector.metrics.Screenshot;

/**
 * Taken from https://commonsware.com/Android (Apache License 2.0)
 *
 * Added counter to discard extra frames from ImageReader
 */
public class ImageTransmogrifier implements ImageReader.OnImageAvailableListener {
    private final int width;
    private final int height;
    private final ImageReader imageReader;
    private Bitmap latestBitmap=null;
    private Screenshot screenshotMetric;
    private long lastTimestamp;

    public ImageTransmogrifier(Screenshot screenshotMetric) {
        this.screenshotMetric = screenshotMetric;
        width = screenshotMetric.getWidth();
        height = screenshotMetric.getHeight();
        this.lastTimestamp = System.currentTimeMillis();

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2); // see docs why 2 instead of 1
        imageReader.setOnImageAvailableListener(this, screenshotMetric.getHandler());
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        final Image image = imageReader.acquireLatestImage(); // must be first or system throws it away

        long currentTime = System.currentTimeMillis(); // filter screenshot by timestamp
        if (lastTimestamp + screenshotMetric.getDelay() > currentTime) {
            if (image != null) {
                image.close();
            }
            return;
        }
        else {
            lastTimestamp = currentTime;
        }

        if (image != null) {
            byte[] newPng = prepareScreenshot(image);
            String url = ScreenshotSaver.processImage_Threaded(newPng,
                    screenshotMetric.getContext().getExternalFilesDir(null));
            screenshotMetric.finishCapture(url);
        }
    }

    private byte[] prepareScreenshot(Image image) {
        Image.Plane[] planes=image.getPlanes();
        ByteBuffer buffer=planes[0].getBuffer();
        int pixelStride=planes[0].getPixelStride();
        int rowStride=planes[0].getRowStride();
        int rowPadding=rowStride - pixelStride * width;
        int bitmapWidth=width + rowPadding / pixelStride;

        if (latestBitmap == null ||
                latestBitmap.getWidth() != bitmapWidth ||
                latestBitmap.getHeight() != height) {
            if (latestBitmap != null) {
                latestBitmap.recycle();
            }

            latestBitmap= Bitmap.createBitmap(bitmapWidth,
                    height, Bitmap.Config.ARGB_8888);
        }
        latestBitmap.copyPixelsFromBuffer(buffer);
        image.close();

        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        Bitmap cropped=Bitmap.createBitmap(latestBitmap, 0, 0,
                width, height);

        cropped.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public Surface getSurface() {
        return(imageReader.getSurface());
    }

    public int getWidth() {
        return(width);
    }

    public int getHeight() {
        return(height);
    }

    public void close() {
        imageReader.close();
    }
}
