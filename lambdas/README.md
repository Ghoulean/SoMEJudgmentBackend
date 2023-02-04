## SoME Judgement Backend Lambda

This package builds two lambdas: GetCaseLambda and SubmitJudgmentLambda

### GetCaseLambda

Lambda that backs `GET /case`

Pass in the following in the body of the request:

```
{
    judgeId: string,
    newActiveCaseOptions: {
        nonVideoSubmission: boolean
    }
}
```

If `judgeId` does not match the authentication token, 403 is returned. If a current active case for `judgeId` exists and conforms to `newActiveCaseOptions`, the current active case is returned. Otherwise, a new case is created that conforms to `newActiveCaseOptions`. 

Response body:
```
{
    submission1: Submission,
    submission2: Submission
}
```

### SubmitJudgmentLambda

Lambda that backs `POST /judgment`

Pass in the following in the body of the request:
```
{
    judgment: {
        judgeId: string,
        winnerId: string,
        loserId: string
    }
}
```

TODO: support adding feedback

If `judgeId` does not match the authentication token, 403 is returned. If the current active case for `judgeId` exists but does not conforms to `winnerId` and `loserId` (that is, there is a submission ID mismatch), 400 is returned. If the current active case was created less than 2 minutes ago, 400 is returned. Otherwise, a new judgment is recorded, and the current active case for `judgeId` is deleted.

Response body:
```
{
}
```

### Data Types

#### Submission

```
{
    id: string,
    submissionLink: string,
    submitters: string,
    emails: string
}
```


## TODO:

1. Add unit test cases
2. Javadocs
