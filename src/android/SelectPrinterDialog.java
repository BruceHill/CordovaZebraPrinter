package za.co.mobility.plugins.zebra;

import java.util.ArrayList;

import nl.omnimove.mobileforms.R;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;

public class SelectPrinterDialog extends FragmentActivity {

    private TextView statusField;
    private Spinner devicesSpinner;
    private Button testButton;
    private Button continueButton;
    
    private ArrayList<DiscoveredPrinterWrapper> printers = new ArrayList<DiscoveredPrinterWrapper>();
    private DiscoveredPrinterWrapper selectedPrinter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zebra_connection);
        devicesSpinner = (Spinner) this.findViewById(R.id.devicesSpinner);
		devicesSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				selectPrinter(printers.get(position));
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				unSelectPrinter();
			}
		});
        findBluetoothPrinters();
        statusField = (TextView) this.findViewById(R.id.statusText);
        testButton = (Button) this.findViewById(R.id.testButton);
        testButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        Looper.prepare();
                        testConnection();
                        Looper.loop();
                        Looper.myLooper().quit();
                    }
                }).start();
            }
        });
        continueButton = (Button) this.findViewById(R.id.continueButton);
        continueButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
             	if (selectedPrinter == null) {
             		returnCancelledResult();
             	}
             	else {
             		returnOkResult();
             	}
            }
        });
    }
    
    protected void returnOkResult() {
    	Intent intent = new Intent();
		intent.putExtra("MAC_ADDRESS", getMacAddress());
		SelectPrinterDialog.this.setResult(RESULT_OK, intent);
		SelectPrinterDialog.this.finish();
	}

	protected void returnCancelledResult() {
    	Intent intent = new Intent();
    	setResult(RESULT_CANCELED, intent);
    	finish();
	}
    
    /* Bluetooth discovery */

	// Find Bluetooth Printers
    protected void findBluetoothPrinters() {
    	printers.clear();
    	(new Thread(new Runnable() {
        	public void run() {
        		Looper.prepare();
        		try {
        			setStatus("Finding bluetooth devices", Color.GRAY);
					com.zebra.sdk.printer.discovery.BluetoothDiscoverer.findPrinters(SelectPrinterDialog.this.getApplicationContext(), new DiscoveryHandler() {
						@Override
						public void discoveryError(String arg0) {
							printerDiscoveryFailed();
						}

						@Override
						public void discoveryFinished() {
							printerDiscoveryFinished();
						}

						@Override
						public void foundPrinter(DiscoveredPrinter discoveredPrinter) {
							registerPrinter(discoveredPrinter);
						}
					});
				} catch (ConnectionException e) {}
        		Looper.loop();
                Looper.myLooper().quit();
        	}
        })).start();
	}
    
    private void printerDiscoveryFailed() {
    	setStatus("Bluetooth discovery failed", Color.RED);
	}
    
    private void printerDiscoveryFinished() {
    	if (printers.size() == 0) {
    		setStatus("No bluetooth devices were found", Color.RED);
    	}
    	else {
    		setStatus("Bluetooth devices found: "+ printers.size(), Color.GREEN);
    		runOnUiThread(new Runnable() {
                public void run() {
					ArrayAdapter<DiscoveredPrinterWrapper> adapter = new ArrayAdapter<DiscoveredPrinterWrapper>(SelectPrinterDialog.this,
				             android.R.layout.simple_spinner_dropdown_item, printers);
				    devicesSpinner.setAdapter(adapter);
				    devicesSpinner.setSelection(0);
                }
            });
    	}
	}
    
    private void registerPrinter(DiscoveredPrinter discoveredPrinter) {
		printers.add(new DiscoveredPrinterWrapper(discoveredPrinter));
	}
    
    /* Select/unselect printer */
    
	protected void selectPrinter(DiscoveredPrinterWrapper discoveredPrinterWrapper) {
		selectedPrinter = discoveredPrinterWrapper;
		enableTestButton(true);
		setContinueButtonText(true);
	}

	protected void unSelectPrinter() {
		selectedPrinter = null;
		enableTestButton(false);
		setContinueButtonText(false);
	}
    
    private void enableTestButton(final boolean enabled) {
        runOnUiThread(new Runnable() {
            public void run() {
                testButton.setEnabled(enabled);
            }
        });
    }
    
    private void setContinueButtonText(boolean printerFound) {
		continueButton.setText(printerFound? "Print": "Close");
	}
    
    /* Set Status */
    private void setStatus(final String statusMessage, final int color) {
        runOnUiThread(new Runnable() {
            public void run() {
                statusField.setTextColor(color);
                statusField.setText("[ "+statusMessage+" ]");
            }
        });
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
    }
    
    private String getMacAddress() {
    	if (selectedPrinter == null) return "";
        return selectedPrinter.getDiscoveredPrinter().toString();
    }
    
    /* Test connection */
    private void testConnection() {
    	setStatus("Connecting", Color.GRAY);
    	Connection connection = ZebraHelper.connect(getMacAddress());
    	if (connection != null && connection.isConnected()) {
    		setStatus("Connected", Color.GRAY);
    		PrinterLanguage language = ZebraHelper.getPrinterLanguage(connection);
    		if (language == PrinterLanguage.CPCL) {
	    		if (ZebraHelper.print(connection, ZebraHelper.getTestLabel(language))) {
		            setStatus("Sent to " + ZebraHelper.getFriendlyName(connection), Color.MAGENTA);
	    		}
    		}
    		else {
    			setStatus("Printer language must be CPCL", Color.RED);
    		}
    		setStatus("Disconnecting", Color.GRAY);
    		ZebraHelper.disconnect(connection);
	        setStatus("Not Connected", Color.GRAY);
    	}
    	else {
    		setStatus("Error opening connection", Color.RED);
    	}
	}
}
