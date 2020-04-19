package de.marmaro.krt.ffupdater.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import java.util.List;
import java.util.Objects;

import de.marmaro.krt.ffupdater.App;
import de.marmaro.krt.ffupdater.device.InstalledApps;

/**
 * Created by Tobiwan on 23.08.2019.
 */
public class DownloadNewAppDialog extends DialogFragment {
    private final Consumer<App> callback;

    public DownloadNewAppDialog(Consumer<App> callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        NotInstalledApps notInstalledApps = getNotInstalledApps();
        return new AlertDialog.Builder(getActivity())
                .setTitle("Download new app")
                .setItems(notInstalledApps.getAppNames(), (dialog, which) -> {
                    App app = notInstalledApps.getApps().get(which);
                    switch (app) {
                        case FENNEC_BETA:
                        case FENNEC_NIGHTLY:
                            showWarning(app);
                            break;
                        default:
                            downloadApp(app);
                    }
                })
                .create();
    }

    private void showWarning(App app) {
        FragmentManager fragmentManager = Objects.requireNonNull(getFragmentManager());
        new WarningAppDialog(callback, app).show(fragmentManager, WarningAppDialog.TAG);
    }

    private void downloadApp(App app) {
        FragmentManager fragmentManager = Objects.requireNonNull(getFragmentManager());
        callback.accept(app);
        new FetchDownloadUrlDialog().show(fragmentManager, FetchDownloadUrlDialog.TAG);
    }

    private NotInstalledApps getNotInstalledApps() {
        Activity activity = Objects.requireNonNull(getActivity());
        InstalledApps detector = new InstalledApps(activity.getPackageManager());
        List<App> notInstalledApps = detector.getNotInstalledApps();

        int appsCount = notInstalledApps.size();
        CharSequence[] appNames = new CharSequence[appsCount];
        for (int i = 0; i < appsCount; i++) {
            appNames[i] = notInstalledApps.get(i).getTitle(activity);
        }
        return new NotInstalledApps(notInstalledApps, appNames);
    }

    private static class NotInstalledApps {
        private final List<App> apps;
        private final CharSequence[] appNames;

        NotInstalledApps(List<App> apps, CharSequence[] appNames) {
            this.apps = apps;
            this.appNames = appNames;
        }

        List<App> getApps() {
            return apps;
        }

        CharSequence[] getAppNames() {
            return appNames;
        }
    }
}