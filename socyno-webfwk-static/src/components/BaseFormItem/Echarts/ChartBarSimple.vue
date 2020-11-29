<template>
  <!-- 为ECharts准备一个具备大小（宽高）的Dom -->
  <div ref="chartBarSimple" style="width: 96%;height:400px;" />
</template>
<script>
import tool from '@/utils/tools'
export default {
  name: 'ChartBarSimple',
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
      dataZoomEndValue: 10,
      finalChartData: [],
      finalChartProps: {}
    }
  },

  watch: {
    chartData: {
      handler: function(newData, oldData) {
        this.innitEcharts()
      }
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
          chartObject.label = v.label
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
      this.myChart = this.$echarts.init(this.$refs.chartBarSimple)
      const posList = [
        'left', 'right', 'top', 'bottom',
        'inside',
        'insideTop', 'insideLeft', 'insideRight', 'insideBottom',
        'insideTopLeft', 'insideTopRight', 'insideBottomLeft', 'insideBottomRight'
      ]
      const app = {}
      app.configParameters = {
        rotate: {
          min: -90,
          max: 90
        },
        align: {
          options: {
            left: 'left',
            center: 'center',
            right: 'right'
          }
        },
        verticalAlign: {
          options: {
            top: 'top',
            middle: 'middle',
            bottom: 'bottom'
          }
        },
        position: {
          options: this.$echarts.util.reduce(posList, function(map, pos) {
            map[pos] = pos
            return map
          }, {})
        },
        distance: {
          min: 0,
          max: 100
        }
      }
      app.config = {
        rotate: 90,
        align: 'left',
        verticalAlign: 'middle',
        position: 'insideBottom',
        distance: 15
      }
      /* bar 内容样式 */
      // eslint-disable-next-line no-unused-vars
      const labelOption = {
        show: true,
        position: app.config.position,
        distance: app.config.distance,
        align: app.config.align,
        verticalAlign: app.config.verticalAlign,
        rotate: app.config.rotate,
        formatter: '{c}  {name|{a}}',
        fontSize: 16,
        rich: {
          name: {
            textBorderColor: '#fff'
          }
        }
      }
      const legendData = []
      const xAxisData = []
      const series = []
      const firstChartObj = this.finalChartData[0]
      const seresProps = this.finalChartProps.seriesProps
      this.dataZoomEndValue = tool.isBlank(this.finalChartProps.dataZoomEndValue) ? this.dataZoomEndValue
        : tool.parseInteger(this.finalChartProps.dataZoomEndValue)
      for (const properties in firstChartObj) {
        if (properties === 'label') {
          continue
        }
        legendData.push(properties)
        const seriesItem = {
          name: properties,
          type: 'bar',
          barGap: 0,
          data: []
        }
        series.push(seriesItem)
      }
      for (let i = 0; i < this.finalChartData.length; i++) {
        const item = this.finalChartData[i]
        xAxisData.push(item.label)
        for (const keyProp in item) {
          if (keyProp === 'label') {
            continue
          }
          series.forEach(o => {
            if (o.name === keyProp) {
              o.data.push(item[keyProp])
              // 根据配置的数据重新定制
              if (!tool.isUndefOrNull(seresProps)) {
                if (tool.hasOwnProperty(seresProps, keyProp)) {
                  if (!tool.isBlank(seresProps[keyProp].type)) {
                    o.type = seresProps[keyProp].type
                  }
                  if (!tool.isBlank(seresProps[keyProp].color)) {
                    o.color = seresProps[keyProp].color
                  }
                  if (!tool.isBlank(seresProps[keyProp].stack)) {
                    o.stack = seresProps[keyProp].stack
                  }
                }
              }
            }
          })
        }
      }
      const option = {
        title: {
          text: this.finalChartProps.title
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        legend: {
          left: 'right',
          data: legendData
        },
        toolbox: {
          show: true,
          orient: 'vertical',
          left: 'right',
          top: 'center',
          feature: {
            mark: { show: true },
            dataView: { show: true, readOnly: false },
            magicType: { show: true, type: ['line', 'bar', 'stack', 'tiled'] },
            restore: { show: true },
            saveAsImage: { show: true }
          }
        },
        xAxis: [
          {
            name: this.finalChartProps.xName,
            type: 'category',
            axisLabel: {
              interval: (tool.isBlank(this.finalChartProps.xAxisLabelInterval) || !tool.looksLikeInteger(this.finalChartProps.xAxisLabelInterval))
                ? 0 : tool.parseInteger(this.finalChartProps.xAxisLabelInterval),
              rotate: (tool.isBlank(this.finalChartProps.xAxisLabelRotate) || !tool.looksLikeInteger(this.finalChartProps.xAxisLabelRotate))
                ? 0 : tool.parseInteger(this.finalChartProps.xAxisLabelRotate)
            },
            axisTick: { show: false },
            data: xAxisData
          }
        ],
        yAxis: [
          {
            name: this.finalChartProps.yName,
            type: 'value'
          }
        ],
        dataZoom: [
          {
            show: xAxisData.length > this.dataZoomEndValue,
            height: 10,
            xAxisIndex: [0],
            bottom: 20,
            showDetail: false,
            showDataShadow: false,
            borderColor: 'transparent',
            textStyle: {
              fontSize: 0
            },
            endValue: this.dataZoomEndValue,
            backgroundColor: 'rgba(0,0,0,0)',
            borderWidth: 1,
            handleSize: '0%',
            handleStyle: {
              color: '#beced7'
            }
          }
        ],
        series: series
      }
      // 绘制图表
      this.myChart.setOption(option)
    }
  }
}
</script>
