import { DynamoDBClient, ScanCommand } from "@aws-sdk/client-dynamodb";
import { AWS_REGION, TABLE_NAME } from "./util.js";

const ddbClient = new DynamoDBClient({ region: AWS_REGION });

export const verifyInTestEnvironment = async () => {
    const command = new ScanCommand({
        TableName: TABLE_NAME,
        Select: "COUNT"
    });
    const results = await ddbClient.send(command);
    const itemCount = results.Count
    if (itemCount > 0) {
        throw new Error("DynamoDB table not empty -- are you sure this is the test environment?")
    }
};
