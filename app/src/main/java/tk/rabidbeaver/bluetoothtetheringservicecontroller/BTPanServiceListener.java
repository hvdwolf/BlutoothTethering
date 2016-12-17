package tk.rabidbeaver.bluetoothtetheringservicecontroller;

import java.lang.reflect.InvocationTargetException;

import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class BTPanServiceListener implements BluetoothProfile.ServiceListener {
    private final Context context;
    public static boolean state = false;
    private boolean enable = false;

    public BTPanServiceListener(final Context context, final boolean enable) {
        this.context = context;
        this.enable = enable;
    }

    @Override
    public void onServiceConnected(final int profile,
                                   final BluetoothProfile proxy) {
        //Some code must be here or the compiler will optimize away this callback.
        Log.i("MyApp", "BTPan proxy connected");
        try {
            boolean nowVal = ((Boolean) proxy.getClass().getMethod("isTetheringOn", new Class[0]).invoke(proxy, new Object[0])).booleanValue();
            if (nowVal && enable) {
                proxy.getClass().getMethod("setBluetoothTethering", new Class[]{Boolean.TYPE}).invoke(proxy, new Object[]{Boolean.valueOf(false)});
                Toast.makeText(context, "Turning bluetooth tethering off", Toast.LENGTH_SHORT).show();
                state = false;
            } else if (enable) {
                proxy.getClass().getMethod("setBluetoothTethering", new Class[]{Boolean.TYPE}).invoke(proxy, new Object[]{Boolean.valueOf(true)});
                Toast.makeText(context, "Turning bluetooth tethering on", Toast.LENGTH_SHORT).show();
                state = true;
            } else state = nowVal;
            // TODO: need to figure out a way to do this that doesn't cause a toggle loop.
            BluetoothTethering.setToggleState(state);
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(final int profile) {
    }
}
