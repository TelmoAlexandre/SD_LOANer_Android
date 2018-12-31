package com.alex.telmo.loaner;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Base64;

public class Main extends AppCompatActivity {

    private Button btnLogin;
    private Spinner spIP;
    private TextView lblFeedback, txtPassword;

    String clientName, psw;
    JsonObject json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // preparar elementos do layout
        bindLayoutElements();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                psw = getPasswordHash(txtPassword.getText().toString());

                // Cria o Json
                json = new JsonObject();
                json.addProperty("type" , "AUTHENTICATE");
                json.addProperty("password", psw);

                new sendJSON().execute();
            }
        });
    }

    /**
     * Recolhe os elementos do layout.
     *
     */
    private void bindLayoutElements() {
        btnLogin = findViewById(R.id.btnLogin);
        lblFeedback = findViewById(R.id.lblFeedbackMain);
        txtPassword = findViewById(R.id.txtPassword);
        spIP = findViewById(R.id.spIP);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private String getPasswordHash(String psw)
    {
        try
        {
            MessageDigest hash = MessageDigest
                    .getInstance("SHA-512");

            hash.update(psw.getBytes());
            return Base64.getEncoder().encodeToString(hash.digest());
        }
        catch (Exception ex)
        {
            lblFeedback.setText("Error processing password.");
        }

        return null;
    }

    private class sendJSON extends AsyncTask<String, Void ,Void> {

        @Override
        protected Void doInBackground(String... str) {

            BufferedWriter bPrinter = null;
            Socket sckt = null;
            try
            {
                sckt = new Socket(spIP.getSelectedItem().toString(), 21_150);

                DataOutputStream out = new DataOutputStream(sckt.getOutputStream());
                bPrinter = new BufferedWriter(new OutputStreamWriter(out));
                BufferedReader br = new BufferedReader(new InputStreamReader(sckt.getInputStream()));

                // Envia JSON
                bPrinter.write(json.toString());
                bPrinter.newLine();
                bPrinter.flush();

                // Aguarda resposta
                String content = br.readLine();
                JsonObject json = new JsonParser().parse(content).getAsJsonObject();
                String status = json.get("response").getAsString();

                if (status.equals("success"))
                {
                    openMovActivity();
                    lblFeedback.setText("Welcome Telmo");
                }else{
                    lblFeedback.setText("Authentication has failed.");
                }
            }
            catch (Exception ex)
            {
                lblFeedback.setText(ex.getMessage());
            }
            finally
            {
                try
                {
                    if (bPrinter != null) {
                        bPrinter.close();
                    }
                    if (sckt != null){
                        sckt.close();
                    }
                }
                catch ( IOException ex )
                {
                    lblFeedback.setText(ex.getMessage());
                }
            }

            return null;
        }
    }

    private void openMovActivity()
    {
        Intent movActivity = new Intent(getApplicationContext(), MovActivity.class);
        movActivity.putExtra("ip", spIP.getSelectedItem().toString());
        startActivity(movActivity);
    }
}
