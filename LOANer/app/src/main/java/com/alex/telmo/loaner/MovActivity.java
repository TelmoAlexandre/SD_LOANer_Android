package com.alex.telmo.loaner;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MovActivity extends AppCompatActivity {

    Button btnDeposit, btnWithdrawal, btnLoanPayment, btnRequestLoan;
    TextView lblFeedback, txtAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mov);

        // preparar elementos do layout
        bindLayoutElements();

        btnDeposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new sendJSON().execute("DEPOSIT");
            }
        });

        btnWithdrawal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new sendJSON().execute("WITHDRAWAL");
            }
        });

        btnRequestLoan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new sendJSON().execute("LOAN");
            }
        });

        btnLoanPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new sendJSON().execute("LOANPAYMENT");
            }
        });
    }

    /**
     * Recolhe os elementos do layout.
     *
     */
    private void bindLayoutElements() {
        btnDeposit = findViewById(R.id.btnDeposit);
        btnWithdrawal = findViewById(R.id.btnWithdrawal);
        btnLoanPayment = findViewById(R.id.btnLoanPayment);
        btnRequestLoan = findViewById(R.id.btnRequestLoan);
        lblFeedback = findViewById(R.id.lblFeedback);
        txtAmount = findViewById(R.id.txtAmount);
    }

    private class sendJSON extends AsyncTask<String, Void ,Void> {

        @Override
        protected Void doInBackground(String... str) {

            // Recolhe as informações do layout
            JsonObject json = new JsonObject();
            json.addProperty("type",str[0]);
            json.addProperty("amount", txtAmount.getText().toString());

            try
            {
                sendJson(json);
            }
            catch (Exception ex)
            {
                lblFeedback.setText(ex.getMessage());
            }

            return null;
        }
    }

    private void sendJson(JsonObject json) throws Exception
    {
        Socket sckt = new Socket("192.168.1.100", 21_150);

        DataOutputStream out = new DataOutputStream(sckt.getOutputStream());
        BufferedWriter printer = new BufferedWriter(new OutputStreamWriter(out));

        printer.write(json.toString());
        printer.newLine();
        printer.flush();
    }
}
