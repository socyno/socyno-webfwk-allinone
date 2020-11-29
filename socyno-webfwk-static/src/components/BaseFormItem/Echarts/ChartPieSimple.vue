<template>
  <!-- 为ECharts准备一个具备大小（宽高）的Dom -->
  <div ref="chartPieSimple" style="width: 96%;height:400px;" />
</template>
<script>
import tool from '@/utils/tools'
export default {
  name: 'ChartPieSimple',
  props: {
    /**
     * 当前的表单名称
     */
    formName: {
      type: String,
      default: null
    },
    /**
     * 当前的表单编号
     */
    formId: {
      type: [String, Number],
      default: null
    },
    /**
     * 当前字段对象
     */
    fieldModel: {
      type: Object,
      default: function() {
        return {}
      }
    },
    /**
     * 绘图数据
     */
    chartData: {
      type: Array,
      default: function() {
        return []
      }
    },
    /**
     * 定制化图形扩展
     */
    chartProperties: {
      type: [Object, String],
      default: function() {
        return {}
      }
    }
  },
  data() {
    return {
      myChart: null,
      legendData: [],
      finalChartData: [],
      finalChartProps: {}
    }
  },
  mounted() {
    this.pareChartData()
    this.innitEcharts()
  },
  methods: {
    pareChartData() {
      if (this.chartData.length > 0 && tool.isPlainObject(this.chartData[0].seresData)) {
        this.chartData.forEach(v => {
          const chartObject = {}
          if (!tool.isUndefOrNull(v.seresData)) {
            for (const prop in v.seresData) {
              chartObject[prop] = v.seresData[prop]
            }
          }
          this.finalChartData.push(chartObject)
        })
      } else {
        this.finalChartData = this.chartData
      }

      if (tool.isString(this.chartProperties) && !tool.isBlank(this.chartProperties)) {
        this.finalChartProps = JSON.parse(this.chartProperties)
      } else {
        this.finalChartProps = this.chartProperties
      }
    },

    innitEcharts() {
      // 基于准备好的dom，初始化echarts实例
      this.myChart = this.$echarts.init(this.$refs.chartPieSimple)
      this.finalChartData.forEach(o => {
        if (!tool.isNullOrUndef(o)) {
          this.legendData.push(o.name)
        }
      })
      const option = {
        title: {
          text: this.finalChartProps.title,
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b} : {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'right',
          data: this.legendData
        },
        series: [
          {
            name: this.finalChartProps.title,
            type: 'pie',
            radius: '55%',
            center: ['50%', '60%'],
            data: this.finalChartData,
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      }
      // 绘制图表
      this.myChart.setOption(option)
    }
  }
}
</script>
