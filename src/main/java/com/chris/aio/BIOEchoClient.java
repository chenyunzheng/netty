package com.chris.aio;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class BIOEchoClient {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 8003);
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write("hello world...\r\n".getBytes());
        outputStream.close();
    }
}
