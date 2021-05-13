package za.co.mobility.plugins.zebra;

import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.util.internal.Base64;

import android.os.AsyncTask;

public class PrintTask extends AsyncTask<String, String, String> {

    public interface PrintTaskListener {
    	public void taskStart();
    	public void taskCompleted(String result);
    	public void taskProgress(String progress);
    }

    // This is the reference to the associated listener
    private final PrintTaskListener taskListener;

    public PrintTask(PrintTaskListener listener) {
        // The listener reference is passed in through the constructor
        this.taskListener = listener;
    }
    
    @Override
    protected void onPreExecute() {
    	super.onPreExecute();
    	if (this.taskListener != null) {
    		this.taskListener.taskStart();
    	}
    }
    
    @Override
    protected void onProgressUpdate(String... values) {
		if (this.taskListener != null) {
			this.taskListener.taskProgress(values[0]);
		}
    }
    
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if(this.taskListener != null) {
            this.taskListener.taskCompleted(result);
        }
    }

    @Override
    protected String doInBackground(String... params) {
    	String macAddress = params[0];
    	byte[] toPrint = Base64.decode(params[1]);
    	String error = "";
    	this.publishProgress("Connecting");
		Connection connection = ZebraHelper.connect(macAddress);
	    if (connection != null && connection.isConnected()) {
	    	this.publishProgress("Connected");
	    	PrinterLanguage language = ZebraHelper.getPrinterLanguage(connection);
    		if (language == PrinterLanguage.CPCL) {
	    		ZebraHelper.print(connection, toPrint);
	    		this.publishProgress("Data sent to printer");
    		}
    		else {
    			this.publishProgress("Error: Printer language must be CPCL");
    			error = "Printer language must be CPCL";
    		}
	    	ZebraHelper.disconnect(connection);
	    }
	    else {
	    	this.publishProgress("Error: Printer connection not established");
	    	error = "Printer connection not established";
	    }
        return error;
    }
}
