package KthDatabaseApp.model;

public class Address {
    public final String street;
    public final String zip;
    public final String city;
    
    public Address(String street, String zip, String city) {
        this.street = street;
        this.zip = zip;
        this.city = city;
    }
}
