package xyz.osei.qrcloner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import xyz.osei.qrcloner.java.LivePreviewActivity;

public class MainActivity extends AppCompatActivity {
    private static String TAG = MainActivity.class.getName();
    public static String SAVED_CODE_CONTENT = "saved_code_content";
    public static String SAVED_CODE_TYPE = "saved_code_type";

    static class Code {
        String content;
        BarcodeFormat format;

        void saveToPreferences(SharedPreferences prefs) {
            prefs.edit()
                .putString(SAVED_CODE_CONTENT, content)
                .putString(SAVED_CODE_TYPE, format.name())
                .apply();
        }

        static Code fromPreferences(SharedPreferences prefs) {
            return fromStrings(
                    prefs.getString(SAVED_CODE_CONTENT, null),
                    prefs.getString(SAVED_CODE_TYPE, null));
        }

        static Code fromIntent(Intent intent) {
            if (intent.hasExtra(SAVED_CODE_CONTENT) && intent.hasExtra(SAVED_CODE_TYPE)) {
                return fromStrings(
                        intent.getStringExtra(SAVED_CODE_CONTENT),
                        intent.getStringExtra(SAVED_CODE_TYPE));
            }
            return null;
        }

        static Code fromStrings(String content, String type) {
            if (content == null || type == null) {
                return null;
            }
            Code code = new Code();
            code.content = content;
            try {
                code.format = BarcodeFormat.valueOf(type);
            } catch (Exception e) {
                Log.w(TAG, "Invalid type");
                return null;
            }
            return code;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.change).setOnClickListener(v -> scanNewCode());

        Intent intent = getIntent();
        Code activeCode = Code.fromIntent(intent);

        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        if (activeCode == null) {
            activeCode = Code.fromPreferences(sharedPref);
        } else {
            activeCode.saveToPreferences(sharedPref);
        }

        Log.d(TAG, "active code");
        try {
            drawCode(activeCode);
        } catch (Exception e) {
            Log.e(TAG, "Failed to draw code: " + e.getMessage());
            activeCode = null;
        }

        if (activeCode == null) {
            Log.d(TAG, "empty code, opening scanning activity");
            scanNewCode();
            finish(); // in this case, back should quit
        }
    }

    private void scanNewCode() {
        Intent intent = new Intent(this, LivePreviewActivity.class);
        startActivity(intent);
    }

    private void drawCode(Code code) throws WriterException {
        Writer writer = new MultiFormatWriter();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int size = Math.min(metrics.widthPixels, metrics.heightPixels);
        BitMatrix bm = writer.encode(code.content, code.format, size, size);
        int w = bm.getWidth();
        int h = bm.getHeight();
        int foreground = Color.BLACK;
        int background = Color.WHITE; // Color.TRANSPARENT

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; ++y) {
                bitmap.setPixel(x, y, bm.get(x, y) ? foreground : background);
            }
        }

        TextView textView = findViewById(R.id.qr_code_text);
        textView.setText(code.format.name() + ": " + code.content);

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        textView.setOnLongClickListener(v -> {
            clipboard.setPrimaryClip(ClipData.newPlainText("text", code.content));
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), getText(R.string.copied_to_clipboard), Snackbar.LENGTH_SHORT);
            snackbar.show();
            return true;
        });

        ImageView imageView = findViewById(R.id.qr_code_image);
        imageView.setImageBitmap(bitmap);
    }
}