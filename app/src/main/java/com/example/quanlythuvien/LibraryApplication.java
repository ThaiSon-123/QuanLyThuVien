package com.example.quanlythuvien;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Application class — apply status bar inset cho mọi activity tự động
 * (do Android 15+ ép edge-to-edge khi targetSdk >= 35).
 *
 * Tìm view id `header` hoặc `topBar` ở mỗi activity → set paddingTop = status bar inset
 * và bù chiều cao tương ứng để nội dung header không bị thu nhỏ.
 */
public class LibraryApplication extends Application {

    /** Track view → original height đã ghi nhận lần đầu, tránh cộng dồn khi re-apply. */
    private static final Map<View, Integer> ORIG_HEIGHTS =
            Collections.synchronizedMap(new WeakHashMap<>());

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new SimpleLifecycleCallbacks() {
            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                applyHeaderInset(activity);
            }
        });
    }

    private void applyHeaderInset(Activity activity) {
        int[] candidates = {R.id.header, R.id.topBar};
        for (int id : candidates) {
            View v = activity.findViewById(id);
            if (v == null) continue;
            attachInsetListener(v);
            return;
        }
    }

    private static void attachInsetListener(final View header) {
        // Ghi nhớ chiều cao gốc (chỉ 1 lần) để tránh cộng dồn padding khi resume nhiều lần.
        if (!ORIG_HEIGHTS.containsKey(header)) {
            ORIG_HEIGHTS.put(header, header.getLayoutParams() != null
                    ? header.getLayoutParams().height : ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        ViewCompat.setOnApplyWindowInsetsListener(header, (view, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            int origH = ORIG_HEIGHTS.getOrDefault(view, ViewGroup.LayoutParams.WRAP_CONTENT);

            view.setPadding(view.getPaddingLeft(), sb.top,
                    view.getPaddingRight(), view.getPaddingBottom());

            ViewGroup.LayoutParams lp = view.getLayoutParams();
            if (lp != null && origH > 0) {
                lp.height = origH + sb.top;
                view.setLayoutParams(lp);
            }
            return insets;
        });
        ViewCompat.requestApplyInsets(header);
    }

    /** Helper interface để khỏi override 7 callback. */
    private interface SimpleLifecycleCallbacks extends ActivityLifecycleCallbacks {
        @Override default void onActivityCreated(@NonNull Activity a, @Nullable Bundle b) {}
        @Override default void onActivityStarted(@NonNull Activity a) {}
        @Override default void onActivityResumed(@NonNull Activity a) {}
        @Override default void onActivityPaused(@NonNull Activity a) {}
        @Override default void onActivityStopped(@NonNull Activity a) {}
        @Override default void onActivitySaveInstanceState(@NonNull Activity a, @NonNull Bundle b) {}
        @Override default void onActivityDestroyed(@NonNull Activity a) {}
    }
}
