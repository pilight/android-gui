package classes;

public class Log {
	public static void printf(String message) {
		System.err.println(message);
    	System.exit(1);	
	}
}