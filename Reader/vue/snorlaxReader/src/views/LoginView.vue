<template>
  <div class="login-container">
    <div class="login-card">
      <el-card class="custom-card">
        <div class="card-body">
          <h2 class="card-title">欢迎登录</h2>
          <el-form ref="form" :model="loginPayload" label-width="80px">
            <el-form-item label="邮箱">
              <el-input v-model="loginPayload.email" placeholder="请输入邮箱"></el-input>
            </el-form-item>
            <el-form-item label="密码">
              <el-input type="password" v-model="loginPayload.password" placeholder="请输入密码"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="login" class="login-btn">登录</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>

.login-container {
  height: 100vh;
}

.login-card {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 90%;
}

.custom-card {
  max-width: 480px;
  width: 100%;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 20px;
}

.card-body {
  text-align: center;
}

.card-title {
  margin-bottom: 20px;
  font-size: 24px;
  color: #333;
  font-weight: bold;
}

/* 按钮悬停动画效果 */
.login-btn {
  transition: background-color 0.3s ease, transform 0.3s ease;
}

.login-btn:hover {
  background-color: #409eff;
  transform: translateY(-2px);
}
</style>

<script setup lang="ts">
import {useRouter} from "vue-router";
import {reactive} from "vue";
import {LoginPayload, loginUser} from "@/services/authService.ts";
import {useUserStore} from "@/stores/userStore.ts";
const loginPayload = reactive<LoginPayload>({email: "", password: ""});
const router = useRouter();

async function login() {
  const response = await loginUser(loginPayload);
  if (response.code === 200) {
    useUserStore().setLoginStatus(true);
    await router.push("/");

  } else {
    alert("登录失败");
    console.error(response.message);
  }
}
</script>
