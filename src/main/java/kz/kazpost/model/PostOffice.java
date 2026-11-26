package kz.kazpost.model;

public class PostOffice {
    private final int officeId;
    private final String name;
    private final String address;

    public PostOffice(int officeId, String name, String address) {
        if (officeId <= 0) {
            throw new IllegalArgumentException("Бөлімше ID мәні оң болуы керек");
        }
        this.officeId = officeId;
        this.name = requireText(name);
        this.address = requireText(address);
    }

    private String requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Мән бос болмауы керек");
        }
        return value.trim();
    }

    public int getOfficeId() {
        return officeId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
}
