import { Stack, StackProps } from "aws-cdk-lib";
import { RestApi } from "aws-cdk-lib/aws-apigateway";
import { Code, Function, Runtime } from "aws-cdk-lib/aws-lambda";
import { Construct } from "constructs";

export interface ComputeStackProps extends StackProps {
    submissionTableArn: string,
    activeCaseTableArn: string,
    judgmentTableArn: string
}

export class ComputeStack extends Stack {
  public readonly authorizerLambda: Function;
  public readonly createCaseLambda: Function;
  public readonly submitCaseLambda: Function;
  public readonly getCaseLambda: Function;
  public readonly apiGateway: RestApi;

  constructor(scope: Construct, id: string, props?: ComputeStackProps) {
    super(scope, id, props);
    this.authorizerLambda = this.buildAuthorizerLambda();
    this.createCaseLambda = this.buildCreateCaseLambda();
    this.submitCaseLambda = this.buildSubmitCaseLambda();
    this.getCaseLambda = this.buildGetCaseLambda();
    this.apiGateway = this.buildApiGateway();
  }

  private buildAuthorizerLambda(): Function {
    return new Function(this, "AuthorizerLambda", {
      runtime: Runtime.JAVA_11,
      handler: "hi",
      code: Code.fromAsset(""),
    });
  }
  private buildCreateCaseLambda(): Function {
    return new Function(this, "CreateCaseLambda", {
      runtime: Runtime.JAVA_11,
      handler: "hi",
      code: Code.fromAsset(""),
    });
  }
  private buildSubmitCaseLambda(): Function {
    return new Function(this, "SubmitCaseLambda", {
      runtime: Runtime.JAVA_11,
      handler: "hi",
      code: Code.fromAsset(""),
    });
  }
  private buildGetCaseLambda(): Function {
    return new Function(this, "GetCaseLambda", {
      runtime: Runtime.JAVA_11,
      handler: "hi",
      code: Code.fromAsset(""),
    });
  }

  private buildApiGateway(): RestApi {
    const api: RestApi = new RestApi(this, "SoMEJudgmentRestApi", {
      restApiName: "SoMEJudgment",
    });

    return api;
  }
}
