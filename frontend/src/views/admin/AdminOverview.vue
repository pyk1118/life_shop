<template>
  <div class="overview-page">
    <h3 class="page-title">数据概览</h3>
    <el-row :gutter="16" class="stat-row">
      <el-col :span="8" v-for="item in statCards" :key="item.label">
        <div class="stat-card">
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </div>
      </el-col>
    </el-row>

    <h3 class="page-title" style="margin-top: 5px">数据图表</h3>
    <el-row :gutter="16">
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title">平台数据统计</div>
          <div ref="barChartRef" class="chart-container"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title-row">
            <span class="chart-title">销售额趋势</span>
            <el-radio-group v-model="salesChartMode" size="small" @change="updateSalesChart">
              <el-radio-button value="monthly">按月</el-radio-button>
              <el-radio-button value="daily">按日</el-radio-button>
            </el-radio-group>
          </div>
          <div ref="salesChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <h3 class="page-title" style="margin-top: 5px">待办事项</h3>
    <el-row :gutter="16">
      <el-col :span="8" v-for="item in todoCards" :key="item.label">
        <div class="todo-card" @click="item.action">
          <div class="todo-value">{{ item.value }}</div>
          <div class="todo-label">{{ item.label }}</div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { getDashboard } from '../../api/admin';
import * as echarts from 'echarts';

const router = useRouter();
const dashboard = ref({});
const statCards = ref([]);
const todoCards = ref([]);
const barChartRef = ref(null);
const salesChartRef = ref(null);
const salesChartMode = ref('monthly');
let barChart = null;
let salesChart = null;

function initBarChart(d) {
  if (!barChartRef.value) {
    return;
  }
  barChart = echarts.init(barChartRef.value);
  barChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 60, right: 30, top: 30, bottom: 40 },
    xAxis: {
      type: 'category',
      data: ['用户数', '商家数', '商品数', '订单总量'],
      axisLabel: { fontSize: 13 }
    },
    yAxis: { type: 'value', axisLabel: { fontSize: 12 } },
    series: [{
      type: 'bar',
      data: [
        { value: d.userCount || 0, itemStyle: { color: '#165dff' } },
        { value: d.merchantCount || 0, itemStyle: { color: '#722ed1' } },
        { value: d.goodsCount || 0, itemStyle: { color: '#0fc6c2' } },
        { value: d.orderCount || 0, itemStyle: { color: '#f77234' } }
      ],
      barWidth: 40,
      label: { show: true, position: 'top', fontSize: 13, fontWeight: 'bold' }
    }]
  });
}

function getSalesChartOption(d) {
  const isMonthly = salesChartMode.value === 'monthly';
  const data = isMonthly ? (d.monthlySales || []) : (d.dailySales || []);
  const xData = data.map(m => isMonthly ? m.month : m.date);
  const salesData = data.map(m => m.sales || 0);
  const orderData = data.map(m => m.orderCount || 0);

  return {
    tooltip: {
      trigger: 'axis',
      formatter(params) {
        let result = params[0].axisValue + '<br/>';
        params.forEach(p => {
          const val = p.seriesName === '销售额(元)' ? '¥' + p.value : p.value;
          result += p.marker + p.seriesName + ': ' + val + '<br/>';
        });
        return result;
      }
    },
    legend: { data: ['销售额(元)', '订单量'], top: 0 },
    grid: { left: 70, right: 60, top: 40, bottom: 40 },
    xAxis: {
      type: 'category',
      data: xData,
      axisLabel: {
        fontSize: isMonthly ? 12 : 10,
        rotate: isMonthly ? 0 : 45
      }
    },
    yAxis: [
      { type: 'value', name: '销售额(元)', axisLabel: { fontSize: 12 } },
      { type: 'value', name: '订单量', axisLabel: { fontSize: 12 } }
    ],
    series: [
      {
        name: '销售额(元)',
        type: 'bar',
        data: salesData,
        barWidth: isMonthly ? 30 : 8,
        itemStyle: { color: '#165dff' },
        label: { show: false }
      },
      {
        name: '订单量',
        type: 'line',
        yAxisIndex: 1,
        data: orderData,
        itemStyle: { color: '#f77234' },
        lineStyle: { width: 2 },
        symbolSize: isMonthly ? 8 : 4,
        label: { show: false }
      }
    ]
  };
}

function initSalesChart(d) {
  if (!salesChartRef.value) {
    return;
  }
  salesChart = echarts.init(salesChartRef.value);
  salesChart.setOption(getSalesChartOption(d));
}

function updateSalesChart() {
  if (salesChart && dashboard.value) {
    salesChart.setOption(getSalesChartOption(dashboard.value), true);
  }
}

function handleResize() {
  barChart && barChart.resize();
  salesChart && salesChart.resize();
}

onMounted(async () => {
  const res = await getDashboard();
  dashboard.value = res.data;
  const d = res.data;
  statCards.value = [
    { label: '用户总数', value: d.userCount },
    { label: '商家总数', value: d.merchantCount },
    { label: '商品总数', value: d.goodsCount },
    { label: '订单总量', value: d.orderCount },
    { label: '平台总销售额', value: '¥' + (d.totalSales || 0) },
    { label: '留言总数', value: d.messageCount }
  ];
  todoCards.value = [
    { label: '待审核入驻申请', value: d.pendingAuditCount, action: () => router.push('/admin/merchant-audit') },
    { label: '待处理售后', value: d.abnormalOrderCount, action: () => router.push('/admin/after-sale') },
    { label: '未回复留言', value: d.unrepliedMessageCount, action: () => router.push('/admin/message') }
  ];
  await nextTick();
  initBarChart(d);
  initSalesChart(d);
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  barChart && barChart.dispose();
  salesChart && salesChart.dispose();
});
</script>

<style scoped lang="less">
.overview-page {
  .page-title {
    font-size: 20px;
    font-weight: 600;
    color: #1d2129;
    margin: 0 0 16px;
  }

  .stat-row {
    .stat-card {
      background: #ffffff;
      border-radius: 12px;
      padding: 24px 20px;
      text-align: center;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      transition: all 0.2s ease-in-out;
      margin-bottom: 12px;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
      }

      .stat-value {
        font-size: 28px;
        font-weight: 700;
        background: linear-gradient(90deg, #165dff 0%, #722ed1 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
      }

      .stat-label {
        font-size: 14px;
        color: #86909c;
        margin-top: 8px;
      }
    }
  }

  .chart-card {
    background: #ffffff;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    margin-bottom: 12px;
    height: 360px;

    .chart-title-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
    }

    .chart-title {
      font-size: 16px;
      font-weight: 600;
      color: #1d2129;
    }

    .chart-container {
      width: 100%;
      height: 300px;
    }
  }

  .todo-card {
    background: #ffffff;
    border-radius: 12px;
    padding: 24px 20px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    cursor: pointer;
    transition: all 0.2s ease-in-out;
    margin-bottom: 12px;
    border-left: 4px solid #ff7d00;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    }

    .todo-value {
      font-size: 28px;
      font-weight: 700;
      color: #ff7d00;
    }

    .todo-label {
      font-size: 14px;
      color: #86909c;
      margin-top: 8px;
    }
  }
}
</style>