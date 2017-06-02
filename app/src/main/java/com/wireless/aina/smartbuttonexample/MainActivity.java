/*
    ©2017 Aina Wireless Inc., All rights reserved.
    ----------------------------------------------

    This file is part of Aina Wireless Inc's Smart Button example.

    Smart Button example is free software: you can redistribute it and/or
    modify it under the terms of the GNU General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Pairing example is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.wireless.aina.smartbuttonexample;

import android.app.AlertDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.Manifest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private BluetoothLeScanner  BLEScanner;
    private BluetoothDevice     BLEDevice;
    private BluetoothGatt       BLEGatt;
    private BluetoothManager    BLEManager;
    private BluetoothAdapter    BLEAdapter;

    private final Handler       TextUpdateHandler = new Handler();

    private boolean             NoScanNeeded = false;

    private boolean             GetSwVersion = false;
    private boolean             GetBattLevel = false;
    private boolean             GetButtons   = false;

    private boolean             Ready        = false;

    private int                 smart_batt_level  = 0x00;
    private int                 smart_button_mask = 0x00;
    private String              smart_sw_version  = "";

    private TextView            TextView_service_1;
    private TextView            TextView_service_2;
    private TextView            TextView_service_3;
    private TextView            TextView_service_4;
    private TextView            TextView_service_5;
    private TextView            TextView_service_6;
    private TextView            TextView_sw_version;
    private TextView            TextView_button_mask;
    private TextView            TextView_buttons;
    private TextView            TextView_battlevel;

    private Button              Button_red_led;
    private Button              Button_green_led;
    private Button              Button_amber_led;
    private Button              Button_off_led;

    private List<BluetoothGattCharacteristic> Aina_Chars;

    private final static int    REQUEST_ENABLE_BT    = 1;

    private static final UUID   CLIENT_CHAR_CONFIG   = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");

    private static final UUID   AINA_SERV  = UUID.fromString("127FACE1-CB21-11E5-93D0-0002A5D5C51B");
    private static final UUID   BATT_SERV  = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB");
    private static final UUID   SW_VERS    = UUID.fromString("127FC0FF-CB21-11E5-93D0-0002A5D5C51B");
    private static final UUID   BUTTONS    = UUID.fromString("127FBEEF-CB21-11E5-93D0-0002A5D5C51B");
    private static final UUID   LEDS       = UUID.fromString("127FDEAD-CB21-11E5-93D0-0002A5D5C51B");
    private static final UUID   BATT_LEVEL = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        TextView_service_1   = (TextView) findViewById(R.id.service1);
        TextView_service_2   = (TextView) findViewById(R.id.service2);
        TextView_service_3   = (TextView) findViewById(R.id.service3);
        TextView_service_4   = (TextView) findViewById(R.id.service4);
        TextView_service_5   = (TextView) findViewById(R.id.service5);
        TextView_service_6   = (TextView) findViewById(R.id.service6);

        TextView_sw_version  = (TextView) findViewById(R.id.sw_version);
        TextView_button_mask = (TextView) findViewById(R.id.textView_buttonmask);
        TextView_buttons     = (TextView) findViewById(R.id.textView_buttons);
        TextView_battlevel   = (TextView) findViewById(R.id.textView_battlevel);

        Button_red_led       = (Button) findViewById(R.id.red_led_btn);
        Button_green_led     = (Button) findViewById(R.id.green_led_btn);
        Button_amber_led     = (Button) findViewById(R.id.amber_led_btn);
        Button_off_led       = (Button) findViewById(R.id.off_led_btn);


        Button_red_led.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                byte tmp[] = { 0x01 };

                if(Ready == true) {

                    BLEGatt.getService(AINA_SERV).getCharacteristic(LEDS).setValue(tmp);
                    BLEGatt.writeCharacteristic(BLEGatt.getService(AINA_SERV).getCharacteristic(LEDS));
                }
            }
        });


        Button_green_led.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                byte tmp[] = { 0x02 };

                if(Ready == true) {

                    BLEGatt.getService(AINA_SERV).getCharacteristic(LEDS).setValue(tmp);
                    BLEGatt.writeCharacteristic(BLEGatt.getService(AINA_SERV).getCharacteristic(LEDS));
                }
            }
        });


        Button_amber_led.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                byte tmp[] = { 0x04 };

                if(Ready == true) {

                    BLEGatt.getService(AINA_SERV).getCharacteristic(LEDS).setValue(tmp);
                    BLEGatt.writeCharacteristic(BLEGatt.getService(AINA_SERV).getCharacteristic(LEDS));
                }
            }
        });


        Button_off_led.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                byte tmp[] = { 0x00 };

                if(Ready == true) {

                    BLEGatt.getService(AINA_SERV).getCharacteristic(LEDS).setValue(tmp);
                    BLEGatt.writeCharacteristic(BLEGatt.getService(AINA_SERV).getCharacteristic(LEDS));
                }
            }
        });


        BLEManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BLEAdapter = BLEManager.getAdapter();


        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("LOCATION SERVICES");
            builder.setMessage("This application needs access to location services to discover devices!");
            builder.setPositiveButton(android.R.string.ok, null);

            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {

                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                }

            });

            builder.show();
        }


        if (!BLEAdapter.isEnabled()) {

            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else
            startLE();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if ((requestCode == REQUEST_ENABLE_BT) && (resultCode == -1)){

            startLE();
        }
    }



    private final ScanCallback BLEScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            int RSSI;

            BLEDevice = result.getDevice();

            String temp = BLEDevice.getName();

            RSSI = result.getRssi();

//            if ((temp.contains("ASB")) && (RSSI > -27)) {     // If rssi based pairing will be used, check rssi level
            if (temp.contains("ASB")) {    //Connect to first found smart button

                IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                registerReceiver(BondReceiver, intent);

                BLEGatt = BLEDevice.connectGatt(getApplicationContext(), true, GattCallback);

                BLEScanner.stopScan(BLEScanCallback);
            }
        }
    };



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        switch (requestCode) {

            case 1: {

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    System.out.println("Coarse location: PERMISSION_GRANTED");

                } else {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setTitle("LOCATION SERVICES");
                    builder.setMessage("This application needs access to location services to discover devices!");
                    builder.setPositiveButton(android.R.string.ok, null);

                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });

                    builder.show();
                }
            }
        }
    }



    private void startLE() {

        boolean found = false;

        BLEScanner = BLEAdapter.getBluetoothLeScanner();

        Set<BluetoothDevice> devices = BLEAdapter.getBondedDevices();

        for (BluetoothDevice device : devices) {

            if (device.getType() == BluetoothDevice.DEVICE_TYPE_LE) {

                if (device.getName().contains("ASB")) {

                    found = true;

                    NoScanNeeded = true;

                    BLEDevice = device;

                    IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                    registerReceiver(BondReceiver, intent);

                    //BLEDevice.createBond();

                    BLEGatt = BLEDevice.connectGatt(getApplicationContext(), true, GattCallback);

                }
            }
        }

        if (found == false) {

            System.out.println("start scanning ble devices...");

            ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();
            ScanFilter filter = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(AINA_SERV.toString())).build();
            List<ScanFilter> filter_list = new ArrayList<ScanFilter>(1);
            filter_list.add(filter);

            BLEScanner.startScan(filter_list, settings, BLEScanCallback);
        }
    }



    private final BroadcastReceiver BondReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {

                System.out.println("ACTION_BOND_STATE_CHANGED");

                final int state        = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState    = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {

                    if (BLEDevice.getBondState() == BluetoothDevice.BOND_BONDED) {

                        GetSwVersion = true;

                        BLEGatt.readCharacteristic(BLEGatt.getService(AINA_SERV).getCharacteristic(SW_VERS));
                    }
                    else
                        Ready = false;

                }
            }
        }
    };




    private final BluetoothGattCallback GattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if (newState == BluetoothProfile.STATE_CONNECTED) {

                gatt.discoverServices();

                smart_button_mask = 0x00;
                TextUpdateHandler.post(updateText);
            }
            else
                Ready = false;


            if(status == 0x13) {

                Ready = false;

                if(BLEGatt != null) {

                    gatt.disconnect();
                    gatt.close();

                    BLEGatt.disconnect();

                    BLEGatt.close();
                }

                NoScanNeeded = false;

                IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                registerReceiver(BondReceiver, intent);

                //BLEDevice.createBond();

                BLEGatt = BLEDevice.connectGatt(getApplicationContext(), true, GattCallback);
            }
        }



        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {

                if (NoScanNeeded) {

                    GetSwVersion = true;

                    BLEGatt.readCharacteristic(BLEGatt.getService(AINA_SERV).getCharacteristic(SW_VERS));
                }
            }
        }



        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            if (characteristic.getUuid().equals(BUTTONS)) {

                smart_button_mask = ((int) characteristic.getValue()[0]);
            }

            if (characteristic.getUuid().equals(BATT_LEVEL)) {

                smart_batt_level = ((int) characteristic.getValue()[0]);
            }

            TextUpdateHandler.post(updateText);
        }



        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {

                if (GetSwVersion) {

                    Ready = true;

                    Aina_Chars = gatt.getService(AINA_SERV).getCharacteristics();

                    smart_sw_version = "SMART Version: ";
                    smart_sw_version += Integer.toHexString((characteristic.getValue()[3] & 0xff));
                    smart_sw_version += Integer.toHexString((characteristic.getValue()[4] & 0xff));
                    smart_sw_version += Integer.toHexString((characteristic.getValue()[5] & 0xff)).toUpperCase();

                    if (smart_sw_version.substring(smart_sw_version.length() - 1, smart_sw_version.length()).equals("0")) {
                        smart_sw_version = smart_sw_version.substring(0, smart_sw_version.length() - 1);
                    }

                    if (smart_sw_version.toUpperCase().contains("BE7A"))
                        smart_sw_version = "SMART Version: Beta release";

                    TextUpdateHandler.post(updateText);

                    GetBattLevel = true;

                    BLEGatt.readCharacteristic(BLEGatt.getService(BATT_SERV).getCharacteristic(BATT_LEVEL));

                } else if (GetBattLevel) {

                    smart_batt_level = ((int) characteristic.getValue()[0]);

                    BLEGatt.setCharacteristicNotification(BLEGatt.getService(AINA_SERV).getCharacteristic(BUTTONS), true);
                    BluetoothGattDescriptor descriptor = BLEGatt.getService(AINA_SERV).getCharacteristic(BUTTONS).getDescriptor(CLIENT_CHAR_CONFIG);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    BLEGatt.writeDescriptor(descriptor);

                    BLEGatt.setCharacteristicNotification(BLEGatt.getService(BATT_SERV).getCharacteristic(BATT_LEVEL), true);
                    descriptor = BLEGatt.getService(BATT_SERV).getCharacteristic(BATT_LEVEL).getDescriptor(CLIENT_CHAR_CONFIG);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    BLEGatt.writeDescriptor(descriptor);

                    TextUpdateHandler.post(updateText);

                }
            }
            else {

                if (GetSwVersion) {

                    GetSwVersion = false;

                    IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                    registerReceiver(BondReceiver, intent);

                    BLEDevice.createBond();

                    BLEGatt = BLEDevice.connectGatt(getApplicationContext(), true, GattCallback);


                } else if (GetBattLevel) {

                    GetBattLevel = false;

                    IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                    registerReceiver(BondReceiver, intent);

                    BLEDevice.createBond();

                    BLEGatt = BLEDevice.connectGatt(getApplicationContext(), true, GattCallback);

                }
            }
        }
    };



    private final Runnable updateText = new Runnable() {

        public void run() {

        if(GetSwVersion) {

            GetSwVersion = false;
            GetButtons   = true;

            TextView_sw_version.setText(smart_sw_version);

            TextView_service_1.setText(Aina_Chars.get(0).getUuid().toString().toUpperCase());
            TextView_service_2.setText(Aina_Chars.get(1).getUuid().toString().toUpperCase());
            TextView_service_3.setText(Aina_Chars.get(2).getUuid().toString().toUpperCase());


            if ((smart_sw_version.contains("Beta")) || (smart_sw_version.contains("17"))) {

                TextView_service_4.setText(Aina_Chars.get(3).getUuid().toString().toUpperCase());
                TextView_service_5.setText(Aina_Chars.get(4).getUuid().toString().toUpperCase());
                TextView_service_6.setText(Aina_Chars.get(5).getUuid().toString().toUpperCase());
            }

            TextView_button_mask.setText("0x00");

        }

        if(GetButtons) {

            if((smart_button_mask & 0xff) < 16)
                TextView_button_mask.setText("0x0" + Integer.toHexString(smart_button_mask & 0xff).toUpperCase());
            else
                TextView_button_mask.setText("0x" + Integer.toHexString(smart_button_mask & 0xff).toUpperCase());

            TextView_buttons.setText("");

            if ((smart_button_mask & 1) == 1) TextView_buttons.append("(PTT1 - 0x01) ");
            if ((smart_button_mask & 2) == 2) TextView_buttons.append("(EMERG - 0x02) ");
            if ((smart_button_mask & 4) == 4) TextView_buttons.append("(APTT2 - 0x04) ");
            if ((smart_button_mask & 8) == 8) TextView_buttons.append("(DOWN - 0x08) ");
            if ((smart_button_mask & 16) == 16) TextView_buttons.append("(UP - 0x10) ");
            if ((smart_button_mask & 32) == 32) TextView_buttons.append("(LEFT - 0x20) ");
            if ((smart_button_mask & 64) == 64) TextView_buttons.append("(RIGHT - 0x40) ");

            if ((smart_button_mask & 128) == 128) TextView_buttons.append("(hearbeat - 0x80)");
        }

        if(GetBattLevel) {
            TextView_battlevel.setText("Battery level: " + smart_batt_level + "%");
        }
    }
    };

}


