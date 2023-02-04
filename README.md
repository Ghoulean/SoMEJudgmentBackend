# SoMEJudgmentBackend

Implementation of a backend system for SoME (Summer of Math Exposition) during the judgment phase of the event utilizing AWS serverless technologies to run on a budget.

## Features

- Pair submissions according to [NodeRank](https://github.com/fcrozatier/NodeRank)
- Users must wait a certain amount of time between votes (currently disabled)

#### TODO:
 - Make wait time configurable via environmental variables
 - Add feedback to judgments
 - Enforce voting start and end times, configured via environmental variables
 - Read submissions CSV via S3 rather than baked within lambdas
 - More configurations for pairing strategy, and configurable via environmental variables
 - Integrate with OpenAPI

User management, submission, and final ranking algorithm are not present; those were intentionally left out-of-scope of this project. SoMEJudgmentBackend uses JWT tokens to authorize and authenticate users making judgments.

## Setup

Install in your development environment:
 - [Node 18](https://nodejs.org)
 - [Gradle 7](https://gradle.org)
 - [AWS CLI v2](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-welcome.html)
 - [AWS CDK v2](https://docs.aws.amazon.com/cdk/v2/guide/cli.html)

Sign up for an AWS account if not done so already. Although a brand new account is sufficient for setting up this service, following [security best practices](https://aws.amazon.com/premiumsupport/knowledge-center/security-best-practices/) is highly recommended.

[Configure a named profile](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-profiles.html) for the AWS CLI (optional, recommended).

Set up a user management system that supports authorization and authentication with JSON web tokens. In my development I use [Auth0](https://auth0.com/); open source alternatives such as [KeyCloak](https://www.keycloak.org/) should also be usable.

## Configuration
This uses [JSON web tokens](https://auth0.com/docs/secure/tokens/json-web-tokens) (JWT) and [JSON web key sets](https://auth0.com/docs/secure/tokens/json-web-tokens/json-web-key-sets) (JWKS) to authorize and authenticate requests.

In the `cdk` directory, the `JWKS_URI` and the `TOKEN_ISSUER` fields must be filled out in the `.env` file.

The `.env` file also requires the `AUDIENCE` field, which should be set to the endpoint URI of the AWS API Gateway (APIGW).

### ⚠️ Note ⚠️
The `AUDIENCE` field is passed into the authorizer lambda. However, the URI is unattainable without first deploying the APIGW, and the APIGW needs to be deployed after deploying the authorizer lambda.

The workaround for this this cyclic dependency is to put a dummy value in `.env` for the `AUDIENCE` field and perform a deployment. Then, update `.env` with the APIGW endpoint and redeploy. You should only need to do this once as the APIGW endpoint shouldn't change between deployments.

## Deployment

1. Configure `.env` in the `cdk` subdirectory if not done so.
2. Enter the `lambdas` subdirectory and run:
```
$ gradle build
```
3. Enter the `jwt-rsa-aws-custom-authorizer` subdirectory and run:
```
$ npm run bundle
```
4. Enter the `cdk` subdirectory and run:
```
$ npm run build
$ npm run cdk deploy -- --profile <PROFILE_NAME>
```

## Testing

After deploying the service, enter the `tests` package, configure `tests/.env`, and run:
```
$ npm run tests
```
Note that the test package assumes [Auth0](https://auth0.com/) is used as the user management solution, and also assumes that it is already set up with a few test users.

### ⚠️ Warning ⚠️

These tests should **NOT** be run against a production environment as it clears the submission database before and after each test run.