<script setup>
import { ref, computed, onMounted,getCurrentInstance} from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import axios from "axios";

const customerData = ref([]);
const search = ref("");
const currentPage = ref(1);
const pageSize = ref(10);
const showDialog = ref(false);
const showViewDialog = ref(false);
const isEdit = ref(false);
const isLoading = ref(false);
const Form = ref({
  customerId: "",
  name: "",
  email: "",
  phone: "",
  address: "",
});
const viewForm = ref({
  customerId: "",
  name: "",
  email: "",
  phone: "",
  address: "",
});
const { appContext } = getCurrentInstance();
const BASEURL = appContext.config.globalProperties.$BASEURL;

// Function to fetch user data
const FetchCustomer = async () => {
  try {
    const response = await axios.get(`${BASEURL}/customer`);
    const { data } = response;
    customerData.value = data.empty ? [] : data;
    console.log("Customer data:", customerData.value);
  } catch (err) {
    console.log("Error fetching customer:", err);
  }
};
// Function to add a new user
const AddNewCustomer = async () => {
  if (!Form.value.name || !Form.value.email || !Form.value.phone || !Form.value.address) {
    ElMessage.error("Please fill in all the required fields.");
    return;
  }
  try {
    isLoading.value = true;
    await axios.post(`${BASEURL}/customer`, Form.value, {
      headers: { 'Content-Type': 'application/json' }
    });
    showDialog.value = false;
    resetForm();
    await new Promise(resolve => setTimeout(resolve, 3000));
    await FetchCustomer();
    ElMessage.success("New customer created successfully");
  } catch (err) {
    console.log("Error adding new customer:", err);
    ElMessage.error("Failed to add customer.");
  } finally {
    isLoading.value = false;
  }
};
// Function to handle edit operation
const EditCustomer = (row) => {
  isEdit.value = true;
  showDialog.value = true;
  Form.value = { ...row}
};
// Function to handle update customer
const UpdateCustomer = async () => {
  if (!Form.value.name || !Form.value.email || !Form.value.phone || !Form.value.address) {
    ElMessage.error("Please fill in all the required fields.");
    return;
  }
  try {
    isLoading.value = true;
    //note:can still update the customer even if remove const customerId and the data object in the Axios request
    // because customerId already exists inside Form.value
    const customerId = Form.value.customerId;
    await axios.put(`${BASEURL}/customer`, Form.value, {
      headers: { 'Content-Type': 'application/json' },
      data: { customerId }
    });
    showDialog.value = false;
    resetForm();
    await new Promise(resolve => setTimeout(resolve, 3000));
    await FetchCustomer();
    ElMessage.success("Customer updated successfully");
  } catch (err) {
    console.log('Error updating customer:', err);
    ElMessage.error("Failed to update customer.");
  } finally {
    isLoading.value = false;
  }
};
// Function to handle delete with confirmation
const confirmDelete = (customerId, name) => {
  ElMessageBox.confirm(
      `Are you sure you want to delete this customer: ${name} ?`,
      'Delete Confirmation',
      {
        confirmButtonText: 'Yes, Delete it.',
        type: 'warning',
        showCancelButton: true,
        center: true,
      }
  )
      .then(() => DeleteCategory(customerId))
      .catch(() => ElMessage.info("Delete action cancelled"));
};
//Function to delete category
const DeleteCategory = async (customerId) => {
  try {
    isLoading.value = true;
    await axios.delete(`${BASEURL}/customer`, {
      headers: { 'Content-Type': 'application/json' },
      data: { customerId }
    });
    await new Promise(resolve => setTimeout(resolve, 3000));
    await FetchCustomer();
    ElMessage.success("Customer deleted successfully");
  } catch (err) {
    console.log("Error deleting customer:", err);
    ElMessage.error("Failed to delete customer.");
  } finally {
    isLoading.value = false;
  }
};
// Function to handle submit form
const handleSubmit = async () => {
  if (isEdit.value) {
    await UpdateCustomer();
  } else {
    await AddNewCustomer();
  }
};
// Function to handle view user
const ViewCustomer = (row) => {
  showViewDialog.value = true;
  viewForm.value = { ...row };
};

// Function to reset form fields
const resetForm = () => {
  Form.value = { customerId: "", name: "", email: "", phone: "", address: "" };
};
//Function to handle cancel
const handleCancel = () => {
  showDialog.value = false;
  showViewDialog.value = false;
  isEdit.value = false;
  resetForm();
};
// Computed property to filter and paginate data
const filteredData = computed(() => {
  const lowerCaseSearch = search.value.toLowerCase();
  return customerData.value.filter(
      (user) =>
          user.customerId.toString().toLowerCase().includes(lowerCaseSearch) ||
          user.name.toLowerCase().includes(lowerCaseSearch) ||
          user.email.toLowerCase().includes(lowerCaseSearch) ||
          user.phone.toLowerCase().includes(lowerCaseSearch) ||
          user.address.toLowerCase().includes(lowerCaseSearch)
  );
});
// Compute paginated data based on filtered data
const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return filteredData.value.slice(start, start + pageSize.value);
});

// Handle page change
const handlePageChange = (newPage) => {
  currentPage.value = newPage;
};
onMounted( async () => {
  await FetchCustomer();
});
</script>

<template>
  <el-table :data="paginatedData">
    <el-table-column prop="customerId" label="Customer ID" width="150" sortable/>
    <el-table-column prop="name" label="Customer Name" width="200" sortable/>
    <el-table-column prop="email" label="Email" width="300"/>
    <el-table-column prop="phone" label="Phone Number" width="200"/>
    <el-table-column prop="address" label="Address" width="250"/>
    <el-table-column fixed="right" label="Operations" min-width="200">
      <template #default="{ row }">
        <el-button link type="primary" @click="ViewCustomer(row)">View</el-button>
        <el-button link type="primary" @click="EditCustomer(row)">Edit</el-button>
        <el-button link type="danger"
                   @click="confirmDelete(row.customerId,row.name)"
                   v-loading.fullscreen.lock="isLoading">
          Remove
        </el-button>
      </template>
    </el-table-column>
    <!-- Search Input -->
    <el-table-column min-width="200" fixed="right" align="center">
      <template #header>
        <el-input
            v-model="search"
            size="default"
            placeholder="Type to search"
            style="min-width: 150px"
        />
      </template>
    </el-table-column>
  </el-table>
  <div class="example-pagination-block">
    <el-pagination
        layout="prev, pager, next"
        :total="customerData.length"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
    />
  </div>
  <!-- Add Item Button -->
  <el-button class="mt-4" style="width: 100%" @click="showDialog = true">Add New Customer</el-button>
  <!-- Dialog for View -->
  <el-dialog v-model="showViewDialog" :title="'View Customer'" @close="handleCancel">
    <el-form :model="viewForm" label-width="100px" status-icon>
      <el-form-item label="Customer ID">
        <el-input v-model="viewForm.customerId" readonly />
      </el-form-item>
      <el-form-item label="Customer Name">
        <el-input v-model="viewForm.name" readonly />
      </el-form-item>
      <el-form-item label="Email">
        <el-input v-model="viewForm.email" readonly />
      </el-form-item>
      <el-form-item label="Password">
        <el-input v-model="viewForm.phone" readonly />
      </el-form-item>
      <el-form-item label="Address">
        <el-input v-model="viewForm.address" readonly />
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleCancel">Close</el-button>
      </span>
    </template>
  </el-dialog>
  <!-- Dialog for Add/Edit -->
  <el-dialog v-model="showDialog"
             :title="isEdit ? 'Edit Customer' : 'Add New Customer'"
             @close="handleCancel"
  >
    <el-form ref="ruleForm"
             style="max-width: 600px"
             :model="Form"
             status-icon label-width="auto"
    >
      <el-form-item label="Customer Name"
                    prop="name"
                    :rules="[{ required: true, message: 'Please input the customer name', trigger: 'blur' }]">
        <el-input v-model="Form.name"/>
      </el-form-item>
      <el-form-item label="Email"
                    prop="email"
                    :rules="[
          { required: true, message: 'Please input the email', trigger: 'blur' },
          {type: 'email', message: 'Please enter a valid email.', trigger: 'blur'}
      ]">
        <el-input v-model="Form.email"/>
      </el-form-item>
      <el-form-item label="Phone Number"
                    prop="phone"
                    :rules="[{ required: true, message: 'Please input the phone number', trigger: 'blur' }]">
        <el-input v-model="Form.phone"/>
      </el-form-item>
      <el-form-item label="Address"
                    prop="address"
                    :rules="[{ required: true, message: 'Please input the address', trigger: 'blur' }]">
        <el-input v-model="Form.address"/>
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleCancel">Cancel</el-button>
        <el-button type="primary" @click="handleSubmit" v-loading.fullscreen.lock="isLoading">
          {{ isEdit ? "Update" : "Create" }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style scoped>

</style>