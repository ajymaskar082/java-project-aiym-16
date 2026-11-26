package kz.kazpost.model;

public abstract class Person {
    private final String fullName;
    private final String phone;
    private final String address;

    protected Person(String fullName, String phone, String address) {
        this.fullName = requireText(fullName, "Аты-жөні");
        this.phone = requireText(phone, "Телефон");
        this.address = requireText(address, "Мекенжай");
    }

    private String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " бос болмауы керек");
        }
        return value.trim();
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public abstract String getRole();
}
