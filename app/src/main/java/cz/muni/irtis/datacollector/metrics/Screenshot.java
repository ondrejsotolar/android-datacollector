package cz.muni.irtis.datacollector.metrics;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.WindowManager;

import java.time.LocalDateTime;

import cz.muni.irtis.datacollector.database.Query;
import cz.muni.irtis.datacollector.metrics.condition.IsScreenOn;
import cz.muni.irtis.datacollector.metrics.util.ImageTransmogrifier;
import cz.muni.irtis.datacollector.schedule.Metric;

import static android.content.Context.MEDIA_PROJECTION_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

public class Screenshot extends Metric {
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

        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        addPrerequisity(new IsScreenOn());
    }

    @Override
    public void save(LocalDateTime dateTime, Object... params) {
        super.save(dateTime, params);
        Query.saveMetric(this);
    }

    /**
     * Run the media projection.
     * Register callback with the image saver (ImageTransmogrifier).
     */
    @Override
    public void run() {
        if (!isConditionsSatisfied())
            return;

        projection = mediaProjectionManager.getMediaProjection(resultCode, resultData);
        imageTransmogrifier = new ImageTransmogrifier(this);

        MediaProjection.Callback callback = new MediaProjection.Callback() {
            @Override
            public void onStop() {
                virtualDisplay.release();
            }
        };

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

    /**
     * Save the image URL & clean virtual display resources.
     * @param imagePath absolute path to image
     */
    public void finishCapture(String imagePath) {
        if (projection != null) {
            projection.stop();
            if (virtualDisplay != null) {
                virtualDisplay.release();
            }
            projection = null;
        }

        if (imagePath != null && !"".equals(imagePath)) {
            this.imagePath = imagePath;
            save(LocalDateTime.now());
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

    private boolean isConditionsSatisfied() {
        if (!getPrerequisity(IsScreenOn.class).check(getContext())) {
            Log.d(getClass().getSimpleName() + " metric:","Screen is off - no screenshots will be taken.");
            return false;
        }
        return true;
    }


}
