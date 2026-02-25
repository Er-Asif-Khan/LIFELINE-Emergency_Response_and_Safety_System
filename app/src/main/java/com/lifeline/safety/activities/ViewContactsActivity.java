package com.lifeline.safety.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.*;

import android.view.View;
import android.widget.*;
import com.lifeline.safety.R;
import com.lifeline.safety.adapters.ContactAdapter;
import com.lifeline.safety.db.DatabaseHelper;
import com.lifeline.safety.models.Contact;

import java.util.ArrayList;
import java.util.List;

public class ViewContactsActivity extends AppCompatActivity {

    private ImageView backButton;
    private ImageView settingsButton;
    private RecyclerView rv;
    private CardView addContactButton;
    private DatabaseHelper db;
    private ContactAdapter contactAdapter;
    private List<Contact> contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_contacts);

        initializeViews();
        setupDatabase();
        setupRecyclerView();
        setupClickListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshContactList();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        settingsButton = findViewById(R.id.settingsButton);
        rv = findViewById(R.id.recyclerContacts);
        addContactButton = findViewById(R.id.addContactButton);
    }

    private void setupDatabase() {
        db = new DatabaseHelper(this);
    }

    private void setupRecyclerView() {
        contactList = db.getAllContacts();
        if (contactList == null) {
            contactList = new ArrayList<>();
        }

        TextView tvEmpty = findViewById(R.id.tvEmptyContacts);

        if (contactList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
        }

        contactAdapter = new ContactAdapter(new ArrayList<>(contactList), db);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(contactAdapter);
    }

    private void setupClickListener() {
        backButton.setOnClickListener(v -> finish());
        settingsButton.setOnClickListener(v -> openSettings());
        addContactButton.setOnClickListener(v -> startActivity(new Intent(this, AddContactActivity.class)));

        // Bottom navigation
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navHistory = findViewById(R.id.navHistory);
        RelativeLayout navSOS = findViewById(R.id.navSOS);
        LinearLayout navContacts = findViewById(R.id.navContacts);
        LinearLayout navSettings = findViewById(R.id.navSettings);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        navHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, AlertHistoryActivity.class));
            finish();
        });

        navSOS.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        navContacts.setOnClickListener(v -> {
            // Already on Contacts
        });

        navSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SafetyGuideActivity.class));
            finish();
        });
    }

    private void openSettings() {
        Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show();
    }

    private void refreshContactList() {
        contactList = db.getAllContacts();
        if (contactList == null) {
            contactList = new ArrayList<>();
        }

        TextView tvEmpty = findViewById(R.id.tvEmptyContacts);

        if (contactList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
        }

        if (contactAdapter != null) {
            contactAdapter.updateContacts(new ArrayList<>(contactList));
        } else {
            contactAdapter = new ContactAdapter(new ArrayList<>(contactList), db);
            rv.setLayoutManager(new LinearLayoutManager(this));
            rv.setAdapter(contactAdapter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close database connection
        if (db != null) {
            db.close();
        }
    }
}