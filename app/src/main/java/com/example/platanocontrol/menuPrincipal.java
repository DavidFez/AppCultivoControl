package com.example.platanocontrol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.example.platanocontrol.Adaptadores.AdaptadorFragmento;
import com.google.android.material.tabs.TabLayout;

public class menuPrincipal extends AppCompatActivity {

    public TabLayout MenuTab;
    public ViewPager2 PageView2;
    public AdaptadorFragmento adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);

        MenuTab = findViewById(R.id.tad);


        TabLayout.Tab tab1 = MenuTab.newTab().setIcon(R.drawable.agregar);
        TabLayout.Tab tab2 = MenuTab.newTab().setIcon(R.drawable.info);
        TabLayout.Tab tab3 = MenuTab.newTab().setIcon(R.drawable.corta);
        TabLayout.Tab tab4 = MenuTab.newTab().setText("Info");


        MenuTab.addTab(tab1);
        MenuTab.addTab(tab2);
        MenuTab.addTab(tab3);
        MenuTab.addTab(tab4);

        PageView2 = findViewById(R.id.viewpage);
        adapter = new AdaptadorFragmento(this);
        PageView2.setAdapter(adapter);


        MenuTab.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                PageView2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });
        PageView2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                MenuTab.getTabAt(position).select();
                super.onPageSelected(position);
            }
        });


    }
}