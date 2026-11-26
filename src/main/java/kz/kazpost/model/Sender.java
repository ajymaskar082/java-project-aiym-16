package kz.kazpost.model;

public class Sender extends Person {

    public Sender(String fullName, String phone, String address) {
        super(fullName, phone, address);
    }

    @Override
    public String getRole() {
        return "Жіберуші";
    }
}
