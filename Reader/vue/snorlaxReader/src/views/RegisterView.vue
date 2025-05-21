<script setup lang="ts">
import {useRouter} from "vue-router";
import {generateRegisterCode, confirmRegisterCode, RegisterPayload} from "@/services/authService";
import {reactive} from "vue";

const router = useRouter();


const generateCode = async (registerPayload: RegisterPayload) => {
  const response = await generateRegisterCode(registerPayload);
  if (response.code === 200) {
    alert("验证码已发送至邮箱，请查收");
  } else {
    alert("验证码发送失败，请稍后再试");
    console.log(response.message);
  }
}

const confirmCode = async (registerPayload: RegisterPayload) => {
  const response = await confirmRegisterCode(registerPayload);
  if (response.code === 200) {
    alert("注册成功");
    await router.push("/");
  } else {
    alert("验证码错误，请重新输入");
    console.log(response.message);
  }
}
const registerPayload = reactive<RegisterPayload>({
  email: "",
  password: "",
  nickname: "",
  verifyCode: ""
});
</script>
<template>
  <div class="register-container">
    <div class="register-card">
      <el-card class="custom-card">
        <div class="card-body">
          <h2 class="card-title">欢迎注册</h2>
          <el-form ref="form" :model="registerPayload" label-width="80px">
            <el-form-item label="邮箱">
              <el-input v-model="registerPayload.email" placeholder="请输入邮箱"></el-input>
            </el-form-item>
            <el-form-item label="昵称">
              <el-input  v-model="registerPayload.nickname" placeholder="请输入密码"></el-input>
            </el-form-item>
            <el-form-item label="密码">
              <el-input type="password" v-model="registerPayload.password" placeholder="请输入密码"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button  @click="generateCode(registerPayload)" class="get-code-btn">获取验证码</el-button>
            </el-form-item>
            <el-form-item label="验证码">
              <el-input type="password" v-model="registerPayload.verifyCode" placeholder="请输入验证码"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="confirmCode(registerPayload)" class="register-btn">注册</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.register-container {
  height: 100vh;
}

.register-card {
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
.register-btn {
  transition: background-color 0.3s ease, transform 0.3s ease;
}

.register-btn:hover {
  background-color: #409eff;
  transform: translateY(-2px);
}
</style>