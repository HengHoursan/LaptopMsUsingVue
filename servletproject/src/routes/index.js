import BaseLayout from "../layout/BaseLayout.vue";
import Authentication from "../auth/Authentication.vue";
import Dashboard from "../views/Dashboard.vue";
import Categories from "../views/Categories.vue";
import ProductList from "../views/ProductList.vue";
import UserList from "../views/UserList.vue";
import Brand from "../views/Brand.vue";
import CustomerList from "../views/CustomerList.vue";
import EmployeeList from "../views/EmployeeList.vue";
import SaleList from "../views/SaleList.vue";
import Import from "../views/Import.vue";
import ProductReport from "../views/ProductReport.vue";
import SaleReport from "../views/SaleReport.vue";
import ImportReport from "../views/ImportReport.vue";
import LayoutNotFound from "../components/LayoutNotFound.vue";
import PageNotFound from "../components/PageNotFound.vue";
import {createRouter, createWebHistory} from "vue-router";

const routes = [
    {
        path: "/",
        name: "Authentication",
        component: Authentication,
    },
    {
        path: "/layout",
        name: "Layout",
        component: BaseLayout,
        children: [
            {
                path: "dashboard",
                name: "Dashboard",
                component: Dashboard,
                meta: {requiresAuth: true},
            },
            {
                path: "user",
                name: "UserList",
                component: UserList,
                meta: {requiresAuth: true},
            },
            {
                path: "employee",
                name: "EmployeeList",
                component: EmployeeList,
                meta: {requiresAuth: true},
            },
            {
                path: "customer",
                name: "CustomerList",
                component: CustomerList,
                meta: {requiresAuth: true},
            },
            {
                path: "brands",
                name: "Brand",
                component: Brand,
                meta: {requiresAuth: true},
            },
            {
                path: "import",
                name: "Import",
                component: Import,
                meta: {requiresAuth: true},
            },
            {
                path: "category",
                name: "Category",
                component: Categories,
                meta: {requiresAuth: true},
            },
            {
                path: "product",
                name: "ProductList",
                component: ProductList,
                meta: {requiresAuth: true},
            },
            {
                path: "sales",
                name: "SaleList",
                component: SaleList,
                meta: {requiresAuth: true},
            },
            {
                path: "productReport",
                name: "ProductReport",
                component: ProductReport,
                meta: {requiresAuth: true},
            },
            {
                path: "importReport",
                name: "ImportReport",
                component: ImportReport,
                meta: {requiresAuth: true},
            },
            {
                path: "saleReport",
                name: "SaleReport",
                component: SaleReport,
                meta: {requiresAuth: true},
            },
            {
                path: ":pathMatch(.*)*",
                name: "LayoutNotFound",
                component: LayoutNotFound,
                meta: {requiresAuth: true},
            },
        ],

    },
    {
        path: "/:pathMatch(.*)*",
        name: "PageNotFound",
        component: PageNotFound,
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes
});
router.beforeEach((to, from, next) => {
    const isAuthenticated = localStorage.getItem("user");
    if (to.meta.requiresAuth && !isAuthenticated) {
        next("/");
    } else {
        next();
    }
});
export default router;