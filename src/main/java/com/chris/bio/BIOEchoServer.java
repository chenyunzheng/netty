package com.chris.bio;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BIOEchoServer {

    private static ExecutorService executorService = Executors.newCachedThreadPool();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8000);
        System.out.println("server start");
        while (true) {
            final Socket socket = serverSocket.accept();
            System.out.println("connect from: " + socket);
            executorService.submit(() -> {
                try {
                    InputStream inputStream = socket.getInputStream();
                    byte[] bytes = new byte[1024];
                    int len;
                    while ((len = inputStream.read(bytes)) != -1) {
                        String data = new String(bytes, 0, len, StandardCharsets.UTF_8);
                        System.out.println(data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        }
    }
}
