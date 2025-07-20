<script setup>
import {ref, computed, onMounted, getCurrentInstance, watch} from "vue";
import axios from 'axios';
import {ElMessage, ElMessageBox} from 'element-plus';
import moment from "moment";

const search = ref("");
const currentPage = ref(1);
const pageSize = ref(10);
const importData = ref([]);
const productData = ref([]);
const showDialog = ref(false);
const viewDialog = ref(false);
const isEdit = ref(false);
const isLoading = ref(false);
const {appContext} = getCurrentInstance();
const BASEURL = appContext.config.globalProperties.$BASEURL;
const viewForm = ref({
  importId: "",
  importDate: "",
  productName: "",
  quantity: "",
  price: "",
  totalPrice: "",
});
const Form = ref({
  importId: "",
  importDate: "",
  productId: "",
  quantity: 0,
  price: 0,
});

//Function fetching data from import
const FetchData = async () => {
  try {
    const [importResponse, productResponse] = await Promise.all([
      axios.get(`${BASEURL}/import`),
      axios.get(`${BASEURL}/product`)
    ]);
    importData.value = importResponse.data.empty ? [] : importResponse.data;
    productData.value = productResponse.data.empty ? [] : productResponse.data;
    console.log('Import data:', importData.value);
    console.log('Product data:', productData.value);
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
    Form.value.price = parseInt(product.price) || 0;
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
//Function to handle add new import
const AddNewImport = async () => {
  Form.value.importDate = moment(Form.value.importDate).format("YYYY-MM-DD");
  if (!Form.value.productId ||
      !Form.value.quantity ||
      !Form.value.price ||
      !Form.value.importDate) {
    ElMessage.error("Please fill in all the required fields.");
    return;
  }
  console.log(Form.value);
  try {
    isLoading.value = true;
    await axios.post(`${BASEURL}/import`, Form.value, {
      headers: {'Content-Type': 'application/json'},
    });
    showDialog.value = false;
    ResetForm();
    await new Promise(resolve => setTimeout(resolve, 3000));
    await FetchData();
    ElMessage.success("Import added successfully.");
  } catch (err) {
    console.error("Error adding import:", err);
  } finally {
    isLoading.value = false;
  }
}
//Function to handle view import
const ViewImport = (row) => {
  viewDialog.value = true;
  viewForm.value = {
    ...row,
    productName: row.product?.model
  }
}
//Function to handle edit
const EditImport = (row) => {
  const getProductId = productData.value.find(p => p.model === row.product?.model);
  console.log(Form.value);
  isEdit.value = true;
  showDialog.value = true;
  Form.value = {
    ...row,
    productId: getProductId.productId,
  }
}
//Function to handle update import
const UpdateImport = async () => {
  Form.value.importDate = moment(Form.value.importDate).format("YYYY-MM-DD");
  if (!Form.value.productId ||
      !Form.value.quantity ||
      !Form.value.price ||
      !Form.value.importDate) {
    ElMessage.error("Please fill in all the required fields.");
    return;
  }
  try {
    isLoading.value = true;
    await axios.put(`${BASEURL}/import`, Form.value, {
      headers: {'Content-Type': 'application/json'},
    });
    showDialog.value = false;
    ResetForm();
    await new Promise(resolve => setTimeout(resolve, 3000));
    await FetchData();
    ElMessage.success("Import updated successfully.");
  } catch (err) {
    console.error("Error updating import:", err);
    ElMessage.error("Error updating import:", err);
  } finally {
    isEdit.value = false;
    isLoading.value = false;
  }
}
//Function handle submission
const handleSubmit = async () => {
  if (isEdit.value) {
    await UpdateImport();
  } else {
    await AddNewImport();
  }
}
// Confirm Delete Import
const confirmDelete = (importId) => {
  ElMessageBox.confirm(
      `Are you sure you want to delete this import?`,
      "Delete Confirmation",
      {
        confirmButtonText: "Yes, Delete it.",
        type: "warning",
        showCancelButton: true,
        center: true,
      }
  )
      .then(() => {
        deleteImport(importId);
      })
      .catch(() => {
        ElMessage.info("Delete canceled.");
      });
};
// Delete Import Function
const deleteImport = async (importId) => {
  try {
    isLoading.value = true;
    await axios.delete(`${BASEURL}/import`, {
      headers: {"Content-Type": "application/json"},
      data: {importId}
    });
    await new Promise((resolve) => setTimeout(resolve, 3000));
    await FetchData();
    ElMessage.success("Import deleted successfully!");
  } catch (error) {
    console.error("Error deleting import:", error);
    ElMessage.error("Failed to delete import.");
  } finally {
    isLoading.value = false;
  }
};
//Function to reset form
const ResetForm = () => {
  Form.value = {
    importId: "",
    importDate: "",
    productId: "",
    quantity: "",
    price: "",
  }
}
const handleClose = () => {
  showDialog.value = false;
  ResetForm();
};
// Computed properties remain the same
const filteredData = computed(() => {
  const lowerCaseSearch = search.value.toLowerCase();
  return importData.value.filter(
      (imp) =>
          imp.importId.toString().includes(lowerCaseSearch) ||
          imp.product?.model.toString().toLowerCase().includes(lowerCaseSearch) ||
          imp.importDate.toLowerCase().includes(lowerCaseSearch)
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
    <el-table-column prop="importId" label="Import ID" width="120" sortable/>
    <el-table-column prop="importDate" label="Import Date" width="150" sortable/>
    <el-table-column prop="product.model" label="Model" width="200" sortable/>
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
    <el-table-column fixed="right" label="Operations" width="200">
      <template #default="{ row }">
        <el-button type="primary" link @click="ViewImport(row)">View</el-button>
        <el-button type="primary" link @click="EditImport(row)">Edit</el-button>
        <el-button type="danger" link @click="confirmDelete(row.importId, row.product.model)"
                   v-loading.fullscreen.lock="isLoading">
          Remove
        </el-button>
      </template>
    </el-table-column>
    <el-table-column min-width="200" fixed="right" align="center">
      <template #header>
        <el-input v-model="search" size="default" placeholder="Type to search" style="min-width: 150px"/>
      </template>
    </el-table-column>
  </el-table>

  <div class="example-pagination-block">
    <el-pagination layout="prev, pager, next" :total="importData.length" :page-size="pageSize"
                   :current-page="currentPage" @current-change="handlePageChange"/>
  </div>

  <el-button class="mt-4" style="width: 100%" @click="showDialog = true">
    Add New Import
  </el-button>

  <!-- View Import Dialog -->
  <el-dialog v-model="viewDialog" title="View Import">
    <el-form :model="viewForm" label-width="auto" style="max-width: 600px">
      <el-form-item label="Import ID">
        <el-input v-model="viewForm.importId" readonly/>
      </el-form-item>
      <el-form-item label="Import Date">
        <el-input v-model="viewForm.importDate" readonly/>
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
    </el-form>
    <template #footer>
      <el-button @click="viewDialog = false">Close</el-button>
    </template>
  </el-dialog>

  <!-- Add or Edit Import Dialog -->
  <el-dialog v-model="showDialog" :title="isEdit ? 'Edit Import' : 'Add New Import'" @close="handleClose">
    <el-form :model="Form" ref="importFormRef" label-width="auto" style="max-width: 600px">
      <el-form-item label="Product" prop="productId"
                    :rules="[{ required: true, message: 'Please select a product.', trigger: 'blur' }]">
        <el-select v-model="Form.productId" placeholder="Select Product">
          <el-option v-for="product in productData" :key="product.productId" :label="product.model"
                     :value="product.productId"/>
        </el-select>
      </el-form-item>

      <el-form-item label="Quantity" prop="quantity"
                    :rules="[{ required: true, message: 'Please enter the quantity.', trigger: 'blur' }]">
        <el-input v-model="Form.quantity" type="number" min="1"/>
      </el-form-item>

      <el-form-item label="Price" prop="price"
                    :rules="[{ required: true, message: 'Please enter a price.', trigger: 'blur' }]">
        <el-input v-model="Form.price" type="number" min="0" readonly/>
      </el-form-item>

      <el-form-item label="Total Price" prop="totalPrice"
                    :rules="[{ required: true, message: 'Please enter a total price.', trigger: 'blur' }]">
        <el-input v-model="Form.totalPrice" type="number" min="0" readonly/>
      </el-form-item>

      <el-form-item label="Import Date" prop="importDate"
                    :rules="[{ required: true, message: 'Please select the import date.', trigger: 'blur' }]">
        <el-date-picker v-model="Form.importDate" type="date" placeholder="Select Import Date"/>
      </el-form-item>
    </el-form>
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