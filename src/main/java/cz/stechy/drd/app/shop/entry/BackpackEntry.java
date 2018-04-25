package cz.stechy.drd.app.shop.entry;

import cz.stechy.drd.model.item.Backpack;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Položka v obchodě představující batoh
 */
public final class BackpackEntry extends ShopEntry {

    // region Variables

    private final IntegerProperty maxLoad = new SimpleIntegerProperty(this, "maxLoad");

    // endregion

    // region Constructors

    /**
     * Vytvoří batoh jako nákupní položku
     *
     * @param backpack {@link Backpack}
     */
    public BackpackEntry(Backpack backpack) {
        super(backpack);

        maxLoad.bind(backpack.maxLoadProperty());
    }

    // endregion

    // region Getters & Setters

    public int getMaxLoad() {
        return maxLoad.get();
    }

    public IntegerProperty maxLoadProperty() {
        return maxLoad;
    }

    public void setMaxLoad(int maxLoad) {
        this.maxLoad.set(maxLoad);
    }

    // endregion
}