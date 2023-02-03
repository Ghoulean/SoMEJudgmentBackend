import {
    auth0Login,
    parseJwt,
    PASSWORDS,
    USERNAMES,
    GET_CASE_ENDPOINT,
    SUBMIT_JUDGMENT_ENDPOINT,
} from "./util.js";
import axios from "axios";

const cache = {};

export const getCase = async (accountIndex, newActiveCaseOptions) => {
    let auth0Creds;
    if (cache[accountIndex]) {
        auth0Creds = cache[accountIndex];
    } else {
        auth0Creds = await auth0Login(
            USERNAMES[accountIndex],
            PASSWORDS[accountIndex]
        );
    }
    const judgeId = parseJwt(auth0Creds.access_token).sub;
    const someGetCaseBody = {
        judgeId: judgeId,
        newActiveCaseOptions: newActiveCaseOptions,
    };
    const someGetCaseOptions = {
        method: "POST",
        url: GET_CASE_ENDPOINT,
        headers: {
            "content-type": "application/json",
            Authorization: `Bearer ${auth0Creds.access_token}`,
        },
        data: someGetCaseBody,
    };
    try {
        return await axios.request(someGetCaseOptions);
    } catch (err) {
        return err;
    }
};

export const submitJudgment = async (accountIndex, winnerId, loserId) => {
    let auth0Creds;
    if (cache[accountIndex]) {
        auth0Creds = cache[accountIndex];
    } else {
        auth0Creds = await auth0Login(
            USERNAMES[accountIndex],
            PASSWORDS[accountIndex]
        );
    }
    const judgeId = parseJwt(auth0Creds.access_token).sub;
    const someSubmitJudgmentBody = {
        judgment: {
            judgeId: judgeId,
            winnerId: winnerId,
            loserId: loserId,
        },
    };
    const someSubmitJudgmentOptions = {
        method: "POST",
        url: SUBMIT_JUDGMENT_ENDPOINT,
        headers: {
            "content-type": "application/json",
            Authorization: `Bearer ${auth0Creds.access_token}`,
        },
        data: someSubmitJudgmentBody,
    };
    try {
        return await axios.request(someSubmitJudgmentOptions);
    } catch (err) {
        return err;
    }
};
