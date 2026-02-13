# CsvRandomizer

CsvRandomizer is a **Java utility** that randomizes specific columns in a CSV file while maintaining format constraints. It is useful for **testing, anonymizing data, or creating sample datasets**.

---

## Features

- Randomizes target CSV columns:
  - `ITEM_NUMBER`
  - `CHILD_ITEM_NUMBER`
  - `COMPONENT_MPN`
  - `MANUFACTURER_ID`
- Format-preserving randomization:
  - Letters remain letters  
  - Digits are randomized  
  - Special characters (`-`, `/`, etc.) remain unchanged  
  - Original string length is preserved
- Supports large CSV files
- Outputs a new randomized CSV file

---

## Prerequisites

- Java 17 or higher  
- Maven 3.8+  
- Optional: VS Code with **Java Extension Pack** for development

---

## Installation / Build

1. Clone the repository:

```bash
git clone <repository-url>
cd CsvRandomizer
```

2. Build the project with Maven:

```bash
mvn clean package
```

This generates a `.jar` file in the `target` folder, e.g.:

```
target/csv-randomizer-1.0-SNAPSHOT.jar
```

---

## Usage

Run the program from the command line:

```bash
java -jar target/csv-randomizer-1.0-SNAPSHOT.jar <input_csv> <output_csv>
```

**Example:**

```bash
java -jar target/csv-randomizer-1.0-SNAPSHOT.jar \
./csv/input.csv \
./csv/outputRandom.csv
```

- `<input_csv>` → Path to the original CSV file  
- `<output_csv>` → Path to save the randomized CSV file  

---

## Configuration

- **Target columns** are currently hard-coded in `CsvRandomizer.java`.  
- To add/remove columns, modify the `TARGET_COLUMNS` set:

```java
private static final Set<String> TARGET_COLUMNS = Set.of(
    "ITEM_NUMBER",
    "CHILD_ITEM_NUMBER",
    "COMPONENT_MPN",
    "MANUFACTURER_ID"
);
```

- Randomization logic preserves:
  - Letters → unchanged  
  - Digits → replaced with random digits  
  - Special characters → unchanged  
  - Total string length → unchanged  

---

## Logging

- Each randomized value is logged to the console for debugging purposes.  
- Optionally, you can extend logging to a file for tracking changes.

---

## Contributing

1. Fork the repository  
2. Create a new branch: `git checkout -b feature/my-feature`  
3. Make your changes  
4. Submit a pull request  

---

## License

This project is licensed under the MIT License.
