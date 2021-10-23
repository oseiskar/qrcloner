/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.osei.qrcloner.java.barcodescanner;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.google.zxing.BarcodeFormat;

import xyz.osei.qrcloner.GraphicOverlay;
import xyz.osei.qrcloner.MainActivity;
import xyz.osei.qrcloner.java.VisionProcessorBase;
import java.util.List;

/** Barcode Detector Demo. */
public class BarcodeScannerProcessor extends VisionProcessorBase<List<Barcode>> {

  private static final String TAG = "BarcodeProcessor";

  private final BarcodeScanner barcodeScanner;
  private final Activity parentContext;

  public BarcodeScannerProcessor(Activity context) {
    super(context);
    // Note that if you know which format of barcode your app is dealing with, detection will be
    // faster to specify the supported barcode formats one by one, e.g.
    // new BarcodeScannerOptions.Builder()
    //     .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
    //     .build();
    barcodeScanner = BarcodeScanning.getClient();
    parentContext = context;
  }

  @Override
  public void stop() {
    super.stop();
    barcodeScanner.close();
  }

  @Override
  protected Task<List<Barcode>> detectInImage(InputImage image) {
    return barcodeScanner.process(image);
  }

  @Override
  protected void onSuccess(
      @NonNull List<Barcode> barcodes, @NonNull GraphicOverlay graphicOverlay) {
    for (int i = 0; i < barcodes.size(); ++i) {
      Barcode barcode = barcodes.get(i);
      graphicOverlay.add(new BarcodeGraphic(graphicOverlay, barcode));
      BarcodeFormat format = convertFormatFromMLKitToZXing(barcode.getFormat());
      if (format == null) continue;

      Intent intent = new Intent(parentContext, MainActivity.class);
      intent.putExtra(MainActivity.SAVED_CODE_CONTENT, barcode.getRawValue());
      intent.putExtra(MainActivity.SAVED_CODE_TYPE, format.name());
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      parentContext.startActivity(intent);
      parentContext.finish();
    }
  }

  private static BarcodeFormat convertFormatFromMLKitToZXing(int barcodeFormat) {
    switch (barcodeFormat) {
      case Barcode.FORMAT_AZTEC: return BarcodeFormat.AZTEC;
      case Barcode.FORMAT_CODABAR: return BarcodeFormat.CODABAR;
      case Barcode.FORMAT_CODE_39: return BarcodeFormat.CODE_39;
      case Barcode.FORMAT_CODE_93: return BarcodeFormat.CODE_93;
      case Barcode.FORMAT_QR_CODE: return BarcodeFormat.QR_CODE;
      case Barcode.FORMAT_CODE_128: return BarcodeFormat.CODE_128;
      case Barcode.FORMAT_DATA_MATRIX: return BarcodeFormat.DATA_MATRIX;
      case Barcode.FORMAT_EAN_8: return BarcodeFormat.EAN_8;
      case Barcode.FORMAT_EAN_13: return BarcodeFormat.EAN_13;
      case Barcode.FORMAT_ITF: return BarcodeFormat.ITF;
      case Barcode.FORMAT_PDF417: return BarcodeFormat.PDF_417;
      case Barcode.FORMAT_UPC_A: return BarcodeFormat.UPC_A;
      case Barcode.FORMAT_UPC_E: return BarcodeFormat.UPC_E;
    }
    return null;
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.e(TAG, "Barcode detection failed " + e);
  }
}
