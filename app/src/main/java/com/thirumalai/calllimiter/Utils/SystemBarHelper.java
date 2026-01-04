package com.thirumalai.calllimiter.Utils;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Insets;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

public class SystemBarHelper {

    public static void setupStatusBarAppearance(Window window, Resources resources, View rootView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {

            window.setDecorFitsSystemWindows(false);

            rootView.setOnApplyWindowInsetsListener((v, insets) -> {
                Insets systemBarsInsets = insets.getInsets(WindowInsets.Type.systemBars());
                v.setPadding(0, systemBarsInsets.top, 0, 0); // Top padding only
                return insets;
            });

            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                boolean isDarkTheme = (resources.getConfiguration().uiMode
                        & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

                controller.setSystemBarsAppearance(
                        isDarkTheme ? 0 : WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                );
            }
        }
    }
}
