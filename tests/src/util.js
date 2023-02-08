import * as dotenv from "dotenv";
dotenv.config();
import axios from "axios";
import { STSClient, GetCallerIdentityCommand } from "@aws-sdk/client-sts";

export const AUTH0_TOKEN_URL = process.env.AUTH0_TOKEN_URL;
export const AUDIENCE = process.env.AUDIENCE;
export const SCOPE = process.env.SCOPE;
export const CLIENT_ID = process.env.CLIENT_ID;
export const CLIENT_SECRET = process.env.CLIENT_SECRET;
export const USERNAMES = process.env.USERNAMES.split(",");
export const PASSWORDS = process.env.PASSWORDS.split(",");
export const GET_CASE_ENDPOINT = process.env.GET_CASE_ENDPOINT;
export const SUBMIT_JUDGMENT_ENDPOINT = process.env.SUBMIT_JUDGMENT_ENDPOINT;
export const TABLE_NAME = process.env.TABLE_NAME;
export const AWS_REGION = process.env.AWS_REGION;

export const getCallerInfo = async () => {
    const stsClient = new STSClient({ region: AWS_REGION });
    const command = new GetCallerIdentityCommand({});
    const { Account } = await stsClient.send(command);
    return Account;
};

export const auth0Login = async (username, password) => {
    const auth0Options = {
        method: "POST",
        url: AUTH0_TOKEN_URL,
        headers: { "content-type": "application/x-www-form-urlencoded" },
        data: new URLSearchParams({
            grant_type: "password",
            username: username,
            password: password,
            audience: AUDIENCE,
            scope: SCOPE,
            client_id: CLIENT_ID,
            client_secret: CLIENT_SECRET,
        }),
    };
    const response = await axios.request(auth0Options);
    return response.data;
};

export const parseJwt = (base64Token) => {
    return JSON.parse(
        Buffer.from(base64Token.split(".")[1], "base64").toString()
    );
};

export const PASSED_TEST = () => {
    return { passed: true };
};

export const FAILED_TEST = (err) => {
    return { passed: false, error: err };
};
