package cz.stechy.drd.service;

import cz.stechy.drd.crypto.RSA.CypherKey;
import cz.stechy.drd.di.Singleton;
import cz.stechy.drd.model.chat.ChatContact;
import cz.stechy.drd.model.chat.OnChatMessageReceived;
import cz.stechy.drd.net.ClientCommunicator;
import cz.stechy.drd.net.OnDataReceivedListener;
import cz.stechy.drd.net.message.ChatMessage;
import cz.stechy.drd.net.message.ChatMessage.ChatMessageAdministrationData;
import cz.stechy.drd.net.message.ChatMessage.ChatMessageAdministrationData.ChatAction;
import cz.stechy.drd.net.message.ChatMessage.ChatMessageAdministrationData.ChatMessageAdministrationClient;
import cz.stechy.drd.net.message.ChatMessage.ChatMessageAdministrationData.ChatMessageAdministrationClientRequestConnect;
import cz.stechy.drd.net.message.ChatMessage.ChatMessageAdministrationData.ChatMessageAdministrationClientRoom;
import cz.stechy.drd.net.message.ChatMessage.ChatMessageAdministrationData.ChatMessageAdministrationClientTyping;
import cz.stechy.drd.net.message.ChatMessage.ChatMessageAdministrationData.ChatMessageAdministrationRoom;
import cz.stechy.drd.net.message.ChatMessage.ChatMessageAdministrationData.IChatMessageAdministrationData;
import cz.stechy.drd.net.message.ChatMessage.ChatMessageCommunicationData;
import cz.stechy.drd.net.message.ChatMessage.IChatMessageData;
import cz.stechy.drd.net.message.MessageSource;
import cz.stechy.drd.net.message.MessageType;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Třída poskytující chatovací služby
 */
@Singleton
public final class ChatService {

    // region Constants

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatService.class);

    // endregion

    // region Variables

    // Kolekce připojených klientů
    private final ObservableMap<String, ChatContact> clients = FXCollections.observableHashMap();
    // Kolekce vytvořených místností
    private final ObservableMap<String, ObservableList<String>> rooms = FXCollections.observableHashMap();
    // Register posluchačů na příjem zprávy
    private final List<OnChatMessageReceived> messageListeners = new ArrayList<>();

    // Komunikátor se serverem
    private ClientCommunicator communicator;
    private CryptoService cryptoService;

    // endregion

    // region Constructors

    /**
     * Vytvoří novou chatovací službu
     *
     * @param communicator {@link ClientCommunicator} Služba poskytující komunikaci se serverem
     * @param cryptoService {@link CryptoService} Služba poskytující šifrovací funkce
     * @param userService  {@link UserService} Služba poskytující informace o uživateli
     */
    public ChatService(ClientCommunicator communicator, CryptoService cryptoService,
        UserService userService) {
        this.communicator = communicator;
        this.cryptoService = cryptoService;
        this.communicator.connectionStateProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case CONNECTED:
                    this.communicator.registerMessageObserver(MessageType.CHAT, this.chatMessageListener);
                    break;
                case CONNECTING:
                    break;
                case DISCONNECTED:
                    this.communicator.unregisterMessageObserver(MessageType.CHAT, this.chatMessageListener);
                    break;
            }

        });
        userService.userProperty().addListener((observable, oldValue, newValue) -> {
            // Odhlásím starého uživatele
            if (oldValue != null) {
                communicator.sendMessage(new ChatMessage(MessageSource.CLIENT,
                    new ChatMessageAdministrationData(
                        new ChatMessageAdministrationClient(ChatAction.CLIENT_DISCONNECTED, oldValue.getId())
                    )));
            }

            if (newValue == null) {
                clients.clear();
                rooms.clear();
                return;
            }

            // Přihlásím nového uživatele
            communicator.sendMessage(new ChatMessage(MessageSource.CLIENT,
                new ChatMessageAdministrationData(
                    new ChatMessageAdministrationClientRequestConnect(newValue.getId(),
                        userService.getUser().getName()))));
        });
    }

    // endregion

    // region Public methods

    /**
     * Odešle zprávu
     *
     * @param message Obsah zprávy
     * @param destination ID cílového klienta
     */
    public void sendMessage(String message, String destination) {
        final ChatContact chatContact = clients.get(destination);
        if (chatContact == null) {
            throw new RuntimeException("Klient nebyl nalezen.");
        }

        byte[] messageData = chatContact.encrypt(message.getBytes());
        communicator.sendMessage(new ChatMessage(MessageSource.CLIENT,
            new ChatMessageCommunicationData(destination, messageData)));

    }

    public void addChatMessageReceivedListener(OnChatMessageReceived listener) {
        messageListeners.add(listener);
    }

    public void removeChatMessageReceivedListener(OnChatMessageReceived listener) {
        messageListeners.remove(listener);
    }

    // endregion

    // region Getters & Setters

    /**
     * Vrátí seznam všech připojených klientů
     *
     * @return {@link ObservableList}
     */
    public ObservableMap<String, ChatContact> getClients() {
        return FXCollections.unmodifiableObservableMap(clients);
    }

    /**
     * Vrátí seznam všech místností vlastněných klientem
     *
     * @return {@link ObservableMap}
     */
    public ObservableMap<String, ObservableList<String>> getRooms() {
        return FXCollections.unmodifiableObservableMap(rooms);
    }

    // endregion

    private final OnDataReceivedListener chatMessageListener = message -> {
        final ChatMessage chatMessage = (ChatMessage) message;
        final IChatMessageData messageData = (IChatMessageData) chatMessage.getData();
        switch (messageData.getDataType()) {
            case DATA_ADMINISTRATION:
                final ChatMessageAdministrationData administrationData = (ChatMessageAdministrationData) messageData;
                IChatMessageAdministrationData data = (IChatMessageAdministrationData) administrationData.getData();
                switch (data.getAction()) {
                    case CLIENT_CONNECTED:
                        final ChatMessageAdministrationClient messageAdministrationClientConnected = (ChatMessageAdministrationClient) data;
                        final String connectedClientID = messageAdministrationClientConnected.getClientID();
                        final String connectedClientName = messageAdministrationClientConnected.getName();
                        final CypherKey connectedClientKey = messageAdministrationClientConnected.getKey();
                        Platform.runLater(() -> clients.putIfAbsent(connectedClientID, new ChatContact(connectedClientName, cryptoService.makeCypher(connectedClientKey))));
                        break;
                    case CLIENT_DISCONNECTED:
                        final ChatMessageAdministrationClient messageAdministrationClientDiconnected = (ChatMessageAdministrationClient) data;
                        final String disconnectedClientID = messageAdministrationClientDiconnected.getClientID();
                        Platform.runLater(() -> clients.remove(disconnectedClientID));
                        break;
                    case ROOM_CREATED:
                        final ChatMessageAdministrationRoom messageAdministrationRoomCreated = (ChatMessageAdministrationRoom) data;
                        final String createdRoomName = messageAdministrationRoomCreated.roomName();
                        Platform.runLater(() -> rooms.put(createdRoomName, FXCollections.observableArrayList()));
                        break;
                    case ROOM_DELETED:
                        final ChatMessageAdministrationRoom messageAdministrationRoomDeleted = (ChatMessageAdministrationRoom) data;
                        final String deletedRoomName = messageAdministrationRoomDeleted.roomName();
                        Platform.runLater(() -> rooms.remove(deletedRoomName));
                        break;
                    case CLIENT_JOINED_ROOM:
                        final ChatMessageAdministrationClientRoom messageAdministrationClientJoinedRoom = (ChatMessageAdministrationClientRoom) data;
                        final String joinedRoomClientID = messageAdministrationClientJoinedRoom.getClient();
                        final String joinedRoomName = messageAdministrationClientJoinedRoom.getRoom();
                        Platform.runLater(() -> rooms.get(joinedRoomName).add(joinedRoomClientID));
                        break;
                    case CLIENT_LEAVE_ROOM:
                        final ChatMessageAdministrationClientRoom messageAdministrationClientLeavedRoom = (ChatMessageAdministrationClientRoom) data;
                        final String leavedRoomClientID = messageAdministrationClientLeavedRoom.getClient();
                        final String leavedRoomName = messageAdministrationClientLeavedRoom.getRoom();
                        Platform.runLater(() -> rooms.get(leavedRoomName).remove(leavedRoomClientID));
                        break;
                    case CLIENT_TYPING:
                        final ChatMessageAdministrationClientTyping messageAdministrationClientTyping = (ChatMessageAdministrationClientTyping) data;
                        final String typingClientId = messageAdministrationClientTyping.getClientID();
                        break;
                    case CLIENT_NOT_TYPING:
                        final ChatMessageAdministrationClientTyping messageAdministrationClientNoTyping = (ChatMessageAdministrationClientTyping) data;
                        final String noTypingClientId = messageAdministrationClientNoTyping.getClientID();
                        break;
                    default:
                        throw new IllegalArgumentException("Neplatny argument.");
                }
                break;
            case DATA_COMMUNICATION:
                final ChatMessageCommunicationData communicationData = (ChatMessageCommunicationData) messageData;
                final String destination = communicationData.getDestination();
                final byte[] messageRaw = (byte[]) communicationData.getData();
                final String messageContent = new String(cryptoService.decrypt(messageRaw),
                    StandardCharsets.UTF_8);
                messageListeners.forEach(
                    listener -> listener.onChatMessageReceived(messageContent, destination));
                break;
            default:
                throw new IllegalArgumentException("Neplatny parametr.");
        }
    };
}
