package org.mvplugins.multiverse.inventories.profile.key;

import java.util.Objects;

public final class ContainerKey {

    public static ContainerKey create(ContainerType containerType, String dataName) {
        return new ContainerKey(containerType, dataName);
    }

    private final ContainerType containerType;
    private final String dataName;

    private ContainerKey(ContainerType containerType, String dataName) {
        this.containerType = containerType;
        this.dataName = dataName;
    }

    public ContainerType getContainerType() {
        return containerType;
    }

    public String getDataName() {
        return dataName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ContainerKey that = (ContainerKey) o;
        return containerType == that.containerType && Objects.equals(dataName, that.dataName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(containerType, dataName);
    }

    @Override
    public String toString() {
        return "ContainerKey{" +
                "containerType=" + containerType +
                ", dataName='" + dataName + '\'' +
                '}';
    }
}
