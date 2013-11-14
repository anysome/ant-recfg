package net.asfun.ant.reconfig;

public class Log {

	static boolean isDebugOn = false;
	
	public static void print(Object msg) {
		if ( isDebugOn ) {
			System.out.println(msg);
		}
	}
}
