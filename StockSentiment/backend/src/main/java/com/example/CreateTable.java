package com.example;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

public class CreateTable {

    public static void main(String[] args) {
        // Replace with your actual AWS Access Key ID and Secret Access Key
        String awsAccessKeyId = "";
        String awsSecretAccessKey = "";

        // Replace with your desired AWS region (e.g., US_WEST_1, US_EAST_1, etc.)
        Region region = Region.US_WEST_1;

        // Initialize DynamoDbClient with credentials
        DynamoDbClient ddb = DynamoDbClient.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(awsAccessKeyId, awsSecretAccessKey)
                ))
                .build();

        String tableName = "StockMarket1";

        // Define the table schema using "Symbol" as the primary key
        CreateTableRequest request = CreateTableRequest.builder()
                .attributeDefinitions(
                        AttributeDefinition.builder()
                                .attributeName("Symbol")
                                .attributeType(ScalarAttributeType.S) // S = String
                                .build())
                .keySchema(
                        KeySchemaElement.builder()
                                .attributeName("Symbol")
                                .keyType(KeyType.HASH) // HASH = Partition key
                                .build())
                .provisionedThroughput(
                        ProvisionedThroughput.builder()
                                .readCapacityUnits(5L)
                                .writeCapacityUnits(5L)
                                .build())
                .tableName(tableName)
                .build();

        // Create the table
        try {
            ddb.createTable(request);
            System.out.println("Table " + tableName + " is being created. Please wait...");

            // Wait until the table is active
            ddb.waiter().waitUntilTableExists(r -> r.tableName(tableName));
            System.out.println("Table " + tableName + " created successfully.");

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
        } finally {
            ddb.close();
        }
    }
}
