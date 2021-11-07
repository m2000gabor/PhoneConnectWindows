package hu.elte.sbzbxr;

import hu.elte.sbzbxr.controller.Controller;
import hu.elte.sbzbxr.model.ServerMain;
import hu.elte.sbzbxr.view.WelcomeScreen;

public class Main {

    public static void main(String[] args) {
        ServerMain server = new ServerMain();
        WelcomeScreen welcomeScreen = new WelcomeScreen();
        Controller controller = new Controller(server, welcomeScreen );
        controller.start();

        try {Thread.sleep( 60000 );} catch( Exception e ){e.printStackTrace();}
    }
}
