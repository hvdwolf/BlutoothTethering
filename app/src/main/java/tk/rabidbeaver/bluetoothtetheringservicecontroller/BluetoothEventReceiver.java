package tk.rabidbeaver.bluetoothtetheringservicecontroller;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static android.content.Context.BLUETOOTH_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class BluetoothEventReceiver extends BroadcastReceiver {
    private BluetoothAdapter mBluetoothAdapter = null;
    Class<?> classBluetoothPan = null;
    Constructor<?> BTPanCtor = null;
    Object BTSrvInstance = null;
    Class<?> noparams[] = {};
    Method mIsBTTetheringOn;
    private Context c;
    BluetoothDevice device;


    @Override
    public void onReceive(Context context, Intent intent) {
        c = context;

        SharedPreferences prefs = c.getSharedPreferences("Settings", MODE_PRIVATE);
        String selectedDevice = prefs.getString("device", null);
        boolean autoconnect = prefs.getBoolean("autoconnect", false);
        boolean autotether = prefs.getBoolean("autotether", false);

        int state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
        if (autotether && state == BluetoothAdapter.STATE_ON){
            Log.d("BluetoothEventReceiver","Bluetooth has turned ON");

            try {
                Thread.sleep(250);
                enableTethering();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        int connstate = intent.getIntExtra("android.bluetooth.adapter.extra.CONNECTION_STATE", -1);
        if (connstate == BluetoothAdapter.STATE_CONNECTED){
            Log.d("BluetoothEventReceiver","Bluetooth has CONNECTED");
            device = intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            if (device != null) {
                Log.d("BluetoothEventReceiver", "Bluetooth has connected to " + device.getName());
                if (autoconnect && selectedDevice != null && device.getAddress().contentEquals(selectedDevice)) {
                    // TODO if device is in auto-tether list, and device not currently tethering, connect PAN
                    // TODO in UI: warn user that this is for car radio, not tablet, since tablet wont auto connect
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
                        Object instance = ctor.newInstance(c, new BTPanClientListener(c));
                    } catch (Exception e) {
                        Log.e("MyApp", "Unable to reflect android.bluetooth.BluetoothPan", e);
                    }
                }
            }
        }
/*
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Set<String> keys = bundle.keySet();
            Iterator<String> it = keys.iterator();
            Log.e("BluetoothEventReceiver","Dumping Intent start");
            while (it.hasNext()) {
                String key = it.next();
                Log.e("BluetoothEventReceiver","[" + key + "=" + bundle.get(key)+"]");
            }
            Log.e("BluetoothEventReceiver","Dumping Intent end");
        }
*/
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

    public void enableTethering() {
        mBluetoothAdapter = getBTAdapter();
        try {
            classBluetoothPan = Class.forName("android.bluetooth.BluetoothPan");
            mIsBTTetheringOn = classBluetoothPan.getDeclaredMethod("isTetheringOn", noparams);
            BTPanCtor = classBluetoothPan.getDeclaredConstructor(Context.class, BluetoothProfile.ServiceListener.class);
            BTPanCtor.setAccessible(true);

            BTSrvInstance = BTPanCtor.newInstance(c, new BTPanServiceEnabler());
            Thread.sleep(250);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    private BluetoothAdapter getBTAdapter() {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1)
            return BluetoothAdapter.getDefaultAdapter();
        else {
            BluetoothManager bm = (BluetoothManager) c.getSystemService(BLUETOOTH_SERVICE);
            return bm.getAdapter();
        }
    }
}