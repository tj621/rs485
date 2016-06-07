import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;

import java.util.HashMap;
public class CommTest {

	public static void main(String[] args) {
		HashMap<String, Comparable> params = new HashMap<String, Comparable>();
		params.put(SerialReader.PARAMS_PORT, "COM3"); 
		params.put(SerialReader.PARAMS_RATE, 9600);
		params.put(SerialReader.PARAMS_TIMEOUT, 1000);
		params.put(SerialReader.PARAMS_DELAY, 200);
		params.put(SerialReader.PARAMS_DATABITS, SerialPort.DATABITS_8);
		params.put(SerialReader.PARAMS_STOPBITS, SerialPort.STOPBITS_1); 
		params.put(SerialReader.PARAMS_PARITY, SerialPort.PARITY_NONE);
		SerialReader sr = new SerialReader(params);
		CommDataObserver joe = new CommDataObserver ("joe");
		sr.addObserver(joe);
		sr.transmit();
	}
}