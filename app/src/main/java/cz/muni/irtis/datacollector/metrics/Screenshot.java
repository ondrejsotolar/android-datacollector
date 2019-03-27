package cz.muni.irtis.datacollector.metrics;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.WindowManager;

import java.time.LocalDateTime;

import cz.muni.irtis.datacollector.metrics.service.ImageTransmogrifier;
import cz.muni.irtis.datacollector.schedule.Metric;
import cz.muni.irtis.datacollector.schedule.SchedulerService;

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
    private MediaProjectionManager mgr;
    private WindowManager wmgr;
    private MediaProjection projection;
    private VirtualDisplay vdisplay;
    private ImageTransmogrifier it;

    public Screenshot(Context context, Object... params) {
        super(context, params);

        mgr = (MediaProjectionManager) getContext().getSystemService(MEDIA_PROJECTION_SERVICE);
        wmgr = (WindowManager) getContext().getSystemService(WINDOW_SERVICE);

        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        resultCode = (int) params[0];
        resultData = (Intent) params[1];
    }

    @Override
    public void save(LocalDateTime dateTime, Object... params) {

    }

    @Override
    public void run() {
        projection = mgr.getMediaProjection(resultCode, resultData);
        it = new ImageTransmogrifier(this);

        MediaProjection.Callback cb = new MediaProjection.Callback() {
            @Override
            public void onStop() {
                vdisplay.release();
            }
        };

        vdisplay = projection.createVirtualDisplay(getClass().getSimpleName(),
                it.getWidth(), it.getHeight(),
                getContext().getResources().getDisplayMetrics().densityDpi,
                VIRT_DISPLAY_FLAGS, it.getSurface(), null, handler);
        projection.registerCallback(cb, handler);
    }

    public void stopCapture(String url) {
        if (projection != null) {
            projection.stop();
            vdisplay.release();
            projection = null;
        }
        if (url != null && !"".equals(url)) {
            save(LocalDateTime.now(), url);
        }
    }

    public String getUrl() {
        throw new UnsupportedOperationException();
    }

    public Handler getHandler() {
        return handler;
    }

    public WindowManager getWmgr() {
        return wmgr;
    }
}
