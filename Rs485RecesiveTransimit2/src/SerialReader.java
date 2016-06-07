import gnu.io.CommPort;  
import gnu.io.CommPortIdentifier; 
import gnu.io.NoSuchPortException; 
import gnu.io.PortInUseException; 
import gnu.io.SerialPort;

import gnu.io.SerialPortEvent;  
import gnu.io.SerialPortEventListener;  
import gnu.io.UnsupportedCommOperationException; 
import java.io.IOException; 
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Enumeration; 
import java.util.HashMap; 
import java.util.HashSet;  
import java.util.Observable;
import java.util.Scanner;
import java.util.TooManyListenersException;

public class SerialReader extends  Observable  implements  Runnable,  SerialPortEventListener {   
	static CommPortIdentifier portId;  
	int delayRead = 200;   
	int numBytes;
	private static byte[] readBuffer = new byte[4096];
	private static byte[] writeBuffer=new byte[4096];//使用outputStream往串口写指令
	String str=null;//用于向console输入字符串命令
	static Enumeration portList;
	InputStream inputStream;
	OutputStream outputStream;
	Scanner sc=new Scanner(System.in);
	SerialPort serialPort;  
	HashMap serialParams;
	public static final String PARAMS_DELAY = "delay read";
	public static final String PARAMS_TIMEOUT = "timeout";
	public static final String PARAMS_PORT = "port name";
	public static final String PARAMS_DATABITS = "data bits";
	public static final String PARAMS_STOPBITS = "stop bits";
	public static final String PARAMS_PARITY = "parity";
	public static final String PARAMS_RATE = "rate";
	
	public SerialReader(HashMap params) {   
		serialParams = params;   
		init();  
	}
	private void init() {   
		try { 
			int timeout = Integer.parseInt(serialParams.get(PARAMS_TIMEOUT).toString());    
			int rate = Integer.parseInt(serialParams.get(PARAMS_RATE).toString());     
			int dataBits = Integer.parseInt(serialParams.get(PARAMS_DATABITS).toString());    
			int stopBits = Integer.parseInt(serialParams.get(PARAMS_STOPBITS).toString());    
			int parity = Integer.parseInt(serialParams.get(PARAMS_PARITY).toString());    
			delayRead = Integer.parseInt(serialParams.get(PARAMS_DELAY).toString());    
			String port = serialParams.get(PARAMS_PORT).toString();
			portId = CommPortIdentifier.getPortIdentifier(port);     
			serialPort = (SerialPort) portId.open("SerialReader", timeout);    
			inputStream = serialPort.getInputStream();
			outputStream=serialPort.getOutputStream();
			serialPort.addEventListener(this);     
			serialPort.notifyOnDataAvailable(true);     
			serialPort.setSerialPortParams(rate, dataBits, stopBits, parity);   
		} catch (PortInUseException e) {    
			System.out.println("端口已经被占用!");    
			e.printStackTrace();    
		} catch (TooManyListenersException e) {    
			System.out.println("端口监听者过多!");    
			e.printStackTrace();    
		} catch (UnsupportedCommOperationException e) {    
			System.out.println("端口操作命令不支持!");    
			e.printStackTrace();    
		} catch (NoSuchPortException e) {    
			System.out.println("端口不存在!"); 
			e.printStackTrace();    
		} catch (IOException e) {    
			e.printStackTrace();   
		}    
		Thread readThread = new Thread(this);   
		readThread.start();
	}

	public void run() {   
		try {     
			Thread.sleep(100);    
		} catch (InterruptedException e) {   }  
	}
	  
	public void serialEvent(SerialPortEvent event) {   
		try { 
			Thread.sleep(delayRead);     
			System.out.print("serialEvent[" + event.getEventType() + "]    ");   
		} catch (InterruptedException e) {    
			e.printStackTrace();   
		}
		switch (event.getEventType()) {   
			case SerialPortEvent.BI: // 10   
			case SerialPortEvent.OE: // 7   
			case SerialPortEvent.FE: // 9   
			case SerialPortEvent.PE: // 8   
			case SerialPortEvent.CD: // 6   
			case SerialPortEvent.CTS: // 3   
			case SerialPortEvent.DSR: // 4    
			case SerialPortEvent.RI: // 5    
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2 
				break;    
			case SerialPortEvent.DATA_AVAILABLE: // 1    
			try {       
					numBytes = inputStream.read(readBuffer);
					changeMessage(readBuffer, numBytes);    
			} catch (IOException e) {     
				e.printStackTrace();    
			}    
			break;   
		}  
	}
	public void transmit(){
		while(true){
			str=sc.nextLine();
			if(str!=null)
				break;
		}
		byte writeBuffer[] = str.getBytes();//String转换为byte[]
		
		try { 
			Thread.sleep(delayRead);       
		} catch (InterruptedException e) {    
			e.printStackTrace();   
		}
		try {		
			outputStream.write(writeBuffer);   
		} catch (IOException e) {
			e.printStackTrace();    
		}
	}
	public void changeMessage(byte[] message, int length) {   
		setChanged();    
		byte[] temp = new byte[length];    
		System.arraycopy(message, 0, temp, 0, length);    
		// System.out.println("msg[" + numBytes + "]: [" + new String(temp) + "]");   
		notifyObservers(temp);  
	}
	static void listPorts() {    
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();   
		while (portEnum.hasMoreElements()) {     
			CommPortIdentifier portIdentifier = (CommPortIdentifier) portEnum.nextElement();    
			System.out.println(portIdentifier.getName() + " - "      
				+ getPortTypeName(portIdentifier.getPortType()));   
		}  
	} 
	static String getPortTypeName(int portType) {   
		switch (portType) {    
			case CommPortIdentifier.PORT_I2C:    
				return "I2C";    
			case CommPortIdentifier.PORT_PARALLEL:    
				return "Parallel";    
			case CommPortIdentifier.PORT_RAW:    
				return "Raw";    
			case CommPortIdentifier.PORT_RS485:    
				return "RS485";
			case CommPortIdentifier.PORT_SERIAL:    
				return "Serial";   
			default:     
				return "unknown type";   
		}  
	}
	
	public static HashSet<CommPortIdentifier> getAvailableSerialPorts() {   
		HashSet<CommPortIdentifier> h = new HashSet<CommPortIdentifier>();   
		Enumeration thePorts = CommPortIdentifier.getPortIdentifiers();   
		while (thePorts.hasMoreElements()) {     
			CommPortIdentifier com = (CommPortIdentifier) thePorts.nextElement();    
			switch (com.getPortType()) {     
				case CommPortIdentifier.PORT_SERIAL:     
					try {
						CommPort thePort = com.open("CommUtil", 50);      
						thePort.close();
						h.add(com);      
					} catch (PortInUseException e) {       
						System.out.println("Port, " + com.getName() + ", is in use.");     
					} catch (Exception e) {       
						System.out.println("Failed to open port " + com.getName() + e);
					}    
			}   
		}    
		return h;  
	}
}
