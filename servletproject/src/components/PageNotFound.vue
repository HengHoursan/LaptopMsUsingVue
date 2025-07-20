<template>
  <div class="w-screen h-screen flex items-center justify-center">
    <el-result
        icon="error"
        title="Resource not found"
        sub-title="Sorry, the page you requested does not exist.">
      <template #extra>
        <el-button @click="goHome">Back to Home</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();
const goHome = () => {
  router.push('/');
};
</script>
<style scoped>
div{
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
}
</style>
