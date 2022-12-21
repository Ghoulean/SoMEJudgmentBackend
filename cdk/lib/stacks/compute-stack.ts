import { NestedStack, NestedStackProps } from "aws-cdk-lib";
import {
    ApiDefinition,
    Method,
    MethodResponse,
    MockIntegration,
    PassthroughBehavior,
    RestApi,
    SpecRestApi,
} from "aws-cdk-lib/aws-apigateway";
import { Code, Function, Runtime } from "aws-cdk-lib/aws-lambda";
import { Construct } from "constructs";

const CASE_RESOURCE_PATH = "case";
const JUDGMENT_RESOURCE_PATH = "judgment";

export interface ComputeStackProps extends NestedStackProps {
    someTableArn: string;
}

export class ComputeStack extends NestedStack {
    /*public readonly authorizerLambda: Function;
    public readonly createCaseLambda: Function;
    public readonly submitCaseLambda: Function;
    public readonly getCaseLambda: Function;*/
    public readonly apiGateway: SpecRestApi;

    constructor(scope: Construct, id: string, props?: ComputeStackProps) {
        super(scope, id, props);
        //this.authorizerLambda = this.buildAuthorizerLambda();
        //this.createCaseLambda = this.buildCreateCaseLambda();
        //this.submitCaseLambda = this.buildSubmitCaseLambda();
        //this.getCaseLambda = this.buildGetCaseLambda();
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
        const getCaseMethod: Method = api.root
            .getResource(CASE_RESOURCE_PATH)!
            .addMethod("GET");
        const submitJudgmentMethod: Method = api.root
            .getResource(JUDGMENT_RESOURCE_PATH)!
            .addMethod("POST");
        const mockMethodResponse: MethodResponse = {
            statusCode: "200"
        }
        getCaseMethod.addMethodResponse(mockMethodResponse);
        submitJudgmentMethod.addMethodResponse(mockMethodResponse);

        return api;
    }
}
