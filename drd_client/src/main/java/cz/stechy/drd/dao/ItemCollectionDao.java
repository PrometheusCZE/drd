package cz.stechy.drd.dao;

import static cz.stechy.drd.R.Database.Collectionsitems.*;

import cz.stechy.drd.db.base.OnlineDatabase;
import cz.stechy.drd.di.Singleton;
import cz.stechy.drd.model.item.ItemCollection;
import cz.stechy.drd.net.ClientCommunicator;
import cz.stechy.drd.net.ConnectionState;
import cz.stechy.drd.net.OnDataReceivedListener;
import cz.stechy.drd.net.message.DatabaseMessage;
import cz.stechy.drd.net.message.DatabaseMessage.DatabaseMessageAdministration;
import cz.stechy.drd.net.message.DatabaseMessage.DatabaseMessageAdministration.DatabaseAction;
import cz.stechy.drd.net.message.DatabaseMessage.DatabaseMessageCRUD;
import cz.stechy.drd.net.message.DatabaseMessage.DatabaseMessageDataType;
import cz.stechy.drd.net.message.DatabaseMessage.IDatabaseMessageData;
import cz.stechy.drd.net.message.IMessage;
import cz.stechy.drd.net.message.MessageSource;
import cz.stechy.drd.net.message.MessageType;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ItemCollectionDao implements OnlineDatabase<ItemCollection> {

    // region Constants

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemCollectionDao.class);

    // endregion

    // region Variables

    private final ObservableList<ItemCollection> collections = FXCollections.observableArrayList();
    private final Map<String, ItemCollectionContentDao> contentMap = new HashMap<>();
    private final ClientCommunicator communicator;

    // endregion

    // region Constructors

    public ItemCollectionDao(ClientCommunicator clientCommunicator) {
        this.communicator = clientCommunicator;
        this.communicator.connectionStateProperty()
            .addListener((observable, oldValue, newValue) -> {
                if (newValue != ConnectionState.CONNECTED) {
                    return;
                }

                this.communicator
                    .registerMessageObserver(MessageType.DATABASE, this.databaseListener);
                LOGGER.info("Posílám registrační požadavek pro tabulku: " + TABLE_NAME);
                this.communicator.sendMessage(getRegistrationMessage());
        });
//        wrapper.firebaseProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue != null) {
//                collections.clear();
//                firebaseReference = newValue.getReference(FIREBASE_CHILD_NAME);
//                firebaseReference.addChildEventListener(childEventListener);
//            }
//        });
    }

    // endregion

    // region Private methods

    private IMessage getRegistrationMessage() {
        return new DatabaseMessage(MessageSource.CLIENT, new DatabaseMessageAdministration(
            FIREBASE_CHILD, DatabaseAction.REGISTER));
    }

    // endregion

    // region Public methods

    @Deprecated
    public ItemCollectionContentDao getContent(ItemCollection collection) {
        final String id = collection.getId();
        ItemCollectionContentDao collectionContent = null;
        if (!contentMap.containsKey(id)) {
//            collectionContent = new ItemCollectionContentDao(firebaseReference.child(collection.getId()).child(COLUMN_RECORDS));
//            contentMap.put(id, collectionContent);
        } else {
            collectionContent = contentMap.get(id);
        }

        return collectionContent;
    }

    // endregion

    // region Private methods

    @Override
    public String getFirebaseChildName() {
        return TABLE_NAME;
    }

    @Override
    public ItemCollection fromStringItemMap(Map<String, Object> map) {
        return new ItemCollection.Builder()
            .id((String) map.get(COLUMN_ID))
            .name((String) map.get(COLUMN_NAME))
            .author((String) map.get(COLUMN_AUTHOR))
            .records((Collection<String>) map.get(COLUMN_RECORDS))
            .build();
    }

    @Override
    public Map<String, Object> toStringItemMap(ItemCollection item) {
        final Map<String, Object> map = new HashMap<>();
        map.put(COLUMN_ID, item.getId());
        map.put(COLUMN_NAME, item.getName());
        map.put(COLUMN_AUTHOR, item.getAuthor());
        map.put(COLUMN_RECORDS, item.getRecords().stream().map(s -> new SimpleEntry<>(s, s)));
        return map;
    }

    @Override
    public CompletableFuture<Void> uploadAsync(ItemCollection item) {
        return null;
    }

    @Override
    public CompletableFuture<Void> deleteRemoteAsync(ItemCollection item) {
        return null;
    }

//    @Override
//    public ItemCollection parseDataSnapshot(DataSnapshot snapshot) {
//        final Builder builder = new Builder()
//            .id(snapshot.child(COLUMN_ID).getValue(String.class))
//            .name(snapshot.child(COLUMN_NAME).getValue(String.class))
//            .author(snapshot.child(COLUMN_AUTHOR).getValue(String.class));
//        return builder.build();
//    }

//    @Override
//    public Map<String, Object> toFirebaseMap(ItemCollection item) {
//        final Map<String, Object> map = new HashMap<>();
//        map.put(COLUMN_ID, item.getId());
//        map.put(COLUMN_NAME, item.getName());
//        map.put(COLUMN_AUTHOR, item.getAuthor());
//        return map;
//    }

//    @Override
//    public void uploadAsync(ItemCollection item, DatabaseReference.CompletionListener listener) {
//        final DatabaseReference child = firebaseReference.child(item.getId());
//        child.setValue(toFirebaseMap(item), listener);
//    }

//    @Override
//    public void deleteRemoteAsync(ItemCollection item, boolean remote,
//        CompletionListener listener) {
//        firebaseReference.child(item.getId()).removeValue(listener);
//    }

    // endregion

    // region Getters & Setters

    public ObservableList<ItemCollection> getCollections() {
        return collections;
    }

    // endregion

    private final OnDataReceivedListener databaseListener = message -> {
        final DatabaseMessage databaseMessage = (DatabaseMessage) message;
        IDatabaseMessageData databaseMessageData = (IDatabaseMessageData) databaseMessage.getData();

        if (databaseMessageData.getDataType() != DatabaseMessageDataType.DATA_MANIPULATION) {
            return;
        }

        final DatabaseMessageCRUD crudMessage = (DatabaseMessageCRUD) databaseMessageData;
        if (!crudMessage.getTableName().equals(FIREBASE_CHILD)) {
            return;
        }

        final DatabaseMessageCRUD.DatabaseAction crudAction = crudMessage.getAction();
        final Map<String, Object> data = (Map<String, Object>) crudMessage.getData();
        final ItemCollection itemCollection = fromStringItemMap(data);

        switch (crudAction) {
            case CREATE:
                LOGGER.info("Přidávám kolekci {} do svého povědomí.", itemCollection.toString());
                collections.add(itemCollection);
                break;
            case UPDATE:
                break;
            case DELETE:
            LOGGER.trace("Kolekce předmětů {} byla smazána z online databáze", itemCollection.toString());
            collections.stream()
                .filter(itemCollection::equals)
                .findFirst()
                .ifPresent(collections::remove);
                break;
            default:
                throw new IllegalArgumentException("Neplatny argument");
        }
    };

//    private final ChildEventListener childEventListener = new ChildEventListener() {
//        @Override
//        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//            final ItemCollection user = parseDataSnapshot(dataSnapshot);
//            LOGGER.trace("Přidávám kolekci předmětů {} z online databáze", user.toString());
//            collections.add(user);
//        }
//
//        @Override
//        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//            LOGGER.trace("Data v kolekci předmětů byla změněna v online databázi");
//        }
//
//        @Override
//        public void onChildRemoved(DataSnapshot dataSnapshot) {
//            final ItemCollection u = parseDataSnapshot(dataSnapshot);
//            LOGGER.trace("Kolekce předmětů byla smazána z online databáze", u.toString());
//            collections.stream()
//                .filter(u::equals)
//                .findFirst()
//                .ifPresent(collections::remove);
//        }
//
//        @Override
//        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//        }
//
//        @Override
//        public void onCancelled(DatabaseError databaseError) {
//
//        }
//    };
}
