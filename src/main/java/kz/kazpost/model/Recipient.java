package kz.kazpost.model;

public class Recipient extends Person {

    public Recipient(String fullName, String phone, String address) {
        super(fullName, phone, address);
    }

    @Override
    public String getRole() {
        return "Алушы";
    }
}
