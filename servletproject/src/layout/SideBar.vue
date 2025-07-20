<template>
  <!-- The <el-menu> component in Element Plus is already designed to work with Vue Router when using the :default-active="$route.path" property. -->
  <el-menu
      :default-active="$route.path"
      class="el-menu-vertical-demo"
      :collapse="isCollapse"
      :router="true"
  >
    <el-menu-item index="/layout/dashboard">
      <el-icon class="icon">
        <Histogram/>
      </el-icon>
      <span>Dashboard</span>
    </el-menu-item>
    <el-sub-menu index="management">
      <template #title>
        <el-icon>
          <Management/>
        </el-icon>
        <span>Management</span>
      </template>
<!--      <el-menu-item index="user">-->
<!--        <el-icon>-->
<!--          <User/>-->
<!--        </el-icon>-->
<!--        User List-->
<!--      </el-menu-item>-->
      <el-menu-item index="employee">
        <el-icon>
          <UserFilled/>
        </el-icon>
        Employee List
      </el-menu-item>
      <el-menu-item index="customer">
        <el-icon>
          <User/>
        </el-icon>
        Customer List
      </el-menu-item>
    </el-sub-menu>
    <el-sub-menu index="products">
      <template #title>
        <el-icon>
          <Platform/>
        </el-icon>
        <span>Products</span>
      </template>
      <el-menu-item index="brands">
        <el-icon>
          <ShoppingBag/>
        </el-icon>
        Brands
      </el-menu-item>
      <el-menu-item index="category">
        <el-icon>
          <Folder/>
        </el-icon>
        Categories
      </el-menu-item>
      <el-menu-item index="import">
        <el-icon>
          <Download/>
        </el-icon>
        Import List
      </el-menu-item>
      <el-menu-item index="product">
        <el-icon>
          <Goods/>
        </el-icon>
        Product List
      </el-menu-item>
      <el-menu-item index="sales">
        <el-icon>
          <Coin/>
        </el-icon>
        Sale List
      </el-menu-item>
    </el-sub-menu>
    <el-sub-menu index="reports">
      <template #title>
        <el-icon>
          <Document/>
        </el-icon>
        <span>Document Report</span>
      </template>
      <el-menu-item index="saleReport">
        <el-icon>
          <TrendCharts/>
        </el-icon>
        Sale Report
      </el-menu-item>
      <el-menu-item index="productReport">
        <el-icon>
          <Notebook/>
        </el-icon>
        Product Report
      </el-menu-item>
      <el-menu-item index="importReport">
        <el-icon>
          <Files/>
        </el-icon>
        Import Report
      </el-menu-item>
    </el-sub-menu>
    <el-menu-item @click="toggleSidebar">
      <el-icon>
        <Expand v-if="isCollapse"/>
        <Fold v-else/>
      </el-icon>
    </el-menu-item>
  </el-menu>
</template>
<script setup>
import {ref} from "vue";
import {
  Fold,
  Expand,
  Histogram,
  Management,
  Platform,
  Document,
  Notebook,
  TrendCharts,
  Coin,
  Goods,
  ShoppingBag,
  User,
  UserFilled,
  Download,
  Folder,
  Files,
} from "@element-plus/icons-vue";

const isCollapse = ref(false);
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value;
};
</script>
<style scoped>
.el-menu-vertical-demo:not(.el-menu--collapse) {
  height: 100vh;
}

.el-menu-vertical-demo {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.el-menu-item span, .el-sub-menu span {
  margin-left: 1rem;
  font-size: 2rem;
}

.el-sub-menu .el-menu-item {
  font-size: 1.8rem;
}

.el-menu-item .el-icon,
.el-sub-menu .el-icon {
  font-size: 2.5rem;
  margin-right: 1rem;
}
</style>

