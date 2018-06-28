package cz.stechy.drd;

import cz.stechy.drd.net.message.HelloMessage;
import cz.stechy.drd.net.message.IMessage;
import cz.stechy.drd.net.message.MessageSource;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client implements Runnable {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    private final UUID id = UUID.randomUUID();
    private final Socket socket;
    private final InputStream inputStream;
    final ObjectOutputStream writer;
    private final WriterThread writerThread;
    private final IMessageReceiveListener messageReceiveListener;

    private OnConnectionClosedListener connectionClosedListener;
    private boolean interrupt = false;

    public Client(Socket client, WriterThread writerThread,
        IMessageReceiveListener messageReceiveListener) throws IOException {
        this.writerThread = writerThread;
        this.inputStream = client.getInputStream();
        this.writer = new ObjectOutputStream(client.getOutputStream());
        socket = client;
        this.messageReceiveListener = messageReceiveListener;
        LOGGER.info("Nový klient byl vytvořen.");
    }

    @Override
    public void run() {
        LOGGER.info("Spouštím nekonečnou smyčku pro komunikaci s klientem.");
        try (ObjectInputStream reader = new ObjectInputStream(inputStream)) {
            LOGGER.info("ObjectInputStream byl úspěšně vytvořen.");
            sendMessage(new HelloMessage(MessageSource.SERVER));
            IMessage received;
            while ((received = (IMessage) reader.readObject()) != null && !interrupt) {
                LOGGER.info(String.format("Bylo přijato: '%s'", received));
                messageReceiveListener.onMessageReceive(received, this);
            }
        } catch (EOFException|SocketException e) {
            LOGGER.info("Klient ukončil spojení.");
        } catch (IOException e) {
            LOGGER.warn("Nastala neočekávaná vyjímka.", e);
        } catch (ClassNotFoundException e) {
            // Nikdy by nemělo nastat
            LOGGER.error("Nebyla nalezena třída.", e);
        } finally {
            LOGGER.info("Volám connectionClosedListener.");
            if (connectionClosedListener != null) {
                connectionClosedListener.onConnectionClosed();
            }
            close();
        }
    }

    public void setConnectionClosedListener(OnConnectionClosedListener connectionClosedListener) {
        this.connectionClosedListener = connectionClosedListener;
    }

    public void close() {
        try {
            LOGGER.info("Uzavírám socket.");
            socket.close();
            LOGGER.info("Socket byl úspěšně uzavřen.");
        } catch (IOException e) {
            LOGGER.error("Socket se nepodařilo uzavřít!", e);
        }
    }

    public synchronized void sendMessage(IMessage message) {
        LOGGER.info(
            String.format("Přidávám novou zprávu do fronty pro klienta: '%s'", message.toString()));
        writerThread.sendMessage(writer, message);
    }

    public UUID getId() {
        return id;
    }

    @FunctionalInterface
    public interface OnConnectionClosedListener {
        void onConnectionClosed();
    }
}