package com.alex.telmo.loaner;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
    ImageButton btnGetMoney;
    TextView lblFeedback, txtAmount, txtTotalMoney;
    String ip = "", totalmoney = "";

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

        // Receber o dinherio do cliente da main activity
        if (getIntent().hasExtra("totalMoney"))
        {
            txtTotalMoney.setText("You have ");
            txtTotalMoney.append(getIntent().getExtras().getString("totalMoney"));
            txtTotalMoney.append(" €");
        }

        btnDeposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performMovement("DEPOSIT");
            }
        });

        btnWithdrawal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performMovement("WITHDRAWAL");
            }
        });

        btnRequestLoan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performMovement("LOAN");
            }
        });

        btnLoanPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performMovement("LOANPAYMENT");
            }
        });

        btnGetMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    performMovement("GET_TOTAL_MONEY");
            }
        });
    }

    /**
     * Verifica se existe conexão à Internet e, caso exista, inicia a Thread que envia JSON.
     *
     * @param str
     */
    private void performMovement(String str)
    {
        if (haveNetworkConnection()) {
            new sendJSON().execute(str);
        } else {
            lblFeedback.setText("Internet connection needed.");
        }
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
        txtTotalMoney = findViewById(R.id.txtTotalMoney);
        btnGetMoney = findViewById(R.id.btnGetMoney);
    }

    /**
     * Class que gere a criação e envio de JSON.
     *
     */
    private class sendJSON extends AsyncTask<String, Void ,Void> {

        @Override
        protected Void doInBackground(String... str) {

            // Recolhe as informações do layout
            JsonObject json = new JsonObject();
            json.addProperty("type",str[0]);
            json.addProperty("amount", txtAmount.getText().toString());

            try
            {
                // Cria socket e envia o Json
                Socket sckt = new Socket(ip, 21_150);
                sendJsonObject(sckt, json);

                // Aguarda resposta em Json
                JsonObject receivedJson = receiveJson(sckt);
                String response = receivedJson.get("response").getAsString();

                if (response.equals("syncMoney"))
                {
                    response = "You have " + receivedJson.get("totalMoney").getAsString() + " €";
                    txtTotalMoney.setText(response);
                }
                else
                {
                    // Oferece feedback ao utilizador dependendo do sucesso do movimento.
                    lblFeedback.setText(str[0]);
                    lblFeedback.append(
                            (response.equals("success")) ? " was successful." : " failed."
                    );
                }
            }
            catch (Exception ex)
            {
                lblFeedback.setText(ex.getMessage());
            }

            return null;
        }
    }

    /**
     * Retorna um objecto JSON recebendo-o por um socket pré-criado.
     *
     * @param sckt
     * @return
     * @throws Exception
     */
    private JsonObject receiveJson(Socket sckt) throws Exception
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(sckt.getInputStream()));
        String content = br.readLine();
        return new JsonParser().parse(content).getAsJsonObject();
    }

    /**
     * Envia um objecto JSON por um socket pré-criado.
     *
     * @param sckt
     * @param json
     * @throws Exception
     */
    private void sendJsonObject(Socket sckt, JsonObject json) throws Exception
    {
        if (!ip.equals("")){

            DataOutputStream out = new DataOutputStream(sckt.getOutputStream());
            BufferedWriter printer = new BufferedWriter(new OutputStreamWriter(out));

            printer.write(json.toString());
            printer.newLine();
            printer.flush();
        }
    }

    /**
     * Verifica se exsite conexão à Internet.
     *
     * @return
     */
    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}
