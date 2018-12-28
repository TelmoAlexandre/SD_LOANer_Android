package com.alex.telmo.loaner;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Main extends AppCompatActivity {

    Button btnLogin;
    TextView lblFeedback, pswClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindLayoutElements();
    }

    /**
     * Recolhe os elementos do layout.
     *
     */
    private void bindLayoutElements() {
        btnLogin = (Button) findViewById(R.id.btnLogin);
        lblFeedback = (TextView) findViewById(R.id.lblFeedback);
        pswClient = (TextView) findViewById(R.id.pswClient);
    }
}
