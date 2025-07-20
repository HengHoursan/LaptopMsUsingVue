import { createApp } from "vue";
import ElementPlus from "element-plus";
import App from "./App.vue";
import "element-plus/dist/index.css";
import "./style.css";
import "element-plus/theme-chalk/dark/css-vars.css";
import router from './routes/index.js'
const app = createApp(App);
app.use(router);
app.use(ElementPlus);
app.mount("#app");
app.config.globalProperties.$BASEURL ='http://localhost:8080/LaptopMS';