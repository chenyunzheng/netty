package com.chris.nio.cases;

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
        ByteBuffer byteBuffer = ByteBuffer.allocate(20);
        while (fileChannel.read(byteBuffer) != -1) {
            // buffer切换为读模式
            byteBuffer.flip();
            if (byteBuffer.hasRemaining()) {
                //采用字节流的方式，一次次sout，不可避免出现乱码现象
                //比如中英文，china你好！
                //在UTF-8编码下，每个中文占3个字节，如果读取的字节数不满3个，则随后的System.out.println()会出现乱码
                //采用字符流InputStreamReader或者按编码方式申请字节数
                byte[] bytes = new byte[byteBuffer.remaining()];
                byteBuffer.get(bytes);
                //若是直接写到文件没有乱码问题
                System.out.println(new String(bytes, StandardCharsets.UTF_8));
            }
            // clear()会将buffer再次切换为写模式
            byteBuffer.clear();
        }
    }
}
