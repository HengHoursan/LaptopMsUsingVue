<template>
  <el-table :data="paginatedData" style="width: 100%">
    <el-table-column prop="userid" label="User ID" width="100" sortable/>
    <el-table-column prop="username" label="UserName" width="200" sortable/>
    <el-table-column prop="email" label="Email" width="200"/>
    <el-table-column prop="password" label="Password" width="200"/>
    <el-table-column prop="address" label="Address" width="250"/>
    <el-table-column label="Operations" min-width="200" fixed="right">
      <template #default="{ row }">
        <el-button link type="primary" @click="ViewUser(row)">View</el-button>
        <el-button link type="primary" @click="EditUser(row)">Edit</el-button>
        <el-button link type="danger"
                   @click="confirmDelete(row.userid, row.username)"
                   v-loading.fullscreen.lock="isLoading"
        >
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
            style="min-width: 200px"
        />
      </template>
    </el-table-column>
  </el-table>

  <div class="example-pagination-block">
    <el-pagination
        layout="prev, pager, next"
        :total="userData.length"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
    />
  </div>

  <!-- Add Item Button -->
  <el-button class="mt-4" style="width: 100%" @click="showDialog = true">Add New User</el-button>

  <!-- Dialog for View -->
  <el-dialog v-model="showViewDialog" :title="'View User'" @close="handleCancel">
    <el-form :model="viewForm" label-width="100px" status-icon>
      <el-form-item label="User ID">
        <el-input v-model="viewForm.userid" disabled />
      </el-form-item>
      <el-form-item label="User Name">
        <el-input v-model="viewForm.username" disabled />
      </el-form-item>
      <el-form-item label="Email">
        <el-input v-model="viewForm.email" disabled />
      </el-form-item>
      <el-form-item label="Password">
        <el-input v-model="viewForm.password" disabled />
      </el-form-item>
      <el-form-item label="Address">
        <el-input v-model="viewForm.address" disabled />
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
             :title="isEdit ? 'Edit User' : 'Add New User'"
             @close="handleCancel"
  >
    <el-form ref="ruleForm"
             style="max-width: 600px"
             :model="Form"
             status-icon label-width="auto"
    >
      <el-form-item label="UserName"
                    prop="username"
                    :rules="[{ required: true, message: 'Please input the username', trigger: 'blur' }]">
        <el-input v-model="Form.username"/>
      </el-form-item>
      <el-form-item label="Email"
                    prop="email"
                    :rules="[
          { required: true, message: 'Please input the email', trigger: 'blur' },
          {type: 'email', message: 'Please enter a valid email.', trigger: 'blur'}
      ]">
        <el-input v-model="Form.email"/>
      </el-form-item>
      <el-form-item label="Password"
                    prop="password"
                    :rules="[{ required: true, message: 'Please input the password', trigger: 'blur' }]">
        <el-input v-model="Form.password"/>
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

<script setup>
import { ref, computed, onMounted,getCurrentInstance} from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import axios from "axios";
import {useRouter} from "vue-router";

const userData = ref([]);
const search = ref("");
// const BASEURL = "http://localhost:8080/LaptopMS";
const currentPage = ref(1);
const pageSize = ref(10);
const showDialog = ref(false);
const showViewDialog = ref(false);
const isEdit = ref(false);
const isLoading = ref(false);
const router = useRouter();
const Form = ref({
  userid: "",
  username: "",
  email: "",
  password: "",
  address: "",
});
const viewForm = ref({
  userid: "",
  username: "",
  email: "",
  password: "",
  address: "",
});
const { appContext } = getCurrentInstance();
const BASEURL = appContext.config.globalProperties.$BASEURL;
const currentUserid = ref(
    JSON.parse(localStorage.getItem("user") || '{}').userid || null
);
//Function to log out current user
const LogoutUser = () => {
  localStorage.removeItem('user'); // Remove the user data
  router.push('/');
};
// Function to fetch user data
const FetchUser = async () => {
  try {
    const response = await axios.get(`${BASEURL}/user`);
    const { data } = response;
    userData.value = data.empty ? [] : data;
    console.log("User data:", userData.value);
  } catch (err) {
    console.log("Error fetching user:", err);
  }
};
onMounted( async () => {
  await FetchUser();
});
// Function to add a new user
const AddNewUser = async () => {
  if (!Form.value.username || !Form.value.email || !Form.value.password || !Form.value.address) {
    ElMessage.error("Please fill in all the required fields.");
    return;
  }
  try {
    isLoading.value = true;
    await axios.post(`${BASEURL}/user`, Form.value, {
      headers: { 'Content-Type': 'application/json' }
    });
    showDialog.value = false;
    resetForm();
    await new Promise(resolve => setTimeout(resolve, 3000));
    await FetchUser();
    ElMessage.success("New user created successfully");
  } catch (err) {
    console.log("Error adding new user:", err);
    ElMessage.error("Failed to add user.");
  } finally {
    isLoading.value = false;
  }
};

// Function to handle edit operation
const EditUser = (row) => {
  isEdit.value = true;
  showDialog.value = true;
  Form.value = { ...row}
};

// Function to handle update user
const UpdateUser = async () => {
  if (!Form.value.username || !Form.value.email || !Form.value.password || !Form.value.address) {
    ElMessage.error("Please fill in all the required fields.");
    return;
  }
  try {
    isLoading.value = true;
    const userid = Form.value.userid;
    await axios.put(`${BASEURL}/user`, Form.value, {
      headers: { 'Content-Type': 'application/json' },
      data: { userid }
    });
    showDialog.value = false;
    resetForm();
    await new Promise(resolve => setTimeout(resolve, 3000));
    await FetchUser();
    ElMessage.success("User updated successfully");
  } catch (err) {
    console.log('Error updating user:', err);
    ElMessage.error("Failed to update user.");
  } finally {
    isLoading.value = false;
  }
};

// Function to handle submit form
const handleSubmit = async () => {
  if (isEdit.value) {
    await UpdateUser();
  } else {
    await AddNewUser();
  }
};

// Function to handle delete with confirmation
const confirmDelete = (userid, username) => {
  let message = `Are you sure you want to delete this user: ${username} ?`;
  if (currentUserid.value === userid) {
    message = `Are you sure you want to delete your own account: ${username} ? This will log you out immediately.`;
  }
  ElMessageBox.confirm(message, 'Delete Confirmation', {
    confirmButtonText: 'Yes, Delete it.',
    type: 'warning',
    showCancelButton: true,
    center: true,
  })
      .then(() => DeleteUser(userid))
      .catch(() => ElMessage.info("Delete action cancelled"));
};

// Function to delete user
const DeleteUser = async (userid) => {
  try {
    isLoading.value = true;
    await axios.delete(`${BASEURL}/user`, {
      headers: { 'Content-Type': 'application/json' },
      data: { userid }
    });
    if (currentUserid.value === userid) {
      await new Promise(resolve => setTimeout(resolve, 2000));
      LogoutUser();
      ElMessage.success("Your account has been deleted. Logging out...");
      return;
    }
    await new Promise(resolve => setTimeout(resolve, 2000));
    await FetchUser();
    ElMessage.success("User deleted successfully");
  } catch (err) {
    console.log("Error deleting user:", err);
    ElMessage.error("Failed to delete user.");
  } finally {
    isLoading.value = false;
  }
};

// Function to handle view user
const ViewUser = (row) => {
  showViewDialog.value = true;
  viewForm.value = { ...row };
};

// Function to reset form fields
const resetForm = () => {
  Form.value = { userid: "", username: "", email: "", password: "", address: "" };
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
  return userData.value.filter(
      (user) =>
          user.userid.toString().toLowerCase().includes(lowerCaseSearch) ||
          user.username.toLowerCase().includes(lowerCaseSearch) ||
          user.email.toLowerCase().includes(lowerCaseSearch) ||
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

</script>

<style scoped>
.example-pagination-block {
  text-align: center;
  display: flex;
  align-items: center;
}
</style>
