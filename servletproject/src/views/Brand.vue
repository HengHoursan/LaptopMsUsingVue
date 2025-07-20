<script setup>
import { ref, computed, onMounted,getCurrentInstance} from "vue";
import {UploadFilled} from '@element-plus/icons-vue';
import axios from 'axios';
import {ElMessage, ElMessageBox} from 'element-plus';
const search = ref("");
const currentPage = ref(1);
const pageSize = ref(10);
const brandData = ref([]);
const showDialog = ref(false);
const viewDialog = ref(false);
const isEdit = ref(false);
const isLoading = ref(false);
const fileList = ref([]);
const { appContext } = getCurrentInstance();
const BASEURL = appContext.config.globalProperties.$BASEURL;
const Form = ref({
  brandId: '',
  name: '',
  description: '',
  image: '',
});
// Data for the view dialog
const viewForm = ref({
  brandId: '',
  name: '',
  description: '',
  image: ''
});
// Function to fetch category data
const FetchBrand = async () => {
  try {
    const response = await axios.get(`${BASEURL}/brand`);
    const {data} = response;
    brandData.value = data.empty ? [] : data;
    console.log("Brand data:", brandData.value);
  } catch (err) {
    console.log("Error fetching brand:", err);
  }
};
//Function to add new brand
const AddNewBrand = async () => {
  if (!Form.value.name || !Form.value.description || !Form.value.image) {
    ElMessage.error("Please fill in all the required fields.");
    return;
  }
  // Create FormData and append data
  const formData = new FormData();
  formData.append("name", Form.value.name);
  formData.append("description", Form.value.description);
  formData.append("image", Form.value.image);

  try {
    isLoading.value = true;
    await axios.post(`${BASEURL}/brand`, formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
    showDialog.value = false;
    await new Promise((resolve) => setTimeout(resolve, 3000));
    await FetchBrand();
    ElMessage.success("Brand added successfully!");
  } catch (err) {
    console.error("Error adding brand:", err);
    ElMessage.error("Failed to add brand.");
  }
  finally{
    isLoading.value = false;
  }
};
// View brand details
const ViewBrand = (row) => {
  viewForm.value = {
    name: row.name,
    description: row.description,
    image: `${BASEURL}/${row.image}`,
  };
  viewDialog.value = true;
};

// Edit brand
const EditBrand = (row) => {
  Form.value = {
    brandId: row.brandId,
    name: row.name,
    description: row.description,
  };
  const imageFileName = row.image.split(/\/|\\/).pop();
  fileList.value = [{
    name: imageFileName,
    url: `${BASEURL}/${row.image}`
  }];
  isEdit.value = true;
  showDialog.value = true;
  handlePreview();
  isEdit.value = true;
  showDialog.value = true;
};
//Function to update brand
const UpdateBrand = async () => {
  if (!Form.value.brandId) {
    ElMessage.error("Brand ID is missing.");
    return;
  }
  if (!Form.value.name || !Form.value.description || !Form.value.image) {
    ElMessage.error("Please fill in all the required fields.");
    return;
  }
  const formData = new FormData();
  formData.append("brandId",Form.value.brandId);
  formData.append("name", Form.value.name);
  formData.append("description", Form.value.description);
  if (Form.value.image) formData.append("image", Form.value.image);

  try {
    isLoading.value = true;
    await axios.put(`${BASEURL}/brand`, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    showDialog.value = false;
    resetForm();
    await new Promise((resolve) => setTimeout(resolve,3000))
    await FetchBrand();
    ElMessage.success("Brand updated successfully!");
  } catch (error) {
    console.error("Error updating brand:", error);
    ElMessage.error("Failed to update brand.");
  } finally {
    isLoading.value = false;
  }
};
// Confirm delete dialog
const confirmDelete = (brandId, name) => {
  ElMessageBox.confirm(
      `Are you sure you want to delete this brand: ${name} ?`,
      "Delete Confirmation",
      {
        confirmButtonText: "Yes, Delete it.",
        type: "warning",
        showCancelButton: true,
        center: true,
      }
  )
      .then(() => {
        DeleteBrand(brandId);
      })
      .catch(() => {
        ElMessage.info("Delete canceled.");
      });
};

// Delete brand function
const DeleteBrand = async (brandId) => {
  try {
    isLoading.value = true;
    await axios.delete(`${BASEURL}/brand`,{
      headers: { "Content-Type": "multipart/form-data" },
      data: { brandId }
    });
    await new Promise((resolve) => setTimeout(resolve, 3000));
    await FetchBrand();
    ElMessage.success("Brand deleted successfully!");
  } catch (error) {
    console.error("Error deleting brand:", error);
    ElMessage.error("Failed to delete brand.");
  } finally {
    isLoading.value = false;
  }
};

onMounted( async () => {
  await FetchBrand();
})
// Handle form submission
const handleSubmit = async () => {
  if (isEdit.value) {
    await UpdateBrand();
  } else {
    await AddNewBrand();
  }
};
// Handle file upload for the logo
const handleFileChange = (file) => {
  if (file.size > 5 * 1024 * 1024) { // Check file size (5MB limit)
    ElMessage.error('File size exceeds 5MB limit.');
    return;
  }
  Form.value.image = file.raw; // Store the raw File object
};
// Preview uploaded file
const handlePreview = async (file) => {
  try {
    const url = URL.createObjectURL(file.raw);
    await ElMessageBox.alert(`<img src="${url}" style="max-width: 100%;" />`, "Logo Preview", {
      dangerouslyUseHTMLString: true,
    });
  } catch (error) {
  }
};
// Function to remove the uploaded file
const handleRemove = () => {
  Form.value.image = "";
  fileList.value = [];
};

// Function to warn when the upload limit is exceeded
const handleExceed = () => {
  ElMessage.error('Only one file allowed.');
};
// Reset the form after add/edit
const resetForm = () => {
  Form.value = {brandId: '', name: '', image: '', description: ''};
  fileList.value = [];
};
const handleClose = () => {
  showDialog.value = false;
  resetForm();
};
// Computed property to filter and paginate data
const filteredData = computed(() => {
  const lowerCaseSearch = search.value.toLowerCase();
  return brandData.value.filter(
      (brand) =>
          brand.brandId.toString().toLowerCase().includes(lowerCaseSearch) ||
          brand.name.toLowerCase().includes(lowerCaseSearch)
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
<template>
  <el-table :data="paginatedData">
    <el-table-column prop="brandId" label="Brand ID" width="150" sortable/>
    <el-table-column prop="name" label="Brand Name" width="150" sortable/>
    <el-table-column prop="image" label="Brand Logo" width="150" align="center">
      <template #default="{ row }">
        <el-image
            v-if="row.image"
            :src="`${BASEURL}/${row.image}`"
            alt="Brand Logo"
            fit="cover"
            style="height: 100px;"
        />
      </template>
    </el-table-column>
    <el-table-column prop="description" label="Description" width="700" sortable/>
    <el-table-column fixed="right" label="Operations" min-width="200">
      <template #default="{row}">
        <el-button link type="primary" @click="ViewBrand(row)">View</el-button>
        <el-button link type="primary" @click="EditBrand(row)">Edit</el-button>
        <el-button
            link
            type="danger"
            @click="confirmDelete(row.brandId, row.name)"
            v-loading.fullscreen.lock="isLoading"
        >
          Remove
        </el-button>
      </template>
    </el-table-column>
    <!-- Search Input -->
    <el-table-column min-width="200" fixed="right" align="center">
      <template #header>
        <el-input v-model="search" size="default" placeholder="Type to search" style="min-width: 150px" />
      </template>
    </el-table-column>
  </el-table>
  <div class="example-pagination-block">
    <el-pagination
        layout="prev, pager, next"
        :total="brandData.length"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
    />
  </div>
  <el-button class="mt-4"
             style="width: 100%"
             @click="showDialog=true"
  >
    Add New Brand
  </el-button>
<!--view brand dialog-->
  <el-dialog v-model="viewDialog"
             title="View Brand">
    <el-form :model="viewForm"
             label-width="auto"
             style="max-width: 600px">
      <el-form-item label="Brand ID">
        <el-input v-model="viewForm.brandId" readonly/>
      </el-form-item>
      <el-form-item label="Brand Name">
        <el-input v-model="viewForm.name" readonly/>
      </el-form-item>
      <el-form-item label="Description">
        <el-input v-model="viewForm.description"
                  type="textarea"
                  disabled rows="4"/>
      </el-form-item>
      <el-form-item label="Brand Logo">
        <el-image :src="viewForm.logo" alt="Brand Logo" fit="cover"
                  style="width: 380px"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="viewDialog = false">Close</el-button>
    </template>
  </el-dialog>

  <!-- Add/Edit Brand Dialog -->
  <el-dialog v-model="showDialog"  :title="isEdit ? 'Edit Brand' : 'Add New Brand'" @close="handleClose">
    <el-form ref="ruleForm"
             style="max-width: 600px"
             :model="Form"
             :status-icon="true"
             label-width="auto"
    >
      <el-form-item label="Brand Name"
                    prop="name"
                    :rules="[{ required: true, message: 'Please input the brand name', trigger: 'blur' }]">
        <el-input v-model="Form.name"/>
      </el-form-item>
      <el-form-item label="Description"
                    prop="description"
                    :rules="[{ required: true, message: 'Please input the description', trigger: 'blur' }]">
        <el-input v-model="Form.description"
                  maxlength="255"
                  show-word-limit
                  type="textarea"
                  rows="4"/>
      </el-form-item>
      <el-form-item label="Brand Logo">
        <el-upload
            class="upload-demo"
            drag
            :auto-upload="false"
            style="width: 500px"
            :limit="1"
            action="https://run.mocky.io/v3/9d059bf9-4660-45f2-925d-ce80ad6c4d15"
            :on-change="handleFileChange"
            :on-preview="handlePreview"
            :on-remove="handleRemove"
            :on-exceed="handleExceed"
            :accept="'.jpg,.png'"
            :file-list="fileList"
            ref="uploadRef"
        >
          <el-icon class="el-icon--upload">
            <upload-filled/>
          </el-icon>
          <div class="el-upload__text">
            Drop file here or <em>{{ !isEdit ? 'click to upload an image' : 'click to update the image' }}</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              jpg/png files with a size less than 5MB
            </div>
          </template>
        </el-upload>
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
<style scoped></style>
