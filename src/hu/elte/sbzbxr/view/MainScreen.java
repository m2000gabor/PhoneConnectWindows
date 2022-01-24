package hu.elte.sbzbxr.view;

import com.sun.jna.NativeLibrary;
import hu.elte.sbzbxr.controller.Controller;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.SocketAddress;
import java.util.Objects;

public class MainScreen extends JFrame {
    private Controller controller;
    JPanel northPanel;
    JPanel centerPanel;
    JLabel ipAddressLabel;
    JLabel connectionLabel;
    JLabel messageLabel;

    EmbeddedMediaPlayerComponent mediaPlayerComponent;

    public MainScreen(SocketAddress serverAddress){
        setFancyLookAndFeel();

        //window
        setLayout(new BorderLayout());

        //UI elements
        northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel,BoxLayout.PAGE_AXIS));

        ipAddressLabel =new JLabel();
        northPanel.add(ipAddressLabel);

        connectionLabel =new JLabel();
        northPanel.add(connectionLabel);

        messageLabel =new JLabel("No message");
        northPanel.add(messageLabel);

        if(Objects.isNull(serverAddress)){
            ipAddressLabel.setText("Ip: Unknown");
            connectionLabel.setText("Not connected");
        }else{
            ipAddressLabel.setText(("Ip: "+serverAddress));
            connectionLabel.setText("Connected");
        }

        //Video panel
        centerPanel = new JPanel(new BorderLayout());

        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "lib");
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mediaPlayerComponent.release();
                super.windowClosing(e);
            }
        });
        centerPanel.add(mediaPlayerComponent);



        //UI final moves
        add(northPanel,BorderLayout.NORTH);
        add(centerPanel,BorderLayout.CENTER);
        setTitle("PhoneConnect");
        //setPreferredSize(new Dimension(300,300));
        GraphicsConfiguration gc = getGraphicsConfiguration();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        int height=Toolkit.getDefaultToolkit().getScreenSize().height - screenInsets.bottom;
        setPreferredSize(new Dimension(height/2,height));
        //setPreferredSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().height/2,Toolkit.getDefaultToolkit().getScreenSize().height));
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void setFancyLookAndFeel() {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("No look and feel");
        }
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void playVideo(String path){
        mediaPlayerComponent.mediaPlayer().submit(() -> {mediaPlayerComponent.mediaPlayer().media().play(path);});
    }

    public void initVideoPlayer() {
        mediaPlayerComponent.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void finished(final MediaPlayer mediaPlayer) {
                controller.videoFinished(mediaPlayer);
            }
        });
    }
}
