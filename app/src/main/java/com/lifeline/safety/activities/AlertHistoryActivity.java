package com.lifeline.safety.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.lifeline.safety.R;
import com.lifeline.safety.adapters.AlertHistoryAdapter;
import com.lifeline.safety.db.DatabaseHelper;

public class AlertHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_history);

        // Initialize views
        ImageView backButton = findViewById(R.id.backButton);
        RecyclerView rv = findViewById(R.id.recyclerHistory);
        TextView empty = findViewById(R.id.tvEmpty);

        // Filter tabs
        TextView filterAll = findViewById(R.id.filterAll);
        TextView filterResolved = findViewById(R.id.filterResolved);
        TextView filterActive = findViewById(R.id.filterActive);

        // Bottom navigation
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navHistory = findViewById(R.id.navHistory);
        RelativeLayout navSOS = findViewById(R.id.navSOS);
        LinearLayout navContacts = findViewById(R.id.navContacts);
        LinearLayout navSettings = findViewById(R.id.navSettings);

        // Back button click
        backButton.setOnClickListener(v -> finish());

        // Load data
        DatabaseHelper db = new DatabaseHelper(this);
        var history = db.getAllAlertHistory();

        if(history.isEmpty()){
            empty.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        } else {
            rv.setLayoutManager(new LinearLayoutManager(this));
            rv.setHasFixedSize(true);
            rv.setAdapter(new AlertHistoryAdapter(history));
        }

        // Filter tab clicks (you can implement filtering logic later)
        filterAll.setOnClickListener(v -> {
            // Update UI and filter
            updateFilterTabs(filterAll, filterResolved, filterActive);
            // TODO: Filter and update adapter
        });

        filterResolved.setOnClickListener(v -> {
            updateFilterTabs(filterResolved, filterAll, filterActive);
            // TODO: Filter and update adapter
        });

        filterActive.setOnClickListener(v -> {
            updateFilterTabs(filterActive, filterAll, filterResolved);
            // TODO: Filter and update adapter
        });

        // Bottom navigation clicks
        navHome.setOnClickListener(v -> {
            // Navigate to Home
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        navHistory.setOnClickListener(v -> {
            // Already on History - do nothing
        });

        navSOS.setOnClickListener(v -> {
            // Trigger SOS
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        navContacts.setOnClickListener(v -> {
            // Navigate to Contacts
            startActivity(new Intent(this, ViewContactsActivity.class));
            finish();
        });

        navSettings.setOnClickListener(v -> {
            // Navigate to Safety Guide
            startActivity(new Intent(this, SafetyGuideActivity.class));
            finish();
        });
    }

    private void updateFilterTabs(TextView selected, TextView... others) {
        // Update selected tab
        selected.setBackgroundResource(R.drawable.tab_selected_bg);
        selected.setTextColor(getResources().getColor(android.R.color.white, null));

        // Update other tabs
        for (TextView other : others) {
            other.setBackgroundResource(R.drawable.tab_unselected_bg);
            other.setTextColor(getResources().getColor(R.color.light_home_text, null));
        }
    }
}