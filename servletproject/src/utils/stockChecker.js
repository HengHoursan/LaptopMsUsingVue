import { ElNotification } from "element-plus";

export const checkStockAlert = (productData) => {
    if (!productData || productData.length === 0) return;

    // Prevent duplicate alerts (Check sessionStorage)
    if (sessionStorage.getItem("stockAlert")) return;

    productData.forEach((product) => {
        if (product.stock <= 0) {
            ElNotification({
                title: "Out of Stock",
                message: `Product "${product.model}" is out of stock!`,
                type: "error",
                duration: 5000,
            });
        } else if (product.stock <= 3) {
            ElNotification({
                title: "Warning",
                message: `Stock for product "${product.model}" is low (only ${product.stock} left)!`,
                type: "warning",
                duration: 5000,
            });
        }
    });
    sessionStorage.setItem("stockAlert", "true");
};
