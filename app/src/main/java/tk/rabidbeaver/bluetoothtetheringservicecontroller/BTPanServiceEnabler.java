package tk.rabidbeaver.bluetoothtetheringservicecontroller;

import android.bluetooth.BluetoothProfile;
import android.util.Log;

class BTPanServiceEnabler implements BluetoothProfile.ServiceListener {
    @Override
    public void onServiceConnected(final int profile, final BluetoothProfile proxy) {
        Log.i("BTPanServiceEnabler", "BTPan proxy connected");
        try {
            boolean nowVal;
            do {
                nowVal = ((Boolean) proxy.getClass().getMethod("isTetheringOn", new Class[0]).invoke(proxy));
                Log.d("BTPanServiceEnabler", "State: "+Boolean.toString(nowVal));
                if (!nowVal) {
                    Log.d("BTPanServiceEnabler","trying to enable tethering");
                    proxy.getClass().getMethod("setBluetoothTethering", new Class[]{Boolean.TYPE}).invoke(proxy, true);
                    Thread.sleep(250);
                }
            } while (!nowVal);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(final int profile) {}
}
