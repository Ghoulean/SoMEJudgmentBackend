import { getCaseTestHolder } from "./src/tests/newcase.js";
import { cleanup } from "./src/cleanup.js";
import { verifyInTestEnvironment } from "./src/verifier.js";
import { submitJudgmentTestHolder } from "./src/tests/submitjudgment.js";
import chalk from "chalk";

const testHolders = [getCaseTestHolder, submitJudgmentTestHolder];

verifyInTestEnvironment();

for (const testHolder of testHolders) {
    await testHolder.setup();
    for (const [index, test] of testHolder.tests.entries()) {
        const result = await test();
        const resultStr = result.passed
            ? chalk.green("PASSED")
            : chalk.red("FAILED; err: ") + result.error;
        console.log(`${testHolder.name} Test ${index}: ${resultStr}`);
    }
}

cleanup();
