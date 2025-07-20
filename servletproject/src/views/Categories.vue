<template>
<el-table :data="paginatedData">
<el-table-column prop="categoryId" label="Category ID" width="150" sortable/>
<el-table-column prop="name" label="Category Name" width="200" sortable/>
<el-table-column prop="description" label="Description" width="700"/>
<el-table-column fixed="right" label="Operations" min-width="200">
  <template #default="{ row }">
    <el-button link type="primary" @click="ViewCategory(row)">View</el-button>
    <el-button link type="primary" @click="EditCategory(row)">Edit</el-button>
    <el-button link type="danger"
               @click="confirmDelete(row.categoryId,row.name)"
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
        :total="categoryData.length"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
    />
  </div>
<el-button class="mt-4" style="width: 100%" @click="showDialog = true"
>Add New Category
</el-button>
<!--  dialog for add/edit-->
<el-dialog
    v-model="showDialog"
    :title="isEdit ? 'Edit Category' : 'Add New Category'"
>
<el-form
    ref="ruleForm"
    style="max-width: 600px"
    :model="Form"
    status-icon
    label-width="auto"
>
  <el-form-item
      label="Category Name"
      prop="name"
      :rules="[
        { required: true, message: 'Please input the category name', trigger: 'blur' }
      ]"
  >
    <el-input v-model="Form.name"/>
  </el-form-item>

  <el-form-item
      label="Description"
      prop="description"
      :rules="[
        { required: true, message: 'Please input the description', trigger: 'blur' }
      ]"
  >
    <el-input
        v-model="Form.description"
        maxlength="255"
        show-word-limit
        type="textarea"
        rows="4"
    />
  </el-form-item>
</el-form>
<template #footer>
      <span class="dialog-footer">
        <el-button @click="handleCancel">Cancel</el-button>
        <el-button
            type="primary"
            @click="handleSubmit"
            v-loading.fullscreen.lock="isLoading"
        >
          {{ isEdit ? "Update" : "Create" }}
        </el-button>
      </span>
</template>
</el-dialog>
<!--  dialog for view-->
<el-dialog v-model="showViewDialog" title="View Categories">
<el-form style="max-width: 600px;" :model="viewForm" label-width="auto">
  <el-form-item label="Category ID">
    <el-input v-model="viewForm.categoryId" readonly/>
  </el-form-item>
  <el-form-item label="Category Name">
    <el-input v-model="viewForm.name" readonly/>
  </el-form-item>
  <el-form-item label="Description">
    <el-input
        v-model="viewForm.description"
        maxlength="255"
        show-word-limit
        type="textarea"
        rows="4"
        disabled
    />
  </el-form-item>
</el-form>
<template #footer>
      <span class="dialog-footer">
        <el-button @click="showViewDialog = false">Close</el-button>
      </span>
</template>
</el-dialog>
</template>
<script setup>
import { ref, computed, onMounted,getCurrentInstance} from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import axios from "axios";
const categoryData = ref([]);
const search = ref("");
const currentPage = ref(1);
const pageSize = ref(10);
const showDialog = ref(false);
const showViewDialog = ref(false)
const isLoading = ref(false);
const isEdit = ref(false);
const { appContext } = getCurrentInstance();
const BASEURL = appContext.config.globalProperties.$BASEURL;
const Form = ref({
  categoryId: "",
  name: "",
  description: ""
});
const viewForm = ref({
  categoryId: "",
  name: "",
  description: ""
});
// Function to fetch category data
const FetchCategory = async () => {
  try {
    const response = await axios.get(`${BASEURL}/category`);
    const { data } = response;
    categoryData.value = data.empty ? [] : data;
    console.log("category data:", categoryData.value);
  } catch (err) {
    console.log("Error fetching category:", err);
  }
};
//Function to add a new category
const AddNewCategory = async () => {
  if (!Form.value.name || !Form.value.description) {
    ElMessage.error("Please fill in all the required fields.");
    return;
  }
  try {
    isLoading.value = true;
    await axios.post(`${BASEURL}/category`, Form.value, {
      headers: { 'Content-Type': 'application/json' }
    });
    showDialog.value = false;
    resetForm();
    await new Promise(resolve => setTimeout(resolve, 3000));
    await FetchCategory();
    ElMessage.success("New category created successfully");
  } catch (err) {
    console.log("Error adding new category:", err);
    ElMessage.error("Failed to adding new category.");
  } finally {
    isLoading.value = false;
  }
};
// Function to handle view category
const ViewCategory = (row) => {
  showViewDialog.value = true;
  viewForm.value = { ...row };
};
// Function to handle edit operation
const EditCategory = (row) => {
  isEdit.value = true;
  showDialog.value = true;
  Form.value = { ...row}
};
// Function to handle update category
const UpdateCategory = async () => {
  if (!Form.value.name || !Form.value.description) {
    ElMessage.error("Please fill in all the required fields.");
    return;
  }
  try {
    isLoading.value = true;
    const categoryId = Form.value.categoryId;
    await axios.put(`${BASEURL}/category`, Form.value, {
      headers: { 'Content-Type': 'application/json' },
      // data: { categoryId }
    });
    showDialog.value = false;
    resetForm();
    await new Promise(resolve => setTimeout(resolve, 3000));
    await FetchCategory();
    ElMessage.success("Category updated successfully");
  } catch (err) {
    console.log('Error updating category:', err);
    ElMessage.error("Failed to update category.");
  } finally {
    isLoading.value = false;
  }
};
// Function to handle delete with confirmation
const confirmDelete = (categoryId, name) => {
  ElMessageBox.confirm(
      `Are you sure you want to delete this category: ${name} ?`,
      'Delete Confirmation',
      {
        confirmButtonText: 'Yes, Delete it.',
        type: 'warning',
        showCancelButton: true,
        center: true,
      }
  )
      .then(() => DeleteCategory(categoryId))
      .catch(() => ElMessage.info("Delete action cancelled"));
};
//Function to delete category
const DeleteCategory = async (categoryId) => {
  try {
    isLoading.value = true;
    await axios.delete(`${BASEURL}/category`, {
      headers: { 'Content-Type': 'application/json' },
      data: { categoryId }
    });
    await new Promise(resolve => setTimeout(resolve, 3000));
    await FetchCategory();
    ElMessage.success("Category deleted successfully");
  } catch (err) {
    console.log("Error deleting category:", err);
    ElMessage.error("Failed to delete category.");
  } finally {
    isLoading.value = false;
  }
};

onMounted( async () => {
  await FetchCategory();
});
//Function to handle submit form
const handleSubmit = async () => {
  if (isEdit.value) {
    await UpdateCategory();
  } else {
    await AddNewCategory();
  }
};
//Function to reset form fields
const resetForm = () => {
  Form.value = { categoryId: "", name: "", description: ""};
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
  return categoryData.value.filter(
      (category) =>
          category.categoryId.toString().toLowerCase().includes(lowerCaseSearch) ||
          category.name.toLowerCase().includes(lowerCaseSearch)
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
<style scoped></style>
