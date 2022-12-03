
import { Stack, StackProps } from "aws-cdk-lib";
import { Construct } from "constructs";
import { ComputeStack } from "./compute-stack";
import { StorageStack } from "./storage-stack";

export class RootStack extends Stack {
    constructor(scope: Construct, id: string, props?: StackProps) {
        super(scope, id, props);
        const storageStack: StorageStack = new StorageStack(this, "SoMEStorageStack", {});
        const computeStack: ComputeStack = new ComputeStack(this, "SoMEComputeStack", {
            submissionTableArn: storageStack.submissionTable.tableArn,
            activeCaseTableArn: storageStack.activeCaseTable.tableArn,
            judgmentTableArn: storageStack.judgmentTable.tableArn,
        });
    }
}
