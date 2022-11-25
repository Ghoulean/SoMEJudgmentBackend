import { Stack, StackProps } from "aws-cdk-lib";
import { AttributeType, BillingMode, Table } from "aws-cdk-lib/aws-dynamodb";
import { Construct } from "constructs";

export class StorageStack extends Stack {
  public readonly submissionTable: Table;
  public readonly activeCaseTable: Table;
  public readonly judgmentTable: Table;

  constructor(scope: Construct, id: string, props?: StackProps) {
    super(scope, id, props);
    this.submissionTable = new Table(this, "SubmissionTable", {
      partitionKey: {
        name: "id",
        type: AttributeType.NUMBER,
      },
      billingMode: BillingMode.PAY_PER_REQUEST,
    });
    this.activeCaseTable = new Table(this, "ActiveCaseTable", {
      partitionKey: {
        name: "judge_id",
        type: AttributeType.STRING,
      },
      billingMode: BillingMode.PAY_PER_REQUEST,
    });
    this.judgmentTable = new Table(this, "JudgmentTable", {
      partitionKey: {
        name: "judge_id",
        type: AttributeType.STRING,
      },
      sortKey: {
        name: "uuid",
        type: AttributeType.STRING,
      },
      billingMode: BillingMode.PAY_PER_REQUEST,
    });
  }
}