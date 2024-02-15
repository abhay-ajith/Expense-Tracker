package com.example.expensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.expensetracker.databinding.ActivityDashboardBinding;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity {
    ActivityDashboardBinding binding;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    int sumExpense = 0;
    int sumIncome = 0;

    ArrayList<TransactionModel> transactionModelArrayList;
    TransactionAdapter transactionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        transactionModelArrayList = new ArrayList<>();
        binding.historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.historyRecyclerView.setHasFixedSize(true);
        binding.addFloatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(DashboardActivity.this, AddTransactionActivity.class));
                } catch (Exception e) {
                    Log.e("DashboardActivity", "Error starting AddTransactionActivity", e);
                }
            }
        });

        /* Whenever a user adds new transaction details, it will refresh those added details */
        binding.refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(DashboardActivity.this, DashboardActivity.class));
                    finish();
                } catch (Exception e) {
                    Log.e("DashboardActivity", "Error refreshing dashboard", e);
                }
            }
        });
        loadData();
    }

    /* For fetching added transaction details and showing calculated value on dashboard Activity */
    private void loadData() {
        firebaseFirestore.collection("Expenses").document(firebaseAuth.getUid()).collection("Notes")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot ds : task.getResult()) {
                                TransactionModel model = new TransactionModel(ds.getString("id"),
                                        ds.getString("note"), ds.getString("amount"),
                                        ds.getString("type"), ds.getString("date"));

                                int amount = Integer.parseInt(ds.getString("amount"));
                                if ("Expense".equals(ds.getString("type"))) {
                                    sumExpense += amount;
                                } else {
                                    sumIncome += amount;
                                }
                                transactionModelArrayList.add(model);
                            }
                            binding.totalIncome.setText(String.valueOf(sumIncome));
                            binding.totalExpense.setText(String.valueOf(sumExpense));
                            binding.totalBalance.setText(String.valueOf(sumIncome - sumExpense));

                            // Update PieChart
                            updatePieChart();

                            transactionAdapter = new TransactionAdapter(DashboardActivity.this, transactionModelArrayList);
                            binding.historyRecyclerView.setAdapter(transactionAdapter);
                        } else {
                            Log.e("DashboardActivity", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    /* Update PieChart with income and expense data */
    private void updatePieChart() {
        PieChart pieChart = binding.pieChart;
        if (pieChart != null) {
            ArrayList<PieEntry> entries = new ArrayList<>();
            entries.add(new PieEntry(sumIncome, "Income"));
            entries.add(new PieEntry(sumExpense, "Expense"));

            PieDataSet dataSet = new PieDataSet(entries, "Expense vs Income");
            dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

            PieData data = new PieData(dataSet);
            pieChart.setData(data);
            pieChart.invalidate();
        } else {
            Log.e("DashboardActivity", "PieChart is null");
        }
    }
}
