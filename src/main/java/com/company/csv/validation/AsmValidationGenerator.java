package com.company.csv.validation;

import com.opencsv.CSVReader;
import java.io.FileReader;
import java.util.*;

public class AsmValidationGenerator {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java -jar csv-validation.jar <input.csv>");
            System.exit(1);
        }

        String file = args[0]; // CSV input

        // Preserve insertion order
        List<String> itemNumbers = new ArrayList<>();
        Map<String, List<String>> childrenByItem = new LinkedHashMap<>();
        Map<String, List<String>> mpnByItem = new LinkedHashMap<>();

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] header = reader.readNext();
            if (header == null) {
                System.out.println("CSV is empty!");
                System.exit(1);
            }

            Map<String, Integer> index = new HashMap<>();
            for (int i = 0; i < header.length; i++) {
                index.put(header[i], i);
            }

            String[] row;
            while ((row = reader.readNext()) != null) {
                String item = get(row, index, "ITEM_NUMBER");
                String child = get(row, index, "CHILD_ITEM_NUMBER");
                String mpn = get(row, index, "COMPONENT_MPN");

                if (!item.isBlank() && !itemNumbers.contains(item)) {
                    itemNumbers.add(item);
                }

                if (!child.isBlank()) {
                    childrenByItem.computeIfAbsent(item, k -> new ArrayList<>()).add(child);
                }

                if (!mpn.isBlank()) {
                    mpnByItem.computeIfAbsent(item, k -> new ArrayList<>()).add(mpn);
                }
            }
        }

        printSubassemblies(itemNumbers);
        printChildAndMpnTests(itemNumbers, childrenByItem, mpnByItem);
        printModuleScoping(itemNumbers);
    }

    private static String get(String[] row, Map<String,Integer> index, String column) {
        Integer i = index.get(column);
        if (i == null || i >= row.length) return "";
        return row[i].trim();
    }

    // ---------- PRINT SECTIONS ----------

    private static void printSubassemblies(List<String> items) {
        System.out.println("1.- Verify that subassemblies are created successfully in ASM.");
        // First 4 unique items only
        items.stream().limit(4).forEach(i -> System.out.println("*" + i));
    }

    private static void printChildAndMpnTests(List<String> items,
                                              Map<String, List<String>> childrenByItem,
                                              Map<String, List<String>> mpnByItem) {

        int testNumber = 2;

        // Limit to first 4 items as per the test logic
        List<String> first4Items = items.stream().limit(4).toList();

        for (String item : first4Items) {
            // Child numbers test
            System.out.println("\n" + testNumber++ + ".- Verify that child numbers* are created successfully under item number " + item + " in ASM.");
            childrenByItem.getOrDefault(item, Collections.emptyList())
                          .forEach(c -> System.out.println("*" + c));

            // MPN test
            System.out.println("\n" + testNumber++ + ".- Verify that parts(MPN) are created under item number " + item + " in ASM.");
            mpnByItem.getOrDefault(item, Collections.emptyList())
                     .forEach(System.out::println);
        }
    }

    private static void printModuleScoping(List<String> items) {
        System.out.println("\n10.- Verify that module scoping matches item_class_matrix per item number.");
        // First 3 items only
        items.stream().limit(3)
             .forEach(i -> System.out.println("*" + i + " => 8 module scoping"));
    }
}