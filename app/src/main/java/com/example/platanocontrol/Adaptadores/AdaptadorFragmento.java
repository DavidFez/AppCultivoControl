package com.example.platanocontrol.Adaptadores;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.platanocontrol.FragmentCorta;
import com.example.platanocontrol.FragmentEstadisticas;
import com.example.platanocontrol.FragmentRegistrar;
import com.example.platanocontrol.FragmentVer;

public class AdaptadorFragmento extends FragmentStateAdapter {

    public AdaptadorFragmento(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position)
        {
            case 0:
                return new FragmentRegistrar();
            case 1:
                return new FragmentVer();
            case 2:
                return new FragmentCorta();
            default:
                return new FragmentEstadisticas();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
