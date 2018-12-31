package com.alex.telmo.loaner;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MovActivity extends AppCompatActivity {

    Button btnDeposit, btnWithdrawal, btnLoanPayment, btnRequestLoan;
    TextView lblFeedback, txtAmount;
    String ip = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mov);

        // preparar elementos do layout
        bindLayoutElements();

        // Receber o ip da main activity
        if (getIntent().hasExtra("ip"))
        {
            ip = getIntent().getExtras().getString("ip");
        }

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

                Socket sckt = new Socket(ip, 21_150);
                send(sckt, json);

                // Aguarda resposta
                BufferedReader br = new BufferedReader(new InputStreamReader(sckt.getInputStream()));
                String content = br.readLine();
                JsonObject jsonReceived = new JsonParser().parse(content).getAsJsonObject();
                String status = jsonReceived.get("response").getAsString();

                // Oferece feedback ao utilizador dependendo do sucesso do movimento.
                lblFeedback.setText(str[0]);
                lblFeedback.append(
                        (status.equals("success")) ? " was successful." : " failed."
                );
            }
            catch (Exception ex)
            {
                lblFeedback.setText(ex.getMessage());
            }

            return null;
        }
    }

    private void send(Socket sckt, JsonObject json) throws Exception
    {
        if (!ip.equals("")){

            DataOutputStream out = new DataOutputStream(sckt.getOutputStream());
            BufferedWriter printer = new BufferedWriter(new OutputStreamWriter(out));

            printer.write(json.toString());
            printer.newLine();
            printer.flush();
        }
    }
}
