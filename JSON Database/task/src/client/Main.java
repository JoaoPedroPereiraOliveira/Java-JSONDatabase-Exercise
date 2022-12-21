package client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Main {

    static String FILENAME_TEST_ENVIRONMENT = System.getProperty("user.dir") + "/src/client/data/";
    static String FILENAME_LOCAL_ENVIRONMENT = System.getProperty("user.dir") + "/JSON Database/task/src/client/data/";

    public static class Args {
        @Parameter(names = {"-t"}, required = false)
        private String type;
        @Parameter(names = {"-k"}, required = false)
        private String key;
        @Parameter(names = {"-v"}, required = false)
        private String value;

        @Parameter(names = {"-in"}, required = false)
        private String input;
    }

    static class Key<T> {
        T keyValue;

        Key(T keyValue) {
            this.keyValue = keyValue;
        }

        public T getKeyValue() {
            return keyValue;
        }
    }

    public static synchronized void main(String[] args) {
        try {
            String ip = "127.0.0.1";
            int port = 23456;

            System.out.println("Client started!");

            Socket socket = new Socket(InetAddress.getByName(ip), port);

            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            Args jArgs = new Args();
            JCommander.newBuilder().addObject(jArgs).build().parse(args);



            Gson gson = new Gson();

            String outputJson = null;

            if (jArgs.input != null) {
                ReadWriteLock lock = new ReentrantReadWriteLock();
                Lock readLock = lock.readLock();
                Lock writeLock = lock.writeLock();

                readLock.lock();
                File inputFile = new File(FILENAME_TEST_ENVIRONMENT + jArgs.input);
                Scanner myReader = new Scanner(inputFile);
                outputJson = myReader.nextLine();
                myReader.close();
                readLock.unlock();
            } else {
                outputJson = gson.toJson(jArgs);
            }

            System.out.println("Sent: " + outputJson);

            output.writeUTF(outputJson);

            System.out.println("Received: " + input.readUTF());

            socket.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
