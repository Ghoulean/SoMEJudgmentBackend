import { App } from "aws-cdk-lib";
import { RootStack } from "./stacks/root-stack";

require("dotenv").config();

const app = new App();

const AWS_ENV_CONFIG = {};

const rootStack: RootStack = new RootStack(app, "SoMEBackendRootStack", {});

app.synth();
