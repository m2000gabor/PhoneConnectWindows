package hu.elte.sbzbxr.phoneconnect.model.connection;

import java.io.IOException;
import java.io.OutputStream;

public class SafeOutputStream {
    private final OutputStream outputStream;

    public SafeOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void write(byte[] bytes) throws IOException {
        synchronized (outputStream){
            outputStream.write(bytes);
        }
    }
}
