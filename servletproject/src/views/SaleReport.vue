<script setup>
import { ref, computed, onMounted, getCurrentInstance } from "vue";
import axios from 'axios';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import moment from "moment";

const saleData = ref([]);
const productData = ref([]);
const customerData = ref([]);
const employeeData = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);

const getUser = localStorage.getItem("user");
const user = ref(getUser ? JSON.parse(getUser) : {
  username: "N/A",
  email: "N/A"
});
const { appContext } = getCurrentInstance();
const BASEURL = appContext.config.globalProperties.$BASEURL;
const FetchData = async () => {
  const endpoints = [
    { key: saleData, url: "/sale" },
    { key: productData, url: "/product" },
    { key: customerData, url: "/customer" },
    { key: employeeData, url: "/employee" },
  ];
  try {
    await Promise.all(
        endpoints.map(async (endpoint) => {
          const response = await axios.get(`${BASEURL}${endpoint.url}`);
          endpoint.key.value = response.data ? response.data : [];
        })
    );
  } catch (err) {
    console.error("Error fetching data:", err);
  }
};
onMounted(async () => {
  await FetchData();
});
const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return saleData.value.slice(start, start + pageSize.value);
});

const handlePageChange = (newPage) => {
  currentPage.value = newPage;
};

const printAsPDF = () => {
  const doc = new jsPDF();
  const formattedDate = moment().format('MMMM Do YYYY, h:mm:ss a');

  doc.setFont("helvetica");
  doc.setFontSize(18);
  doc.text("Sales Report", 105, 16, {align: "center"});


  // Add date and time below the title
  doc.setFontSize(12);
  doc.text(`Report Date: ${formattedDate}`, 14, 24);
  doc.text(`User: ${user.value.username}`, 14, 32);
  doc.text(`Email: ${user.value.email}`, 14, 40);

  const tableData = paginatedData.value.map(sale => [
    sale.saleId,
    sale.saleDate,
    sale.product?.model,
    sale.quantity,
    `${sale.price} $`,
    `${sale.totalPrice} $`,
    sale.customer?.name,
    sale.employee?.name,
  ]);
  const grandTotal = paginatedData.value.reduce((sum, sale) => sum + (parseFloat(sale.totalPrice) || 0), 0);

  autoTable(doc, {
    head: [['Sale ID', 'Sale Date', 'Model', 'Quantity', 'Price', 'Total Price', 'Customer Name', 'Sold By']],
    body: tableData,
    startY: 50, // Adjusted for spacing with the date/time
    styles: { fontSize: 10 },
    headStyles: { fillColor: [200, 200, 200], textColor: [0, 0, 0], fontSize: 12 },
    didDrawPage: (data) => {
      // Add the grand total below the table
      const tableBottomY = data.cursor.y + 10;
      doc.setFontSize(12);
      doc.text(`Grand Total: ${grandTotal.toFixed(2)} $`, 14, tableBottomY);
    }
  });
  doc.save("sales_report.pdf");
};
</script>
<template>
  <el-table :data="paginatedData" width="100%">
    <el-table-column prop="saleId" label="Sale ID" width="200" sortable />
    <el-table-column prop="saleDate" label="Sale Date" width="200" sortable />
    <el-table-column prop="product.model" label="Model" width="200"/>
    <el-table-column prop="quantity" label="Quantity" width="150" />
    <el-table-column label="Price" width="180">
      <template #default="{ row }">
        {{ `${row.price.toFixed(2)} $` }}
      </template>
    </el-table-column>
    <el-table-column label="Total Price" width="150">
      <template #default="{ row }">
        {{ `${row.totalPrice.toFixed(2)} $` }}
      </template>
    </el-table-column>
    <el-table-column prop="customer.name" label="Customer Name" width="200" />
    <el-table-column prop="employee.name" label="Sold By" width="200" />
  </el-table>
  <div class="example-pagination-block">
    <el-pagination
        layout="prev, pager, next"
        :total="paginatedData.length"
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
.mt-4{
  margin-top: 10px;
}
</style>
