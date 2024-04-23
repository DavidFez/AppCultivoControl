package com.example.platanocontrol;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class FragmentEstadisticas extends Fragment {

    public TextView txtTotalRacimos, txtTotalRacimosCorta;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_estadisticas, container, false);

        // Relacionar el elemento
        txtTotalRacimos = view.findViewById(R.id.txtTotalDeRacimos);
        txtTotalRacimosCorta = view.findViewById(R.id.txtTotalRacimoCorta);

        // Llamar al método para calcular y mostrar el total de racimos
        calculoDeRacimos();
        calculoRacimosCortados();


        return view;
    }

    public void calculoDeRacimos() {
        DatabaseReference referenceNodoRacimos = FirebaseDatabase.getInstance().getReference("Racimos");
        referenceNodoRacimos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Se ejecuta cada vez que hay cambios en el nodo "Racimos"
                long totalDeRacimos = snapshot.getChildrenCount();
                String conversionTotalDeRacimos = String.valueOf(totalDeRacimos);
                txtTotalRacimos.setText(conversionTotalDeRacimos);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mostrarAlerta("NO SE PUDO CONTAR", SweetAlertDialog.ERROR_TYPE);
            }
        });
    }

    public void calculoRacimosCortados(){
        DatabaseReference referenciaNodoCorta = FirebaseDatabase.getInstance().getReference("Corta");
        referenciaNodoCorta.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalCortaRacimos = snapshot.getChildrenCount();
                String conversionCortaTotal = String.valueOf(totalCortaRacimos);
                txtTotalRacimosCorta.setText(conversionCortaTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void mostrarAlerta(String mensaje, int tipoAlerta) {
        SweetAlertDialog alertDialog = new SweetAlertDialog(getContext(), tipoAlerta);
        alertDialog.setTitleText(mensaje);
        alertDialog.show();
    }
}
