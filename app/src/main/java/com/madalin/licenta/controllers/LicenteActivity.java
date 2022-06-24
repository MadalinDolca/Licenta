package com.madalin.licenta.controllers;

import static com.madalin.licenta.global.EdgeToEdge.edgeToEdge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.madalin.licenta.R;
import com.madalin.licenta.adapters.BannerLicentaAdapter;
import com.madalin.licenta.controllers.fragments.LicenteTipFragment;
import com.madalin.licenta.controllers.fragments.SolicitariStadiuFragment;
import com.madalin.licenta.global.EdgeToEdge;
import com.madalin.licenta.models.Licenta;
import com.madalin.licenta.models.Solicitare;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LicenteActivity extends AppCompatActivity {

    private LinearLayout toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licente);

        initializareVederi();
        edgeToEdge(this, toolbar, EdgeToEdge.Spatiere.PADDING, EdgeToEdge.Directie.SUS);
        edgeToEdge(this, viewPager2, EdgeToEdge.Spatiere.MARGIN, EdgeToEdge.Directie.JOS);

        // seteaza adapter-ul pentru ViewPager2
        viewPager2.setAdapter(new LicenteAdapter(this));

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

    /**
     * Adapter pentru a oferi vederi de pagina la cerere. Furnizeaza instante ale
     * {@link LicenteTipFragment} ca pagini pentru {@link #viewPager2} in functie de tipul de
     * {@link Licenta}.
     */
    public class LicenteAdapter extends FragmentStateAdapter {

        public LicenteAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return LicenteTipFragment.newInstance(Licenta.PRIMITA);

                default:
                    return LicenteTipFragment.newInstance(Licenta.OFERITA);
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    /**
     * Initializeaza vederile din aceasta activitate.
     */
    private void initializareVederi() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.licente_tabLayout);
        viewPager2 = findViewById(R.id.licente_viewPager2);

        toolbar.findViewById(R.id.toolbar_imageViewButonInapoi).setOnClickListener(v -> super.onBackPressed());
        TextView textView = toolbar.findViewById(R.id.toolbar_textViewTitlu);
        textView.setText("Licențe");
    }
}