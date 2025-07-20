<script setup>
import { ref, computed, onMounted, getCurrentInstance } from "vue";
import { UploadFilled } from '@element-plus/icons-vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';

const search = ref("");
const currentPage = ref(1);
const pageSize = ref(10);
const productData = ref([]);
const brandData = ref([]);
const categoryData = ref([]);

const showDialog = ref(false);
const viewDialog = ref(false);
const isEdit = ref(false);
const isLoading = ref(false);
const fileList = ref([]);
const selectedCategory = ref("")
const { appContext } = getCurrentInstance();
const BASEURL = appContext.config.globalProperties.$BASEURL;

const Form = ref({
  productId: "",
  brandId: "",
  categoryId: "",
  model: "",
  price: "",
  image: "",
  details: "",
  description: ""
});

const viewForm = ref({
  productId: "",
  brandName: "",
  categoryName: "",
  model: "",
  price: "",
  stock: "",
  image: "",
  details: "",
  description: ""
});

// Function to fetch product, brand, and category data
const FetchData = async () => {
  try {
    const [productResponse, brandResponse, categoryResponse,importResponse] = await Promise.all([
      axios.get(`${BASEURL}/product`),
      axios.get(`${BASEURL}/brand`),
      axios.get(`${BASEURL}/category`),

    ]);
    productData.value = productResponse.data.empty ? [] : productResponse.data;
    brandData.value = brandResponse.data.empty ? [] : brandResponse.data;
    categoryData.value = categoryResponse.data.empty ? [] : categoryResponse.data;
    console.log('Product data:', productData.value);
    console.log('Brand data:', brandData.value);
    console.log('Category data:', categoryData.value);
  } catch (err) {
    console.error("Error fetching data:", err);
  }
};
onMounted(async () => {
  await FetchData();
});

// Function to add a new product
const AddNewProduct = async () => {
  if (!Form.value.brandId || !Form.value.categoryId || !Form.value.model || !Form.value.price || !Form.value.image || !Form.value.details || !Form.value.description) {
    ElMessage.error("Please fill in all the required fields.");
    return;
  }
  const formData = new FormData();
  formData.append("brandId", Form.value.brandId);
  formData.append("categoryId", Form.value.categoryId);
  formData.append("model", Form.value.model?.trim() || '');
  formData.append("price", Form.value.price);
  formData.append("details", Form.value.details?.trim() || '');
  formData.append("description", Form.value.description?.trim() || '');
  if (Form.value.image) {
    formData.append("image", Form.value.image);
  }

  try {
    isLoading.value = true;
    await axios.post(`${BASEURL}/product`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    showDialog.value = false;
    ResetForm();
    await new Promise(resolve => setTimeout(resolve, 3000)); // Simulate some delay before re-fetch
    await FetchData();
    ElMessage.success("Product added successfully.");
  } catch (err) {
    console.log('Error adding product:', err);
    ElMessage.error("Failed to add product.");
  } finally {
    isLoading.value = false;
  }
};
// Edit product
const EditProduct = (row) => {
  const brand_id = brandData.value.find(b => b.name === row.brand?.name);
  const category_id = categoryData.value.find(c => c.name === row.category?.name);
  Form.value = {
    ...row,
    brandId: brand_id ? brand_id.brandId : null,
    categoryId: category_id ? category_id.categoryId : null,
  };
  const imageFileName = row.image.split(/\/|\\/).pop();
  fileList.value = [{
    name: imageFileName,
    url: `${BASEURL}/${row.image}`
  }];
  isEdit.value = true;
  showDialog.value = true;
  handlePreview();
};

// Function to update product
const UpdateProduct = async () => {
  if (!Form.value.productId) {
    ElMessage.error("Product ID is missing.");
    return;
  }
  if (!Form.value.brandId || !Form.value.categoryId || !Form.value.model || !Form.value.price || !Form.value.image || !Form.value.details || !Form.value.description) {
    ElMessage.error("Please fill in all the required fields.");
    return;
  }
  const formData = new FormData();
  formData.append("productId", Form.value.productId);
  formData.append("brandId", Form.value.brandId);
  formData.append("categoryId", Form.value.categoryId);
  formData.append("model", Form.value.model?.trim() || '');
  formData.append("price", Form.value.price);
  formData.append("details", Form.value.details?.trim() || '');
  formData.append("description", Form.value.description?.trim() || '');
  if (Form.value.image) {
    formData.append("image", Form.value.image);
  }
  try {
    isLoading.value = true;
    await axios.put(`${BASEURL}/product`, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    showDialog.value = false;
    await new Promise(resolve => setTimeout(resolve, 3000));
    await FetchData();
    ElMessage.success("Product updated successfully.");
  } catch (err) {
    console.log('Error updating product:', err);
    ElMessage.error("Failed to update product.");
  } finally {
    isLoading.value = false;
  }
};

// Function to handle form submission
const handleSubmit = async () => {
  if (isEdit.value) {
    await UpdateProduct();
  } else {
    await AddNewProduct();
  }
};
// View product details
const ViewProduct = (row) => {
  viewForm.value = {
    ...row,
    brandName: row.brand?.name,
    categoryName: row.category?.name,
    image: `${BASEURL}/${row.image}`
  };
  viewDialog.value = true;
};
// Confirm delete dialog for product
const confirmDelete = (productId, model) => {
  ElMessageBox.confirm(
      `Are you sure you want to delete this product: ${model} ?`,
      "Delete Confirmation",
      {
        confirmButtonText: "Yes, Delete it.",
        type: "warning",
        showCancelButton: true,
        center: true,
      }
  )
      .then(() => {
        deleteProduct(productId);
      })
      .catch(() => {
        ElMessage.info("Delete canceled.");
      });
};
// Delete product function
const deleteProduct = async (productId) => {
  try {
    isLoading.value = true;
    await axios.delete(`${BASEURL}/product`, {
      headers: { "Content-Type": "multipart/form-data" },
      data: { productId }
    });
    await new Promise((resolve) => setTimeout(resolve, 3000));
    await FetchData();
    ElMessage.success("Product deleted successfully!");
  } catch (error) {
    console.error("Error deleting product:", error);
    ElMessage.error("Failed to delete product.");
  } finally {
    isLoading.value = false;
  }
};
// Function to reset filters
const resetFilters = () => {
  search.value = "";
  selectedCategory.value = "";
};
// Computed property to filter and paginate data
const filteredData = computed(() => {
  const lowerCaseSearch = search.value.toLowerCase();
  return productData.value.filter((product) => {
    const matchesSearch =
        product.productId.toString().includes(lowerCaseSearch) ||
        product.brand?.name.toLowerCase().includes(lowerCaseSearch) ||
        product.model.toLowerCase().includes(lowerCaseSearch) ||
        product.category?.name.toLowerCase().includes(lowerCaseSearch);

    const matchesCategory =
        !selectedCategory.value || product.category?.name === selectedCategory.value;

    return matchesSearch && matchesCategory;
  });
});

// Compute paginated data based on filtered data
const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return filteredData.value.slice(start, start + pageSize.value);
});

// Reset form
const ResetForm = () => {
  Form.value = {
    productId: "",
    brandId: "",
    categoryId: "",
    model: "",
    price: "",
    image: "",
    details: "",
    description: "",
  };
  fileList.value = [];
};

// Function to handle image file change
const handleFileChange = (file) => {
  Form.value.image = file.raw;
};

// Preview uploaded file
const handlePreview = async (file) => {
  try {
    const url = URL.createObjectURL(file.raw);
    await ElMessageBox.alert(`<img src="${url}" style="max-width: 100%; padding: 1rem;"  />`, "Logo Preview", {
      dangerouslyUseHTMLString: true,
    });
  } catch (error) {
    // Handle error if needed
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

const handleClose = () => {
  showDialog.value = false;
  ResetForm();
};

// Handle page change
const handlePageChange = (newPage) => {
  currentPage.value = newPage;
};
</script>

<template>
  <!-- Filter Section -->
  <div style="margin-bottom: 20px; display: flex; gap: 20px; align-items: center;">
    <el-select
        v-model="selectedCategory"
        placeholder="Filter by Category"
        clearable
        @change="currentPage = 1"
    style="width: 200px;"
    >
    <el-option
        v-for="category in categoryData"
        :key="category.categoryId"
        :label="category.name"
        :value="category.name"
    />
    </el-select>
    <el-input
        v-model="search"
        size="default"
        placeholder="Type to search"
        style="width: 200px;"
    />
    <el-button @click="resetFilters">Reset</el-button>
  </div>
  <el-table :data="paginatedData">
    <el-table-column prop="productId" label="Product ID" width="150" sortable />
    <el-table-column prop="model" label="Model" width="250"/>
    <el-table-column prop="brand.name" label="Brand" width="150" />
    <el-table-column prop="category.name" label="Category" width="150"/>
    <el-table-column prop="image" label="Product Image" width="180" align="center">
      <template #default="{ row }">
        <el-image
            v-if="row.image"
            :src="`${BASEURL}/${row.image}`"
            alt="Product Image"
            fit="cover"
            style="height: 80px;"
        />
      </template>
    </el-table-column>
    <el-table-column label="Price" width="100">
      <template #default="{ row }">
        {{ `${row.price.toFixed(2)} $` }}
      </template>
    </el-table-column>
    <el-table-column prop="stock" label="Stock" width="100" align="center"/>
    <el-table-column label="Status" width="180" align="center">
      <template #default="{ row }">
        <el-tag effect="light" :type="row.stock === 0 ? 'danger' : 'success'">
          {{ row.stock === 0 ? 'No Stock Available' : 'In Stock' }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="details" label="Details" width="500"/>
    <el-table-column prop="description" label="Description" width="500"/>
    <el-table-column fixed="right" label="Operations" width="200">
      <template #default="{ row }">
        <el-button type="primary" link @click="ViewProduct(row)">View</el-button>
        <el-button type="primary" link @click="EditProduct(row)">Edit</el-button>
        <el-button
            type="danger"
            link
            @click="confirmDelete(row.productId, row.model)"
            v-loading.fullscreen.lock="isLoading"
        >
          Remove
        </el-button>
      </template>
    </el-table-column>
  </el-table>

  <div class="example-pagination-block">
    <el-pagination
        layout="prev, pager, next"
        :total="filteredData.length"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
    />
  </div>

  <el-button class="mt-4" style="width: 100%" @click="showDialog = true">
    Add New Product
  </el-button>

  <!-- View Product Dialog -->
  <el-dialog v-model="viewDialog" title="View Product">
    <el-form :model="viewForm" label-width="auto" style="max-width: 600px">
      <el-form-item label="Product ID">
        <el-input v-model="viewForm.productId" readonly />
      </el-form-item>
      <el-form-item label="Model">
        <el-input v-model="viewForm.model" readonly />
      </el-form-item>
      <el-form-item label="Brand">
        <el-input v-model="viewForm.brandName" readonly />
      </el-form-item>
      <el-form-item label="Category">
        <el-input v-model="viewForm.categoryName" readonly />
      </el-form-item>
      <el-form-item label="Price">
        <el-input v-model="viewForm.price" readonly />
      </el-form-item>
      <el-form-item label="Stock">
        <el-input v-model="viewForm.stock" readonly />
      </el-form-item>
      <el-form-item label="Product Image">
        <el-image :src="viewForm.image" alt="Product Image" fit="cover" style="width: 300px" />
      </el-form-item>
      <el-form-item label="Details">
        <el-input v-model="viewForm.details" type="textarea" readonly rows="5" />
      </el-form-item>
      <el-form-item label="Description">
        <el-input v-model="viewForm.description" type="textarea" readonly rows="5" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="viewDialog = false">Close</el-button>
    </template>
  </el-dialog>

  <!-- Add or Edit Product Dialog -->
  <el-dialog v-model="showDialog" :title="isEdit ? 'Edit Product' : 'Add New Product'" @close="handleClose">
    <el-form :model="Form" ref="productFormRef" label-width="auto" style="max-width: 600px">
      <el-form-item label="Model" prop="model" :rules="[{ required: true, message: 'Please enter a model name.', trigger: 'blur' }]">
        <el-input v-model="Form.model" />
      </el-form-item>
      <el-form-item label="Brand" prop="brandId" :rules="[{ required: true, message: 'Please select a brand.', trigger: 'blur' }]">
        <el-select v-model="Form.brandId" placeholder="Select Brand">
          <el-option v-for="brand in brandData" :key="brand.brandId" :label="brand.name" :value="brand.brandId" />
        </el-select>
      </el-form-item>
      <el-form-item label="Category" prop="categoryId" :rules="[{ required: true, message: 'Please select a category.', trigger: 'blur' }]">
        <el-select v-model="Form.categoryId" placeholder="Select Category">
          <el-option v-for="category in categoryData" :key="category.categoryId" :label="category.name" :value="category.categoryId" />
        </el-select>
      </el-form-item>
      <el-form-item label="Price" prop="price" :rules="[{ required: true, message: 'Please enter a price.', trigger: 'blur' }]">
        <el-input v-model="Form.price" type="number" min="0" />
      </el-form-item>
      <el-form-item label="Product Image" prop="image">
        <el-upload
            class="upload-demo"
            drag
            :auto-upload="false"
            style="width: 600px"
            :limit="1"
            :accept="'.jpg,.png'"
            :on-change="handleFileChange"
            :on-remove="handleRemove"
            :on-exceed="handleExceed"
            :on-preview="handlePreview"
            :file-list="fileList"
        >
          <el-icon class="el-icon--upload">
            <UploadFilled />
          </el-icon>
          <div class="el-upload__text">
            Drop file here or <em>{{ isEdit ? 'click to update the image' : 'click to upload an image' }}</em>
          </div>
        </el-upload>
      </el-form-item>
      <el-form-item label="Details" prop="details" :rules="[{ required: true, message: 'Please enter product details.', trigger: 'blur' }]">
        <el-input v-model="Form.details" type="textarea" rows="5" />
      </el-form-item>
      <el-form-item label="Description" prop="description" :rules="[{ required: true, message: 'Please enter product description.', trigger: 'blur' }]">
        <el-input v-model="Form.description" type="textarea" rows="5" />
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