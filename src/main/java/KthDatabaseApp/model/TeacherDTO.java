package KthDatabaseApp.model;

public class TeacherDTO {
    private final int id;
    private final String name;
    private final String lastName;

    public TeacherDTO(int id, String name, String lastName) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
    }


    public int getId() {

        return id;
    }

    public String getFirstName(){

        return name;
    }

    public String getLastName(){

        return lastName;
    }

    public String getFullName(){

        return name + " " + lastName;
    }
    
}
