<script setup>
import {ref, computed, onMounted, getCurrentInstance, watch} from "vue";
import axios from 'axios';
import {ElMessage, ElMessageBox} from 'element-plus';
import { ElNotification } from 'element-plus';

import moment from 'moment';

const search = ref("");
const currentPage = ref(1);
const pageSize = ref(10);
const saleData = ref([]);
const productData = ref([]);
const customerData = ref([]);
const employeeData = ref([]);
const showDialog = ref(false);
const viewDialog = ref(false);
const isEdit = ref(false);
const isLoading = ref(false);
const {appContext} = getCurrentInstance();
const BASEURL = appContext.config.globalProperties.$BASEURL;
const oldQuantity = ref(0);
const viewForm = ref({
  saleId: "",
  saleDate: "",
  productName: "",
  quantity: "",
  price: "",
  totalPrice: "",
  customerName: "",
  employeeName: "",
});

const Form = ref({
  saleId: "",
  saleDate: "",
  productId: "",
  quantity: 0,
  price: 0,
  totalPrice: 0,
  customerId: "",
  employeeId: "",
});
//Function fetching data from sale
const FetchData = async () => {
  const endpoints = [
    {key: saleData, url: "/sale"},
    {key: productData, url: "/product"},
    {key: customerData, url: "/customer"},
    {key: employeeData, url: "/employee"},
  ];
  try {
    await Promise.all(
        endpoints.map(async (endpoint) => {
          const response = await axios.get(`${BASEURL}${endpoint.url}`);
          endpoint.key.value = response.data ? response.data : [];
        })
    );
    console.log("Data fetched successfully:", {
      saleData: saleData.value,
      productData: productData.value,
      customerData: customerData.value,
      employeeData: employeeData.value
    });
  } catch (err) {
    console.error("Error fetching data:", err);
  }
};

onMounted(async () => {
  await FetchData();
});
// Function for automatically calculating price
  const calculatePrice = () => {
    const product = productData.value.find(p => p.productId === Form.value.productId);
    if (product) {
      Form.value.price = parseInt(product.price).toFixed(2) || 0;
      Form.value.totalPrice = (parseInt(Form.value.price) * parseInt(Form.value.quantity || 1)).toFixed(2);
    } else {
      Form.value.price = 0;
      Form.value.totalPrice = 0;
    }
};
// Watch productId to set quantity to 1 when changed from empty
watch(() => Form.value.productId, (newProductId, oldProductId) => {
  if (newProductId && !oldProductId && !Form.value.quantity) {
    Form.value.quantity = 1;
  }
  calculatePrice();
});

// Watch quantity for price updates
watch(() => Form.value.quantity, () => {
  calculatePrice();
});
//Function to view sale
const ViewSale = (row) => {
  viewDialog.value = true;
  viewForm.value = {
    ...row,
    oldQuantity: row.quantity,
    productName: row.product?.model,
    customerName: row.customer?.name,
    employeeName: row.employee?.name,
  }
}
// Function to add new sale
const AddNewSale = async () => {
  Form.value.saleDate = moment(Form.value.saleDate).format('YYYY-MM-DD');
  console.log(Form.value);
  const productStock = productData.value.find(p => p.productId === Form.value.productId);
  if (!productStock) {
    ElMessage.error("Selected product not found.");
    return;
  }
  // Check stock availability
  if (productStock.stock <= 0) {
    ElMessage.error("This product is out of stock.");
    return;
  }
  if (Form.value.quantity > productStock.stock) {
    ElMessage.error("Not enough stock available.");
    return;
  }
  if (!Form.value.productId ||
      !Form.value.customerId ||
      !Form.value.employeeId ||
      !Form.value.quantity ||
      !Form.value.price ||
      !Form.value.totalPrice ||
      !Form.value.saleDate) {
    ElMessage.error("Please fill in all the required fields.");
    return;
  }
  try {
    isLoading.value = true;
    await axios.post(`${BASEURL}/sale`, Form.value, {
      headers: {'Content-Type': 'application/json'},
    });
    showDialog.value = false;
    ResetForm();
    await new Promise(resolve => setTimeout(resolve, 3000));
    await FetchData();
    ElMessage.success("Sale added successfully.");
  } catch (err) {
    ElMessage.error("Error adding Sale");
    console.log("Error adding sale", err);
  } finally {
    isLoading.value = false;
  }
};
// Function to edit sale
const EditSale = (row) => {
  oldQuantity.value = row.quantity;
  const getProductId = productData.value.find(p => p.model === row.product?.model);
  const getCustomerId = customerData.value.find(c => c.name === row.customer?.name);
  const getEmployeeId = employeeData.value.find(e => e.name === row.employee?.name);
  Form.value = {
    ...row,
    productId: getProductId ? getProductId.productId : null,
    employeeId: getEmployeeId ? getEmployeeId.employeeId : null,
    customerId: getCustomerId ? getCustomerId.customerId : null,
  };
  isEdit.value = true;
  showDialog.value = true;
  console.log(Form.value);
};
//Function to update sale
const UpdateSale = async () => {
  Form.value.saleDate = moment(Form.value.saleDate).format('YYYY-MM-DD');
  console.log(Form.value);
  const productStock = productData.value.find(p => p.productId === Form.value.productId);
  // const oldQuantity = saleData.value.find(s => s.saleId === Form.value.saleId).quantity;
  // console.log('this old qty',oldQuantity);
  // console.log('this product in stock',productStock.stock);
  if (!productStock) {
    ElMessage.error("Selected product not found.");
    return;
  }
  if (productStock.stock <= 0) {
    ElMessage.error("This product is out of stock.");
    return;
  }
  const currentQuantity = oldQuantity.value;
  console.log('this current qty',currentQuantity);
  // const originalSale = saleData.value.find(s => s.saleId === Form.value.saleId);
  // const previousQuantity = originalSale ? originalSale.quantity : 0;
  const availableStock = productStock.stock + currentQuantity;
  console.log('Product max allowed quantity:', availableStock);
  if(Form.value.quantity !== oldQuantity) {
    if (Form.value.quantity > availableStock) {
      ElMessage.error("Not enough stock available.");
      return;
    }
  }
  if (!Form.value.productId ||
      !Form.value.customerId ||
      !Form.value.employeeId ||
      !Form.value.quantity ||
      !Form.value.price ||
      !Form.value.saleDate) {
    ElMessage.error("Please fill in all the required fields.");
    return;
  }
  try {
    isLoading.value = true;
    await axios.put(`${BASEURL}/sale`, Form.value, {
      headers: {'Content-Type': 'application/json'},
    });
    showDialog.value = false;
    ResetForm();
    await new Promise(resolve => setTimeout(resolve, 3000));
    await FetchData();
    ElMessage.success("Sale updated successfully.");
  } catch (err) {
    console.error("Error updating Sale");
    ElMessage.error("Error updating Sale");
  } finally {
    isEdit.value = false;
    isLoading.value = false;
  }
}
// Function to handle submission
const handleSubmit = async () => {
  if (isEdit.value) {
    await UpdateSale();
  } else {
    await AddNewSale();
  }
};
// Confirm Delete Sale
const confirmDelete = (saleId) => {
  ElMessageBox.confirm(
      `Are you sure you want to delete this sale?`,
      "Delete Confirmation",
      {
        confirmButtonText: "Yes, Delete it.",
        type: "warning",
        showCancelButton: true,
        center: true,
      }
  )
      .then(() => {
        deleteSale(saleId);
      })
      .catch(() => {
        ElMessage.info("Delete canceled.");
      });
};

// Delete Sale Function
const deleteSale = async (saleId) => {
  try {
    isLoading.value = true;
    await axios.delete(`${BASEURL}/sale`, {
      headers: {"Content-Type": "application/json"},
      data: {saleId}
    });
    await new Promise((resolve) => setTimeout(resolve, 3000));
    await FetchData();
    ElMessage.success("Sale deleted successfully!");
  } catch (error) {
    console.error("Error deleting sale:", error);
    ElMessage.error("Failed to delete sale.");
  } finally {
    isLoading.value = false;
  }
};

// Function to reset form
const ResetForm = () => {
  Form.value = {
    saleId: "",
    saleDate: "",
    productId: "",
    quantity: 0,
    price: 0,
    customerId: "",
    employeeId: "",
  };
};

const handleClose = () => {
  showDialog.value = false;
  ResetForm();
};

// Computed properties remain the same
const filteredData = computed(() => {
  const lowerCaseSearch = search.value.toLowerCase();
  return saleData.value.filter(
      (sale) =>
          sale.saleId.toString().includes(lowerCaseSearch) ||
          sale.product?.model.toString().toLowerCase().includes(lowerCaseSearch) ||
          sale.customer?.name.toLowerCase().includes(lowerCaseSearch) ||
          sale.employee?.name.toLowerCase().includes(lowerCaseSearch)
  );
});

const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return filteredData.value.slice(start, start + pageSize.value);
});

const handlePageChange = (newPage) => {
  currentPage.value = newPage;
};
</script>

<template>
  <el-table :data="paginatedData">
    <el-table-column prop="saleId" label="Sale ID" width="100" sortable/>
    <el-table-column prop="saleDate" label="Sale Date" width="150" sortable/>
    <el-table-column prop="product.model" label="Model" width="200" align="center" sortable/>
    <el-table-column prop="quantity" label="Quantity" width="150" align="center"/>
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
    <el-table-column prop="customer.name" label="Customer Name" width="200"/>
    <el-table-column prop="employee.name" label="Sold By" width="200"/>
    <el-table-column fixed="right" label="Operations" width="200">
      <template #default="{ row }">
        <el-button type="primary" link @click="ViewSale(row)">View</el-button>
        <el-button type="primary" link @click="EditSale(row)">Edit</el-button>
        <el-button
            type="danger"
            link
            @click="confirmDelete(row.saleId, row.model)"
            v-loading.fullscreen.lock="isLoading"
        >
          Remove
        </el-button>
      </template>
    </el-table-column>
    <!-- Search Input -->
    <el-table-column min-width="200" fixed="right" align="center">
      <template #header>
        <el-input v-model="search" size="default" placeholder="Type to search" style="min-width: 150px"/>
      </template>
    </el-table-column>
  </el-table>

  <div class="example-pagination-block">
    <el-pagination
        layout="prev, pager, next"
        :total="saleData.length"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
    />
  </div>

  <el-button class="mt-4" style="width: 100%" @click="showDialog = true">
    Add New Sale
  </el-button>

  <!-- View Sale Dialog -->
  <el-dialog v-model="viewDialog" title="View Sale">
    <el-form :model="viewForm" label-width="auto" style="max-width: 600px">
      <el-form-item label="Sale ID">
        <el-input v-model="viewForm.saleId" readonly/>
      </el-form-item>
      <el-form-item label="Sale Date">
        <el-input v-model="viewForm.saleDate" readonly/>
      </el-form-item>
      <el-form-item label="Model">
        <el-input v-model="viewForm.productName" readonly/>
      </el-form-item>
      <el-form-item label="Quantity">
        <el-input v-model="viewForm.quantity" readonly/>
      </el-form-item>
      <el-form-item label="Price">
        <el-input v-model="viewForm.price" readonly/>
      </el-form-item>
      <el-form-item label="Total Price">
        <el-input v-model="viewForm.totalPrice" readonly/>
      </el-form-item>
      <el-form-item label="Customer Name">
        <el-input v-model="viewForm.customerName" readonly/>
      </el-form-item>
      <el-form-item label="Sold By">
        <el-input v-model="viewForm.employeeName" readonly/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="viewDialog = false">Close</el-button>
    </template>
  </el-dialog>
  <!--  Add or Edit Sale Dialog -->
  <el-dialog v-model="showDialog" :title="isEdit ? 'Edit Sale' : 'Add New Sale'" @close="handleClose">
    <el-form :model="Form" ref="saleFormRef" label-width="auto" style="max-width: 600px">

      <!-- Product -->
      <el-form-item label="Product" prop="productId"
                    :rules="[{ required: true, message: 'Please select a product.', trigger: 'blur' }]">
        <el-select v-model="Form.productId" placeholder="Select Product">
          <el-option v-for="product in productData" :key="product.productId" :label="product.model"
                     :value="product.productId"/>
        </el-select>
      </el-form-item>

      <!-- Customer -->
      <el-form-item label="Customer" prop="customerId"
                    :rules="[{ required: true, message: 'Please select a customer.', trigger: 'blur' }]">
        <el-select v-model="Form.customerId" placeholder="Select Customer">
          <el-option v-for="customer in customerData" :key="customer.customerId" :label="customer.name"
                     :value="customer.customerId"/>
        </el-select>
      </el-form-item>

      <!-- Employee -->
      <el-form-item label="Employee" prop="employeeId"
                    :rules="[{ required: true, message: 'Please select an employee.', trigger: 'blur' }]">
        <el-select v-model="Form.employeeId" placeholder="Select Employee">
          <el-option v-for="employee in employeeData" :key="employee.employeeId" :label="employee.name"
                     :value="employee.employeeId"/>
        </el-select>
      </el-form-item>

      <!-- Quantity -->
      <el-form-item label="Quantity" prop="quantity"
                    :rules="[{ required: true, message: 'Please enter the quantity.', trigger: 'blur' }]">
        <el-input v-model="Form.quantity" type="number" min="1"/>
      </el-form-item>

      <!-- Price -->
      <el-form-item label="Price" prop="price"
                    :rules="[{ required: true, message: 'Please enter a price.', trigger: 'blur' }]">
        <el-input v-model="Form.price" type="number" min="0" readonly/>
      </el-form-item>
      <!-- Total Price -->
      <el-form-item label="Total Price" prop="totalPrice"
                    :rules="[{ required: true, message: 'Please enter a total price.', trigger: 'blur' }]">
        <el-input v-model="Form.totalPrice" type="number" min="0" readonly/>
      </el-form-item>
      <!-- Sale Date -->
      <el-form-item label="Sale Date" prop="saleDate"
                    :rules="[{ required: true, message: 'Please select the sale date.', trigger: 'blur' }]">
        <el-date-picker v-model="Form.saleDate" type="date" placeholder="Select Sale Date"/>
      </el-form-item>
    </el-form>
    <!-- Submit Button -->
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">Cancel</el-button>
        <el-button type="primary" @click="handleSubmit" v-loading.fullscreen.lock="isLoading">
          {{ isEdit ? 'Update' : 'Create' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>

</style>