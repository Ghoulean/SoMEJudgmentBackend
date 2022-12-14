import * as path from "path";
import { NestedStack, NestedStackProps } from "aws-cdk-lib";
import {
    AuthorizationType,
    IdentitySource,
    Method,
    MethodOptions,
    MethodResponse,
    MockIntegration,
    PassthroughBehavior,
    RequestAuthorizer,
    RestApi,
    SpecRestApi,
    TokenAuthorizer,
} from "aws-cdk-lib/aws-apigateway";
import { Code, Function, Runtime } from "aws-cdk-lib/aws-lambda";
import { Construct } from "constructs";

const CASE_RESOURCE_PATH = "case";
const JUDGMENT_RESOURCE_PATH = "judgment";

const PROJECT_BASE_PATH = "../../../";

export interface ComputeStackProps extends NestedStackProps {
    someTableArn: string;
}

export class ComputeStack extends NestedStack {
    public readonly authorizerLambda: Function;
    /*public readonly submitJudgmentLambda: Function;
    public readonly getCaseLambda: Function;*/
    public readonly apiGateway: SpecRestApi;

    constructor(scope: Construct, id: string, props?: ComputeStackProps) {
        super(scope, id, props);
        this.authorizerLambda = this.buildAuthorizerLambda();
        //this.submitJudgmentLambda = this.buildSubmitJudgmentLambda();
        //this.getCaseLambda = this.buildGetCaseLambda();
        this.apiGateway = this.buildApiGateway();
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
            // TODO: do not hardcode
            environment: {
                JWKS_URI: "https://dev-hqnpivv6huikhzpy.us.auth0.com/.well-known/jwks.json",
                AUDIENCE:
                    "https://kj1sydw9hi.execute-api.us-west-2.amazonaws.com/prod",
                TOKEN_ISSUER: "https://dev-hqnpivv6huikhzpy.us.auth0.com/",
            },
        });
    }

    private buildSubmitJudgmentLambda(): Function {
        return new Function(this, "SubmitJudgmentLambda", {
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
        const auth = new RequestAuthorizer(this, "SoMERequestAuthorizer", {
            handler: this.authorizerLambda,
            identitySources: [IdentitySource.header("Authorization")],
        });

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
        api.root.addResource(CASE_RESOURCE_PATH);
        api.root.addResource(JUDGMENT_RESOURCE_PATH);

        const mockMethodResponse: MethodResponse = {
            statusCode: "200",
        };
        const methodOptions: MethodOptions = {
            authorizationType: AuthorizationType.CUSTOM,
            authorizer: auth,
            methodResponses: [mockMethodResponse],
        };
        const getCaseMethod: Method = api.root
            .getResource(CASE_RESOURCE_PATH)!
            .addMethod("GET", undefined, methodOptions);
        const submitJudgmentMethod: Method = api.root
            .getResource(JUDGMENT_RESOURCE_PATH)!
            .addMethod("POST", undefined, methodOptions);

        return api;
    }
}
