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


public class LibraryApplication extends Application {

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
