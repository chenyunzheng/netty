package com.chris.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class NIOEchoServer {

    public static void main(String[] args) throws IOException {
        //IO多路复用
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(8001));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("nio echo server start!!!");
        while (true) {
            //blocking for ready i/o
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            for (SelectionKey selectionKey : selectionKeys) {
                if (selectionKey.isAcceptable()) {
                    ServerSocketChannel ssc = (ServerSocketChannel)selectionKey.channel();
                    SocketChannel sc = ssc.accept();
                    System.out.println("accept new conn from: " + sc.getRemoteAddress());
                    sc.configureBlocking(false);
                    sc.register(selector, SelectionKey.OP_READ);
                } else if (selectionKey.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel)selectionKey.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    int len;
                    while ((len = socketChannel.read(byteBuffer)) > 0) {
                        byteBuffer.flip();
                        byte[] bytes = new byte[byteBuffer.remaining()];
                        // 将数据读入到byte数组中
                        byteBuffer.get(bytes);
                        String content = new String(bytes, StandardCharsets.UTF_8);
                        System.out.println("receive content: " + content);
                    }
                }
                /////Exception - java.util.ConcurrentModificationException
                selectionKeys.remove(selectionKey);
            }

        }

    }
}
