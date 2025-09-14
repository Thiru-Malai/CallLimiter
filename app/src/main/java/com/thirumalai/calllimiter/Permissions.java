package com.thirumalai.calllimiter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Permissions extends AppCompatActivity {
    private LinearLayout permissionContainer, permissionsSystemHandled;
    private ImageView back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_permissions);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.permissions_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        back_btn = findViewById(R.id.back_btn_permissions);

        PermissionItem[] runtimePermissions = new PermissionItem[]{
                new PermissionItem("Phone", Manifest.permission.READ_PHONE_STATE, getResources().getString(R.string.permission_1)),
                new PermissionItem("Call Logs", Manifest.permission.READ_CALL_LOG, getResources().getString(R.string.permission_2)),
                new PermissionItem("Contacts", Manifest.permission.READ_CONTACTS, getResources().getString(R.string.permission_3)),
                new PermissionItem("Notifications", Manifest.permission.POST_NOTIFICATIONS, getResources().getString(R.string.permission_5))
        };

        // Auto-granted/system permissions (informational only)
        final AutoPermissionItem[] autoPermissions = new AutoPermissionItem[]{
                new AutoPermissionItem("Manage Own Calls", getResources().getString(R.string.permission_4)),
                new AutoPermissionItem("Foreground Service", getResources().getString(R.string.permission_6)),
                new AutoPermissionItem("Auto Restart on Boot", getResources().getString(R.string.permission_7)),
                new AutoPermissionItem("Vibrate", getResources().getString(R.string.permission_8))
        };

        permissionContainer = findViewById(R.id.permission_container);

        for (PermissionItem item:
             runtimePermissions) {
            View permissionView = LayoutInflater.from(this).inflate(R.layout.permission_item, permissionContainer, false);

            TextView permissionTitle = permissionView.findViewById(R.id.permission_title);
            TextView permissionDesc = permissionView.findViewById(R.id.permission_desc);
            ImageView permissionGranted = permissionView.findViewById(R.id.permission_granted);
            Button permissionNotGranted = permissionView.findViewById(R.id.permission_not_granted);

            permissionTitle.setText(item.title);
            permissionDesc.setText(item.description);

            boolean granted = false;

            if (item.permission.equals(Manifest.permission.POST_NOTIFICATIONS)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                            == PackageManager.PERMISSION_GRANTED;
                } else {
                    // Android < 13: treat as “granted by default”
                    granted = true;
                }
            } else {
                // normal permission check
                granted = ContextCompat.checkSelfPermission(this, item.permission)
                        == PackageManager.PERMISSION_GRANTED;
            }
            if (granted) {
                permissionGranted.setVisibility(View.VISIBLE);
                permissionNotGranted.setVisibility(View.GONE);
            } else{
                permissionNotGranted.setVisibility(View.VISIBLE);
                permissionGranted.setVisibility(View.GONE);
                permissionNotGranted.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(
                                Permissions.this,
                                new String[] { item.permission },
                                1001
                        );
                    }
                });
            }
        permissionContainer.addView(permissionView);
        }

        permissionsSystemHandled = findViewById(R.id.permission_system_handled);

        for (AutoPermissionItem item:
                autoPermissions) {
            View permissionView = LayoutInflater.from(this).inflate(R.layout.permission_item, permissionsSystemHandled, false);

            TextView permissionTitle = permissionView.findViewById(R.id.permission_title);
            TextView permissionDesc = permissionView.findViewById(R.id.permission_desc);
            ImageView permissionGranted = permissionView.findViewById(R.id.permission_granted);
            Button permissionNotGranted = permissionView.findViewById(R.id.permission_not_granted);

            permissionTitle.setText(item.title);
            permissionDesc.setText(item.description);

            permissionGranted.setVisibility(View.VISIBLE);
            permissionNotGranted.setVisibility(View.GONE);
            permissionsSystemHandled.addView(permissionView);
        }

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Permissions.this, com.thirumalai.calllimiter.Settings.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                updatePermissionStatus(true, permissions);
            } else {
                // Permission denied
                updatePermissionStatus(false, permissions);
            }
        }
    }

    private void updatePermissionStatus(boolean isGranted, String [] permissions) {
        LinearLayout permissionContainer = findViewById(R.id.permission_container);

        PermissionItem[] runtimePermissions = new PermissionItem[]{
                new PermissionItem("Phone", Manifest.permission.READ_PHONE_STATE, getResources().getString(R.string.permission_1)),
                new PermissionItem("Call Logs", Manifest.permission.READ_CALL_LOG, getResources().getString(R.string.permission_2)),
                new PermissionItem("Contacts", Manifest.permission.READ_CONTACTS, getResources().getString(R.string.permission_3)),
                new PermissionItem("Notifications", Manifest.permission.POST_NOTIFICATIONS, getResources().getString(R.string.permission_5))
        };

        String permissionToBeUpdated = "";

        for(PermissionItem item: runtimePermissions){
            if(item.permission.equals(permissions[0])){
                permissionToBeUpdated = item.title;
                break;
            }
        }

        for (int i = 1; i < permissionContainer.getChildCount(); i++) {
            View permView = permissionContainer.getChildAt(i);
            ImageView permissionGranted = permView.findViewById(R.id.permission_granted);
            Button permissionNotGranted = permView.findViewById(R.id.permission_not_granted);
            TextView permissionTitle = permView.findViewById(R.id.permission_title);
            if(permissionToBeUpdated.contentEquals(permissionTitle.getText())){
                if (isGranted) {
                    permissionGranted.setVisibility(View.VISIBLE);
                    permissionNotGranted.setVisibility(View.GONE);
                } else{
                    permissionNotGranted.setVisibility(View.VISIBLE);
                    permissionGranted.setVisibility(View.GONE);
                    permissionNotGranted.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", getPackageName(), null));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    });
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        }
    }


    private static class PermissionItem {
        String title;
        String permission;
        String description;
        PermissionItem(String title, String permission, String description) {
            this.title = title;
            this.permission = permission;
            this.description = description;
        }
    }


    private static class AutoPermissionItem {
        String title;
        String description;
        AutoPermissionItem(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }
}