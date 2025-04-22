import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class DatabaseManager {
    private Connection connection;
    private static final String TABLE_NAME = "pelicula";
    private boolean isConnected = false;
    
    public DatabaseManager() {
        try {
            // Establish a real connection to CockroachDB
            connection = getRealConnection();
            isConnected = (connection != null);
            
            if (isConnected) {
                System.out.println("Connected to CockroachDB successfully.");
                // Create table if it doesn't exist
                //createTableIfNotExists(); //Removed create table since the table is expected to exist
            } else {
                System.out.println("Failed to connect to database. Running in offline mode.");
            }
        } catch (Exception e) {
            System.out.println("Error initializing database: " + e.getMessage());
            System.out.println("Running in offline mode.");
        }
    }
    
    //Removed createTableIfNotExists()
    
    public boolean addMovie(Movie movie) {
        if (!isConnected) {
            System.out.println("Database connection not available. Cannot add movie.");
            return false;
        }
        
        // Note: id and created_at are auto-generated
        String sql = "INSERT INTO " + TABLE_NAME + 
                     " (titulo, duracion, fecha_estreno, clasificacion, region) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING id";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, movie.getTitulo());
            
            // Handle nullable duration
            if (movie.getDuracion() != null) {
                // Convert Duration to PostgreSQL interval string
                pstmt.setString(2, formatDurationForPostgres(movie.getDuracion()));
            } else {
                pstmt.setNull(2, Types.OTHER);
            }
            
            // Handle nullable release date
            if (movie.getFechaEstreno() != null) {
                pstmt.setDate(3, Date.valueOf(movie.getFechaEstreno()));
            } else {
                pstmt.setNull(3, Types.DATE);
            }
            
            // Handle nullable classification
            if (movie.getClasificacion() != null && !movie.getClasificacion().isEmpty()) {
                pstmt.setString(4, movie.getClasificacion());
            } else {
                pstmt.setNull(4, Types.VARCHAR);
            }
            
            pstmt.setString(5, movie.getRegion());
            
            // Get the auto-generated ID
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    movie.setId((UUID) rs.getObject(1)); // Set the auto-generated UUID
                    return true;
                }
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error adding movie: " + e.getMessage());
            return false;
        }
    }
    
    private String formatDurationForPostgres(Duration duration) {
        long hours = duration.toHours();
        int minutes = duration.toMinutesPart();
        int seconds = duration.toSecondsPart();
        
        return String.format("%d hours %d minutes %d seconds", hours, minutes, seconds);
    }
    
    public List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();
        
        if (!isConnected) {
            System.out.println("Database connection not available. Cannot retrieve movies.");
            return movies;
        }
        
        String sql = "SELECT * FROM " + TABLE_NAME;
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Movie movie = extractMovieFromResultSet(rs);
                movies.add(movie);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving movies: " + e.getMessage());
        }
        
        return movies;
    }
    
    public Movie getMovieById(UUID id) {
        if (!isConnected) {
            System.out.println("Database connection not available. Cannot retrieve movie.");
            return null;
        }
        
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setObject(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractMovieFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving movie: " + e.getMessage());
        }
        
        return null;
    }
    
    private Movie extractMovieFromResultSet(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        String titulo = rs.getString("titulo");
        
        // Handle interval type for duration
        Duration duracion = null;
        String durationStr = rs.getString("duracion");
        if (durationStr != null) {
            duracion = parseDurationFromPostgres(durationStr);
        }
        
        // Handle date
        LocalDate fechaEstreno = null;
        Date date = rs.getDate("fecha_estreno");
        if (date != null) {
            fechaEstreno = date.toLocalDate();
        }
        
        String clasificacion = rs.getString("clasificacion");
        String region = rs.getString("region");
        Timestamp createdAt = rs.getTimestamp("created_at");
        
        return new Movie(id, titulo, duracion, fechaEstreno, clasificacion, region, createdAt);
    }
    
    private Duration parseDurationFromPostgres(String pgInterval) {
        // This is a simplified parser for PostgreSQL interval
        // A more robust solution would handle all PostgreSQL interval formats
        try {
            // Example: "1 hour 30 mins" or "01:30:00"
            if (pgInterval.contains(":")) {
                // Parse time format HH:MM:SS
                String[] parts = pgInterval.split(":");
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                int seconds = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
                
                return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
            } else {
                // Try to extract hours, minutes, seconds from text format
                long hours = 0;
                long minutes = 0;
                long seconds = 0;
                
                if (pgInterval.contains("hour")) {
                    String hourPart = pgInterval.split("hour")[0].trim();
                    hours = Long.parseLong(hourPart.split(" ")[0]);
                }
                
                if (pgInterval.contains("min")) {
                    String minPart = pgInterval.split("min")[0].trim();
                    if (minPart.contains("hour")) {
                        minPart = minPart.split("hour")[1].trim();
                    }
                    minutes = Long.parseLong(minPart.split(" ")[0]);
                }
                
                if (pgInterval.contains("sec")) {
                    String secPart = pgInterval.split("sec")[0].trim();
                    if (secPart.contains("min")) {
                        secPart = secPart.split("min")[1].trim();
                    }
                    seconds = Long.parseLong(secPart.split(" ")[0]);
                }
                
                return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
            }
        } catch (Exception e) {
            System.out.println("Error parsing duration: " + pgInterval);
            return Duration.ZERO;
        }
    }
    
    public boolean updateMovie(Movie movie) {
        if (!isConnected) {
            System.out.println("Database connection not available. Cannot update movie.");
            return false;
        }
        
        String sql = "UPDATE " + TABLE_NAME + 
                     " SET titulo = ?, duracion = ?, fecha_estreno = ?, clasificacion = ?, region = ? " +
                     "WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, movie.getTitulo());
            
            // Handle nullable duration
            if (movie.getDuracion() != null) {
                pstmt.setString(2, formatDurationForPostgres(movie.getDuracion()));
            } else {
                pstmt.setNull(2, Types.OTHER);
            }
            
            // Handle nullable release date
            if (movie.getFechaEstreno() != null) {
                pstmt.setDate(3, Date.valueOf(movie.getFechaEstreno()));
            } else {
                pstmt.setNull(3, Types.DATE);
            }
            
            // Handle nullable classification
            if (movie.getClasificacion() != null && !movie.getClasificacion().isEmpty()) {
                pstmt.setString(4, movie.getClasificacion());
            } else {
                pstmt.setNull(4, Types.VARCHAR);
            }
            
            pstmt.setString(5, movie.getRegion());
            pstmt.setObject(6, movie.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error updating movie: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteMovie(UUID id) {
        if (!isConnected) {
            System.out.println("Database connection not available. Cannot delete movie.");
            return false;
        }
        
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setObject(1, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting movie: " + e.getMessage());
            return false;
        }
    }
    
    public List<Map<String, String>> showPartitions() {
        List<Map<String, String>> partitions = new ArrayList<>();
        
        if (!isConnected) {
            System.out.println("Database connection not available. Cannot show partitions.");
            return partitions;
        }
        
        String sql = "SHOW PARTITIONS FROM TABLE " + TABLE_NAME;
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            // Get column names
            String[] columnNames = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                columnNames[i-1] = metaData.getColumnName(i);
            }
            
            // Get data rows
            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String value = rs.getString(i);
                    row.put(columnName, value);
                }
                partitions.add(row);
            }
        } catch (SQLException e) {
            System.out.println("Error showing partitions: " + e.getMessage());
        }
        
        return partitions;
    }
    
    public List<String> getAvailableRegions() {
        List<String> regions = new ArrayList<>();
        
        if (!isConnected) {
            System.out.println("Database connection not available. Cannot retrieve regions.");
            // Add some default regions for offline mode
            regions.add("aws-ap-south-1");
            regions.add("aws-eu-central-1");
            regions.add("aws-us-east-1");
            return regions;
        }
        
        String sql = "SELECT DISTINCT partition_name FROM [SHOW PARTITIONS FROM TABLE " + TABLE_NAME + "]";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String region = rs.getString(1);
                if (region != null && !region.isEmpty()) {
                    regions.add(region);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving regions: " + e.getMessage());
            // Add some default regions if query fails
            regions.add("aws-ap-south-1");
            regions.add("aws-eu-central-1");
            regions.add("aws-us-east-1");
        }
        
        return regions;
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.out.println("Error closing database connection: " + e.getMessage());
        }
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public static Connection getRealConnection() throws SQLException {
        // Retrieve the JDBC URL from the environment variable
        String jdbcUrl = System.getenv("JDBC_DATABASE_URL");

        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            System.out.println("Error: JDBC_DATABASE_URL environment variable is not set.");
            return null;
        }

        // Load the PostgreSQL JDBC driver
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver not found. Include it in your library path.");
            e.printStackTrace();
            return null;
        }

        // Establish the connection
        try {
            return DriverManager.getConnection(jdbcUrl);
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database.");
            e.printStackTrace();
            throw e;
        }
    }
}

