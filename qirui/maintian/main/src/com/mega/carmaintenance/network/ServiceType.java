package com.mega.carmaintenance.network;

public enum ServiceType {
    MAINTAIN("1"),
    REPAIR("2");
    public String value;
    ServiceType(String v) {
        value = v;
    }
}
