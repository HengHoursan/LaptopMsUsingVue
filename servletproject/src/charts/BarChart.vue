<template>
  <div ref="chartRef" style="width: 100%; height: 400px;"></div>
</template>

<script setup>
import {ref, onMounted, watch, getCurrentInstance} from 'vue';
import axios from 'axios';
import * as echarts from 'echarts/core';
import { TooltipComponent, GridComponent } from 'echarts/components';
import { BarChart } from 'echarts/charts';
import { CanvasRenderer } from 'echarts/renderers';
import moment from 'moment';

echarts.use([TooltipComponent, GridComponent, BarChart, CanvasRenderer]);
const chartRef = ref(null);
const saleData = ref([]);
const {appContext} = getCurrentInstance();
const BASEURL = appContext.config.globalProperties.$BASEURL;

// Function to fetch sale data
const FetchData = async () => {
  try {
    const response = await axios.get(`${BASEURL}/sale`);
    saleData.value = response.data || [];
    updateChart();
  } catch (error) {
    console.error("Error fetching sales data:", error);
  }
};
// Function to process sales data for the bar chart
const processChartData = () => {
  const monthlySales = Array(12).fill(0);

  saleData.value.forEach((sale) => {
    const monthIndex = moment(sale.saleDate).month();
    monthlySales[monthIndex] += parseFloat(sale.totalPrice);
  });
  return monthlySales;
};

// Function to update the chart
const updateChart = () => {
  if (!chartRef.value) return;

  const myChart = echarts.init(chartRef.value);
  const chartData = processChartData();

  const option = {
    title: {
      text: 'Monthly Sales Overview',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
    },
    grid: {
      left: '1%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: [{
      type: 'category',
      data: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
      axisTick: { alignWithLabel: true },
    }],
    yAxis: [{ type: 'value' }],
    series: [{
      name: 'Total Price ',
      type: 'bar',
      barWidth: '60%',
      data: chartData,
    }],
  };

  myChart.setOption(option);
};

// Fetch data on component mount
onMounted(FetchData);

// Watch for saleData updates and refresh the chart
watch(saleData, updateChart);
</script>

<style scoped>
</style>
