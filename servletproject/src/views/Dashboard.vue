<template>
  <div class="main-container">
    <div class="dashboard-container">
      <div class="statistic-section">
        <Statistic />
      </div>
      <div class="charts-section">
        <div class="bar-chart">
          <BarChart />
        </div>
        <div class="pie-chart">
          <PieChart />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import BarChart from "../charts/BarChart.vue";
import PieChart from "../charts/PieChart.vue";
import Statistic from "../charts/Statistic.vue";
import UserList from "./UserList.vue";
</script>

<style scoped>
.main-container {
  display: flex;
  flex-direction: column;
  gap: 50px;
  padding: 10px;
  max-width: 1900px;
  margin: 0 auto;
}

.dashboard-container {
  display: flex;
  flex-direction: column;
  gap: 50px;
}

.statistic-section {
  margin-bottom: 20px;
  width: 100%;
}

.charts-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 50px;
}

.bar-chart,
.pie-chart {
  width: 100%;
  height: 400px;
}
.bar-chart {
  margin-top: 35px;
}
.pie-chart {
  margin: 40px 0 50px 0;
}

.user-list-section {
  width: 100%;
}
</style>