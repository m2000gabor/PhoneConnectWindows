package hu.elte.sbzbxr;

import hu.elte.sbzbxr.controller.Controller;
import hu.elte.sbzbxr.model.QrGenerator;
import hu.elte.sbzbxr.model.ServerMainModel;
import hu.elte.sbzbxr.view.WelcomeScreen;

public class Main {

    public static void main(String[] args) {
        ServerMainModel server = new ServerMainModel();
        WelcomeScreen welcomeScreen = new WelcomeScreen();
        Controller controller = new Controller(server, welcomeScreen );
        controller.init();
        controller.start();
    }
}
