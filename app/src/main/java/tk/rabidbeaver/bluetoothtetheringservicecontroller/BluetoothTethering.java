package tk.rabidbeaver.bluetoothtetheringservicecontroller;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;


public class BluetoothTethering extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter = null;
    Class<?> classBluetoothPan = null;
    Constructor<?> BTPanCtor = null;
    Object BTSrvInstance = null;
    Class<?> noparams[] = {};
    Method mIsBTTetheringOn;
    public static Switch toggle ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_tethering);
        toggleTethering(false);
        toggle = (Switch) findViewById(R.id.wifi_switch);
        toggle.setOnClickListener(new Switch.OnClickListener(){
            public void onClick(View v){
                try {
                    mBluetoothAdapter = getBTAdapter();
                    mBluetoothAdapter.enable();
                    Thread.sleep(100);
                    toggleTethering(true);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        PairedDev[] pda = new PairedDev[pairedDevices.size()];
        int i = 0;
        for (BluetoothDevice device : pairedDevices) {
            Log.d("BTTether",device.getName()+", "+device.getAddress());
            pda[i] = new PairedDev(device);
            i++;
        }

        final Spinner spinner = (Spinner) findViewById(R.id.devspin);
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item,
                pda);
        spinner.setAdapter(spinnerArrayAdapter);

        Button button = (Button) findViewById(R.id.panbtn);
        button.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                String sClassName = "android.bluetooth.BluetoothPan";

                class BTPanClientListener implements BluetoothProfile.ServiceListener {

                    private final Context context;

                    public BTPanClientListener(final Context context) {
                        this.context = context;
                    }

                    @Override
                    public void onServiceConnected(final int profile,
                                                   final BluetoothProfile proxy) {
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
                    public void onServiceDisconnected(final int profile) {
                    }
                }

                try {

                    Class<?> classBluetoothPan = Class.forName(sClassName);

                    Constructor<?> ctor = classBluetoothPan.getDeclaredConstructor(Context.class, BluetoothProfile.ServiceListener.class);
                    ctor.setAccessible(true);
                    Object instance = ctor.newInstance(getApplicationContext(), new BTPanClientListener(getApplicationContext()));
                } catch (Exception e) {
                    Log.e("MyApp", "Unable to reflect android.bluetooth.BluetoothPan", e);
                }
            }
        });
    }

    private class PairedDev {
        String dev;
        String name;
        public PairedDev(BluetoothDevice btd){
            name = btd.getName();
            dev = btd.getAddress();
        }
        public String toString(){return name;}
        public String getDev(){return dev;}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bluetooth_tethering, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void toggleTethering(boolean enable) {
        Context MyContext = getApplicationContext();
        mBluetoothAdapter = getBTAdapter();
        String sClassName = "android.bluetooth.BluetoothPan";
        try {
            classBluetoothPan = Class.forName("android.bluetooth.BluetoothPan");
            mIsBTTetheringOn = classBluetoothPan.getDeclaredMethod("isTetheringOn", noparams);
            BTPanCtor = classBluetoothPan.getDeclaredConstructor(Context.class, BluetoothProfile.ServiceListener.class);
            BTPanCtor.setAccessible(true);

            BTSrvInstance = BTPanCtor.newInstance(MyContext, new BTPanServiceListener(MyContext, enable));
            Thread.sleep(250);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    private BluetoothAdapter getBTAdapter() {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1)
            return BluetoothAdapter.getDefaultAdapter();
        else {
            BluetoothManager bm = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
            return bm.getAdapter();
        }
    }

    public  static void setToggleState(boolean state) {
        try{
            if(state){
                toggle.setChecked(BTPanServiceListener.state);
            }else {
                toggle.setChecked(BTPanServiceListener.state);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
