<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";
import axios from "axios";
const isLogin = ref(true);
const isShowDialog = ref(false);
const isLoading = ref(false);
const isShowPassword = ref(false)
const router = useRouter();
const Form = ref({
  username: "",
  email:"",
  password:"",
  confirmPassword:"",
  address: ""
})
const codeForm = ref({
  code: "",
})
const userData = ref([]);
const BASEURL = "http://localhost:8080/LaptopMS";
const adminVerifyCode = ref("123456")

// Check if user is already logged in and redirect to dashboard
onMounted(() => {
  const user = localStorage.getItem("user");
  if (user) {
    router.push("/layout/dashboard");
  } else {
    router.push("/")
  }
});
//Toggle between Login and SignUp forms
const toggleForm = () => {
  isLoading.value = true;
  setTimeout(() => {
    isLoading.value = false;
    isLogin.value = !isLogin.value;
    resetForm();
    isShowPassword.value = false;
  }, 1500);
};
// Function to reset form fields
const resetForm = () => {
  Form.value = {username: "", email: "", password: "", address: "", confirmPassword: ""};
};

// Function to fetch user data
const FetchUser = async () => {
  try {
    const response = await axios.get(`${BASEURL}/user`);
    const { data } = response;
    userData.value = data.empty ? [] : data;
    console.log("User data:", userData.value);
  } catch (err) {
    console.log("Error fetching user:", err);
  }
};
//This is login section
//Function to handel form Login
const LoginForm = async () => {
  if (!Form.value.email || !Form.value.password) {
    ElMessage.error("All fields are required");
    return;
  }
  try {
    isLoading.value = true;
    //note: use method .find() if you need to get the actual object if found(or undefined if not found).
    const userInfo = userData.value.find(
        (user) => user.email === Form.value.email && user.password === Form.value.password
    );
    if (userInfo) {
      await new Promise((resolve) => setTimeout(resolve, 2000));

      const userToStore = {
        userid: userInfo.userid,
        username: userInfo.username,
        email: userInfo.email,
        password: userInfo.password,
        address: userInfo.address,
      };
      localStorage.setItem("user", JSON.stringify(userToStore));
      ElMessage.success(`Login successful.Welcome, ${userInfo.username}!`);
      router.push("/layout/dashboard");
    } else {
      isLoading.value = true;
      await new Promise((resolve) => setTimeout(resolve, 2000));
      isLoading.value = false;
      ElMessage.error("Incorrect email or password.Please try again!");

    }
  } catch (err) {
    console.error("Error logging in:", err);
    ElMessage.error("Login failed. Please try again.");
  } finally {
    isLoading.value = false;
  }
};
//This is sign up section
// Function to handle sign-up form
const SignUpForm = async () => {
  if (!Form.value.username || !Form.value.email || !Form.value.password || !Form.value.address || !Form.value.confirmPassword) {
    ElMessage.error("All fields are required");
    return;
  }
  //note: use method .some() if you just need a true/false check.
  const emailExists = userData.value.some(user => user.email === Form.value.email);
  if (emailExists) {
    isLoading.value = true;
    await new Promise((resolve) => setTimeout(resolve, 2000));
    isLoading.value = false;
    ElMessage.error("Email is already exist.");
    return;
  }
  const usernameExists = userData.value.some(user => user.username === Form.value.username);
  if (usernameExists) {
    isLoading.value = true;
    await new Promise((resolve) => setTimeout(resolve, 2000));
    isLoading.value = false;
    ElMessage.error("Username is already taken");
    return;
  }
  try {
    isLoading.value = true;
    await axios.post(`${BASEURL}/user`, Form.value, {
      headers: { 'Content-Type': 'application/json' }
    });
    await new Promise((resolve) => setTimeout(resolve, 2000));
    await FetchUser();
    isShowDialog.value = true;
  } catch (err) {
    console.error('Error signing up user:', err);
  } finally {
    isLoading.value = false;
  }
}
// Function to handle admin code verification
const VerifyCode = async () => {
  try {
    if (codeForm.value.code === adminVerifyCode.value) {
      isShowDialog.value = false;
      isLoading.value = true;
      await new Promise((resolve) => setTimeout(resolve, 2000));
      isLogin.value = true;
      await FetchUser();
      ElMessage.success('Sign up successful. Please log in!');
    } else {
      ElMessage.error('Incorrect verification code.');
    }
  } catch (err) {
    console.error('Error verifying code:', err);
  } finally {
    isLoading.value = false;
  }
}
// Function to submit the form
const SubmitForm = async () => {
  if (isLogin.value) {
    await LoginForm();
  } else {
    await SignUpForm();
  }
}
onMounted(FetchUser)
</script>
<template>
  <div class="form-container">
    <el-form
        ref="ruleFormRef"
        :style="{ maxWidth: '600px', height: isLogin ? '600px' : '700px' }"
        :model="Form"
        status-icon
        label-width="auto"
        class="demo-ruleForm"
    >
      <!-- Logo Section -->
      <div class="logo-container">
        <img
            src="/src/images/element-plus-logo.svg"
            alt="App Logo"
            class="logo"
        />
      </div>
      <h1>{{ isLogin ? "Login" : "Sign Up" }}</h1>
      <!-- Username Field -->
      <el-form-item label="Username" prop="username" v-if="!isLogin"  :rules="[{ required: true, message: 'Please input the username', trigger: 'blur' }]">
        <el-input v-model="Form.username" type="text" style="width: 100%" />
      </el-form-item>
      <!-- Email Field -->
      <el-form-item label="Email" prop="email" :rules="[
          { required: true, message: 'Please input the email', trigger: 'blur' },
          {type: 'email', message: 'Please enter a valid email.', trigger: 'blur'}
      ]">
        <el-input v-model="Form.email" style="width: 100%"/>
      </el-form-item>
      <!-- Address Field -->
      <el-form-item v-if="!isLogin" label="Address" prop="address"  :rules="[{ required: true, message: 'Please input the address', trigger: 'blur' }]">
        <el-input
            v-model="Form.address"
            type="text"
            style="width: 100%"
        />
      </el-form-item>
      <!-- Password Field -->
      <el-form-item label="Password" prop="password"  :rules="[{ required: true, message: 'Please input the password', trigger: 'blur' }]">
        <el-input
            v-model="Form.password"
            :type="isShowPassword ? 'text' : 'password'"
            autocomplete="off"
            style="width: 100%"
        />
      </el-form-item>
      <!-- Confirm Password Field -->
      <el-form-item
          v-if="!isLogin"
          label="Confirm"
          prop="confirmPassword"
          :rules="[
    { required: true, message: 'Please confirm your password.', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== Form.password) {
          callback(new Error('Passwords do not match!'));
        } else {
          callback();
        }
      },
      trigger: 'blur',
    },
  ]"
      >
        <el-input
            v-model="Form.confirmPassword"
            :type="isShowPassword ? 'text' : 'password'"
            autocomplete="off"
            style="width: 100%"
        />
      </el-form-item>
      <!-- Show Password Checkbox -->
      <el-form-item>
        <el-checkbox v-model="isShowPassword">Show Password</el-checkbox>
      </el-form-item>
      <!-- Submit Button -->
      <el-form-item>
        <div class="btn-container">
          <el-button type="primary" class="btn-login" @click="SubmitForm">
            {{ isLogin ? "Log In" : "Sign Up" }}
          </el-button>
          <div class="register">
            <p>
              {{
                isLogin ? "Don't have an account?" : "Already have an account?"
              }}
            </p>
            <el-link
                type="primary"
                @click="toggleForm"
                v-loading.fullscreen.lock="isLoading"
            >{{ isLogin ? "Sign up now" : "Log in" }}</el-link
            >
          </div>
        </div>
      </el-form-item>
    </el-form>
    <!-- Admin Role Code Verification Dialog -->
    <el-dialog
        title="Admin Verification"
        v-model="isShowDialog"
        width="400px"
    >
      <el-form :model="codeForm">
        <el-form-item
            label="Enter 6-Digit Code"
            prop="code"
            :rules="[
    { required: true, message: 'Please enter the 6-digit code.', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: 'The code must be exactly 6 digits.', trigger: 'blur' }
  ]"
        >
          <el-input
              v-model="codeForm.code"
              placeholder="Enter code"
              maxlength="6"
              type="password"
              autocomplete="off"
              style="width: 100%"
              clearable
              show-password
          />
        </el-form-item>

      </el-form>

      <span class="dialog-footer">
        <el-button @click="isShowDialog = false">Cancel</el-button>
        <el-button type="primary" @click="VerifyCode">Verify</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<style scoped>
.form-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: rgb(255, 255, 255);
  background: linear-gradient(
      150deg,
      rgba(255, 255, 255, 1) 50%,
      rgba(64, 158, 255, 1) 50%
  );
}
.logo {
  width: 200px;
  height: auto;
  object-fit: cover;
  margin-bottom: 15px;
}
.demo-ruleForm {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 600px;
  height: 7000px;
  padding: 10px;
  outline: solid 1px #d9dce3;
  border-radius: 10px;
  box-shadow: 5px 5px 10px rgba(0, 0, 0, 0.1);
  background-color: #fff;
  gap: 10px;
}
.btn-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 40px;
  width: 100%;
}
.register {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}
.btn-login {
  width: 100px;
}
h1 {
  font-size: 3rem;
  color: #333;
  font-weight: bold;
  text-align: center;
  margin-bottom: 20px;
  letter-spacing: 2.5px;
}
.el-form-item {
  width: 70%;
}
</style>