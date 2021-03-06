package cz.stechy.drd.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

/**
 * Třída obsahující tři hodnoty
 * Maximální, minimální a aktuální hodnotu
 * Aktuální hodnota nemůže překročit hranice minimální a maximální hodnoty
 */
public final class MaxActValue {

    // region Variables

    private final ObjectProperty<Number> minValue = new SimpleObjectProperty<>(0);
    private final ObjectProperty<Number> maxValue = new SimpleObjectProperty<>(0);
    private final ObjectProperty<Number> actValue = new SimpleObjectProperty<>(0);

    // endregion

    // region Constructors

    public MaxActValue() {
        this(0, 100, 0);
    }

    public MaxActValue(Number maxValue) {
        this(0, maxValue, 0);
    }

    /**
     * Vytvoří novou instanci třídy představující intervalovou hodnotu
     *
     * @param minValue Minimální hodnota
     * @param maxValue Maximální hodnota
     * @param actValue Aktuální hodnota
     */
    public MaxActValue(Number minValue, Number maxValue, Number actValue) {
        this.actValue.addListener(valueListener);
        setMinValue(minValue);
        setMaxValue(maxValue);
        setActValue(actValue);
    }

    // endregion

    // region Public methods

    public void add(int ammount) {
        int act = actValue.get().intValue();
        actValue.set(act + ammount);
    }

    public void subtract(int ammount) {
        int act = actValue.get().intValue();
        actValue.set(act - ammount);
    }

    // endregion

    // region Getters & Setters

    public Number getMinValue() {
        return minValue.get();
    }

    public ObjectProperty<Number> minValueProperty() {
        return minValue;
    }

    public void setMinValue(Number minValue) {
        this.minValue.set(minValue);
    }

    public Number getMaxValue() {
        return maxValue.get();
    }

    public ObjectProperty<Number> maxValueProperty() {
        return maxValue;
    }

    public void setMaxValue(Number maxValue) {
        this.maxValue.set(maxValue);
    }

    public Number getActValue() {
        return actValue.get();
    }

    public ObjectProperty<Number> actValueProperty() {
        return actValue;
    }

    public void setActValue(Number actValue) {
        this.actValue.set(actValue);
    }

    public ChangeListener<Number> getValueListener() {
        return valueListener;
    }

    // endregion

    private final ChangeListener<Number> valueListener = (observable, oldValue, newValue) -> {
        int value = newValue.intValue();
        if (value > maxValue.get().doubleValue()) {
            actValue.setValue(maxValue.get());
        } else if (value < minValue.get().doubleValue()) {
            actValue.setValue(minValue.get());
        }
    };
}
