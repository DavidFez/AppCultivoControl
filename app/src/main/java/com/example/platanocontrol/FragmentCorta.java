package com.example.platanocontrol;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class FragmentCorta extends Fragment {

    public TextView txtInfoIdCorta, txtInfoCortaFecha;
    public Button btnRegistrarCorta, btnBuscarRacimoCorta;

    private DatabaseReference referenceFirebase;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_corta, container, false);

        //Instancimos la clase
        referenceFirebase = FirebaseDatabase.getInstance().getReference();

        //------------ Ralcionamos los elemntos -----------
        txtInfoIdCorta = view.findViewById(R.id.txtInfoCortaId);
        txtInfoCortaFecha = view.findViewById(R.id.txtInfoCortaFecha);


        //------------------- Buscar el racimo segun el codigo -------------
        btnBuscarRacimoCorta = view.findViewById(R.id.btnBuscarRacimoCorta);
        btnBuscarRacimoCorta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                limpiarCampos();
                escanearCodigo();
            }
        });

        //--------- Guardar la corta -----------------
        btnRegistrarCorta = view.findViewById(R.id.btnRegistrarCorta);
        btnRegistrarCorta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardarCortarEnFirebase();
            }
        });


        return view;
    }

    public void guardarCortarEnFirebase()
    {
        // Obtener los valores de los campos
        String idRacimo = txtInfoIdCorta.getText().toString().trim();
        String fechaDeRegistro = txtInfoCortaFecha.getText().toString().trim();
        String fechaDeCorta = calcularFecha();
        String horaDeCorta = calcularHora();
        String estadoRacimo = "Cortado";

        if (idRacimo.isEmpty() || fechaDeRegistro.isEmpty()) {
            mostrarAlerta("PRIMERO ESCANEAR EL CODIGO", SweetAlertDialog.WARNING_TYPE);
            return;
        }

        referenceFirebase = FirebaseDatabase.getInstance().getReference("Racimos").child(idRacimo);

        // Realizar una consulta para verificar si el ID ya existe
        referenceFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    referenceFirebase.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Nodo eliminado con éxito, ahora puedes guardar los nuevos datos
                            guardarDatos(idRacimo, fechaDeRegistro, fechaDeCorta, horaDeCorta, estadoRacimo);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Hubo un error al intentar eliminar el nodo
                            mostrarAlerta("NO SE ELIMINÓ", SweetAlertDialog.ERROR_TYPE);
                        }
                    });

                } else {
                    mostrarAlerta("EL RACIMO NO EXISTE !!", SweetAlertDialog.ERROR_TYPE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Mostrar un Toast si hay un error en la consulta
                mostrarAlerta("Error al buscar el ID", SweetAlertDialog.ERROR_TYPE);
            }
        });

    }

    private void guardarDatos(String idRacimo, String fechaRegistro, String fechaCorta, String horaCorta, String estadoRacimo) {
        // Mostrar el ProgressDialog para la operación de guardado
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Guardando corta...");
        progressDialog.setCancelable(false);  // Evitar que el usuario cancele el ProgressDialog
        progressDialog.show();

        // Obtener una referencia al nodo principal en Firebase
        DatabaseReference racimosRef = FirebaseDatabase.getInstance().getReference("Corta");

        // Crea un nuevo nodo con el ID como clave
        DatabaseReference racimoActualRef = racimosRef.child(idRacimo);

        // Guarda los datos dentro de ese nodo
        racimoActualRef.child("Fecha").setValue(fechaCorta);
        racimoActualRef.child("Hora").setValue(horaCorta);
        racimoActualRef.child("FechaDeRegistro").setValue(fechaRegistro);
        racimoActualRef.child("Estado").setValue(estadoRacimo, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                progressDialog.dismiss(); // Ocultar el ProgressDialog

                if (error == null) {
                    // Limpiar los campos después de guardar si no hay errores
                    limpiarCampos();
                    mostrarAlerta("CORTA GUARDADA", SweetAlertDialog.SUCCESS_TYPE);
                } else {
                    mostrarAlerta("ERROR AL GUARDAR LA CORTA", SweetAlertDialog.ERROR_TYPE);
                }
            }
        });
    }

    private void limpiarCampos() {
        txtInfoIdCorta.setText("");
        txtInfoCortaFecha.setText("");
    }

    public String calcularFecha()
    {
        // Obtener la fecha actual
        Calendar calendar = Calendar.getInstance();
        Date fechaActual = calendar.getTime();

        // Formato de fecha
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaFormateada = formatoFecha.format(fechaActual);

        return fechaFormateada;
    }

    public String calcularHora()
    {
        // Obtener la hora actual
        Calendar calendar = Calendar.getInstance();

        // Formato de hora
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String horaFormateada = formatter.format(calendar.getTime());

        return horaFormateada;
    }


    public void buscarRacimo(String resultadoEscaneado)
    {
        DatabaseReference referenciaBuscar = FirebaseDatabase.getInstance().getReference("Racimos");

        referenciaBuscar.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChild(resultadoEscaneado))
                {
                    limpiarCampos();

                    DataSnapshot propiedadesDelRegistro = snapshot.child(resultadoEscaneado);
                    String resultadoFecha = propiedadesDelRegistro.child("Fecha").getValue(String.class);

                    txtInfoIdCorta.setText(resultadoEscaneado);
                    txtInfoCortaFecha.setText(resultadoFecha);

                }
                else {
                    limpiarCampos();
                    mostrarAlerta("EL QR NO EXISTE !",SweetAlertDialog.ERROR_TYPE);
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
                        mostrarAlerta("Error al Escanear", SweetAlertDialog.ERROR_TYPE);
                    }
                }
            }
    );

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