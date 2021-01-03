package me.tcklpl.naturaldisaster.config;

import me.tcklpl.naturaldisaster.config.exceptions.IllegalOperationOnContainerTypeException;
import me.tcklpl.naturaldisaster.config.exceptions.ValueOutOfBoundsException;

import java.util.Objects;

public class ConfigFieldContainer<T> {

    private final String name;
    private T value;
    private int min, max;

    public ConfigFieldContainer(String name, T value) {
        this.name = name;
        this.value = value;
    }

    //region equals, hashcode and tostring
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigFieldContainer<?> that = (ConfigFieldContainer<?>) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "ConfigFieldContainer{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", min=" + min +
                ", max=" + max +
                '}';
    }

    public T getValue() {
        return value;
    }
    //endregion

    public void setValue(T value) {
        if (value instanceof Integer) {
            int newValue = (int) value;
            if (newValue < min || newValue > max)
                throw new ValueOutOfBoundsException("New value out of config field bounds.");
        }
        this.value = value;
    }

    private void throwIfNotInteger() {
        if (!(value instanceof Integer))
            throw new IllegalOperationOnContainerTypeException("Cannot do this operation to a non integer type.");
    }

    public int getMin() {
        throwIfNotInteger();
        return min;
    }

    public void setMin(int min) {
        throwIfNotInteger();
        this.min = min;
    }

    public int getMax() {
        throwIfNotInteger();
        return max;
    }

    public void setMax(int max) {
        throwIfNotInteger();
        this.max = max;
    }
}
