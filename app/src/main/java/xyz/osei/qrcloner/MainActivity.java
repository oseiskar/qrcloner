package xyz.osei.qrcloner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import xyz.osei.qrcloner.java.LivePreviewActivity;

public class MainActivity extends AppCompatActivity {
    private static String TAG = MainActivity.class.getName();
    public static String SAVED_QR_CODE_KEY = "savedQrCode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String activeCode = null;
        if (intent.hasExtra(SAVED_QR_CODE_KEY))
            activeCode = intent.getStringExtra(SAVED_QR_CODE_KEY);

        findViewById(R.id.change).setOnClickListener(v -> scanNewCode());

        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);

        if (activeCode == null) {
            activeCode = sharedPref.getString(SAVED_QR_CODE_KEY, null);
        } else {
            sharedPref.edit().putString(SAVED_QR_CODE_KEY, activeCode).apply();
        }

        if (activeCode == null) {
            Log.d(TAG, "empty QR code, opening scanning activity");
            scanNewCode();
            finish(); // in this case, back should quit
            return;
        }

        Log.d(TAG, "active QR code");
        try {
            drawQrCode(activeCode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void scanNewCode() {
        Intent intent = new Intent(this, LivePreviewActivity.class);
        startActivity(intent);
    }

    private void drawQrCode(String code) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        BitMatrix bm = writer.encode(code, BarcodeFormat.QR_CODE, metrics.widthPixels, metrics.heightPixels);
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

        ImageView view = findViewById(R.id.qr_code_image);
        view.setImageBitmap(bitmap);
    }
}