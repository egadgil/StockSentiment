package com.example;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

public class UploadCSVToDynamoDB {

    public static void main(String[] args) {
        // Replace with your actual AWS Access Key ID and Secret Access Key
        String awsAccessKeyId = "AKIA3ISBVMOWZ56KRNNA";
        String awsSecretAccessKey = "AOgfL51fw5eNWPJUhG3buKgDr5wEXXyRbg2oNfYu";

        // Replace with your desired AWS region (e.g., US_WEST_1, US_EAST_1, etc.)
        Region region = Region.US_WEST_1;

        // Initialize DynamoDbClient with credentials
        DynamoDbClient client = DynamoDbClient.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(awsAccessKeyId, awsSecretAccessKey)
                ))
                .build();

        String tableName = "StockMarket1";  // Your DynamoDB table name

        // CSV file path
        String csvFilePath = "/Users/eshagadgil/selected_symbols.csv";  // Path to your CSV file

        // Read and upload data from CSV
        try (CSVParser parser = new CSVParser(new FileReader(Paths.get(csvFilePath).toFile()),
                CSVFormat.DEFAULT.withHeader())) {
            for (CSVRecord record : parser) {
                String symbol = record.get("Symbol");  // Stock ticker (A, etc.)
                String securityName = record.get("Security Name");  // Full company name

                // Example scraped news articles (dummy data for now)
                List<String> newsArticles = Arrays.asList(
                    "Title 1: Some good news about " + symbol,
                    "Title 2: More good news about " + symbol,
                    "Title 3: Another news update about " + symbol
                );

                // Create a new item to insert into the table
                Map<String, AttributeValue> item = new HashMap<>();
                item.put("Symbol", AttributeValue.builder().s(symbol).build());  // Primary key
                item.put("Security Name", AttributeValue.builder().s(securityName).build());  // Attribute

                // Add the news articles to the item
                item.put("News", AttributeValue.builder().l(
                        newsArticles.stream()
                            .map(article -> AttributeValue.builder().s(article).build())
                            .toArray(AttributeValue[]::new)
                ).build());

                // Prepare the PutItemRequest
                PutItemRequest request = PutItemRequest.builder()
                        .tableName(tableName)
                        .item(item)
                        .build();

                // Insert the item into the DynamoDB table
                try {
                    PutItemResponse response = client.putItem(request);
                    System.out.println("Inserted: " + symbol + " (" + securityName + ") with news articles.");
                } catch (DynamoDbException e) {
                    System.err.println("Unable to insert item: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close the DynamoDbClient
            client.close();
        }
    }
}
