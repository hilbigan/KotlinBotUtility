package helper;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

/**
 * Created by Aaron on 31.01.2017.
 */
public class JNHUtil {
	
	private JNHUtil(){}
	
	private static boolean isActive = false;
	
	public static void activate(){
		if(isActive) return;
		try {
			GlobalScreen.registerNativeHook();
		}  catch (NativeHookException ex) {
			ex.printStackTrace();
		}
		isActive = true;
	}
	
	public static void deactivate(){
		if(!isActive) return;
		try {
			GlobalScreen.unregisterNativeHook();
		}  catch (NativeHookException ex) {
			ex.printStackTrace();
		}
		isActive = false;
	}
	
}
