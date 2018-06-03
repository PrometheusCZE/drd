package cz.stechy.drd.dao;

import cz.stechy.drd.di.Singleton;
import cz.stechy.drd.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Služba spravující CRUD operace nad třídou {@link User}
 */
@Singleton
public final class UserDao {

    // region Constants

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);

    // region Názvy sloupců v databázi

    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_NAME = "name";

    // endregion

    // endregion

    // region Variables

    private final ObservableList<User> onlineDatabase = FXCollections.observableArrayList();

    // endregion

    // region Constructors

    /**
     * Vytvoří nového správce uživatelů
     */
    public UserDao() {
//        wrapper.firebaseProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue != null) {
//                onlineDatabase.clear();
//                firebaseReference = newValue.getReference(FIREBASE_CHILD_NAME);
//                firebaseReference.addChildEventListener(childEventListener);
//            }
//        });
    }

    // endregion

    // region Private methods

//    @Override
//    public void uploadAsync(User user, CompletionListener listener) {
//        final DatabaseReference child = firebaseReference.child(user.getId());
//        child.setValue(toFirebaseMap(user), listener);
//    }

//    @Override
//    public void deleteRemoteAsync(User item, boolean remote, CompletionListener listener) {
//        throw new NotImplementedException();
//    }

    // endregion

    // region Public methods

//    @Override
//    public User parseDataSnapshot(DataSnapshot snapshot) {
//        return new User.Builder()
//            .id(snapshot.getKey())
//            .name(snapshot.child(COLUMN_NAME).getValue(String.class))
//            .password(snapshot.child(COLUMN_PASSWORD).getValue(String.class))
//            .build();
//    }

//    @Override
//    public Map<String, Object> toFirebaseMap(User user) {
//        Map<String, Object> map = new HashMap<>();
//        map.put(COLUMN_NAME, user.getName());
//        map.put(COLUMN_PASSWORD, user.getPassword());
//        return map;
//    }

    public ObservableList<User> getUsers() {
        return FXCollections.unmodifiableObservableList(onlineDatabase);
    }

    // endregion

//    private final ChildEventListener childEventListener = new ChildEventListener() {
//        @Override
//        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//            final User user = parseDataSnapshot(dataSnapshot);
//            LOGGER.trace("Přidávám uživatele {} z online databáze", user.toString());
//            onlineDatabase.add(user);
//        }
//
//        @Override
//        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//            LOGGER.trace("Data uživatele byla změněna v online databázi");
//        }
//
//        @Override
//        public void onChildRemoved(DataSnapshot dataSnapshot) {
//            final User u = parseDataSnapshot(dataSnapshot);
//            LOGGER.trace("Uživatel byl smazán z online databáze", u.toString());
//            onlineDatabase.stream()
//                .filter(u::equals)
//                .findFirst()
//                .ifPresent(onlineDatabase::remove);
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
