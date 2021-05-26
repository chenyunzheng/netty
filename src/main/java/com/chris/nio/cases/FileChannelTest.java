package com.chris.nio.cases;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class FileChannelTest {

    public static void main(String[] args) throws IOException {
        //从文件获取fileChannel
        FileChannel fileChannel = new RandomAccessFile("FileChannelTest.txt", "rw").getChannel();
        //声明一个Byte类型的Buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        while (fileChannel.read(byteBuffer) > 0) {
            byteBuffer.flip();
            if (byteBuffer.hasRemaining()) {
                byte[] bytes = new byte[byteBuffer.remaining()];
                byteBuffer.get(bytes);
                System.out.println(new String(bytes, StandardCharsets.UTF_8));
            }
            byteBuffer.clear();
        }
    }
}
