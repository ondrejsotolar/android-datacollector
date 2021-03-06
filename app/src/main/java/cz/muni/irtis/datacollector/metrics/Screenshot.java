package cz.muni.irtis.datacollector.metrics;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import org.joda.time.DateTime;

import cz.muni.irtis.datacollector.database.Query;
import cz.muni.irtis.datacollector.metrics.util.screenshot.ImageTransmogrifier;
import cz.muni.irtis.datacollector.schedule.Metric;

import static android.content.Context.MEDIA_PROJECTION_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

/**
 * Capture screenshots
 */
public class Screenshot extends Metric {
    private static final String TAG = Screenshot.class.getSimpleName();

    private final int VIRT_DISPLAY_FLAGS =
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY |
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

    private final HandlerThread handlerThread =
            new HandlerThread(getClass().getSimpleName(),
                    android.os.Process.THREAD_PRIORITY_BACKGROUND);

    private Handler handler;
    private int resultCode;
    private Intent resultData;
    private MediaProjectionManager mediaProjectionManager;
    private WindowManager windowManager;
    private MediaProjection projection;
    private VirtualDisplay virtualDisplay;
    private ImageTransmogrifier imageTransmogrifier;
    private String imagePath;
    private int width;
    private int height;
    private int delay = 5000;

    /**
     * Constructor.
     * Init system services, handler thread, get permission from params.
     * @param context context
     * @param params 0: resultCode, 1: resultIntent - special permission for media projection API
     */
    public Screenshot(Context context, Object... params) {
        super(context, params);

        mediaProjectionManager =
                (MediaProjectionManager) getContext().getSystemService(MEDIA_PROJECTION_SERVICE);
        windowManager = (WindowManager) getContext().getSystemService(WINDOW_SERVICE);

        resultCode = (int) params[0];
        resultData = (Intent) params[1];
        delay = (int) params[2];

        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        initScreenSize();
    }

    @Override
    public void save(DateTime dateTime, Object... params) {
        super.save(dateTime, params);
        Query.saveMetric(this);
    }

    /**
     * Run the media projection.
     * Register callback with the image saver (ImageTransmogrifier).
     */
    @Override
    public void run() {
         try {
            if (projection == null) {
                projection = mediaProjectionManager.getMediaProjection(resultCode, resultData);
            }
        } catch(IllegalStateException e) {
            Log.w(TAG, "Screenshot skipped: Cannot start already started MediaProjection");
            return;
        }

        imageTransmogrifier = new ImageTransmogrifier(this);
        MediaProjection.Callback callback = new MediaProjection.Callback() {
            @Override
            public void onStop() {
                virtualDisplay.release();
            }
        };
        if (projection != null && imageTransmogrifier != null) {
            virtualDisplay = projection.createVirtualDisplay(
                    getClass().getSimpleName(),
                    imageTransmogrifier.getWidth(),
                    imageTransmogrifier.getHeight(),
                    getContext().getResources().getDisplayMetrics().densityDpi,
                    VIRT_DISPLAY_FLAGS,
                    imageTransmogrifier.getSurface(),
                    null,
                    handler);
            projection.registerCallback(callback, handler);
        }
    }

    @Override
    public void stop() {
        if (projection != null) {
            projection.stop();
            if (virtualDisplay != null) {
                virtualDisplay.release();
            }
            projection = null;
        }
        if (imageTransmogrifier != null) {
            imageTransmogrifier.close();
        }
        super.stop();
    }

    /**
     * Save the image URL & clean virtual display resources.
     * @param imagePath absolute path to image
     */
    public void finishCapture(String imagePath) {
        if (imagePath != null && !"".equals(imagePath)) {
            this.imagePath = imagePath;
            save(DateTime.now());
        }
    }

    public String getUrl() {
        return imagePath;
    }

    public Handler getHandler() {
        return handler;
    }

    public WindowManager getWindowManager() {
        return windowManager;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDelay() {
        return delay;
    }

    private void initScreenSize() {
        if (width > 0 && height > 0) {
            return;
        }

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();

        display.getSize(size);
        width = size.x;
        height = size.y;

        while (width * height > (2 << 19)) {
            width = width >> 1;
            height = height >> 1;
        }
    }
}
