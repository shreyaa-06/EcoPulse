package com.gen.ecopulse.entity;

public enum ResourceType {
    ENERGY,
    WATER,
    WASTE;

    public String defaultUnit() {
        return switch (this) {
            case ENERGY -> "kWh";
            case WATER -> "L";
            case WASTE -> "kg";
        };
    }
}
