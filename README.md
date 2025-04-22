# Distributed Database System

## Description 

This is a simple Java CRUD terminal application that demonstrates a distributed database system using CockroachDB. The application allows users to manage a movie database with basic operations (Create, Read, Update, Delete) through a terminal interface. CockroachDB provides the distributed database functionality with vertical fragmentation across multiple regions.

## Table of contents
- [Installation](#installation)
- [Usage](#usage)
- [Features](#features)
- [License](#license)
- [Contact](#contact)

## Installation

### Prerequisites
- JDK 21 or above
- PostgreSQL JDBC Driver (included in the repository)
- CockroachDB account (credentials are pre-configured)
- CockroachDB root certificate (instructions below)

### Steps

1. Clone the repository
```bash
git clone --recursive https://github.com/HugeErick/DistribuitedSystem.git
cd DistribuitedSystem
```

2. Download the CockroachDB root certificate

For Unix/Linux/macOS:
```bash
curl --create-dirs -o $HOME/.postgresql/root.crt 'https://cockroachlabs.cloud/clusters/690e5a03-c6aa-48d5-82b8-60ec652e68f7/cert'
```

For Windows:
```bash
# Create the directory if it doesn't exist
mkdir -p "$env:APPDATA\postgresql"

# Download the certificate
curl -o "$env:APPDATA\postgresql\root.crt" "https://cockroachlabs.cloud/clusters/690e5a03-c6aa-48d5-82b8-60ec652e68f7/cert"
```

Alternatively, for Windows users without curl:
1. Open your browser and navigate to: https://cockroachlabs.cloud/clusters/690e5a03-c6aa-48d5-82b8-60ec652e68f7/cert
2. Save the file as `root.crt`
3. Create a folder named `postgresql` in your `%APPDATA%` directory
4. Move the downloaded `root.crt` file to the `%APPDATA%\postgresql\` folder

3. Compile (If needed) 
```bash
javac -d bin -cp postgresql-42.7.5.jar src/*.java
```

4. Run it (for Unix)
```bash
java -cp bin:postgresql-42.7.5.jar Main
```

5. Run it (for Windows)
```bash
java -cp bin;postgresql-42.7.5.jar Main
```

## Usage

1. Compile (If needed) 
```bash
javac -d bin -cp postgresql-42.7.5.jar src/*.java
```

2. Run it (for Unix)
```bash
java -cp bin:postgresql-42.7.5.jar Main
```

2. Run it (for Windows)
```bash
java -cp bin;postgresql-42.7.5.jar Main
```

## Features

The application provides the following functionality:

1. **Add a new movie** - Create a new movie record with ID, title, director, year, and genre
2. **View all movies** - Display all movies in the database
3. **View movie by ID** - Find and display a specific movie by its ID
4. **Update a movie** - Modify the details of an existing movie
5. **Delete a movie** - Remove a movie from the database

Each section of the application is clearly separated with '#' characters for better readability in the terminal interface.

## Database Structure

The application connects to a CockroachDB instance, which is a distributed SQL database that provides:

- Horizontal scaling
- Fault tolerance
- Consistent replication
- Distributed transactions

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact

Erick Gonzalez Parada - erick.parada101@gmail.com

Project Link: [https://github.com/HugeErick/DistribuitedSystem](https://github.com/HugeErick/DistribuitedSystem)

