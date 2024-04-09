package pe.com.etna.validadordocs;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CHOOSE_IMAGE = 1001;
    private TextInputEditText etTexto1, etTexto2, etImporte;
    private TextView tvNombreArchivo, tvResultados;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        etTexto1 = findViewById(R.id.etTexto1);
        etTexto2 = findViewById(R.id.etTexto2);
        etImporte = findViewById(R.id.etImporte);
        tvNombreArchivo = findViewById(R.id.tvNombreArchivo);
        tvResultados = findViewById(R.id.tvResultados);

        findViewById(R.id.ibCargarArchivo).setOnClickListener(v -> cargarArchivo());
        findViewById(R.id.btnBuscar).setOnClickListener(v -> buscar());
        findViewById(R.id.ibLimpiar).setOnClickListener(v -> limpiar());

        viewModel.texto1.observe(this, texto1 -> etTexto1.setText(texto1));
        viewModel.texto2.observe(this, texto2 -> etTexto2.setText(texto2));
        viewModel.importe.observe(this, importe -> etImporte.setText(importe));
        viewModel.resultado.observe(this, resultado -> tvResultados.setText(resultado));
        viewModel.nombreArchivo.observe(this, nombreArchivo -> tvNombreArchivo.setText(nombreArchivo));
    }

    private void cargarArchivo() {
        startChooseImageIntentForResult();
    }

    private void buscar() {
        String texto1 = (etTexto1.getText() != null ? etTexto1.getText().toString() : "").trim();
        String texto2 = (etTexto2.getText() != null ? etTexto2.getText().toString() : "").trim();
        String importe = (etImporte.getText() != null ? etImporte.getText().toString() : "").trim();
        viewModel.buscar(this, texto1, texto2, importe);
    }

    private void limpiar() {
        viewModel.limpiar();
    }

    private void startChooseImageIntentForResult() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == RESULT_OK) {
            // In this case, imageUri is returned by the chooser, save it.
            viewModel.setUriFile(data.getData());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}