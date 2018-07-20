package helper;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by Aaron on 31.01.2017.
 */
public class JNHUtil {
	
	private JNHUtil(){}
	
	private static boolean isActive = false;

	public static void noLog(){
		LogManager.getLogManager().reset();
		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.OFF);
		logger.setUseParentHandlers(false);
	}
	
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
