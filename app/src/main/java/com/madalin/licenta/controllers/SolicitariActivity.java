package com.madalin.licenta.controllers;

import static com.madalin.licenta.global.EdgeToEdge.edgeToEdge;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.madalin.licenta.R;
import com.madalin.licenta.controllers.fragments.SolicitariStadiuFragment;
import com.madalin.licenta.global.EdgeToEdge;
import com.madalin.licenta.models.Solicitare;

public class SolicitariActivity extends AppCompatActivity {

    private LinearLayout toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitari);
        initializareVederi();
        edgeToEdge(this, toolbar, EdgeToEdge.Spatiere.PADDING, EdgeToEdge.Directie.SUS);
        edgeToEdge(this, viewPager2, EdgeToEdge.Spatiere.PADDING, EdgeToEdge.Directie.JOS);

        // seteaza adapter-ul pentru ViewPager2
        viewPager2.setAdapter(new SolicitariAdapter(this));

        // la apasarea unui Tab se va seta pagina selectata
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition()); // seteaza pagina care se afla la aceeasi pozitie cu Tab-ul selectat
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // la schimbarea paginii din ViewPager se va selecta Tab-ul corespunzator paginii
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select(); // selecteaza Tab-ul care se afla la aceeasi pozitie cu pagina
            }
        });
    }

    public class SolicitariAdapter extends FragmentStateAdapter {

        public SolicitariAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 1:
                    return SolicitariStadiuFragment.newInstance(Solicitare.NEEVALUATA);
                case 2:
                    return SolicitariStadiuFragment.newInstance(Solicitare.ACCEPTATA);
                default:
                    return SolicitariStadiuFragment.newInstance(Solicitare.RESPINSA);
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    /**
     * Initializeaza vederile din aceasta activitate.
     */
    private void initializareVederi() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.solicitari_tabLayout);
        viewPager2 = findViewById(R.id.solicitari_viewPager2);

        toolbar.findViewById(R.id.toolbar_imageViewButonInapoi).setOnClickListener(v -> super.onBackPressed());
        TextView textView = toolbar.findViewById(R.id.toolbar_textViewTitlu);
        textView.setText("Solicitări & Licențe");
    }
}