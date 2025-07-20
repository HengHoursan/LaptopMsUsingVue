<template>
  <div :class="{'dark-mode': isDark}">
    <div ref="chartRef" style="width: 100%; height: 400px;"></div>
  </div>
</template>

<script setup>
import {ref, onMounted, getCurrentInstance} from 'vue';
import * as echarts from 'echarts/core';
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent
} from 'echarts/components';
import {PieChart} from 'echarts/charts';
import {LabelLayout} from 'echarts/features';
import {CanvasRenderer} from 'echarts/renderers';
import axios from "axios";

echarts.use([
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  PieChart,
  CanvasRenderer,
  LabelLayout
]);

const chartRef = ref(null);
const saleData = ref([]);
const productData = ref([]);
const {appContext} = getCurrentInstance();
const BASEURL = appContext.config.globalProperties.$BASEURL;

// Fetch data from the API
const FetchData = async () => {
  try {
    const [saleResponse, productResponse] = await Promise.all([
      axios.get(`${BASEURL}/sale`),
      axios.get(`${BASEURL}/product`),
    ]);

    saleData.value = saleResponse.data.empty ? [] : saleResponse.data;
    productData.value = productResponse.data.empty ? [] : productResponse.data;

    console.log('Sale data:', saleData.value);
    console.log('Product data:', productData.value);

    // Generate the chart after data is fetched
    generateChart();
  } catch (err) {
    console.error("Error fetching data:", err);
  }
};

// Process sale data and calculate sales by category
const processSalesByCategory = () => {
  const categorySales = {};

  // Loop through saleData and aggregate sales by category
  saleData.value.forEach((sale) => {
    // Find the corresponding product in productData by model
    const productInfo = productData.value.find(
        (p) => p.model === sale.product.model
    );
    // If the product is found and has a category
    if (productInfo && productInfo.category) {
      const categoryName = productInfo.category.name;

      if (!categorySales[categoryName]) {
        categorySales[categoryName] = 0;
      }
      categorySales[categoryName] += sale.quantity;
    }
  });
  // Convert to the format required for the pie chart
  return Object.keys(categorySales).map(category => ({
    name: category,
    value: categorySales[category]
  }));
};

// Generate the pie chart
const generateChart = () => {
  if (chartRef.value) {
    const myChart = echarts.init(chartRef.value);
    const categoryData = processSalesByCategory();
    const option = {
      title: {
        text: 'Quantity Sold by Category',
        left: 'center'
      },
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)'

      },
      legend: {
        orient: 'vertical',
        left: 'right',
      },
      series: [
        {
          name: 'Sales by Category',
          type: 'pie',
          radius: '70%',
          data: categoryData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    };
    myChart.setOption(option);
  }
};

onMounted(() => {
  FetchData();
});
</script>

<style>
#chart {
  width: 100%;
  height: 400px;
}

</style>
