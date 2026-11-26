package kz.kazpost.model;

public enum DeliveryStatus {
    REGISTERED("Тіркелді"),
    ACCEPTED("Қабылданды"),
    IN_TRANSIT("Жолда"),
    ARRIVED_AT_OFFICE("Бөлімшеге келді"),
    DELIVERED("Жеткізілді"),
    LOST("Жоғалған");

    private final String displayName;

    DeliveryStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canTransitionTo(DeliveryStatus next) {
        if (next == null || this == next) {
            return false;
        }

        return switch (this) {
            case REGISTERED ->
                    next == ACCEPTED || next == LOST;
            case ACCEPTED ->
                    next == IN_TRANSIT || next == LOST;
            case IN_TRANSIT ->
                    next == ARRIVED_AT_OFFICE || next == LOST;
            case ARRIVED_AT_OFFICE ->
                    next == IN_TRANSIT || next == DELIVERED || next == LOST;
            case DELIVERED, LOST -> false;
        };
    }
}
