import { 
  Effect, 
  IManagedPolicy, 
  ManagedPolicy, 
  PolicyStatement, 
  ServicePrincipal 
} from "aws-cdk-lib/aws-iam";

export const LAMBDA_SERVICE_PRINCIPAL: ServicePrincipal = new ServicePrincipal("lambda.amazonaws.com")

export const LAMBDA_MANAGED_POLICY: IManagedPolicy = ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaBasicExecutionRole");

export function getDynamoDbAccessPolicy(tableArn: string): PolicyStatement {
  return new PolicyStatement({
    actions: [
      "dynamodb:BatchGetItem",
      "dynamodb:BatchWriteItem",
      "dynamodb:DeleteItem",
      "dynamodb:GetItem",
      "dynamodb:PutItem",
      "dynamodb:Query",
      "dynamodb:Scan",
      "dynamodb:UpdateItem",
    ],
    effect: Effect.ALLOW,
    resources: [tableArn],
  });
}
