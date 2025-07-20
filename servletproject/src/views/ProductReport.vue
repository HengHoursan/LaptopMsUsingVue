<script setup>
import {ref, onMounted, computed, getCurrentInstance} from "vue";
import axios from 'axios';
import jsPDF from 'jspdf';
import moment from "moment";
import autoTable from 'jspdf-autotable';

const productData = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);

const getUser = localStorage.getItem("user");
const user = ref(getUser ? JSON.parse(getUser) : {
  username: "N/A",
  email: "N/A"
});
const {appContext} = getCurrentInstance();
const BASEURL = appContext.config.globalProperties.$BASEURL;

const FetchData = async () => {
  try {
    const productResponse = await axios.get(`${BASEURL}/product`);
    productData.value = productResponse.data?.length ? productResponse.data : [];
  } catch (err) {
    console.error("Error fetching data:", err);
  }
};

onMounted(async () => {
  await FetchData();
});

const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return productData.value.slice(start, start + pageSize.value);
});

const handlePageChange = (newPage) => {
  currentPage.value = newPage;
};

const printAsPDF = () => {
  const doc = new jsPDF();
  const formattedDate = moment().format('MMMM Do YYYY, h:mm:ss a');

  doc.setFont("helvetica");
  doc.setFontSize(18);
  doc.text("Product Report", 105, 16, {align: "center"});

  doc.setFontSize(12);
  doc.text(`Report Date: ${formattedDate}`, 14, 24);
  doc.text(`User: ${user.value.username}`, 14, 24);
  doc.text(`Email: ${user.value.email}`, 14, 32);

  const tableData = paginatedData.value.map(product => [
    product.productId,
    product.model,
    product.price,
    product.stock,
    product.brand?.name,
    product.category?.name,
  ]);
  autoTable(doc, {
    head: [['Product ID', 'Model', 'Price', 'Stock', 'Brand', 'Category']],
    body: tableData,
    startY: 40,
    styles: {fontSize: 10},
    headStyles: {fillColor: [200, 200, 200], textColor: [0, 0, 0], fontSize: 12},
  });

  doc.save("product_report.pdf");
};
</script>

<template>
  <el-table :data="paginatedData" width="100%">
    <el-table-column prop="productId" label="Product ID" width="180" sortable/>
    <el-table-column prop="brand.name" label="Brand" width="200"/>
    <el-table-column prop="category.name" label="Category" width="300"/>
    <el-table-column prop="model" label="Model" width="300"/>
    <el-table-column label="Price" width="180">
      <template #default="{ row }">
        {{ `${row.price.toFixed(2)} $` }}
      </template>
    </el-table-column>
    <el-table-column prop="stock" label="Stock" width="300"/>
  </el-table>

  <div class="example-pagination-block">
    <el-pagination
        layout="prev, pager, next"
        :total="productData.length"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
    />
  </div>

  <el-button class="mt-4" @click="printAsPDF">
    Print as PDF
  </el-button>
</template>

<style scoped>
.mt-4 {
  margin-top: 10px;
}
</style>
