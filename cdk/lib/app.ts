import { App } from "aws-cdk-lib";
import { ComputeStack } from "./stacks/compute-stack";
import { StorageStack } from "./stacks/storage-stack";

require("dotenv").config();

const app = new App();

const AWS_ENV_CONFIG = {};

const storageStack: StorageStack = new StorageStack(app, "SoMEStorageStack", {
  env: AWS_ENV_CONFIG
});

const computeStack: ComputeStack = new ComputeStack(app, "SoMEComputeStack", {
  env: AWS_ENV_CONFIG,
  submissionTableArn: storageStack.submissionTable.tableArn,
  activeCaseTableArn: storageStack.activeCaseTable.tableArn,
  judgmentTableArn: storageStack.judgmentTable.tableArn
})

app.synth();
