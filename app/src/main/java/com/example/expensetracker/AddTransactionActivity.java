package com.example.expensetracker;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.expensetracker.databinding.ActivityAddTransactionBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AddTransactionActivity extends AppCompatActivity {
    ActivityAddTransactionBinding binding;
    /*For sending data to firebase for storing*/
    FirebaseFirestore fStore;
    /*for Authentication of firebase*/
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    /*Whenever a person will click on check box it will create a value*/
    /**/
    String type = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        /*Initializing Firestore DB*/
        fStore = FirebaseFirestore.getInstance();

        /*Initializing FirebaseAuth*/
        firebaseAuth = FirebaseAuth.getInstance();

        /*User will combine in Auth*/
        firebaseUser = firebaseAuth.getCurrentUser();



        /*for expense*/
        binding.expenseCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "Expense";
                binding.expenseCheckBox.setChecked(true);
                binding.incomeCheckBox.setChecked(false);
            }
        });
        /*for  Income*/
        binding.incomeCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "Income";
                binding.expenseCheckBox.setChecked(false);
                binding.incomeCheckBox.setChecked(true);
            }
        });

        /*for Adding transaction details*/
        binding.btnTransctionAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amount = binding.userAmountAdd.getText().toString().trim();
                String note = binding.userNoteAdd.getText().toString().trim();

                /*validation*/
                if (amount.length() <= 0) {
                    return;
                }

                if (type.length() <= 0) {
                    Toast.makeText(AddTransactionActivity.this, "Select transactions type", Toast.LENGTH_SHORT).show();
                }
                SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy_HH:mm", Locale.getDefault());
                String currentDateAndTime = sdf.format(new Date());

                String id = UUID.randomUUID().toString();
                Map<String, Object> transactions = new HashMap<>();
                transactions.put("id", id);
                transactions.put("amount", amount);
                transactions.put("note", note);
                transactions.put("type", type);
                transactions.put("date", currentDateAndTime);
                /*for storing data on firebase*/
                fStore.collection("Expenses").document(firebaseAuth.getUid()).collection("Notes").document(id)
                        .set(transactions)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(AddTransactionActivity.this, "Added", Toast.LENGTH_SHORT).show();
                                binding.userNoteAdd.setText("");
                                binding.userAmountAdd.setText("");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AddTransactionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });


    }
}