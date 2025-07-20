<template>
  <div class="common-layout">
    <el-container>
      <el-header>
        <Header/>
      </el-header>
      <el-container>
        <el-aside width="300px">
          <SideBar/>
        </el-aside>
        <el-main v-loading="isLoading" >
          <router-view v-if="!isLoading"></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>
<script setup>
import Header from "./Header.vue";
import SideBar from "./SideBar.vue";
import {ref} from "vue";
import {useRouter} from "vue-router";

const isLoading = ref(false);
const router = useRouter();

// Trigger loading before route change
router.beforeEach((to, from, next) => {
  isLoading.value = true;
  next(); // Continue to the next route
});

// Hide loading after the route has changed
router.afterEach(() => {
  setTimeout(() => {
    isLoading.value = false;
  }, 2000);
});
</script>
<style scoped>

</style>

