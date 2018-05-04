package cz.stechy.drd.app.shop;

import cz.stechy.drd.app.shop.entry.ShopEntry;
import cz.stechy.drd.model.MaxActValue;
import cz.stechy.drd.model.Money;
import cz.stechy.drd.model.entity.hero.Hero;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

/**
 * Třída představující nákupní košík
 */
public final class ShoppingCart implements IShoppingCart {

    // region Variables

    private final Map<ShopEntry, AmmountListener> ammountListeners = new HashMap<>();
    private final BooleanProperty enoughtMoney = new SimpleBooleanProperty(this, "enoughtMoney",
        true);

    public final ObservableSet<ShopEntry> orderList = FXCollections.observableSet();
    public final Money totalPrice = new Money();

    private Hero hero;

    // endregion

    // region Constructors

    /**
     * Vytvoří novou instanci nákupnho košíku
     *
     * @param hero Hrdina, pro který je nákupní košík určen
     */
    public ShoppingCart(Hero hero) {
        this.hero = hero;
        enoughtMoney.set(hero != null);
        orderList.addListener(this::orderListHandler);
    }

    // endregion

    // region Private methods

    // region Method handlers

    private void orderListHandler(SetChangeListener.Change<? extends ShopEntry> change) {
        if (change.wasAdded()) {
            final ShopEntry entry = change.getElementAdded();
            final Money price = entry.getPrice();
            final MaxActValue ammount = entry.getAmmount();
            final AmmountListener listener = new AmmountListener(price);
            ammountListeners.put(entry, listener);
            ammount.actValueProperty().addListener(listener);
            final Money priceDuplicate = new Money(price);
            priceDuplicate.multiply(ammount.getActValue().intValue());
            totalPrice.add(priceDuplicate);
        }
        if (change.wasRemoved()) {
            final ShopEntry entry = change.getElementRemoved();
            final Money price = entry.getPrice();
            final MaxActValue ammount = entry.getAmmount();
            ammount.actValueProperty().removeListener(ammountListeners.get(entry));
            if (ammount.getActValue() == null) {
                return;
            }

            final Money priceDuplicate = new Money(price);
            priceDuplicate.multiply(ammount.getActValue().intValue());
            totalPrice.subtract(priceDuplicate);
        }

        if (hero != null) {
            enoughtMoney.set(hero.getMoney().getRaw() >= totalPrice.getRaw());
        }
    }

    // endregion

    // endregion

    @Override
    public void addItem(ShopEntry entry) {
        orderList.add(entry);
        entry.setInShoppingCart(true);
    }

    @Override
    public void removeItem(ShopEntry entry) {
        orderList.remove(entry);
        entry.setInShoppingCart(false);
    }

    @Override
    public boolean containsEntry(ShopEntry entry) {
        return orderList.contains(entry);
    }

    @Override
    public Optional<ShopEntry> getEntry(String id) {
        return orderList.stream()
            .filter(entry -> id.equals(entry.getId())).findFirst();
    }

    // region Getters & Setters

    public ReadOnlyBooleanProperty enoughtMoneyProperty() {
        return enoughtMoney;
    }

    // endregion

    private final class AmmountListener implements ChangeListener<Number> {

        private final Money price;

        private AmmountListener(Money price) {
            this.price = price;
        }

        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue,
            Number newValue) {
            if (oldValue != null) {
                final Money priceDuplicate = new Money(price);
                priceDuplicate.multiply(oldValue.intValue());
                totalPrice.subtract(priceDuplicate);
            }

            if (newValue != null) {
                final Money priceDuplicate = new Money(price);
                priceDuplicate.multiply(newValue.intValue());
                totalPrice.add(priceDuplicate);
            }
        }
    }
}
