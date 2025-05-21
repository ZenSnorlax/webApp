import request from "@/utils/request.ts";

export interface LoginPayload {
    email: string;
    password: string;
}

export interface RegisterPayload {
    email: string;
    password: string;
    nickname: string;
    verifyCode: string;
}

export async function loginUser(payload: LoginPayload) {
    try {
        const response = await request.post('/auth/login', payload);
        if (response.status !== 200) {
            console.error("登录失败:", response.statusText);
        }
        return response.data;
    } catch (error) {
        console.error("登录失败:", error);
        throw error;
    }
}

export async function generateRegisterCode(payload: RegisterPayload) {
    try {
        const response = await request.post('/auth/register/generate_code', payload);
        if (response.status !== 200) {
            console.error("生成注册码失败:", response.statusText);
        }
        return response.data;
    } catch (error) {
        console.error("生成注册码失败:", error);
        throw error;
    }
}

export async function confirmRegisterCode(payload: RegisterPayload) {
    try {
        const response = await request.post('/auth/register/confirm_code', payload);
        if (response.status !== 200) {
            console.error("确认注册码失败:", response.statusText);
        }
        return response.data;
    } catch (error) {
        console.error("确认注册码失败:", error);
        throw error;
    }
}

export async function checkCookieValid() {
    try {
        const response = await request.get('/auth/check_cookie');
        if (response.status !== 200) {
            console.error("检查Cookie是否有效失败:", response.statusText);
        }
        return response.data;
    } catch (error) {
        console.error("检查Cookie是否有效失败:", error);
        throw error;
    }
}