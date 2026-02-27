<template>
  <div class="wrap">
    <h1>校园环境监测系统</h1>

    <div class="panel">
      <label>
        type：
        <select v-model="type">
          <option value="air">air</option>
          <option value="soil">soil</option>
          <option value="water">water</option>
        </select>
      </label>

      <label>
        metric：
        <select v-model="metric">
          <option v-for="m in metricOptions" :key="m.value" :value="m.value">
            {{ m.label }}
          </option>
        </select>
      </label>

      <label>
        start：
        <input type="datetime-local" v-model="startStr" />
      </label>

      <label>
        end：
        <input type="datetime-local" v-model="endStr" />
      </label>

      <label>
        devid：
        <input v-model="devid" placeholder="例如 cfec_air_0001" />
      </label>

      <button @click="loadTrend" :disabled="loading">
        {{ loading ? "查询中..." : "查询趋势" }}
      </button>
    </div>

    <div class="hint" v-if="errorMsg">{{ errorMsg }}</div>

    <div v-if="metric === 'airPM25' && aqiResult" class="aqi-panel" :style="{ borderColor: aqiResult.color }">
      <div class="aqi-header" :style="{ backgroundColor: aqiResult.color, color: aqiResult.textColor }">
        <div class="aqi-left">
          <span class="aqi-score">{{ aqiResult.value }}</span>
          <span class="aqi-badge">AQI</span>
        </div>
        <div class="aqi-right">
          <div class="aqi-level">{{ aqiResult.level }} {{ aqiResult.label }}</div>
          <div class="aqi-conc">PM2.5 平均浓度: {{ aqiResult.avgConc }} μg/m³</div>
        </div>
      </div>
      <div class="aqi-body">
        <div class="aqi-row">
          <span class="label">健康影响：</span>
          <span class="text">{{ aqiResult.health }}</span>
        </div>
        <div class="aqi-row">
          <span class="label">建议措施：</span>
          <span class="text">{{ aqiResult.measure }}</span>
        </div>
      </div>
    </div>
    <div ref="chartEl" class="chart"></div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue";
import axios from "axios";
import * as echarts from "echarts";

const type = ref("air");
const metric = ref("airPM25"); // 默认选中 PM2.5 方便调试
const devid = ref("");

function pad2(n) { return String(n).padStart(2, "0"); }
function toLocalInputValue(d) {
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}T${pad2(d.getHours())}:${pad2(d.getMinutes())}`;
}

const now = new Date();
const startStr = ref(toLocalInputValue(new Date(now.getTime() - 24 * 3600 * 1000)));
const endStr = ref(toLocalInputValue(now));

const metricDict = {
  air: [
    { label: "空气温度 airTemp", value: "airTemp" },
    { label: "空气湿度 airHumi", value: "airHumi" },
    { label: "气压 airPres", value: "airPres" },
    { label: "PM2.5 airPM25", value: "airPM25" },
    { label: "PM10 airPM10", value: "airPM10" },
    { label: "风速 windSpeed", value: "windSpeed" },
    { label: "风向 windDir", value: "windDir" },
  ],
  soil: [
    { label: "温度 temperature", value: "temperature" },
    { label: "湿度 humidity", value: "humidity" },
    { label: "PH ph", value: "ph" },
    { label: "电导 electrical", value: "electrical" },
    { label: "氮 nitrogen", value: "nitrogen" },
    { label: "磷 phosphorus", value: "phosphorus" },
    { label: "钾 potassium", value: "potassium" },
  ],
  water: [
    { label: "水温 waterTemp", value: "waterTemp" },
    { label: "PH waterPh", value: "waterPh" },
    { label: "电导 waterEc", value: "waterEc" },
    { label: "TDS waterTds", value: "waterTds" },
    { label: "盐度 waterSal", value: "waterSal" },
    { label: "浊度 waterTurb", value: "waterTurb" },
    { label: "溶氧浓度 waterOxycon", value: "waterOxycon" },
    { label: "溶氧饱和度 waterOxysat", value: "waterOxysat" },
  ],
};

const metricOptions = computed(() => metricDict[type.value] ?? []);

watch(type, () => {
  metric.value = metricOptions.value[0]?.value ?? "";
});

const loading = ref(false);
const errorMsg = ref("");

const chartEl = ref(null);
let chart = null;

function ensureChart() {
  if (!chartEl.value) return;
  if (!chart) {
    chart = echarts.init(chartEl.value);
    window.addEventListener("resize", resizeChart);
  }
}
function resizeChart() {
  if (chart) chart.resize();
}
onBeforeUnmount(() => {
  window.removeEventListener("resize", resizeChart);
  if (chart) {
    chart.dispose();
    chart = null;
  }
});

function formatForBackend(datetimeLocalStr) {
  return datetimeLocalStr.length === 16 ? `${datetimeLocalStr}:00` : datetimeLocalStr;
}

function downsample(points, maxPoints = 800) {
  if (!Array.isArray(points)) return [];
  const n = points.length;
  if (n <= maxPoints) return points;
  const step = Math.ceil(n / maxPoints);
  const sampled = [];
  for (let i = 0; i < n; i += step) {
    sampled.push(points[i]);
  }
  if (sampled[sampled.length - 1] !== points[n - 1]) {
    sampled.push(points[n - 1]);
  }
  return sampled;
}

// --- AQI 核心配置 ---

const aqiResult = ref(null);

// 定义 PM2.5 标准数据与颜色 (参考文档与国标颜色)
// limit: 上限值
// color: 16进制标准色
const aqiStandards = [
  { limit: 35,  iaqi: 50,  level: '一级', label: '优',       color: '#009966', textColor: '#fff', health: '空气质量令人满意，基本无空气污染', measure: '各类人群可正常活动' },
  { limit: 75,  iaqi: 100, level: '二级', label: '良',       color: '#ffde33', textColor: '#fff', health: '空气质量可接受，但某些污染物可能对极少数异常敏感人群健康较弱影响', measure: '极少数异常敏感人群应减少户外活动' }, // 你提到的黄色
  { limit: 115, iaqi: 150, level: '三级', label: '轻度污染', color: '#ff9933', textColor: '#fff', health: '易感人群症状有轻度加剧，健康人群出现刺激症状', measure: '儿童、老年人及心脏病、呼吸系统疾病患者应减少长时间、高强度的户外锻炼' },
  { limit: 150, iaqi: 200, level: '四级', label: '中度污染', color: '#cc0033', textColor: '#fff', health: '进一步加剧易感人群症状，可能对健康人群心脏、呼吸系统有影响', measure: '儿童、老年人及心脏病、呼吸系统疾病患者避免长时间、高强度的户外锻练，一般人群适量减少户外运动' },
  { limit: 250, iaqi: 300, level: '五级', label: '重度污染', color: '#660099', textColor: '#fff', health: '心脏病和肺病患者症状显著加剧，运动耐受力降低，健康人群普遍出现症状', measure: '儿童、老年人及心脏病、呼吸系统疾病患者应停留在室内，停止户外运动，一般人群减少户外运动' },
  { limit: 500, iaqi: 500, level: '六级', label: '严重污染', color: '#7e0023', textColor: '#fff', health: '健康人群运动耐受力降低，有明显强烈症状，提前出现某些疾病', measure: '儿童、老年人和病人应当留在室内，避免体力消耗，一般人群应避免户外活动' }
];

// 工具：Hex 转 RGBA (用于背景透明度)
function hexToRgba(hex, alpha) {
  let c;
  if(/^#([A-Fa-f0-9]{3}){1,2}$/.test(hex)){
    c= hex.substring(1).split('');
    if(c.length== 3){
      c= [c[0], c[0], c[1], c[1], c[2], c[2]];
    }
    c= '0x'+c.join('');
    return 'rgba('+[(c>>16)&255, (c>>8)&255, c&255].join(',')+','+alpha+')';
  }
  return hex;
}

// 计算 AQI
function calcPM25AQI(val) {
  if (val > 500) return 500;
  // 寻找区间
  let lowC = 0, lowI = 0;
  for (let s of aqiStandards) {
    const highC = s.limit;
    const highI = s.iaqi;
    if (val <= highC) {
      const result = ((highI - lowI) / (highC - lowC)) * (val - lowC) + lowI;
      return Math.round(result);
    }
    lowC = highC;
    lowI = highI;
  }
  return 500;
}

// 获取详情
function getAqiInfo(aqi) {
  // 根据 AQI 值反查等级 (注意这里的逻辑是根据计算出的 AQI 分数查找)
  // 文档对应：0-50 优, 51-100 良...
  if (aqi <= 50) return aqiStandards[0];
  if (aqi <= 100) return aqiStandards[1];
  if (aqi <= 150) return aqiStandards[2];
  if (aqi <= 200) return aqiStandards[3];
  if (aqi <= 300) return aqiStandards[4];
  return aqiStandards[5];
}

async function loadTrend() {
  errorMsg.value = "";
  loading.value = true;
  aqiResult.value = null;

  try {
    const params = {
      type: type.value,
      metric: metric.value,
      start: formatForBackend(startStr.value),
      end: formatForBackend(endStr.value),
    };
    if (devid.value.trim()) params.devid = devid.value.trim();

    const resp = await axios.get("/api/sensor/trend", { params });
    let list = resp?.data?.data ?? [];
    if (type.value === 'soil') {
      list = downsample(list, 200);
    }

    const x = list.map((p) => p.time);
    const y = list.map((p) => (p.value == null ? null : Number(p.value)));

    let markArea = undefined;
    let markLine = undefined;
    // 默认显示 Y 轴标签，如果是 PM2.5 则隐藏默认标签，改用标准线标签
    let showYAxisLabel = true;
    let yAxisMax = null; // 自动计算最大值

    if (metric.value === 'airPM25') {
      showYAxisLabel = false; // 🔴 核心修改：隐藏默认 Y 轴杂乱数字

      const validValues = list.filter(p => p.value != null).map(p => Number(p.value));
      const currentMax = validValues.length > 0 ? Math.max(...validValues) : 0;
      yAxisMax = currentMax < 150 ? 150 : null;

      if (validValues.length > 0) {
        const sum = validValues.reduce((a, b) => a + b, 0);
        const avg = sum / validValues.length;
        const val = calcPM25AQI(avg);
        const info = getAqiInfo(val);
        aqiResult.value = {
          value: val,
          avgConc: avg.toFixed(1),
          ...info
        };
      }

      const areaData = [];
      const lineData = [];

      let lowerBound = 0;
      aqiStandards.forEach((s) => {
        // 背景色带
        areaData.push([
          { yAxis: lowerBound, itemStyle: { color: hexToRgba(s.color, 1) } },
          { yAxis: s.limit }
        ]);

        // 标线
        lineData.push({
          yAxis: s.limit,
          label: {
            formatter: `${s.limit}`,
            position: 'insideEnd', // 🔴 核心修改：放在内部右侧，不再被切掉
            distance: 5,           // 离边缘一点距离
            color: '#fff',         // 白字
            backgroundColor: s.color, // 背景色即等级颜色
            padding: [4, 8],       // 给文字加个胶囊背景，更好看
            borderRadius: 4,
            fontSize: 11,
            fontWeight: 'bold'
          },
          lineStyle: { type: 'dashed', color: s.color, opacity: 0.8 }
        });

        lowerBound = s.limit;
      });

      // 补全顶部
      areaData.push([
        { yAxis: 500, itemStyle: { color: hexToRgba('#7E0023', 0.3) } },
        { yAxis: 2000 }
      ]);

      markArea = { silent: true, data: areaData };
      markLine = {
        symbol: 'none',
        data: lineData,
        label: { show: true }
      };
    }

    await nextTick();
    ensureChart();

    chart.setOption({
      title: { text: `${type.value} - ${metric.value}` },
      tooltip: { trigger: "axis" },
      // 🔴 恢复正常的 grid 设置
      grid: {
        left: 20,
        right: 20, // 不需要很大的右边距了，因为文字在里面
        top: 50,
        bottom: 50,
        containLabel: true
      },
      xAxis: {
        type: "category",
        data: x,
        axisLabel: { rotate: 30 },
      },
      yAxis: {
        type: "value",
        min: 0,
        max: yAxisMax, // 动态设置最大值，防止图表太矮
        axisLabel: {
          show: showYAxisLabel, // PM2.5 时隐藏默认坐标
          color: '#666'
        },
        splitLine: {
          show: showYAxisLabel // PM2.5 时隐藏默认网格线，只看标准线
        }
      },
      series: [
        {
          type: "line",
          data: y,
          showSymbol: false,
          connectNulls: false,
          sampling: "lttb",
          progressive: 0,
          markArea: markArea,
          markLine: markLine,
          lineStyle: { width: 3 } // 加粗折线，看得更清楚
        },
      ],
    });
  } catch (e) {
    console.error(e);
    const msg = e?.response?.data?.msg || e?.message || "请求失败";
    errorMsg.value = msg;
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadTrend();
});
</script>

<style scoped>
.wrap {
  max-width: 1100px;
  margin: 40px auto;
  padding: 0 16px;
  font-family: system-ui, -apple-system, "Segoe UI", Roboto, Arial, "PingFang SC", "Microsoft YaHei", sans-serif;
}
h1 { margin: 0 0 18px; }

.panel {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 14px;
  align-items: center;
  padding: 14px;
  border: 1px solid #eee;
  border-radius: 10px;
  background: #fafafa;
}
label {
  display: flex;
  gap: 8px;
  align-items: center;
}
select, input {
  padding: 6px 8px;
  border: 1px solid #ddd;
  border-radius: 8px;
}
button {
  padding: 8px 12px;
  border: 0;
  border-radius: 10px;
  background: #2f6fed;
  color: #fff;
  cursor: pointer;
}
button:disabled { opacity: 0.7; cursor: not-allowed; }

.hint {
  margin: 12px 0;
  color: #c0392b;
}

.chart {
  margin-top: 14px;
  /* height: 520px;  <-- 删除或注释掉这一行 */
  height: 60vh;      /* <-- 改成这个，意思是“屏幕高度的 60%” */
  min-height: 300px; /* 最小不低于 300px */
  border: 1px solid #eee;
  border-radius: 10px;
}

/* ... existing styles ... */

.aqi-panel {
  margin: 20px 0;
  border: 2px solid #ddd;
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.aqi-header {
  padding: 16px 24px;
  display: flex;
  align-items: center;
  gap: 20px;
}

.aqi-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  line-height: 1;
}

.aqi-score {
  font-size: 42px;
  font-weight: 800;
}

.aqi-badge {
  font-size: 12px;
  opacity: 0.9;
  font-weight: bold;
}

.aqi-right {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.aqi-level {
  font-size: 24px;
  font-weight: bold;
}

.aqi-conc {
  font-size: 14px;
  opacity: 0.9;
}

.aqi-body {
  padding: 16px 24px;
  background: #fff;
}

.aqi-row {
  display: flex;
  margin-bottom: 8px;
  line-height: 1.5;
}

.aqi-row:last-child {
  margin-bottom: 0;
}

.aqi-row .label {
  flex-shrink: 0;
  width: 80px;
  font-weight: bold;
  color: #555;
}

.aqi-row .text {
  color: #333;
}
</style>
