package com.thirumalai.calllimiter.Service;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class CommonService {
    public void copyToClipboard(Context context, String text){
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied to clipboard", text);
        clipboard.setPrimaryClip(clip);
    }
}
