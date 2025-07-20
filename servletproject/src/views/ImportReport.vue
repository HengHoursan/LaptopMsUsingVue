<script setup>
import { ref, computed, onMounted, getCurrentInstance } from "vue";
import axios from 'axios';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import moment from "moment";
import { ElMessage } from 'element-plus';

const importData = ref([]);
const productData = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);

const { appContext } = getCurrentInstance();
const BASEURL = appContext.config.globalProperties.$BASEURL;


const getUser = localStorage.getItem("user");
const user = ref(getUser ? JSON.parse(getUser) : {
  username: "N/A",
  email: "N/A"
});
// Fetch Data from API
const FetchData = async () => {
  try {
    const [importResponse, productResponse] = await Promise.all([
      axios.get(`${BASEURL}/import`),
      axios.get(`${BASEURL}/product`)
    ]);
    importData.value = importResponse.data.empty ? [] : importResponse.data;
    productData.value = productResponse.data.empty ? [] : productResponse.data;
  } catch (err) {
    console.error("Error fetching data:", err);
  }
};

onMounted(async () => {
  await FetchData();
});

// Paginated data
const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return importData.value.slice(start, start + pageSize.value);
});

const handlePageChange = (newPage) => {
  currentPage.value = newPage;
};

// Function to generate PDF report
const generateReport = () => {
  const doc = new jsPDF();
  const formattedDate = moment().format('MMMM Do YYYY, h:mm:ss a');

  doc.setFont("helvetica");
  doc.setFontSize(18);
  doc.text("Import Report", 105, 16, { align: "center" });

  doc.setFontSize(12);
  doc.text(`Report Date: ${formattedDate}`, 14, 24);
  doc.text(`User: ${user.value.username}`, 14, 24);
  doc.text(`Email: ${user.value.email}`, 14, 32);

  const tableData = paginatedData.value.map(importItem => [
    importItem.importId,
    importItem.importDate,
    importItem.product?.model,
    importItem.quantity,
    `${importItem.price} $`,
    `${importItem.totalPrice} $`
  ]);
  const grandTotal = paginatedData.value.reduce((sum, imp) => sum + (parseFloat(imp.totalPrice) || 0), 0);
  autoTable(doc, {
    head: [['Import ID', 'Import Date', 'Model', 'Quantity', 'Price', 'Total Price']],
    body: tableData,
    startY: 40,
    styles: { fontSize: 10 },
    headStyles: { fillColor: [200, 200, 200], textColor: [0, 0, 0], fontSize: 12 },
    didDrawPage: (data) => {
      // Add the grand total below the table
      const tableBottomY = data.cursor.y + 10; // Position 10mm below the table
      doc.setFontSize(12);
      doc.text(`Grand Total: ${grandTotal.toFixed(2)} $`, 14, tableBottomY);
    }
  });

  doc.save("import_report.pdf");
};

</script>

<template>
  <el-table :data="paginatedData" width="100%">
    <el-table-column prop="importId" label="Import ID" width="120" sortable />
    <el-table-column prop="importDate" label="Import Date" width="150" sortable />
    <el-table-column prop="product.model" label="Model" width="200" />
    <el-table-column prop="quantity" label="Quantity" width="150" align="center" />
    <el-table-column label="Price" width="150">
      <template #default="{ row }">
        {{ `${row.price.toFixed(2)} $` }}
      </template>
    </el-table-column>
    <el-table-column label="Total Price" width="150">
      <template #default="{ row }">
        {{ `${row.totalPrice.toFixed(2)} $` }}
      </template>
    </el-table-column>
  </el-table>

  <div class="example-pagination-block">
    <el-pagination
        layout="prev, pager, next"
        :total="importData.length"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
    />
  </div>

  <el-button class="mt-4" @click="generateReport">
    Print as PDF
  </el-button>
</template>

<style scoped>
.mt-4 {
  margin-top: 10px;
}
</style>
