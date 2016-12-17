package tk.rabidbeaver.bluetoothtetheringservicecontroller;

import android.bluetooth.BluetoothProfile;
import android.util.Log;

public class BTPanServiceEnabler implements BluetoothProfile.ServiceListener {

    public BTPanServiceEnabler() {}

    @Override
    public void onServiceConnected(final int profile, final BluetoothProfile proxy) {
        Log.i("BTPanServiceEnabler", "BTPan proxy connected");
        try {
            boolean nowVal;
            do {
                nowVal = ((Boolean) proxy.getClass().getMethod("isTetheringOn", new Class[0]).invoke(proxy, new Object[0])).booleanValue();
                Log.d("BTPanServiceEnabler", "State: "+Boolean.toString(nowVal));
                if (!nowVal) {
                    Log.d("BTPanServiceEnabler","trying to enable tethering");
                    proxy.getClass().getMethod("setBluetoothTethering", new Class[]{Boolean.TYPE}).invoke(proxy, new Object[]{Boolean.valueOf(true)});
                    Thread.sleep(250);
                }
            } while (!nowVal);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(final int profile) {
    }
}
