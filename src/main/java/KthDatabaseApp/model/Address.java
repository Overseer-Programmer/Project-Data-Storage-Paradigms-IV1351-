package KthDatabaseApp.model;

import java.util.Objects;

public class Address {
    public final String street;
    public final String zip;
    public final String city;
    
    public Address(String street, String zip, String city) {
        this.street = Objects.requireNonNull(street);
        this.zip = Objects.requireNonNull(zip);
        this.city = Objects.requireNonNull(city);
    }
}
