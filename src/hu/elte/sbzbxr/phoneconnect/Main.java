package hu.elte.sbzbxr.phoneconnect;

import hu.elte.sbzbxr.phoneconnect.controller.Controller;

public class Main {

    public static void main(String[] args) {
        Controller controller = new Controller();
        controller.init();
        controller.start();
    }
}
