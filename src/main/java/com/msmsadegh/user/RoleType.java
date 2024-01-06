package com.msmsadegh.user;

public enum RoleType {
    ORGANIZATION_MANAGER("مدیر سازمان", "organization manager");

    private final String faName;
    private final String enName;

    RoleType(String faName, String enName) {
        this.faName = faName;
        this.enName = enName;
    }
}