<template>
  <el-row>
    <el-col :span="6">
      <el-statistic title="Total Sales Revenue" :value="totalSales" prefix="$"/>
    </el-col>
    <el-col :span="6">
      <el-statistic title="Total Quantity Sold" :value="totalQuantity"/>
    </el-col>
    <el-col :span="6">
      <el-statistic title="Total Transactions" :value="totalTransactions"/>
    </el-col>
    <el-col :span="6">
      <el-statistic title="Best Selling Category" :value="bestCategory"/>
    </el-col>
  </el-row>
</template>

<script setup>
import {ref, onMounted, getCurrentInstance} from 'vue';
import axios from 'axios';

const totalSales = ref(0);
const totalQuantity = ref(0);
const totalTransactions = ref(0);
const bestCategory = ref('N/A');

const {appContext} = getCurrentInstance();
const BASEURL = appContext.config.globalProperties.$BASEURL;

const fetchStatistics = async () => {
  try {
    const [saleResponse, productResponse] = await Promise.all([
      axios.get(`${BASEURL}/sale`),
      axios.get(`${BASEURL}/product`),
    ]);

    const salesData = saleResponse.data || [];
    const productData = productResponse.data || [];

    let categorySales = {}; //declare as empty object

    salesData.forEach((sale) => {
      totalSales.value += parseFloat(sale.totalPrice);
      totalQuantity.value += sale.quantity;
      totalTransactions.value++;

      const productInfo = productData.find((p) => p.model === sale.product.model);
      if (productInfo) {
        const categoryName = productInfo.category.name;
        // categorySales[categoryName] = (categorySales[categoryName] || 0) + sale.quantity;
        if (categorySales[categoryName] === undefined) {
          categorySales[categoryName] = 0;
        }
        categorySales[categoryName] += sale.quantity;

      }
    });
    let highestCategory = ['N/A', 0];
    //Object.entries() : use to convert object to array
    Object.entries(categorySales).forEach(([categoryName, quantity]) => {
      if (quantity > highestCategory[1]) {
        highestCategory = [categoryName, quantity];
      }
    });
    bestCategory.value = highestCategory[0];
  } catch (error) {
    console.error('Error fetching statistics:', error);
  }
};
onMounted(fetchStatistics);
</script>
<style scoped>
.el-col {
  text-align: center;
}
</style>
