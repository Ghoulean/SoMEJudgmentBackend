Note that there is a cyclic dependency with the authorizer lambda and the cdk. In order to bundle  the lambda authorizer properly, `.env` requires the APIGW URL. However, you can't get the APIGW URL without deploying the CDK. The current workaround is to put a dummy value in `.env` and make a CDK deployment. Then, update `.env` with the APIGW, rebuild, and rebundle.
