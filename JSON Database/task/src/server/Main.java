package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Main {

    static String FILENAME_TEST_ENVIRONMENT = System.getProperty("user.dir") + "/src/server/data/db.json";
    static String FILENAME_LOCAL_ENVIRONMENT = System.getProperty("user.dir") + "/JSON Database/task/src/server/data/db.json";

    static class DataBase<T> {
        Map<String, String> database;
        File dbfile;
        final String valid = "OK";
        final String error = "ERROR";
        final String reason = "No such key";

        ReadWriteLock lock = new ReentrantReadWriteLock();
        Lock writeLock = lock.writeLock();

        public DataBase() {
            this.database = new HashMap<String, String>();
            try {
                this.dbfile = new File(FILENAME_TEST_ENVIRONMENT);
                dbfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public Send get(T key) {
            if (key.getClass().getName().equals("java.util.ArrayList")) {
                ArrayList<String> k = new ArrayList<>((ArrayList<String>) key);

                Gson gson = new Gson();

                JsonObject result = gson.fromJson(database.get(k.get(0)), JsonObject.class);

                if (k.size() > 1)
                    return new Send(valid, result.get(k.get(k.size() - 1)).getAsString());
                else
                    return new Send(valid, result);
            }
            if (database.containsKey(key.toString())) {
                return new Send(valid, database.get(key.toString()));
            } else {
                return new Send(error, null, reason);
            }

        }

        public Send set(T key, String value) {
            if (key.getClass().getName().equals("java.util.ArrayList")) {
                ArrayList<String> k = new ArrayList<>((ArrayList<String>) key);

                Gson gson = new Gson();

                JsonObject result = gson.fromJson(database.get(k.get(0)), JsonObject.class);

                result.get(k.get(1)).getAsJsonObject().addProperty(k.get(2), value.replace("\"", ""));

                database.put(k.get(0), gson.toJson(result));

            } else {
                database.put((String) key, value);
            }

            writeLock.lock();
            try {
                Gson gson = new Gson();
                FileWriter writer = new FileWriter(dbfile);
                writer.write(gson.toJson(database));
                writer.close();
            } catch (IOException e) {
                return new Send(e.toString());
            }
            writeLock.unlock();
            return new Send(valid);
        }

        public Send delete(T key) {
            if (key.getClass().getName().equals("java.util.ArrayList")) {
                ArrayList<String> k = new ArrayList<>((ArrayList<String>) key);

                if (!database.containsKey(k.get(0)))
                    return new Send(error, reason);

                Gson gson = new Gson();

                JsonObject result = gson.fromJson(database.get(k.get(0)), JsonObject.class);

                result.get(k.get(1)).getAsJsonObject().remove(k.get(k.size() - 1));

                database.put(k.get(0), gson.toJson(result));
                return new Send(valid);
            } else {
                if (!database.containsKey(key))
                    return new Send(error, reason);

                database.remove(key);
                return new Send(valid);
            }
        }
    }

    static class conv {
        Object value;

        public Object getValue() {
            return value;
        }
    }

    static class Send<T> {
        String response;
        String reason;
        T value;

        public Send(String response) {
            this.response = response;
        }

        public Send(String response, T value) {
            this.response = response;
            this.value = value;
        }

        public Send(String response, T value, String reason) {
            this.response = response;
            this.value = value;
            this.reason = reason;
        }
    }

    class Key<T> {
        T keyValue;

        Key(T keyValue) {
            this.keyValue = keyValue;
        }

        public String getKeyValue() {
            return keyValue.toString();
        }
    }

    static class Interpt<T> {
        String type;
        T key;
        Object value;

        public String getType() {
            return type;
        }

        public T getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }
    }

    private static Main server;
    private ServerSocket serverSocket;

    private ExecutorService executorService = Executors.newFixedThreadPool(20);
    DataBase dataBase = new DataBase();
    public static void main(String[] args) throws IOException {
        server = new Main();
        server.runServer();
    }

    private void runServer() {
        int serverPort = 23456;
        try {
            System.out.println("Server started!");
            serverSocket = new ServerSocket(serverPort);

            while(!serverSocket.isClosed()) {
                try {
                    Socket s = serverSocket.accept();
                    executorService.submit(new ServiceRequest(s));
                } catch(IOException ioe) {
                    System.out.println("Error accepting connection");
                    ioe.printStackTrace();
                }
            }
        }catch(IOException e) {
            System.out.println("Error starting Server on "+serverPort);
            e.printStackTrace();
        }
    }

    private void stopServer() {
        executorService.shutdownNow();
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Error in server shutdown");
            e.printStackTrace();
        }
        System.exit(0);
    }

    class ServiceRequest implements Runnable {

        private Socket socket;

        public ServiceRequest(Socket connection) {
            this.socket = connection;
        }

        public void run() {

            try {
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());

                Gson gson = new Gson();
                
                String op = input.readUTF();

                Interpt interpt = gson.fromJson(op, Interpt.class);

                switch (interpt.getType()) {
                    case "get":
                        output.writeUTF(gson.toJson(dataBase.get(interpt.getKey())));
                        break;
                    case "set":
                        output.writeUTF(gson.toJson(dataBase.set(interpt.getKey(), gson.toJson(interpt.getValue()))));
                        break;
                    case "delete":
                        output.writeUTF(gson.toJson(dataBase.delete(interpt.getKey())));
                        break;
                    case "exit":
                        output.writeUTF(gson.toJson(new Send("OK")));
                        stopServer();
                        break;
                }

                try {
                    socket.close();
                } catch (IOException e){

                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
