package za.co.mobility.plugins.zebra;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;

public class Zebra extends CordovaPlugin {
	
	public static final int REQUEST_CODE = 1;
	
	private static final String SELECT_PRINTER_INTENT = "za.co.mobility.plugins.zebra.SELECT_PRINTER";
	private static final String PRINT = "print";
	
	private static ProgressDialog printProgress = null;
	private String toPrint;
	private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
    	this.callbackContext = callbackContext;
    	if (action.equals(PRINT)) {
    		toPrint = data.getString(0);
    		final String macAddress = SettingsHelper.getZebraPrinter(cordova.getActivity().getApplicationContext());
    		if (macAddress != "") {
				PrintTask print = new PrintTask(new PrintTask.PrintTaskListener() {
					@Override
					public void taskStart() {
						startProgressDialog(macAddress);
					}

					@Override
					public void taskCompleted(String error) {
						dismissProgressDialog();
						if (error == "") {
							Zebra.this.callbackContext.success();
						}
						else {
							selectPrinter();
						}
					}

					@Override
					public void taskProgress(String progress) {
						updateProgressDialog(progress);
					}
				});
				print.execute(macAddress, toPrint);
    		}
    		else {
    			selectPrinter();
    		}
    		return true;
    	}
    	return false;
    }
    
    /* Progress Dialog */
    private void startProgressDialog(final String message) {
    	final CordovaInterface cordova = this.cordova;
    	cordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (Zebra.printProgress != null) {
					Zebra.printProgress.dismiss();
				}
				Zebra.printProgress = ProgressDialog.show(cordova.getActivity(), "Printing", message, true);
			}
		});
	}
    
    private void dismissProgressDialog() {
    	cordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (Zebra.printProgress != null) {
					Zebra.printProgress.dismiss();
					Zebra.printProgress = null;
				}
			}
		});
    }
    
    private void updateProgressDialog(final String newMessage) {
    	cordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (Zebra.printProgress != null) {
					Zebra.printProgress.setMessage(newMessage);
				}
			}
    	});
    }
    
    /** Find printer */

	private void selectPrinter() {
		Intent intentPrinterDialog = new Intent(SELECT_PRINTER_INTENT);
		this.cordova.startActivityForResult((CordovaPlugin) this, intentPrinterDialog, REQUEST_CODE);
	}
	
	 /**
     * Called when the setup printer intent completes.
     *
     * @param requestCode The request code originally supplied to startActivityForResult(),
     *                       allowing you to identify who this result came from.
     * @param resultCode  The integer result code returned by the child activity through its setResult().
     * @param intent      An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
            	final String macAddress = intent.getStringExtra("MAC_ADDRESS");
            	SettingsHelper.saveZebraPrinter(cordova.getActivity().getApplicationContext(), macAddress);
            	PrintTask print = new PrintTask(new PrintTask.PrintTaskListener() {
					@Override
					public void taskStart() {
						startProgressDialog(macAddress);
					}

					@Override
					public void taskCompleted(String error) {
						dismissProgressDialog();
						if (error == "") {
							Zebra.this.callbackContext.success();
						}
						else {
							Zebra.this.callbackContext.error(error);
						}
					}

					@Override
					public void taskProgress(String progress) {
						updateProgressDialog(progress);
					}
				});
				print.execute(macAddress, toPrint);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
            	this.callbackContext.error("Select print dialog canceled");
            }
        }
    }

    /* Old method, does not define callback to the plugin class. 
     * Replaced by PrintTask, but left here as a comment purely for reference purposes.
	private void print(final String macAddress) {
		startProgressDialog(macAddress);
		cordova.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				String error = "";
				updateProgressDialog("Connecting");
				Connection connection = ZebraHelper.connect(macAddress);
			    if (connection != null && connection.isConnected()) {
			    	updateProgressDialog("Connected");
			    	PrinterLanguage language = ZebraHelper.getPrinterLanguage(connection);
		    		if (language == PrinterLanguage.CPCL) {
			    		ZebraHelper.print(connection, toPrint);
			    		updateProgressDialog("Data sent to printer");
		    		}
		    		else {
		    			updateProgressDialog("Error: Printer language must be CPCL");
		    			error = "Printer language must be CPCL";
		    		}
			    	ZebraHelper.disconnect(connection);
			    }
			    else {
			    	updateProgressDialog("Error: Printer connection not established");
			    	error = "Printer connection not established";
			    }
				dismissProgressDialog();
				if (error != "") {
			    	Zebra.this.callbackContext.error(error);
			    }
			    else {
			    	Zebra.this.callbackContext.success();
			    }
			}
		});
	}*/
}
