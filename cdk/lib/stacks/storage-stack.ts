import { NestedStack, NestedStackProps } from "aws-cdk-lib";
import { AttributeType, BillingMode, Table } from "aws-cdk-lib/aws-dynamodb";
import { Construct } from "constructs";

const PARTITION_KEY: string = "PK";
const SORT_KEY: string = "SK";

export class StorageStack extends NestedStack {
    public readonly someTable: Table;

    constructor(scope: Construct, id: string, props?: NestedStackProps) {
        super(scope, id, props);
        this.someTable = new Table(this, "SoMETable", {
            partitionKey: {
                name: PARTITION_KEY,
                type: AttributeType.STRING,
            },
            sortKey: {
                name: SORT_KEY,
                type: AttributeType.STRING
            },
            billingMode: BillingMode.PAY_PER_REQUEST,
        });
    }
}
