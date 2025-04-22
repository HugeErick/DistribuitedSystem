import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class ConsoleUI {
    private DatabaseManager dbManager;
    private Scanner scanner;
    private boolean running;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public ConsoleUI(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.scanner = new Scanner(System.in);
        this.running = true;
    }
    
    public void start() {
        printWelcome();
        
        while (running) {
            printMenu();
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1:
                    addMovie();
                    break;
                case 2:
                    viewAllMovies();
                    break;
                case 3:
                    viewMovieById();
                    break;
                case 4:
                    updateMovie();
                    break;
                case 5:
                    deleteMovie();
                    break;
                case 6:
                    showPartitions();
                    break;
                case 7:
                    checkDatabaseConnection();
                    break;
                case 8:
                    running = false;
                    System.out.println("Exiting application. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        
        scanner.close();
        dbManager.close();
    }
    
    private void printWelcome() {
        System.out.println("################################################");
        System.out.println("#                                              #");
        System.out.println("#        MOVIE DATABASE MANAGEMENT SYSTEM      #");
        System.out.println("#                                              #");
        System.out.println("################################################");
        System.out.println();
        
        if (!dbManager.isConnected()) {
            System.out.println("WARNING: Running in offline mode. Database operations will not work.");
            System.out.println("Make sure the PostgreSQL JDBC driver is in your classpath.");
            System.out.println();
        }
    }
    
    private void printMenu() {
        System.out.println("\n################################################");
        System.out.println("#                  MAIN MENU                   #");
        System.out.println("################################################");
        System.out.println("1. Add a new movie");
        System.out.println("2. View all movies");
        System.out.println("3. View movie by ID");
        System.out.println("4. Update a movie");
        System.out.println("5. Delete a movie");
        System.out.println("6. Show database partitions");
        System.out.println("7. Check database connection");
        System.out.println("8. Exit");
        System.out.println("################################################");
    }
    
    private void checkDatabaseConnection() {
        System.out.println("\n################################################");
        System.out.println("#           DATABASE CONNECTION STATUS         #");
        System.out.println("################################################");
        
        if (dbManager.isConnected()) {
            System.out.println("Database connection is ACTIVE.");
        } else {
            System.out.println("Database connection is NOT AVAILABLE.");
            System.out.println("Make sure the PostgreSQL JDBC driver is in your classpath.");
            System.out.println("Run the application with: java -cp bin:postgresql-42.7.5.jar Main");
        }
        
        System.out.println("################################################");
    }
    
    private void showPartitions() {
        System.out.println("\n################################################");
        System.out.println("#             DATABASE PARTITIONS              #");
        System.out.println("################################################");
        
        if (!dbManager.isConnected()) {
            System.out.println("Cannot show partitions: Database connection not available.");
            System.out.println("################################################");
            return;
        }
        
        List<Map<String, String>> partitions = dbManager.showPartitions();
        
        if (partitions.isEmpty()) {
            System.out.println("No partition information available.");
        } else {
            // Print header
            Map<String, String> firstRow = partitions.get(0);
            for (String columnName : firstRow.keySet()) {
                System.out.print(String.format("%-20s", columnName));
            }
            System.out.println();
            
            // Print separator
            for (int i = 0; i < firstRow.size() * 20; i++) {
                System.out.print("-");
            }
            System.out.println();
            
            // Print data rows
            for (Map<String, String> row : partitions) {
                for (String value : row.values()) {
                    System.out.print(String.format("%-20s", value != null ? value : "NULL"));
                }
                System.out.println();
            }
        }
        
        System.out.println("\nThis shows the vertical fragmentation of the database across multiple regions.");
        System.out.println("Each partition represents a different physical location where the data is stored.");
        System.out.println("################################################");
    }
    
    private void addMovie() {
        System.out.println("\n################################################");
        System.out.println("#               ADD NEW MOVIE                  #");
        System.out.println("################################################");
        
        if (!dbManager.isConnected()) {
            System.out.println("Cannot add movie: Database connection not available.");
            System.out.println("################################################");
            return;
        }
        
        System.out.print("Enter movie title: ");
        String titulo = scanner.nextLine();
        
        Duration duracion = getDurationInput("Enter duration (format: 2h 30m or 150m): ");
        
        LocalDate fechaEstreno = getDateInput("Enter release date (format: YYYY-MM-DD): ");
        
        System.out.print("Enter rating (e.g., PG-13, R): ");
        String clasificacion = scanner.nextLine();
        
        String region = getRegionInput();
        
        // Create movie without ID (it will be auto-generated)
        Movie movie = new Movie(titulo, duracion, fechaEstreno, clasificacion, region);
        boolean success = dbManager.addMovie(movie);
        
        System.out.println("\n################################################");
        if (success) {
            System.out.println("Movie added successfully!");
            System.out.println("Added: " + movie);
        } else {
            System.out.println("Failed to add movie. Please try again.");
        }
        System.out.println("################################################");
    }
    
    private String getRegionInput() {
        List<String> availableRegions = dbManager.getAvailableRegions();
        
        System.out.println("Available regions:");
        for (int i = 0; i < availableRegions.size(); i++) {
            System.out.println((i + 1) + ". " + availableRegions.get(i));
        }
        
        int regionIndex = -1;
        while (regionIndex < 0 || regionIndex >= availableRegions.size()) {
            regionIndex = getIntInput("Select region (1-" + availableRegions.size() + "): ") - 1;
            if (regionIndex < 0 || regionIndex >= availableRegions.size()) {
                System.out.println("Invalid selection. Please try again.");
            }
        }
        
        return availableRegions.get(regionIndex);
    }
    
    private Duration getDurationInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                return null; // Allow null for optional field
            }
            
            try {
                // Try to parse formats like "2h 30m", "150m", "2.5h"
                if (input.contains("h") || input.contains("m")) {
                    long hours = 0;
                    long minutes = 0;
                    
                    // Extract hours
                    if (input.contains("h")) {
                        String hourPart = input.split("h")[0].trim();
                        hours = Long.parseLong(hourPart);
                        input = input.substring(input.indexOf("h") + 1).trim();
                    }
                    
                    // Extract minutes
                    if (input.contains("m")) {
                        String minPart = input.split("m")[0].trim();
                        minutes = Long.parseLong(minPart);
                    } else if (!input.isEmpty() && !input.contains("h")) {
                        // If there's no "m" but there's still input and no "h", assume it's minutes
                        minutes = Long.parseLong(input);
                    }
                    
                    return Duration.ofHours(hours).plusMinutes(minutes);
                } else {
                    // Try to parse as total minutes
                    long minutes = Long.parseLong(input);
                    return Duration.ofMinutes(minutes);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid duration format. Please use formats like '2h 30m', '150m', or just enter a number of minutes.");
            }
        }
    }
    
    private LocalDate getDateInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                return null; // Allow null for optional field
            }
            
            try {
                return LocalDate.parse(input, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD format.");
            }
        }
    }
    
    private void viewAllMovies() {
        System.out.println("\n################################################");
        System.out.println("#                ALL MOVIES                    #");
        System.out.println("################################################");
        
        if (!dbManager.isConnected()) {
            System.out.println("Cannot view movies: Database connection not available.");
            System.out.println("################################################");
            return;
        }
        
        List<Movie> movies = dbManager.getAllMovies();
        
        if (movies.isEmpty()) {
            System.out.println("No movies found in the database.");
        } else {
            for (Movie movie : movies) {
                System.out.println(movie);
                System.out.println("------------------------------------------------");
            }
        }
        
        System.out.println("################################################");
    }
    
    private void viewMovieById() {
        System.out.println("\n################################################");
        System.out.println("#              FIND MOVIE BY ID                #");
        System.out.println("################################################");
        
        if (!dbManager.isConnected()) {
            System.out.println("Cannot view movie: Database connection not available.");
            System.out.println("################################################");
            return;
        }
        
        System.out.print("Enter movie ID (UUID format): ");
        String idStr = scanner.nextLine().trim();
        
        try {
            UUID id = UUID.fromString(idStr);
            Movie movie = dbManager.getMovieById(id);
            
            System.out.println("\n################################################");
            if (movie != null) {
                System.out.println("Movie found:");
                System.out.println(movie);
            } else {
                System.out.println("No movie found with ID: " + idStr);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("\n################################################");
            System.out.println("Invalid UUID format. Please enter a valid UUID.");
        }
        
        System.out.println("################################################");
    }
    
    private void updateMovie() {
        System.out.println("\n################################################");
        System.out.println("#               UPDATE MOVIE                   #");
        System.out.println("################################################");
        
        if (!dbManager.isConnected()) {
            System.out.println("Cannot update movie: Database connection not available.");
            System.out.println("################################################");
            return;
        }
        
        System.out.print("Enter movie ID (UUID format): ");
        String idStr = scanner.nextLine().trim();
        
        try {
            UUID id = UUID.fromString(idStr);
            Movie movie = dbManager.getMovieById(id);
            
            if (movie != null) {
                System.out.println("Current movie details:");
                System.out.println(movie);
                System.out.println("------------------------------------------------");
                System.out.println("Enter new details (press Enter to keep current value):");
                
                System.out.print("Title [" + movie.getTitulo() + "]: ");
                String titulo = scanner.nextLine();
                if (!titulo.isEmpty()) {
                    movie.setTitulo(titulo);
                }
                
                System.out.print("Duration [" + movie.getFormattedDuration() + "] (format: 2h 30m or 150m): ");
                String durationStr = scanner.nextLine();
                if (!durationStr.isEmpty()) {
                    Duration duracion = getDurationInput("Enter new duration (format: 2h 30m or 150m): ");
                    movie.setDuracion(duracion);
                }
                
                System.out.print("Release date [" + movie.getFormattedFechaEstreno() + "] (format: YYYY-MM-DD): ");
                String dateStr = scanner.nextLine();
                if (!dateStr.isEmpty()) {
                    LocalDate fechaEstreno = getDateInput("Enter new release date (format: YYYY-MM-DD): ");
                    movie.setFechaEstreno(fechaEstreno);
                }
                
                System.out.print("Rating [" + (movie.getClasificacion() != null ? movie.getClasificacion() : "N/A") + "]: ");
                String clasificacion = scanner.nextLine();
                if (!clasificacion.isEmpty()) {
                    movie.setClasificacion(clasificacion);
                }
                
                System.out.println("Current region: " + movie.getRegion());
                System.out.print("Do you want to change the region? (y/n): ");
                String changeRegion = scanner.nextLine().trim().toLowerCase();
                if (changeRegion.equals("y") || changeRegion.equals("yes")) {
                    String region = getRegionInput();
                    movie.setRegion(region);
                }
                
                boolean success = dbManager.updateMovie(movie);
                
                System.out.println("\n################################################");
                if (success) {
                    System.out.println("Movie updated successfully!");
                    System.out.println("Updated: " + movie);
                } else {
                    System.out.println("Failed to update movie. Please try again.");
                }
            } else {
                System.out.println("\n################################################");
                System.out.println("No movie found with ID: " + idStr);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("\n################################################");
            System.out.println("Invalid UUID format. Please enter a valid UUID.");
        }
        
        System.out.println("################################################");
    }
    
    private void deleteMovie() {
        System.out.println("\n################################################");
        System.out.println("#               DELETE MOVIE                   #");
        System.out.println("################################################");
        
        if (!dbManager.isConnected()) {
            System.out.println("Cannot delete movie: Database connection not available.");
            System.out.println("################################################");
            return;
        }
        
        System.out.print("Enter movie ID (UUID format): ");
        String idStr = scanner.nextLine().trim();
        
        try {
            UUID id = UUID.fromString(idStr);
            Movie movie = dbManager.getMovieById(id);
            
            if (movie != null) {
                System.out.println("Movie to delete:");
                System.out.println(movie);
                System.out.println("------------------------------------------------");
                
                System.out.print("Are you sure you want to delete this movie? (y/n): ");
                String confirm = scanner.nextLine().trim().toLowerCase();
                
                if (confirm.equals("y") || confirm.equals("yes")) {
                    boolean success = dbManager.deleteMovie(id);
                    
                    System.out.println("\n################################################");
                    if (success) {
                        System.out.println("Movie deleted successfully!");
                    } else {
                        System.out.println("Failed to delete movie. Please try again.");
                    }
                } else {
                    System.out.println("\n################################################");
                    System.out.println("Delete operation cancelled.");
                }
            } else {
                System.out.println("\n################################################");
                System.out.println("No movie found with ID: " + idStr);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("\n################################################");
            System.out.println("Invalid UUID format. Please enter a valid UUID.");
        }
        
        System.out.println("################################################");
    }
    
    private int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }
}

