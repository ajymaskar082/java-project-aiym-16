package kz.kazpost.exception;

public class ParcelNotFoundException extends RuntimeException {

    public ParcelNotFoundException(String trackingNumber) {
        super("Сәлемдеме табылмады. Трек-нөмір: " + trackingNumber);
    }
}
