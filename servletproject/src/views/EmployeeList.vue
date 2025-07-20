<script setup>
import {ref, computed, onMounted, getCurrentInstance} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import axios from "axios";

const employeeData = ref([]);
const search = ref("");
const currentPage = ref(1);
const pageSize = ref(10);
const showDialog = ref(false);
const showViewDialog = ref(false);
const isEdit = ref(false);
const isLoading = ref(false);

// Define options for position and gender dropdowns
const positionOptions = ref([
  {value: "Inventory Manager", label: "Inventory Manager"},
  {value: "Sales Manager", label: "Sales Manager"},
  {value: "HR Manager", label: "HR Manager"},
  {value: "System Administrator", label: "System Administrator"},
]);

const genderOptions = ref([
  {value: "Male", label: "Male"},
  {value: "Female", label: "Female"},
]);

const Form = ref({
  employeeId: "",
  name: "",
  gender: "",
  position: "",
  email: "",
  phone: "",
});

const viewForm = ref({
  employeeId: "",
  name: "",
  gender: "",
  position: "",
  email: "",
  phone: "",
});

const {appContext} = getCurrentInstance();
const BASEURL = appContext.config.globalProperties.$BASEURL;

// Function to fetch user data
const FetchEmployee = async () => {
  try {
    const response = await axios.get(`${BASEURL}/employee`);
    const {data} = response;
    employeeData.value = data.empty ? [] : data;
    console.log("Employee data:", employeeData.value);
  } catch (err) {
    console.log("Error fetching employee:", err);
  }
};

// Function to add a new user
const AddNewEmployee = async () => {
  if (!Form.value.name || !Form.value.gender || !Form.value.position || !Form.value.email || !Form.value.phone) {
    ElMessage.error("Please fill in all the required fields.");
    return;
  }
  const existEmail = employeeData.value.some(emp => emp.email === Form.value.email);
  if (existEmail) {
    ElMessage.error("Email is already exist.");
    return;
  }
  const existPhone = employeeData.value.some(emp => emp.phone === Form.value.phone);
  if (existPhone) {
    ElMessage.error("Phone number is already exist.");
    return;
  }
  try {
    isLoading.value = true;
    await axios.post(`${BASEURL}/employee`, Form.value, {
      headers: {"Content-Type": "application/json"},
    });
    showDialog.value = false;
    resetForm();
    await new Promise((resolve) => setTimeout(resolve, 3000));
    await FetchEmployee();
    ElMessage.success("New employee created successfully");
  } catch (err) {
    console.log("Error adding new employee:", err);
    ElMessage.error("Failed to add employee.");
  } finally {
    isLoading.value = false;
  }
};

// Function to handle edit operation
const EditEmployee = (row) => {
  isEdit.value = true;
  showDialog.value = true;
  Form.value = {...row};
};

// Function to handle update employee
const UpdateEmployee = async () => {
  if (!Form.value.name || !Form.value.gender || !Form.value.position || !Form.value.email || !Form.value.phone) {
    ElMessage.error("Please fill in all the required fields.");
    return;
  }
  try {
    isLoading.value = true;
    await axios.put(`${BASEURL}/employee`, Form.value, {
      headers: {"Content-Type": "application/json"},
    });
    showDialog.value = false;
    resetForm();
    await new Promise((resolve) => setTimeout(resolve, 3000));
    await FetchEmployee();
    ElMessage.success("Employee updated successfully");
  } catch (err) {
    console.log("Error updating employee:", err);
    ElMessage.error("Failed to update employee.");
  } finally {
    isLoading.value = false;
  }
};

// Function to handle delete with confirmation
const confirmDelete = (employeeId, name) => {
  ElMessageBox.confirm(
      `Are you sure you want to delete this employee: ${name} ?`,
      "Delete Confirmation",
      {
        confirmButtonText: "Yes, Delete it.",
        type: "warning",
        showCancelButton: true,
        center: true,
      }
  )
      .then(() => DeleteEmployee(employeeId))
      .catch(() => ElMessage.info("Delete action cancelled"));
};

// Function to delete employee
const DeleteEmployee = async (employeeId) => {
  try {
    isLoading.value = true;
    await axios.delete(`${BASEURL}/employee`, {
      headers: {"Content-Type": "application/json"},
      data: {employeeId},
    });
    await new Promise((resolve) => setTimeout(resolve, 3000));
    await FetchEmployee();
    ElMessage.success("Employee deleted successfully");
  } catch (err) {
    console.log("Error deleting employee:", err);
    ElMessage.error("Failed to delete employee.");
  } finally {
    isLoading.value = false;
  }
};

// Function to handle submit form
const handleSubmit = async () => {
  if (isEdit.value) {
    await UpdateEmployee();
  } else {
    await AddNewEmployee();
  }
};

// Function to handle view employee
const ViewEmployee = (row) => {
  showViewDialog.value = true;
  viewForm.value = {...row};
};

// Function to reset form fields
const resetForm = () => {
  Form.value = {employeeId: "", name: "", gender: "", position: "", email: "", phone: ""};
};

// Function to handle cancel
const handleCancel = () => {
  showDialog.value = false;
  showViewDialog.value = false;
  isEdit.value = false;
  resetForm();
};

// Computed property to filter and paginate data
const filteredData = computed(() => {
  const lowerCaseSearch = search.value.toLowerCase();
  return employeeData.value.filter(
      (emp) =>
          emp.employeeId.toString().toLowerCase().includes(lowerCaseSearch) ||
          emp.name.toLowerCase().includes(lowerCaseSearch) ||
          emp.gender.toLowerCase().includes(lowerCaseSearch) ||
          emp.position.toLowerCase().includes(lowerCaseSearch) ||
          emp.email.toLowerCase().includes(lowerCaseSearch) ||
          emp.phone.toLowerCase().includes(lowerCaseSearch)
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

onMounted(async () => {
  await FetchEmployee();
});
</script>

<template>
  <el-table :data="paginatedData">
    <el-table-column prop="employeeId" label="Employee ID" width="150" sortable/>
    <el-table-column prop="name" label="Employee Name" width="200" sortable/>
    <el-table-column prop="gender" label="Gender" width="200" sortable/>
    <el-table-column prop="position" label="Position" width="200"/>
    <el-table-column prop="email" label="Email" width="200"/>
    <el-table-column prop="phone" label="Phone Number" width="200"/>
    <el-table-column fixed="right" label="Operations" min-width="200">
      <template #default="{ row }">
        <el-button link type="primary" @click="ViewEmployee(row)">View</el-button>
        <el-button link type="primary" @click="EditEmployee(row)">Edit</el-button>
        <el-button
            link
            type="danger"
            @click="confirmDelete(row.employeeId, row.name)"
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
        :total="employeeData.length"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
    />
  </div>
  <!-- Add Item Button -->
  <el-button class="mt-4" style="width: 100%" @click="showDialog = true">Add New Employee</el-button>
  <!-- Dialog for View -->
  <el-dialog v-model="showViewDialog" :title="'View Employee'" @close="handleCancel">
    <el-form :model="viewForm" style="max-width: 600px" label-width="auto" status-icon>
      <el-form-item label="Employee ID">
        <el-input v-model="viewForm.employeeId" readonly/>
      </el-form-item>
      <el-form-item label="Employee Name">
        <el-input v-model="viewForm.name" readonly/>
      </el-form-item>
      <el-form-item label="Gender">
        <el-input v-model="viewForm.gender" readonly/>
      </el-form-item>
      <el-form-item label="Position">
        <el-input v-model="viewForm.position" readonly/>
      </el-form-item>
      <el-form-item label="Email">
        <el-input v-model="viewForm.email" readonly/>
      </el-form-item>
      <el-form-item label="Phone Number">
        <el-input v-model="viewForm.phone" readonly/>
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleCancel">Close</el-button>
      </span>
    </template>
  </el-dialog>
  <!-- Dialog for Add/Edit -->
  <el-dialog
      v-model="showDialog"
      :title="isEdit ? 'Edit Employee' : 'Add New Employee'"
      @close="handleCancel"
  >
    <el-form
        ref="ruleForm"
        style="max-width: 600px"
        :model="Form"
        status-icon
        label-width="auto"
    >
      <el-form-item
          label="Employee Name"
          prop="name"
          :rules="[{ required: true, message: 'Please input the employee name', trigger: 'blur' }]"
      >
        <el-input v-model="Form.name"/>
      </el-form-item>
      <el-form-item
          label="Gender"
          prop="gender"
          :rules="[{ required: true, message: 'Please select the gender', trigger: 'change' }]"
      >
        <el-select v-model="Form.gender" placeholder="Select Gender" style="width: 100%">
          <el-option
              v-for="gender in genderOptions"
              :key="gender.value"
              :label="gender.label"
              :value="gender.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item
          label="Position"
          prop="position"
          :rules="[{ required: true, message: 'Please select the position', trigger: 'change' }]"
      >
        <el-select v-model="Form.position" placeholder="Select Position" style="width: 100%">
          <el-option
              v-for="position in positionOptions"
              :key="position.value"
              :label="position.label"
              :value="position.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item
          label="Email"
          prop="email"
          :rules="[
          { required: true, message: 'Please input the email', trigger: 'blur' },
          { type: 'email', message: 'Please enter a valid email.', trigger: 'blur' }
        ]"
      >
        <el-input v-model="Form.email"/>
      </el-form-item>
      <el-form-item
          label="Phone Number"
          prop="phone"
          :rules="[{ required: true, message: 'Please input the phone number', trigger: 'blur' }]"
      >
        <el-input v-model="Form.phone"/>
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