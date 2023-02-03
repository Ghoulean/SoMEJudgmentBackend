import {
    DynamoDBClient,
    ScanCommand,
    BatchWriteItemCommand,
} from "@aws-sdk/client-dynamodb";
import { AWS_REGION, TABLE_NAME } from "./util.js";

const ddbClient = new DynamoDBClient({ region: AWS_REGION });

export const cleanup = async () => {
    const command = new ScanCommand({
        TableName: TABLE_NAME,
        ProjectionExpression: "PK,SK",
        Select: "SPECIFIC_ATTRIBUTES",
    });
    const results = await ddbClient.send(command);
    const items = results.Items;
    const parsedItems = items.map((item) => {
        return {
            DeleteRequest: {
                Key: {
                    PK: {
                        S: item.PK.S,
                    },
                    SK: {
                        S: item.SK.S,
                    }
                },
            },
        };
    });
    const partitionedItems = partitionArray(parsedItems, 25);
    const responses = [];
    for (const partition of partitionedItems) {
        const input = {
            RequestItems: {
                [TABLE_NAME]: partition,
            },
        };
        const command = new BatchWriteItemCommand(input);
        responses.push(await ddbClient.send(command));
    }
    return responses;
};

const partitionArray = (a, s) => {
    let output = [];
    for (let i = 0; i < a.length; i += s) {
        output.push(a.slice(i, i + s));
    }
    return output;
};
