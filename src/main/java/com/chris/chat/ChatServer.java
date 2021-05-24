package com.chris.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class ChatServer {

    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(8080));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                if (selectionKey.isAcceptable()) {
                    ServerSocketChannel ssc = (ServerSocketChannel)selectionKey.channel();
                    SocketChannel sc = ssc.accept();
                    System.out.println("accept new conn: " + sc.getRemoteAddress());
                    sc.configureBlocking(false);
                    sc.register(selector, SelectionKey.OP_READ);
                    // 加入群聊
                    ChatHolder.join(sc);
                } else if (selectionKey.isReadable()){
                    SocketChannel socketChannel = (SocketChannel)selectionKey.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    int len = socketChannel.read(byteBuffer);
                    if (len > 0) {
                        byteBuffer.flip();
                        byte[] bytes = new byte[byteBuffer.remaining()];
                        byteBuffer.get(bytes);
                        String content = new String(bytes, StandardCharsets.UTF_8).replace("\r\n", "");
                        if ("quit".equalsIgnoreCase(content)) {
                            ChatHolder.quit(socketChannel);
                            socketChannel.close();
                            selectionKey.cancel();
                        } else {
                            ChatHolder.propagate(socketChannel, content);
                        }
                    }
                }
                iterator.remove();
            }
        }
    }

    private static class ChatHolder {
        private static final ConcurrentHashMap<SocketChannel, String> userMap = new ConcurrentHashMap<>();

        /**
         * 加入群聊
         * @param socketChannel
         */
        public static void join(SocketChannel socketChannel) {
            // 有人加入就给他分配一个id
            String userId = "用户" + ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
            // 告知用户
            send(socketChannel, "您的id为：" + userId + "\r\n");
            // 通知其他用户
            for (SocketChannel sc : userMap.keySet()) {
                send(sc, userId + "加入了群聊\r\n");
            }
            // 将当前用户加入到map中
            userMap.putIfAbsent(socketChannel, userId);
        }

        /**
         * 退出群聊
         * @param socketChannel
         */
        public static void quit(SocketChannel socketChannel) {
            String userId = userMap.remove(socketChannel);
            if (Objects.nonNull(userId)) {
                send(socketChannel, "您退出了群聊" + "\r\n");
                for (SocketChannel sc : userMap.keySet()) {
                    send(sc, userId + "退出群聊\r\n");
                }
            }
        }

        /**
         * 发送消息
         * @param socketChannel
         * @param message
         */
        private static void send(SocketChannel socketChannel, String message) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            byteBuffer = byteBuffer.put(message.getBytes());
            byteBuffer.flip();
            try {
                socketChannel.write(byteBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 扩散说话的内容
         * @param socketChannel
         * @param content
         */
        public static void propagate(SocketChannel socketChannel, String content) {
            String userId = userMap.get(socketChannel);
            if (Objects.nonNull(userId)) {
                for (SocketChannel sc : userMap.keySet()) {
                    if (sc != socketChannel) {
                        send(sc, userId + "说：" + content + "\r\n");
                    }
                }
            }
        }
    }
}
