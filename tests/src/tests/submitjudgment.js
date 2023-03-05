import { cleanup } from "../cleanup.js";
import { PASSED_TEST, FAILED_TEST } from "../util.js";
import { getCase, submitJudgment } from "../some_client.js";

const setup = async () => {
    return await cleanup();
};

const test1 = async () => {
    const response = await submitJudgment(0, 2, 6);
    const statusCode = response.statusCode || response.status;
    if (statusCode === 200) {
        return FAILED_TEST(`Status code = ${statusCode}, expecting failure`);
    }
    return PASSED_TEST();
};

const sleep = async (ms) => {
    return new Promise((resolve) => setTimeout(resolve, ms));
};

const test2 = async () => {
    return new Promise(async (resolve, reject) => {
        const getCaseResponse = await getCase(0, {
            submissionType: "video",
        });
        // 2-min wait time is enforced
        await sleep(125 * 1000);
        
        const getCaseStatusCode =
            getCaseResponse.statusCode || getCaseResponse.status;
        if (getCaseStatusCode !== 200) {
            resolve(FAILED_TEST(`Get case status code = ${getCaseStatusCode}`));
        }
        const submitJudgmentResponse = await submitJudgment(
            0,
            getCaseResponse.data["submission1"].id,
            getCaseResponse.data["submission2"].id
        );
        const statusCode = submitJudgmentResponse.statusCode || submitJudgmentResponse.status;
        if (statusCode !== 200) {
            resolve(FAILED_TEST(`Status code = ${statusCode}`));
        }
        return resolve(PASSED_TEST());
    });
};

const tests = [test1, test2, test1];

export const submitJudgmentTestHolder = {
    setup: setup,
    tests: tests,
    name: "SubmitJudgment",
};
