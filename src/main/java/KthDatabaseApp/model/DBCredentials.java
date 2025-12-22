package KthDatabaseApp.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DBCredentials {
    public static final String CREDENTIAL_STORE_NAME = "DatabaseCredentials";
    public final String username;
    public final String password;

    public DBCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static void storeCredentials(DBCredentials credentials) throws IOException {
        File credentialStore = new File(CREDENTIAL_STORE_NAME);
        credentialStore.createNewFile();
        FileWriter writer = new FileWriter(credentialStore);
        writer.write(credentials.username + "\n");
        writer.write(credentials.password + "\n");
        writer.close();
    }

    public static DBCredentials readCredentials() throws IOException, InvalidCredentialsException {
        Path credentialPath = Path.of(CREDENTIAL_STORE_NAME);
        if (!Files.exists(credentialPath)) {
            throw new InvalidCredentialsException("Please enter valid credentials for the database");
        }
        List<String> lines = Files.readAllLines(credentialPath);
        String username = lines.get(0);
        String password = lines.get(1);
        return new DBCredentials(username, password);
    }
}
