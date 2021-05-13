package za.co.mobility.plugins.zebra;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.util.Xml.Encoding;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

public class ZebraHelper {
	public static Connection connect(String macAddress) {
    	Connection printerConnection = null;
        printerConnection = new BluetoothConnection(macAddress);
        try {
            printerConnection.open();
        } catch (ConnectionException e) {
            printerConnection = null;
        }
        return printerConnection;
    }
    
    // Get printer language
    public static PrinterLanguage getPrinterLanguage(Connection printerConnection) {
    	PrinterLanguage pl = null;
    	try {
			pl = ZebraPrinterFactory.getInstance(printerConnection).getPrinterControlLanguage();
		} catch (ConnectionException e) {
			e.printStackTrace();
		} catch (ZebraPrinterLanguageUnknownException e) {
			e.printStackTrace();
		}
    	return pl;
    }
    
    // Test 
    public static void disconnect(Connection printerConnection) {
        try {
            if (printerConnection != null) {
                printerConnection.close();
            }
        } catch (ConnectionException e) {} 
    }
    
    private static byte[] concat(byte[]...arrays)
    {
        // Determine the length of the result array
        int totalLength = 0;
        for (int i = 0; i < arrays.length; i++)
        {
            totalLength += arrays[i].length;
        }

        // create the result array
        byte[] result = new byte[totalLength];

        // copy the source arrays into the result array
        int currentIndex = 0;
        for (int i = 0; i < arrays.length; i++)
        {
            System.arraycopy(arrays[i], 0, result, currentIndex, arrays[i].length);
            currentIndex += arrays[i].length;
        }

        return result;
    }

    public static Boolean print(Connection printerConnection, byte[] toPrint) {
    	byte[] toPrintWithCR = concat(toPrint, System.getProperty("line.separator").getBytes());
        try {
	       printerConnection.write(toPrintWithCR, 0, toPrintWithCR.length);
	       return true;
        } catch (ConnectionException e) {}
        return false;
    }
    
    public static void print(Connection connection, String toPrint) {
		ZebraHelper.print(connection, toPrint.getBytes());
	}

    /*
    * Returns the command for a test label depending on the printer control language
    * The test label is a box with the word "TEST" inside of it
    * 
    * _________________________
    * |                       |
    * |                       |
    * |        TEST           |
    * |                       |
    * |                       |
    * |_______________________|
    * 
    * 
    */
    public static byte[] getTestLabel(PrinterLanguage language) {
    	String linefeed = System.getProperty("line.separator");
        String configLabel = null;
        if (language == PrinterLanguage.ZPL) {
            configLabel = "^XA^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDTEST^FS^XZ";
        } else if (language == PrinterLanguage.CPCL) {
            configLabel = "! 0 200 200 1215 1" + linefeed +
                          "PW 815" + linefeed +
		                  "TONE 0" + linefeed +
                          "SPEED 0" + linefeed +
                          "ON-FEED IGNORE" + linefeed +
                          "NO-PACE" + linefeed +
                          "GAP-SENSE" + linefeed +
                          "00000F52" + linefeed +
                          "D044" + linefeed +
                          "PCX 32 32 !<AGRIRECO.PCX" + linefeed +
                          "T 0 3 547 74 \\\\" + linefeed +
                          "T 0 3 547 110 \\\\" + linefeed +
                          "T 4 0 269 214 Attest" + linefeed +
                          "T 4 0 205 284 \\\\" + linefeed +
                          "T 4 0 205 354 \\\\" + linefeed +
                          "T 4 0 205 424 \\\\" + linefeed +
                          "T 4 0 181 492 BTW NR" + linefeed +
                          "T 5 0 14 577 Heeft binnengebracht" + linefeed +
                          "T 5 0 142 614 Agrirecover zak(ken) waarvan" + linefeed +
                          "T 5 0 142 651 Gespoelde zak(ken)" + linefeed +
                          "T 5 0 142 688 Niet - gespoelde zak(ken)" + linefeed +
                          "T 5 0 142 726 Afgekeurde zak(ken)" + linefeed +
                          "T 5 0 142 763 NBGM" + linefeed +
                          "T 5 0 142 800 Doppen apart" + linefeed +
                          "T 5 0 142 837 Groepdozen" + linefeed +
                          "T 5 0 142 874 Vat(en) Slib" + linefeed +
                          "T 5 0 142 911 Meststoffenzakken" + linefeed +
                          "T 5 0 69 651 \\\\" + linefeed +
                          "T 5 0 69 688 \\\\" + linefeed +
                          "T 5 0 69 726 \\\\" + linefeed +
                          "T 5 0 81 763 \\\\" + linefeed +
                          "T 5 0 81 800 \\\\" + linefeed +
                          "T 5 0 81 837 \\\\" + linefeed +
                          "T 5 0 69 874 \\\\" + linefeed +
                          "T 5 0 69 911 \\\\" + linefeed +
                          "T 4 0 416 214 \\\\" + linefeed +
                          "T 4 0 377 492 \\\\" + linefeed +
		                  "PRINT" + linefeed;
        }
        return configLabel.getBytes();
    }

	public static String getFriendlyName(Connection connection) {
		String friendlyName = "Unknown";
		if (connection instanceof BluetoothConnection) {
            friendlyName = ((BluetoothConnection) connection).getFriendlyName();
        }
		return friendlyName;
	}
}
