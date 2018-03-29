package helper;

import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * <br>Einfach und schnell Hotkeys anlegen.<br>
 * <br>
 * Beispiel:<br>
 * <code>Hotkey.instance.{@link #addHotkey}(TRIGGER_TYPE.PRESSED, (e)->{<br>
 * System.out.println("Steuerung + C");<br>
 * }, NativeKeyEvent.VC_CONTROL_L, NativeKeyEvent.VC_C);<br>Hotkey.instance.{@link #activate()};</code><br>
 * <br>
 * Hinweis: {@link #activate()} muss immer aufgerufen werden!<br>
 * <br>
 * Version 1.0<br>
 * Created by Aaron Hilbig on 29.12.2016.
 */
public class Hotkey implements NativeKeyListener {

    public boolean active = false;

    private static class HotkeyDispatcher {

        private int[] keys = new int[3];
        private TRIGGER_TYPE type;
        public Consumer<NativeKeyEvent> consumer;

        public HotkeyDispatcher(TRIGGER_TYPE type, Consumer<NativeKeyEvent> consumer, int... keys){
            this.type = type;
            this.keys = keys;
            this.consumer = consumer;
        }

        @Override
        public String toString() {
            return "HotkeyDispatcher{" +
                    "keys=" + Arrays.toString(keys) +
                    ", type=" + type +
                    ", consumer=" + consumer +
                    '}';
        }
    }

    /**
     * Der Typ des Event-Auslösers.<br>Standardwert ist {@link #TYPED}
     */
    public enum TRIGGER_TYPE {

        PRESSED(0),
        RELEASED(1),
        TYPED(2);

        public int code = 0;

        TRIGGER_TYPE(int i){
            code = i;
        }
    }

    /**
     * Aktuelle Instanz. Sollte zum Aufrufen und Verändern der Hotkeys benutzt werden.
     */
    public static Hotkey instance = new Hotkey();

    private static ArrayList<HotkeyDispatcher> hotkeys = new ArrayList<>();

    private static ArrayList<Integer> currentlyPressed = new ArrayList<>();

    private Hotkey(){
        LogManager.getLogManager().reset();
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
    }

    /**
     * @param type Legt fest, wann das Event ausgelöst werden soll. Gilt nur für die letzte gedrückte Taste, heißt bei Tastenkombinationen nur für die letzte noch Fehlende; alle anderen müssen schon gedrückt ('pressed') sein
     * @param consumer Wird bei Auslösen des Events aufgerufen
     * @param keys Beliebig viele Tasten für Tastenkombinationen. Die Tastencodes sollten alle aus {@link NativeKeyEvent} stammen! Wenn keys[0]==-1 löst das Event immer aus.
     */
    public void addHotkey(TRIGGER_TYPE type, Consumer<NativeKeyEvent> consumer, int... keys){
        hotkeys.add(new HotkeyDispatcher(type, consumer, keys));
    }

    /**
     * @param consumer Wird bei Auslösen des Events aufgerufen, in diesem Fall bei eintippen ('typed') der Tasten
     * @param keys Beliebig viele Tasten für Tastenkombinationen. Die Tastencodes sollten alle aus {@link NativeKeyEvent} stammen! Wenn keys[0]==-1 löst das Event immer aus.
     */
    public void addHotkey(Consumer<NativeKeyEvent> consumer, int... keys){
        hotkeys.add(new HotkeyDispatcher(TRIGGER_TYPE.TYPED, consumer, keys));
    }

    /**
     * @param consumer Wird bei Auslösen des Events aufgerufen, in diesem Fall bei eintippen ('typed') der Taste
     * @param key Eine Taste. Der Tastencodes sollte aus {@link NativeKeyEvent} stammen! Wenn key==-1 löst das Event immer aus.
     */
    public void addHotkey(Consumer<NativeKeyEvent> consumer, int key){
        hotkeys.add(new HotkeyDispatcher(TRIGGER_TYPE.TYPED, consumer, key));
        //hotkeys.forEach((hk) -> System.out.println(hk.toString()));
    }

    /**
     * Startet den Listening-Thread, muss zwingend aufgerufen werden bevor Tastendrucke registriert werden.
     * @return <code>true</code> bei fehlerfreier Initialisierung
     */
    public boolean activate(){
        if(active) return true;
        JNHUtil.activate();
        
        GlobalScreen.addNativeKeyListener(this);
        active = true;
        return true;
    }

    /**
     * Interne Methode.
     * @param nativeKeyEvent
     */
    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        currentlyPressed.add(nativeKeyEvent.getKeyCode());

        //System.out.println(nativeKeyEvent.getKeyCode()());
        //System.out.println(KeyEvent.VK_CONTROL);

        for(HotkeyDispatcher hk : hotkeys) {
            if(hk.type == TRIGGER_TYPE.PRESSED){
                if(shouldDispatch(hk, nativeKeyEvent)){
                    hk.consumer.accept(nativeKeyEvent);
                }
            }
        }
    }

    /**
     * Interne Methode.
     * @param nativeKeyEvent
     */
    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
        currentlyPressed.remove(new Integer(nativeKeyEvent.getKeyCode()));

        for(HotkeyDispatcher hk : hotkeys) {
            if(hk.type == TRIGGER_TYPE.RELEASED){
                if(shouldDispatch(hk, nativeKeyEvent)){
                    hk.consumer.accept(nativeKeyEvent);
                }
            }
        }
    }

    /**
     * Interne Methode.
     * @param nativeKeyEvent
     */
    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {
        for(HotkeyDispatcher hk : hotkeys) {
            if(hk.type == TRIGGER_TYPE.TYPED){
                if(shouldDispatch(hk, nativeKeyEvent)){
                    hk.consumer.accept(nativeKeyEvent);
                }
            }
        }
    }

    private boolean shouldDispatch(HotkeyDispatcher hk, NativeKeyEvent currentEvent){
        boolean b = true;
        if(hk.keys[0]==-1) return true;
        for(int i : hk.keys){
            //System.out.println(i + " " + currentlyPressed.contains(i) + " " + currentEvent.getKeyCode()());
            if(!currentlyPressed.contains(i) && !(currentEvent.getKeyCode()==i))
                b = false;
        }
        //System.out.println(b);
        return b;
    }
}