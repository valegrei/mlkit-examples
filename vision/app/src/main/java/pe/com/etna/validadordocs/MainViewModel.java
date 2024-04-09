package pe.com.etna.validadordocs;

import android.content.Context;
import android.graphics.Bitmap;
import android.icu.text.DecimalFormat;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

public class MainViewModel extends ViewModel {
    private static final String FORMAT1 = "#0.00";
    private static final String FORMAT2 = "##,###,##0.00";
    public final MutableLiveData<String> _texto1 = new MutableLiveData<>("");
    public final MutableLiveData<String> _texto2 = new MutableLiveData<>("");
    public final MutableLiveData<String> _importe = new MutableLiveData<>("");
    private final MutableLiveData<String> _resultado = new MutableLiveData<>("");
    private final MutableLiveData<String> _nombreArchivo = new MutableLiveData<>("");
    private MutableLiveData<Uri> _uriFile = new MutableLiveData<Uri>();

    public LiveData<String> texto1 = _texto1;
    public LiveData<String> texto2 = _texto2;
    public LiveData<String> importe = _importe;
    public LiveData<String> resultado = _resultado;
    public LiveData<String> nombreArchivo = _nombreArchivo;
    private TextRecognizer recognizer;

    public MainViewModel() {
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }

    public void buscar(Context context, String texto1, String texto2, String importe) {
        _texto1.setValue(texto1);
        _texto2.setValue(texto2);
        _importe.setValue(importe);
        if (_uriFile.getValue() != null) {
            procesar(context);
        }
    }

    public void limpiar(){
        _texto1.setValue("");
        _texto2.setValue("");
        _importe.setValue("");
        _resultado.setValue("");
        _uriFile.setValue(null);
        _nombreArchivo.setValue("");
    }

    public void setUriFile(Uri uri) {
        _uriFile.setValue(uri);
        Log.d("ImageFile", uri.toString());
        Log.d("ImageFile", uri.getPath());
        String path = uri.getPath();
        if (path != null) {
            int index = path.lastIndexOf('/');
            _nombreArchivo.setValue(path.substring(index + 1));
        }
    }

    private void procesar(Context context) {
        Uri imageUri = _uriFile.getValue();
        InputImage image;
        try {
            Bitmap imageBitmap = BitmapUtils.getBitmapFromContentUri(context.getContentResolver(), imageUri);
            if (imageBitmap == null) {
                return;
            }
            imageBitmap = BitmapUtils.resizeBitmap(imageBitmap);

            image = InputImage.fromBitmap(imageBitmap, 0);
            recognizer.process(image)
                    .addOnSuccessListener(text -> coincidencias(text.getText()))
                    .addOnFailureListener(e -> Log.e("Recognizer", e.getMessage(), e));
        } catch (IOException ex) {
            Log.e("InputImage", ex.getMessage(), ex);
        }
    }

    private void coincidencias(String text) {
        StringBuilder res = new StringBuilder("Coincidencias:\n");

        String texto1 = _texto1.getValue().toLowerCase();
        String texto2 = _texto2.getValue().toLowerCase();
        String importe = _importe.getValue().toLowerCase();

        text = text.trim().toLowerCase();
        if (text.isEmpty())
            return;

        if (!texto1.isEmpty()) {
            res.append(texto1);
            if (text.contains(texto1)) {
                res.append(": SI\n");
            } else {
                res.append(": NO\n");
            }
        }

        if (!texto2.isEmpty()) {
            res.append(texto2);
            if (text.contains(texto2)) {
                res.append(": SI\n");
            } else {
                res.append(": NO\n");
            }
        }

        if (!importe.isEmpty()) {
            res.append(importe);
            if (coincidenciaImporte(text, importe)) {
                res.append(": SI\n");
            } else {
                res.append(": NO\n");
            }
        }

        res.append("\n");
        res.append(text);

        _resultado.setValue(res.toString());
    }

    private boolean coincidenciaImporte(String text, String importe) {
        if (importe.isEmpty())
            return false;
        try {
            Float monto = Float.parseFloat(importe);
            DecimalFormat format1 = new DecimalFormat(FORMAT1);
            String importe1 = format1.format(monto);

            DecimalFormat format2 = new DecimalFormat(FORMAT2);
            String importe2 = format2.format(monto);

            String importe3 = importe2.replace(',', '.');

            return text.contains(importe1) || text.contains(importe2) || text.contains(importe3);
        } catch (Exception ex) {
            return false;
        }
    }
}
