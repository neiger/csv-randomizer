package com.company.csv.generator;

import com.opencsv.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class CsvGenerator {

    private static final Set<String> TARGET_COLUMNS = Set.of(
            "ITEM_NUMBER",
            "CHILD_ITEM_NUMBER", 
            "COMPONENT_MPN",
            "MANUFACTURER_ID"
    );

    private static final Map<String, String> VALUE_MAP = new HashMap<>();
    private static final Random RANDOM = new Random();

    public static void main(String[] args) throws Exception {
        
        if (args.length < 3) {
            System.out.println("Usage: java CsvSubsetGenerator <template.csv> <output.csv> <targetDistinctCount>");
            System.exit(1);
        }

        Path templatePath = Path.of(args[0]);
        Path outputPath = Path.of(args[1]);
        int targetDistinctCount = Integer.parseInt(args[2]);

        System.out.println("▶ Loading template: " + templatePath);

        List<String[]> templateRows;
        try (CSVReader reader = new CSVReader(new FileReader(templatePath.toFile()))) {
            templateRows = reader.readAll();
        }

        String[] header = templateRows.get(0);
        
        // Find ITEM_NUMBER column
        int itemNumberIndex = -1;
        Map<Integer, String> targetIndexes = new HashMap<>();
        
        for (int i = 0; i < header.length; i++) {
            if ("ITEM_NUMBER".equals(header[i])) {
                itemNumberIndex = i;
            }
            if (TARGET_COLUMNS.contains(header[i])) {
                targetIndexes.put(i, header[i]);
            }
        }

        // Group rows by ITEM_NUMBER (preserving original BOM structure)
        Map<String, List<String[]>> itemGroups = new LinkedHashMap<>();
        for (int i = 1; i < templateRows.size(); i++) {
            String itemNumber = templateRows.get(i)[itemNumberIndex];
            itemGroups.computeIfAbsent(itemNumber, k -> new ArrayList<>()).add(templateRows.get(i));
        }

        System.out.println("▶ Original distinct ITEM_NUMBERs: " + itemGroups.size());
        System.out.println("▶ Target distinct ITEM_NUMBERs: " + targetDistinctCount);

        // Select random subset of items
        List<String> allItemNumbers = new ArrayList<>(itemGroups.keySet());
        Collections.shuffle(allItemNumbers, RANDOM);
        List<String> selectedItems = allItemNumbers.subList(0, 
            Math.min(targetDistinctCount, allItemNumbers.size()));

        System.out.println("▶ Generating output with " + selectedItems.size() + " distinct items...");

        try (CSVWriter writer = new CSVWriter(Files.newBufferedWriter(outputPath))) {
            
            writer.writeNext(header, false);
            int totalRows = 0;

            for (String originalItemNumber : selectedItems) {
                List<String[]> itemRows = itemGroups.get(originalItemNumber);
                
                for (String[] row : itemRows) {
                    String[] newRow = row.clone();
                    
                    // Apply consistent randomization
                    for (Integer idx : targetIndexes.keySet()) {
                        newRow[idx] = randomizeConsistent(row[idx]);
                    }
                    
                    writer.writeNext(newRow, false);
                    totalRows++;
                }
            }

            System.out.println("✅ Generated " + totalRows + " total rows");
            System.out.println("✅ With " + selectedItems.size() + " distinct ITEM_NUMBERs");
        }
    }

    private static String randomizeConsistent(String value) {
        if (value == null || value.isEmpty()) return value;

        // Use VALUE_MAP for consistency (same input -> same output)
        if (VALUE_MAP.containsKey(value)) {
            return VALUE_MAP.get(value);
        }

        StringBuilder sb = new StringBuilder();
        for (char c : value.toCharArray()) {
            if (Character.isDigit(c)) {
                sb.append(RANDOM.nextInt(10));
            } else if (Character.isLetter(c)) {
                sb.append(Character.isUpperCase(c)
                        ? (char) ('A' + RANDOM.nextInt(26))
                        : (char) ('a' + RANDOM.nextInt(26)));
            } else {
                sb.append(c);
            }
        }

        String randomized = sb.toString();
        VALUE_MAP.put(value, randomized);
        return randomized;
    }
}