package cz.stechy.drd.dao;

import static cz.stechy.drd.R.Database.Hero.*;

import cz.stechy.drd.db.BaseDatabaseService;
import cz.stechy.drd.db.base.Database;
import cz.stechy.drd.di.Singleton;
import cz.stechy.drd.model.entity.hero.Hero;
import cz.stechy.drd.service.ItemRegistry;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Služba spravující CRUD operace nad třídou {@link Hero}
 */
@Singleton
public final class HeroDao extends BaseDatabaseService<Hero> {

    // region Constants

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(HeroDao.class);

    private static final String[] COLUMNS = new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_AUTHOR,
        COLUMN_DESCRIPTION, COLUMN_CONVICTION, COLUMN_RACE, COLUMN_PROFESSION, COLUMN_LEVEL,
        COLUMN_MONEY, COLUMN_EXPERIENCES, COLUMN_STRENGTH, COLUMN_DEXTERITY, COLUMN_IMMUNITY,
        COLUMN_INTELLIGENCE, COLUMN_CHARISMA, COLUMN_HEIGHT, COLUMN_DEFENCE_NUMBER, COLUMN_LIVE,
        COLUMN_MAX_LIVE, COLUMN_MAG, COLUMN_MAX_MAG};
    private static final String COLUMNS_KEYS = GENERATE_COLUMN_KEYS(COLUMNS);
    private static final String COLUMNS_VALUES = GENERATE_COLUMNS_VALUES(COLUMNS);
    private static final String COLUMNS_UPDATE = GENERATE_COLUMNS_UPDATE(COLUMNS);
    private static final String QUERY_CREATE = String.format("CREATE TABLE IF NOT EXISTS %s("
            + "%s VARCHAR PRIMARY KEY NOT NULL UNIQUE,"         // id
            + "%s VARCHAR(255) NOT NULL,"                       // name
            + "%s VARCHAR(255),"                                // author
            + "%s VARCHAR(255),"                                // description
            + "%s INT NOT NULL,"                                // conviction
            + "%s INT NOT NULL,"                                // race
            + "%s INT NOT NULL,"                                // profession
            + "%s INT NOT NULL,"                                // level
            + "%s INT NOT NULL,"                                // money
            + "%s INT NOT NULL,"                                // experiences
            + "%s INT NOT NULL,"                                // strength
            + "%s INT NOT NULL,"                                // dexterity
            + "%s INT NOT NULL,"                                // immunity
            + "%s INT NOT NULL,"                                // intelligence
            + "%s INT NOT NULL,"                                // charisma
            + "%s INT NOT NULL,"                                // height
            + "%s INT NOT NULL,"                                // defence number
            + "%s INT NOT NULL,"                                // baseLive
            + "%s INT NOT NULL,"                                // max baseLive
            + "%s INT NOT NULL,"                                // mag
            + "%s INT NOT NULL"                                 // max mag
            + ");", TABLE_NAME, COLUMN_ID, COLUMN_NAME, COLUMN_AUTHOR, COLUMN_DESCRIPTION,
        COLUMN_CONVICTION, COLUMN_RACE, COLUMN_PROFESSION, COLUMN_LEVEL, COLUMN_MONEY,
        COLUMN_EXPERIENCES, COLUMN_STRENGTH, COLUMN_DEXTERITY, COLUMN_IMMUNITY, COLUMN_INTELLIGENCE,
        COLUMN_CHARISMA, COLUMN_HEIGHT, COLUMN_DEFENCE_NUMBER, COLUMN_LIVE, COLUMN_MAX_LIVE,
        COLUMN_MAG, COLUMN_MAX_MAG);

    // endregion

    // region Variables

    private static boolean tableInitialized = false;

    private final ItemRegistry itemRegistry;
    // Správce inventáře pro hrdinu
    private InventoryDao inventoryDao;

    // endregion

    // region Constructors

    /**
     * Vytvoří nového správce hrdinů
     *
     * @param db {@link Database} Databáze, která obsahuje data o hrdinech
     * @param itemRegistry
     */
    public HeroDao(Database db, ItemRegistry itemRegistry) {
        super(db);
        this.itemRegistry = itemRegistry;
    }

    // endregion

    // region Private methods

    @Override
    protected Hero parseResultSet(ResultSet resultSet) throws SQLException {
        return new Hero.Builder()
            .id(resultSet.getString(COLUMN_ID))
            .name(resultSet.getString(COLUMN_NAME))
            .author(resultSet.getString(COLUMN_AUTHOR))
            .description(resultSet.getString(COLUMN_DESCRIPTION))
            .conviction(resultSet.getInt(COLUMN_CONVICTION))
            .race(resultSet.getInt(COLUMN_RACE))
            .profession(resultSet.getInt(COLUMN_PROFESSION))
            .level(resultSet.getInt(COLUMN_LEVEL))
            .money(resultSet.getInt(COLUMN_MONEY))
            .experiences(resultSet.getInt(COLUMN_EXPERIENCES))
            .strength(resultSet.getInt(COLUMN_STRENGTH))
            .dexterity(resultSet.getInt(COLUMN_DEXTERITY))
            .immunity(resultSet.getInt(COLUMN_IMMUNITY))
            .intelligence(resultSet.getInt(COLUMN_INTELLIGENCE))
            .charisma(resultSet.getInt(COLUMN_CHARISMA))
            .height(resultSet.getInt(COLUMN_HEIGHT))
            .defenceNumber(resultSet.getInt(COLUMN_DEFENCE_NUMBER))
            .live(resultSet.getInt(COLUMN_LIVE))
            .maxLive(resultSet.getInt(COLUMN_MAX_LIVE))
            .mag(resultSet.getInt(COLUMN_MAG))
            .maxMag(resultSet.getInt(COLUMN_MAX_MAG))
            .build();
    }

    @Override
    protected List<Object> itemToParams(Hero hero) {
        return new ArrayList<>(Arrays.asList(
            hero.getId(),
            hero.getName(),
            hero.getAuthor(),
            hero.getDescription(),
            hero.getConviction().ordinal(),
            hero.getRace().ordinal(),
            hero.getProfession().ordinal(),
            hero.getLevel(),
            hero.getMoney().getRaw(),
            hero.getExperiences().getActValue().intValue(),
            hero.getStrength().getValue(),
            hero.getDexterity().getValue(),
            hero.getImmunity().getValue(),
            hero.getIntelligence().getValue(),
            hero.getCharisma().getValue(),
            hero.getHeight().ordinal(),
            hero.getDefenceNumber(),
            hero.getLive().getActValue().intValue(),
            hero.getLive().getMaxValue().intValue(),
            hero.getMag().getActValue().intValue(),
            hero.getMag().getMaxValue().intValue()
        ));
    }

    @Override
    protected String getTable() {
        return TABLE_NAME;
    }

    @Override
    protected String getColumnWithId() {
        return COLUMN_ID;
    }

    @Override
    protected String getColumnsKeys() {
        return COLUMNS_KEYS;
    }

    @Override
    protected String getColumnValues() {
        return COLUMNS_VALUES;
    }

    @Override
    protected String getColumnsUpdate() {
        return COLUMNS_UPDATE;
    }

    @Override
    protected String getInitializationQuery() {
        return QUERY_CREATE;
    }

    // endregion

    // region Public methods

    @Override
    public CompletableFuture<Void> createTableAsync() {
        if (tableInitialized) {
            return CompletableFuture.completedFuture(null);
        }

        return super.createTableAsync()
            .thenAccept(ignore -> tableInitialized = true);
    }

    @Override
    public CompletableFuture<Hero> insertAsync(Hero hero) {
        return getInventoryAsync(hero)
            // Před vložením hrdiny se pro něj musí vytvořit inventář
            .thenCompose(inventoryService -> inventoryService
                .insertAsync(InventoryDao.standartInventory(hero)))
            // Až poté se vloží záznam o hrdinovi do databáze
            .thenCompose(inventory -> super.insertAsync(hero));
    }

    /**
     * Vytvoří novou instanci správce itemů pro zadaného hrdinu
     *
     * @param hero {@link Hero}
     * @return {@link CompletableFuture<  InventoryContentDao  >}
     */
    public CompletableFuture<InventoryDao> getInventoryAsync(Hero hero) {
        if (inventoryDao == null) {
            inventoryDao = new InventoryDao(db, hero, itemRegistry);
        }

        return inventoryDao.selectAllAsync().thenApply(inventories -> inventoryDao);
    }

    public void resetInventory() {
        inventoryDao = null;
    }

    // endregion

}
