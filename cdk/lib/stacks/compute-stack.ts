import * as path from "path";
import { NestedStack, NestedStackProps } from "aws-cdk-lib";
import {
    AuthorizationType,
    IdentitySource,
    LambdaIntegration,
    Method,
    MethodOptions,
    MethodResponse,
    MockIntegration,
    PassthroughBehavior,
    RequestAuthorizer,
    RestApi,
} from "aws-cdk-lib/aws-apigateway";
import { Code, Function, Runtime } from "aws-cdk-lib/aws-lambda";
import { Construct } from "constructs";
import { Policy, PolicyDocument, Role } from "aws-cdk-lib/aws-iam";
import {
    getDynamoDbAccessPolicy,
    LAMBDA_MANAGED_POLICY,
    LAMBDA_SERVICE_PRINCIPAL,
} from "../helper/iam";

const CASE_RESOURCE_PATH = "case";
const JUDGMENT_RESOURCE_PATH = "judgment";

const PROJECT_BASE_PATH = "../../../";
const JAR_FILE_LOCATION = path.join(
    __dirname,
    PROJECT_BASE_PATH,
    "lambdas",
    "build",
    "distributions",
    "SoMEJudgmentBackend-0.1.0.zip"
);

export interface ComputeStackProps extends NestedStackProps {
    someTableArn: string;
    someTableName: string;
}

export class ComputeStack extends NestedStack {
    public readonly authorizerLambda: Function;
    public readonly submitJudgmentLambda: Function;
    public readonly getCaseLambda: Function;
    public readonly apiGateway: RestApi;

    constructor(scope: Construct, id: string, props: ComputeStackProps) {
        super(scope, id, props);
        this.apiGateway = this.instantiateApiGateway();
        this.authorizerLambda = this.buildAuthorizerLambda();
        this.submitJudgmentLambda = this.buildSubmitJudgmentLambda(
            props.someTableArn,
            props.someTableName
        );
        this.getCaseLambda = this.buildGetCaseLambda(
            props.someTableArn,
            props.someTableName
        );
        this.setupApiGatewayMethods();
    }

    private buildAuthorizerLambda(): Function {
        return new Function(this, "AuthorizerLambda", {
            runtime: Runtime.NODEJS_14_X,
            handler: "index.handler",
            code: Code.fromAsset(
                path.join(
                    __dirname,
                    PROJECT_BASE_PATH,
                    "jwt-rsa-aws-custom-authorizer",
                    "custom-authorizer.zip"
                )
            ),
            environment: {
                JWKS_URI: process.env.JWKS_URI!,
                AUDIENCE: process.env.AUDIENCE!,
                TOKEN_ISSUER: process.env.TOKEN_ISSUER!,
            },
        });
    }

    private buildSubmitJudgmentLambda(
        someTableArn: string,
        someTableName: string
    ): Function {
        return new Function(this, "SubmitJudgmentLambda", {
            runtime: Runtime.JAVA_11,
            handler: "com.ghoulean.somejudgment.lambda.SubmitJudgmentLambda",
            memorySize: 1024,
            code: Code.fromAsset(JAR_FILE_LOCATION),
            environment: {
                TABLE_NAME: someTableName,
                WAIT_TIME_SECONDS: "120"
            },
            role: new Role(this, "SubmitJudgmentLambdaRole", {
                assumedBy: LAMBDA_SERVICE_PRINCIPAL,
                managedPolicies: [LAMBDA_MANAGED_POLICY],
                inlinePolicies: {
                    default: new PolicyDocument({
                        statements: [getDynamoDbAccessPolicy(someTableArn)],
                    }),
                },
            }),
        });
    }
    private buildGetCaseLambda(
        someTableArn: string,
        someTableName: string
    ): Function {
        return new Function(this, "GetCaseLambda", {
            runtime: Runtime.JAVA_11,
            handler: "com.ghoulean.somejudgment.lambda.GetCaseLambda",
            memorySize: 1024,
            code: Code.fromAsset(JAR_FILE_LOCATION),
            environment: {
                TABLE_NAME: someTableName,
            },
            role: new Role(this, "GetCaseLambdaRole", {
                assumedBy: LAMBDA_SERVICE_PRINCIPAL,
                managedPolicies: [LAMBDA_MANAGED_POLICY],
                inlinePolicies: {
                    default: new PolicyDocument({
                        statements: [getDynamoDbAccessPolicy(someTableArn)],
                    }),
                },
            }),
        });
    }

    private instantiateApiGateway(): RestApi {
        const api: RestApi = new RestApi(this, "SoMEJudgmentRestApi", {
            defaultIntegration: new MockIntegration({
                integrationResponses: [
                    {
                        statusCode: "200",
                    },
                ],
                passthroughBehavior: PassthroughBehavior.NEVER,
                requestTemplates: {
                    "application/json": '{ "statusCode": 200 }',
                },
            }),
        });

        return api;
    }

    private setupApiGatewayMethods() {
        this.apiGateway.root.addResource(CASE_RESOURCE_PATH);
        this.apiGateway.root.addResource(JUDGMENT_RESOURCE_PATH);

        const auth = new RequestAuthorizer(this, "SoMERequestAuthorizer", {
            handler: this.authorizerLambda,
            identitySources: [IdentitySource.header("Authorization")],
        });

        const mockMethodResponse: MethodResponse = {
            statusCode: "200",
        };
        const methodOptions: MethodOptions = {
            authorizationType: AuthorizationType.CUSTOM,
            authorizer: auth,
            methodResponses: [mockMethodResponse],
        };
        const getCaseMethod: Method = this.apiGateway.root
            .getResource(CASE_RESOURCE_PATH)!
            .addMethod(
                "POST",
                new LambdaIntegration(this.getCaseLambda, {
                    allowTestInvoke: true,
                }),
                {
                    authorizationType: AuthorizationType.CUSTOM,
                    authorizer: auth,
                    methodResponses: [
                        { statusCode: "200" },
                        { statusCode: "403" },
                    ],
                }
            );
        const submitJudgmentMethod: Method = this.apiGateway.root
            .getResource(JUDGMENT_RESOURCE_PATH)!
            .addMethod(
                "POST",
                new LambdaIntegration(this.submitJudgmentLambda, {
                    allowTestInvoke: true,
                }),
                {
                    authorizationType: AuthorizationType.CUSTOM,
                    authorizer: auth,
                    methodResponses: [
                        { statusCode: "200" },
                        { statusCode: "403" },
                    ],
                }
            );
    }
}
