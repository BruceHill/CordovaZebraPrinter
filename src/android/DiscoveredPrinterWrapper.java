package za.co.mobility.plugins.zebra;

import java.lang.String;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

public class DiscoveredPrinterWrapper {
	private DiscoveredPrinter _printer;
	public DiscoveredPrinterWrapper(DiscoveredPrinter printer) {
		_printer = printer;
	}
	
	public DiscoveredPrinter getDiscoveredPrinter() {
		return _printer;
	}
	
	public String toString() {
		return _printer.getDiscoveryDataMap().get("FRIENDLY_NAME")+" ("+_printer.toString()+")";
	}
}
