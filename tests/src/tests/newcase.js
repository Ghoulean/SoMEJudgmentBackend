import { cleanup } from "../cleanup.js";
import { PASSED_TEST, FAILED_TEST } from "../util.js";
import { getCase } from "../some_client.js";

const setup = async () => {
    return await cleanup();
};
const test1 = async () => {
    const response = await getCase(0, {
        submissionType: "video",
    });
    const statusCode = response.statusCode || response.status;
    if (statusCode !== 200) {
        return FAILED_TEST(`Status code = ${statusCode}`);
    }
    const payload = response.data;
    const submission1Id = payload["submission1"].id;
    const submission2Id = payload["submission2"].id;
    if (submission1Id !== "2" || submission2Id !== "6") {
        return FAILED_TEST(
            `Unexpected submission ids; expected 2 and 6 but got ${submission1Id} and ${submission2Id}`
        );
    }
    return PASSED_TEST();
};
const test2 = async () => {
    const response = await getCase(1, {
        submissionType: "video",
    });
    const statusCode = response.statusCode || response.status;
    if (statusCode !== 200) {
        return FAILED_TEST(`Status code = ${statusCode}`);
    }
    const payload = response.data;
    const submission1Id = payload["submission1"].id;
    const submission2Id = payload["submission2"].id;
    if (submission1Id !== "6" || submission2Id !== "9") {
        return FAILED_TEST(
            `Unexpected submission ids; expected 2 and 6 but got ${submission1Id} and ${submission2Id}`
        );
    }
    return PASSED_TEST();
};
const test3 = async () => {
    const response = await getCase(2, {
        submissionType: "nonvideo",
    });
    const statusCode = response.statusCode || response.status;
    if (statusCode !== 200) {
        return FAILED_TEST(`Status code = ${statusCode}`);
    }
    const payload = response.data;
    const submission1Id = payload["submission1"].id;
    const submission2Id = payload["submission2"].id;
    if (submission1Id !== "1" || submission2Id !== "3") {
        return FAILED_TEST(
            `Unexpected submission ids; expected 2 and 6 but got ${submission1Id} and ${submission2Id}`
        );
    }
    return PASSED_TEST();
};

const tests = [test1, test2, test1, test3];

export const getCaseTestHolder = {
    setup: setup,
    tests: tests,
    name: "GetCase",
};
