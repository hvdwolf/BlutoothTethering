package tk.rabidbeaver.bluetoothtetheringservicecontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;

public class BluetoothTethering extends AppCompatActivity {
    Class<?> classBluetoothPan = null;
    Constructor<?> BTPanCtor = null;
    Object BTSrvInstance = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_tethering);

        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String selectedDevice = prefs.getString("device", null);
        boolean autoconnect = prefs.getBoolean("autoconnect", false);
        boolean autotether = prefs.getBoolean("autotether", false);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        PairedDev[] pda = new PairedDev[pairedDevices.size()];
        int i = 0, selectedIDX = 0;
        for (BluetoothDevice device : pairedDevices) {
            Log.d("BTTether",device.getName()+", "+device.getAddress());
            if (selectedDevice != null && device.getAddress().contentEquals(selectedDevice)) selectedIDX = i;
            pda[i] = new PairedDev(device);
            i++;
        }

        final Spinner spinner = (Spinner) findViewById(R.id.devspin);
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, pda);
        spinner.setAdapter(spinnerArrayAdapter);

        spinner.setSelection(selectedIDX);

        Switch nswitch = (Switch) findViewById(R.id.clisw);
        nswitch.setChecked(autoconnect);
        nswitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putBoolean("autoconnect", isChecked);
                editor.putString("device", ((PairedDev) spinner.getSelectedItem()).getDev());
                editor.apply();
            }
        });

        nswitch = (Switch) findViewById(R.id.srvsw);
        nswitch.setChecked(autotether);
        nswitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putBoolean("autotether", isChecked);
                editor.apply();
            }
        });

        findViewById(R.id.srvbtn).setOnClickListener(new Switch.OnClickListener(){
            public void onClick(View v){
                try {
                    BluetoothManager bm = (BluetoothManager) getApplicationContext().getSystemService(BLUETOOTH_SERVICE);
                    BluetoothAdapter mBluetoothAdapter = bm.getAdapter();
                    mBluetoothAdapter.enable();
                    Thread.sleep(100);
                    enableTethering();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Button sett = (Button) findViewById(R.id.btset);
        sett.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intentOpenBluetoothSettings = new Intent();
                intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intentOpenBluetoothSettings);
            }
        });

        Button button = (Button) findViewById(R.id.panbtn);
        button.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                String sClassName = "android.bluetooth.BluetoothPan";

                class BTPanClientListener implements BluetoothProfile.ServiceListener {
                    @Override
                    public void onServiceConnected(final int profile, final BluetoothProfile proxy) {
                        Log.e("MyApp", "BTPan proxy connected");
                        String dev = ((PairedDev)spinner.getSelectedItem()).getDev();
                        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(dev); //e.g. this line gets the hardware address for the bluetooth device with MAC AA:BB:CC:DD:EE:FF. You can use any BluetoothDevice
                        try {
                            Method connectMethod = proxy.getClass().getDeclaredMethod("connect", BluetoothDevice.class);
                            if(!((Boolean) connectMethod.invoke(proxy, device))){
                                Log.e("MyApp", "Unable to start connection");
                            }
                        } catch (Exception e) {
                            Log.e("MyApp", "Unable to reflect android.bluetooth.BluetoothPan", e);
                        }
                    }

                    @Override
                    public void onServiceDisconnected(final int profile) {}
                }

                try {
                    Class<?> classBluetoothPan = Class.forName(sClassName);

                    Constructor<?> ctor = classBluetoothPan.getDeclaredConstructor(Context.class, BluetoothProfile.ServiceListener.class);
                    ctor.setAccessible(true);
                    ctor.newInstance(getApplicationContext(), new BTPanClientListener());
                } catch (Exception e) {
                    Log.e("MyApp", "Unable to reflect android.bluetooth.BluetoothPan", e);
                }
            }
        });
    }

    private class PairedDev {
        String dev;
        String name;
        PairedDev(BluetoothDevice btd){
            name = btd.getName();
            dev = btd.getAddress();
        }
        public String toString(){return name;}
        String getDev(){return dev;}
    }

    private void enableTethering() {
        try {
            classBluetoothPan = Class.forName("android.bluetooth.BluetoothPan");
            BTPanCtor = classBluetoothPan.getDeclaredConstructor(Context.class, BluetoothProfile.ServiceListener.class);
            BTPanCtor.setAccessible(true);
            BTSrvInstance = BTPanCtor.newInstance(getApplicationContext(), new BTPanServiceEnabler());
            Thread.sleep(250);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
