import java.sql.Date;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Movie {
    private UUID id;
    private String titulo;
    private Duration duracion;
    private LocalDate fechaEstreno;
    private String clasificacion;
    private String region;
    private java.sql.Timestamp createdAt;
    
    // Constructor with all fields (for existing movies)
    public Movie(UUID id, String titulo, Duration duracion, LocalDate fechaEstreno, 
                String clasificacion, String region, java.sql.Timestamp createdAt) {
        this.id = id;
        this.titulo = titulo;
        this.duracion = duracion;
        this.fechaEstreno = fechaEstreno;
        this.clasificacion = clasificacion;
        this.region = region;
        this.createdAt = createdAt;
    }
    
    // Constructor without ID and createdAt (for new movies)
    public Movie(String titulo, Duration duracion, LocalDate fechaEstreno, 
                String clasificacion, String region) {
        this.titulo = titulo;
        this.duracion = duracion;
        this.fechaEstreno = fechaEstreno;
        this.clasificacion = clasificacion;
        this.region = region;
    }
    
    // Getters and setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public Duration getDuracion() {
        return duracion;
    }
    
    public void setDuracion(Duration duracion) {
        this.duracion = duracion;
    }
    
    public LocalDate getFechaEstreno() {
        return fechaEstreno;
    }
    
    public void setFechaEstreno(LocalDate fechaEstreno) {
        this.fechaEstreno = fechaEstreno;
    }
    
    public String getClasificacion() {
        return clasificacion;
    }
    
    public void setClasificacion(String clasificacion) {
        this.clasificacion = clasificacion;
    }
    
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    public java.sql.Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(java.sql.Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    // Format duration as hours and minutes
    public String getFormattedDuration() {
        if (duracion == null) return "N/A";
        
        long hours = duracion.toHours();
        long minutes = duracion.toMinutesPart();
        
        if (hours > 0) {
            return String.format("%d hours %d minutes", hours, minutes);
        } else {
            return String.format("%d minutes", minutes);
        }
    }
    
    // Format date as yyyy-MM-dd
    public String getFormattedFechaEstreno() {
        if (fechaEstreno == null) return "N/A";
        return fechaEstreno.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
    
    @Override
    public String toString() {
        return String.format("ID: %s\nTitle: %s\nDuration: %s\nRelease Date: %s\nRating: %s\nRegion: %s\nCreated: %s",
                id, titulo, getFormattedDuration(), getFormattedFechaEstreno(), 
                clasificacion != null ? clasificacion : "N/A", 
                region, createdAt != null ? createdAt.toString() : "N/A");
    }
}

