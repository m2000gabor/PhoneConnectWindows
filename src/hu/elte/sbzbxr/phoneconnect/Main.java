package hu.elte.sbzbxr.phoneconnect;

import hu.elte.sbzbxr.phoneconnect.controller.Controller;

public class Main {
    public static final boolean LOG_SEGMENTS=false;
    public static final boolean SAVE_RESIZED_IMG = false;
    public static void main(String[] args) {
        Controller controller = new Controller();
        controller.init();
        controller.start();
    }
}
