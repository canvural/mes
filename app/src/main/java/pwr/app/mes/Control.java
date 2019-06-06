package pwr.app.mes;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class Control extends AppCompatActivity implements Dance.OnFragmentInteractionListener, Manual.OnFragmentInteractionListener {
    String address = null;
    TabLayout tabLayout;
    TabItem tabDance;
    TabItem tabManual;
    ViewPager viewPager;
    BluetoothAdapter bluetoothAdapter = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        address = intent.getStringExtra(MainActivity.EXTRA_ADDRESS);

        setContentView(R.layout.content_control);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        new ConnectBT().execute();

        FloatingActionButton fab = findViewById(R.id.disFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Unpair", Snackbar.LENGTH_LONG)
                        .setAction("Action", new View.OnClickListener() {

                                    @Override
                                    public void onClick(View v) {

                                        Control.this.disconnect();
                                    }
                                }
                        ).show();
            }
        });

        tabLayout = findViewById(R.id.tabLayout);
        tabDance = findViewById(R.id.danceItem);
        tabManual = findViewById(R.id.manualItem);
        viewPager = findViewById(R.id.viewPager);

        PageAdapter pageAdapter = new PageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pageAdapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 1) {
                    tabLayout.setBackgroundColor(ContextCompat.getColor(Control.this, android.R.color.darker_gray));
                    getWindow().setStatusBarColor(ContextCompat.getColor(Control.this, android.R.color.darker_gray));
                } else if (tab.getPosition() == 2) {
                    tabLayout.setBackgroundColor(ContextCompat.getColor(Control.this, android.R.color.darker_gray));
                    getWindow().setStatusBarColor(ContextCompat.getColor(Control.this, android.R.color.darker_gray));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        viewPager.setVisibility(View.INVISIBLE);
    }

    public void disconnect() {
        try {
            btSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendSignal(String signal) {
        if (btSocket != null) {
            try {
                Log.i("SIGNAL", signal);
                btSocket.getOutputStream().write(signal.toString().getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFragmentInteraction(String signal) {
        sendSignal(signal);
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            msg("Connecting to device");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isBtConnected) {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = bluetoothAdapter.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                Toast.makeText(getApplicationContext(), "Could not connect to device", Toast.LENGTH_LONG).show();
                finish();
            } else {
                msg("Connected!");
                viewPager.setVisibility(View.VISIBLE);
                isBtConnected = true;
            }
        }
    }

}
