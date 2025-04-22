import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        ConsoleUI ui = new ConsoleUI(dbManager);
        ui.start();
    }
}

