package com.example.platanocontrol;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class FragmentVer extends Fragment {

    public TextView txtId, txtFecha, txtHora, txtEstado;
    public Button btnBuscarRacimo;

    private DatabaseReference referenciaFirebase;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ver, container, false);

        //Intancimos la clase de firebase
        referenciaFirebase = FirebaseDatabase.getInstance().getReference();

        //---------- Relacionamos los elementos ----------------
        txtId = view.findViewById(R.id.txtInfoIdRacimo);
        txtFecha = view.findViewById(R.id.txtInfoFechaRacimo);
        txtHora = view.findViewById(R.id.txtInfoHoraRacimo);
        txtEstado = view.findViewById(R.id.txtInfoEstadoRacimo);

        btnBuscarRacimo = view.findViewById(R.id.btnBuscarRacimo);
        btnBuscarRacimo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                limpiarCampos();
                escanearCodigo();
            }
        });


        return view;
    }

    public void buscarRacimo(String resultadoEscaneado)
    {
        DatabaseReference referenciaBaseDatos = referenciaFirebase.child("Racimos");

        referenciaBaseDatos.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChild(resultadoEscaneado))
                {

                    DataSnapshot propiedadesDelRegistro = snapshot.child(resultadoEscaneado);
                    String resultadoEstado = propiedadesDelRegistro.child("Estado").getValue(String.class);
                    String resultadoFecha = propiedadesDelRegistro.child("Fecha").getValue(String.class);
                    String resultadoHora = propiedadesDelRegistro.child("Hora").getValue(String.class);

                    txtId.setText(resultadoEscaneado);
                    txtEstado.setText(resultadoEstado);
                    txtFecha.setText(resultadoFecha);
                    txtHora.setText(resultadoHora);

                }
                else {
                    mostrarAlerta("AÚN NO SE HA REGISTRADO !!",SweetAlertDialog.ERROR_TYPE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mostrarAlerta("Error con la lectura",SweetAlertDialog.ERROR_TYPE);
            }
        });

    }

    private final ActivityResultLauncher<Intent> scanActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        String codigoEscaneado = data.getStringExtra("SCAN_RESULT");

                        //Se llama la funcion y se envia el dato escaneado
                        buscarRacimo(codigoEscaneado);
                    }
                    else {
                        mostrarAlerta("Erro al Escanear", SweetAlertDialog.ERROR_TYPE);
                    }
                }
            }
    );

    private void limpiarCampos() {
        txtId.setText("");
        txtFecha.setText("");
        txtHora.setText("");
        txtEstado.setText("");
    }

    private void escanearCodigo() {
        IntentIntegrator integrator = new IntentIntegrator(requireActivity());
        integrator.setOrientationLocked(false);
        scanActivityResultLauncher.launch(integrator.createScanIntent());
    }

    private void mostrarAlerta(String mensaje, int tipoAlerta) {
        SweetAlertDialog alertDialog = new SweetAlertDialog(getContext(), tipoAlerta);
        alertDialog.setTitleText(mensaje);
        alertDialog.show();
    }
}