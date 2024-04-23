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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class FragmentRegistrar extends Fragment {

    public Button btnEscanear, btnGuardarDatos;
    public EditText txtIdRacimo, txtFechaRegistro, txtHoraRegistro;
    public Spinner spnOpcionesDeEstado;


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_registrar, container, false);

        //--------------- Relacionando las vistas -------------------
        txtIdRacimo = view.findViewById(R.id.txtIdRacimo);
        txtFechaRegistro = view.findViewById(R.id.txtFechaRegistro);
        txtHoraRegistro = view.findViewById(R.id.txtHoraRegistro);
        spnOpcionesDeEstado = view.findViewById(R.id.opcionesDeEstado);

        btnGuardarDatos = view.findViewById(R.id.btnGuardarDatos);
        btnEscanear = view.findViewById(R.id.btnEscanearCodigo);

        //----------- Funciones para los botones-------------------
        btnEscanear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                escanearCodigo();
            }
        });

        btnGuardarDatos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardarDatosEnFirebase();
            }
        });

        llenarSpinner();
        return view;
    }

    public void guardarDatosEnFirebase()
    {
        // Obtener los valores de los campos
        String idRacimo = txtIdRacimo.getText().toString().trim();
        String fechaRegistro = txtFechaRegistro.getText().toString().trim();
        String horaRegistro = txtHoraRegistro.getText().toString().trim();
        String estadoRacimo = spnOpcionesDeEstado.getSelectedItem().toString().trim();

        // Validar que los campos no estén vacíos
        if (idRacimo.isEmpty() || fechaRegistro.isEmpty() || horaRegistro.isEmpty() || estadoRacimo.isEmpty()) {
            mostrarAlerta("Todos los campos son obligatorios !!", SweetAlertDialog.WARNING_TYPE);
            return;  // Salir de la función si hay campos vacíos
        }

        // Crear y mostrar el ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Buscando ID...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Obtener una referencia al nodo en Firebase con el ID proporcionado
        DatabaseReference racimoExistenteRef = FirebaseDatabase.getInstance().getReference("Racimos").child(idRacimo);

        // Realizar una consulta para verificar si el ID ya existe
        racimoExistenteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Ocultar el ProgressDialog cuando se complete la búsqueda
                progressDialog.dismiss();

                if (snapshot.exists()) {
                    // Con el siguiente código para mostrar una alerta Sweet Alert:
                    mostrarAlerta("EL CODIGO YA EXISTE", SweetAlertDialog.ERROR_TYPE);
                } else {
                    guardarDatos(idRacimo, fechaRegistro, horaRegistro, estadoRacimo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Ocultar el ProgressDialog cuando se complete la búsqueda
                progressDialog.dismiss();

                // Mostrar un Toast si hay un error en la consulta
                mostrarAlerta("Error al buscar el ID", SweetAlertDialog.ERROR_TYPE);
            }
        });
    }

    // Método para realizar la operación de guardado después de verificar el ID
    private void guardarDatos(String idRacimo, String fechaRegistro, String horaRegistro, String estadoRacimo) {
        // Mostrar el ProgressDialog para la operación de guardado
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Guardando datos...");
        progressDialog.setCancelable(false);  // Evitar que el usuario cancele el ProgressDialog
        progressDialog.show();

        // Obtener una referencia al nodo principal en Firebase
        DatabaseReference racimosRef = FirebaseDatabase.getInstance().getReference("Racimos");

        // Crea un nuevo nodo con el ID como clave
        DatabaseReference racimoActualRef = racimosRef.child(idRacimo);

        // Guarda los datos dentro de ese nodo
        racimoActualRef.child("Fecha").setValue(fechaRegistro);
        racimoActualRef.child("Hora").setValue(horaRegistro);
        racimoActualRef.child("Estado").setValue(estadoRacimo, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                progressDialog.dismiss(); // Ocultar el ProgressDialog

                if (error == null) {
                    // Limpiar los campos después de guardar si no hay errores
                    limpiarCampos();

                    mostrarAlerta("GUARDADO", SweetAlertDialog.SUCCESS_TYPE);
                } else {
                    mostrarAlerta("ERROR AL GUARDAR", SweetAlertDialog.ERROR_TYPE);
                }
            }
        });
    }

    // Método para limpiar los campos
    private void limpiarCampos() {
        txtIdRacimo.setText("");
        txtFechaRegistro.setText("");
        txtHoraRegistro.setText("");
        // También puedes reiniciar el Spinner si es necesario
        spnOpcionesDeEstado.setSelection(0);
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

    public void llenarSpinner()
    {
        // Crear una lista de opciones (pueden ser cadenas)
        List<String> opciones = new ArrayList<>();
        opciones.add("Tierno");
        opciones.add("Medio");
        opciones.add("Listo_Corta");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, opciones);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnOpcionesDeEstado.setAdapter(adapter);
    }

    private final ActivityResultLauncher<Intent> scanActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {

                        String codigoEscaneado = data.getStringExtra("SCAN_RESULT");

                        //-------- Se establecen los datos a los elentos del formulario---------------------
                        String obtnerFecha = calcularFecha();
                        String obtnerHora = calcularHora();

                        txtIdRacimo.setText(codigoEscaneado);
                        txtFechaRegistro.setText(obtnerFecha);
                        txtHoraRegistro.setText(obtnerHora);
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