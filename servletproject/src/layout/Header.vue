<template>
  <el-menu
      :default-active="activeIndex"
      class="el-menu-demo"
      mode="horizontal"
      :ellipsis="false"
  >
    <el-menu-item index="0">
      <img style="width: 12rem" src="/src/images/element-plus-logo.svg" alt="Element logo"/>
    </el-menu-item>
    <div style="flex-grow: 1"></div>

    <el-sub-menu index="1" class="profile">
      <template #title>
        <div class="profile-title">
          <img class="avatar" :src="userInfo.avatar" alt="User Avatar"/>
          <span>{{ userInfo.name }}</span>
        </div>
      </template>

      <el-menu-item index="1-1" @click="isShowDialog = true">
        <el-icon>
          <User/>
        </el-icon>
        Profile
      </el-menu-item>

      <el-sub-menu index="1-2">
        <template #title>
          <el-icon>
            <Setting/>
          </el-icon>
          Theme
        </template>
        <el-menu-item index="1-2-1" @click="toggleTheme">
          <el-icon>
            <Sunny v-if="isDark"/>
            <Moon v-else/>
          </el-icon>
          {{ isDark ? "Light Mode" : "Dark Mode" }}
        </el-menu-item>
      </el-sub-menu>

      <el-menu-item
          index="1-3"
          class="logout"
          v-loading.fullscreen.lock="isLoading"
          @click="handleLogout"
      >
        <el-icon>
          <SwitchButton/>
        </el-icon>
        Logout
      </el-menu-item>
    </el-sub-menu>

    <el-menu-item class="notification-item" @click="isDrawerOpen = true">
      <div class="notification-wrapper">
        <el-badge
            :is-dot="notifications.some(notification => !notification.read)"
            class="item"
            type="danger"
        >
          <el-icon>
            <BellFilled style="font-size: 25px"/>
          </el-icon>
        </el-badge>
      </div>
    </el-menu-item>
  </el-menu>

  <el-dialog title="User Profile" v-model="isShowDialog" width="40%">
    <el-card shadow="hover" class="profile-card">
      <div class="profile-header">
        <img class="dialog-avatar" :src="userInfo.avatar" alt="User Avatar"/>
        <h1><strong>Username:</strong> {{ userInfo.name }}</h1>
      </div>
      <el-divider/>
      <div class="profile-detail">
        <p><strong>Email:</strong> {{ userInfo.email }}</p>
        <p><strong>Address:</strong> {{ userInfo.address }}</p>
      </div>
      <el-divider/>
    </el-card>
  </el-dialog>

  <el-drawer
      v-model="isDrawerOpen"
      title="Stock Notifications"
      direction="rtl"
      size="30%"
  >
    <div style="display: flex; justify-content: space-between; margin-bottom: 10px;">
      <el-button @click="markAllAsRead" type="text" size="small">Mark All Read</el-button>
      <el-button @click="deleteAllMessages" type="text" size="small" style="color: red;">Delete All</el-button>
    </div>

    <div v-if="notifications.length">
      <el-timeline>
        <el-timeline-item
            v-for="(item, index) in notifications"
            :key="index"
            :timestamp="item.time"
            placement="top"
        >
          <div>
            {{ item.message }}
          </div>
        </el-timeline-item>
      </el-timeline>
    </div>
    <div v-else>
      <p style="text-align: center; margin-top: 20px; font-size: 15px">No notifications</p>
    </div>
  </el-drawer>
</template>
<script setup>
import {ref, onMounted, getCurrentInstance} from "vue";
import axios from "axios";
import {useDark} from "@vueuse/core";
import {
  Sunny,
  Moon,
  User,
  Setting,
  SwitchButton,
  BellFilled
} from "@element-plus/icons-vue";
import {ElMessage} from "element-plus";
import {useRouter} from "vue-router";

const activeIndex = ref("0");
const isDark = useDark();
const router = useRouter();

const isLoading = ref(false);
const isShowDialog = ref(false);
const isDrawerOpen = ref(false);

const toggleTheme = () => {
  isDark.value = !isDark.value;
};

const userInfo = ref({
  avatar: "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png",
  name: "",
  role: "",
  email: "",
  address: "",
});
const {appContext} = getCurrentInstance();
const BASEURL = appContext.config.globalProperties.$BASEURL;
const notifications = ref([]);

onMounted(async () => {
  const savedNotifications = JSON.parse(localStorage.getItem("notifications"));
  if (savedNotifications && savedNotifications.length > 0) {
    notifications.value = savedNotifications;
  } else {
    try {
      const res = await axios.get(`${BASEURL}/product`);
      const lowStock = res.data.filter((p) => p.stock <= 3);

      notifications.value = lowStock.map((product) => {
        const message =
            product.stock === 0
                ? `Product ID: ${product.productId} is out of stock!`
                : `Product ID: ${product.productId} is low in stock (only ${product.stock} left!)`;

        return {
          message,
          time: new Date().toLocaleString(),
          read: false
        };
      });
      localStorage.setItem("notifications", JSON.stringify(notifications.value));
    } catch (error) {
      console.error("Failed to fetch products:", error);
    }
  }

  const getUser = localStorage.getItem("user");
  if (getUser) {
    const parsedUser = JSON.parse(getUser);
    userInfo.value = {
      avatar: userInfo.value.avatar,
      name: parsedUser.username || "Guest",
      email: parsedUser.email || "guest@gmail.com",
      address: parsedUser.address || "1234 Elm Street"
    };
  }
});
const markAllAsRead = () => {
  if (notifications.value.length === 0) {
    ElMessage({
      type: "warning",
      message: "No notifications to mark as read.",
    });
    return;
  }
  notifications.value = notifications.value.map(item => ({
    ...item,
    read: true
  }));
  localStorage.setItem("notifications", JSON.stringify(notifications.value));
  ElMessage({
    type: "success",
    message: "All notifications marked as read.",
  });
};
const deleteAllMessages = () => {
  if (notifications.value.length === 0) {
    ElMessage({
      type: "warning",
      message: "No notifications to delete.",
    });
    return;
  }
  notifications.value = [];
  localStorage.setItem("notifications", JSON.stringify([])); // Clear the stored notifications
  ElMessage({
    type: "success",
    message: "All notifications deleted.",
  });
};
const handleLogout = () => {
  isLoading.value = true;
  localStorage.removeItem("user");
  if (router.currentRoute.value.path !== "/login") {
    setTimeout(() => {
      isLoading.value = false;
      ElMessage({
        type: "success",
        title: "Logged out",
        message: "You have been logged out.",
        duration: 2000,
        offset: 20,
      });
      router.push("/");
    }, 2000);
  }
};
</script>
<style scoped>
.el-menu--horizontal {
  display: flex;
  align-items: center;
}

.profile-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.logout {
  color: red;
}

.profile-card {
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 600px;
  padding: 20px;
  margin: 0 auto;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 15px;
}

.profile-detail {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.dialog-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
}

.el-dialog .el-card {
  margin-top: 15px;
}

.notification-item {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 15px;
}

.notification-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.item {
  --el-badge-size: 14px;
  transform: translateY(1px);
}

.item :deep(.el-badge__content) {
  top: 20px;
  right: 16px;
}
</style>
