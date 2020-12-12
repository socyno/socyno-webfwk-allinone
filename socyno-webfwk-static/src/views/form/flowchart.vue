<template>
  <div>
    <el-button type="primary" style="position: absolute; top: 10px; left: 10px; z-index: 999; width: 250px; height: 80px" @click="toggleFlowChartType">
      {{ unchanged ? '隐藏无状态流转的事件' : '显示所有事件' }}
    </el-button>
    <div id="form-flow-chart-canvas" />
  </div>
</template>
<script>
import go from 'gojs'
import tool from '@/utils/tools'
import FormApi from '@/apis/formApi'
const MAKE = go.GraphObject.make
export default {
  data() {
    return {
      myDiagram: null,
      formId: this.$route.query.formId,
      formName: this.$route.params.formName,
      unchanged: this.$route.query.unchanged === 'true'
    }
  },
  mounted() {
    this.initializeFlowChart()
  },
  methods: {
    toggleFlowChartType() {
      history.replaceState(
        {},
        null,
        '#' + this.$route.path +
        tool.setUrlParams('?', {
          formId: tool.stringify(this.formId),
          unchanged: !this.unchanged
        })
      )
      location.reload()
    },

    initializeFlowChart() {
      const blueGrad = MAKE(go.Brush, 'Linear', { 0: '#c8cdc4', 1: '#c8cdc4' })
      const greenGrad = MAKE(go.Brush, 'Linear', { 0: '#4ACA6D', 1: '#4ACA6D' })

      this.myDiagram =
        MAKE(go.Diagram, 'form-flow-chart-canvas',
          {
            // have mouse wheel events zoom in and out instead of scroll up and down
            'toolManager.mouseWheelBehavior': go.ToolManager.WheelZoom,
            initialContentAlignment: go.Spot.Center,
            initialAutoScale: go.Diagram.Uniform,
            contentAlignment: go.Spot.Center, // 对齐方式
            'toolManager.hoverDelay': 100, // tooltip提示显示延时
            'toolManager.toolTipDuration': 100000, // tooltip持续显示时间
            'isReadOnly': true, // 只读
            'linkingTool.direction': go.LinkingTool.ForwardsOnly,
            layout: MAKE(go.LayeredDigraphLayout, { isInitial: true, isOngoing: true, layerSpacing: 50 })
          })

      this.myDiagram.nodeTemplate =
        MAKE(go.Node,
          new go.Binding('location', 'loc', go.Point.parse),
          MAKE(go.Shape, 'RoundedRectangle', { fill: '#c8cdc4', stroke: 'black', strokeWidth: 2, height: 30 }),
          'Auto',
          MAKE(go.Panel, 'Horizontal', { padding: 5 },
            MAKE(go.Panel, 'Vertical',
              MAKE(go.TextBlock, 'Event', { margin: 7, stroke: 'black', font: '14px sans-serif' }, new go.Binding('text', 'text')),
            )
          )
        )

      this.myDiagram.linkTemplate =
        MAKE(go.Link,
          new go.Binding('points').makeTwoWay(),
          { curve: go.Link.Bezier, toShortLength: 15 },
          new go.Binding('curviness', 'curviness'),
          MAKE(go.Shape,
            { stroke: '#2F4F4F', strokeWidth: 2.0 }),
          MAKE(go.Shape,
            { toArrow: 'kite', fill: '#4f2f2f', stroke: null, scale: 1.5 })
        )

      this.myDiagram.nodeTemplateMap.add('STATE',
        MAKE(go.Node, 'Auto', this.nodeStyle(),
          new go.Binding('location', 'loc', go.Point.parse).makeTwoWay(go.Point.stringify),
          MAKE(go.Shape, 'RoundedRectangle',
            { fill: blueGrad, portId: '', toLinkable: true, toEndSegmentLength: 50 }),
          MAKE(go.TextBlock, 'Success!', this.stateStyle(),
            new go.Binding('text', 'text').makeTwoWay())
        ))

      this.myDiagram.nodeTemplateMap.add('STATE_CURRENT',
        MAKE(go.Node, 'Auto', this.nodeStyle(),
          new go.Binding('location', 'loc', go.Point.parse).makeTwoWay(go.Point.stringify),
          MAKE(go.Shape, 'RoundedRectangle',
            { fill: greenGrad, portId: '', toLinkable: true, toEndSegmentLength: 50 }),
          MAKE(go.TextBlock, 'Success!', this.stateStyle(),
            new go.Binding('text', 'text').makeTwoWay())
        ))

      this.myDiagram.linkTemplateMap.add('LINK_CURRENT',
        MAKE(go.Link,
          new go.Binding('points').makeTwoWay(),
          { curve: go.Link.Bezier, toShortLength: 15 },
          new go.Binding('curviness', 'curviness'),
          MAKE(go.Shape,
            { stroke: 'green', strokeWidth: 3.5 }),
          MAKE(go.Shape,
            { toArrow: 'kite', fill: 'green', stroke: null, scale: 2.5 }),
        ))

      this.myDiagram.nodeTemplateMap.add('NEXT_ACTION',
        MAKE(go.Node,
          new go.Binding('location', 'loc', go.Point.parse),
          MAKE(go.Shape, 'RoundedRectangle', { fill: '#c8cdc4', stroke: 'green', strokeWidth: 2, height: 30 }),
          'Auto',
          MAKE(go.Panel, 'Horizontal', { padding: 5 },
            MAKE(go.Panel, 'Vertical',
              MAKE(go.Picture, {
                source: require('../../assets/male.png'), column: 1,
                margin: 5, width: 20, height: 20
              })),
            MAKE(go.Panel, 'Vertical',
              MAKE(go.TextBlock, 'Event', { margin: 7, stroke: 'black', font: '14px sans-serif' }, new go.Binding('text', 'text')),
            ),
          ),
          {
            toolTip: MAKE(go.Adornment, 'Spot',
              MAKE(go.TextBlock,
                {
                  wrap: go.TextBlock.WrapFit,
                  stroke: '#127edc',
                  font: 'bold 16px sans-serif',
                  margin: 5
                },
                new go.Binding('text', '',
                  function(d) {
                    const arr = d.currentApprovalPeopleList
                    if (tool.isUndefOrNull(arr) || arr.length <= 0) {
                      return '当前处理人不存在或未分配!'
                    }
                    let str = ''
                    arr.forEach(function(item) {
                      str += item + '\n'
                    })
                    return str
                  })
              )
            )
          }
        ))

      this.myDiagram.nodeTemplateMap.add('UNCHANGED',
        MAKE(go.Node, 'Auto', this.nodeStyle(),
          new go.Binding('location', 'loc', go.Point.parse).makeTwoWay(go.Point.stringify),
          MAKE(go.Shape, 'RoundedRectangle',
            { fill: blueGrad, portId: '', toLinkable: true, toEndSegmentLength: 50 }),
          MAKE(go.TextBlock, 'Success!', this.stateStyle(),
            new go.Binding('text', 'text').makeTwoWay())
        ))

      this.myDiagram.nodeTemplateMap.add('YESNO',
        MAKE(go.Node, 'Table', this.nodeStyle(),
          MAKE(go.Panel, 'Spot',
            MAKE(go.Shape, 'Circle',
              { desiredSize: new go.Size(40, 40), fill: '#c8cdc4' }),
            MAKE(go.TextBlock, 'Start', this.textStyle(),
              new go.Binding('text'))
          )))

      this.myDiagram.nodeTemplateMap.add('CHOICE',
        MAKE(go.Node, 'Table', this.nodeStyle(),
          MAKE(go.Panel, 'Auto',
            MAKE(go.Shape, 'Diamond',
              { fill: '#c8cdc4' },
              new go.Binding('figure', 'figure')),
            MAKE(go.TextBlock, this.textStyle(),
              new go.Binding('text').makeTwoWay())
          )
        ))

      const myModel = MAKE(go.GraphLinksModel)
      new FormApi(this.formName).loadFlowChart(this.formId, this.unchanged).then(res => {
        myModel.nodeDataArray = res.data.nodeData
        myModel.linkDataArray = res.data.linkData
      }).catch(res => {
        this.$notify.error('加载数据异常')
      })
      this.myDiagram.model = myModel
    },

    textStyle() {
      return {
        margin: 6,
        wrap: go.TextBlock.WrapFit,
        textAlign: 'center',
        editable: true,
        font: 'bold 11pt Helvetica, Arial, sans-serif'
      }
    },

    stateStyle() {
      return {
        margin: 25,
        wrap: go.TextBlock.WrapFit,
        textAlign: 'center',
        editable: true,
        font: 'bold 11pt Helvetica, Arial, sans-serif'
      }
    },

    nodeStyle() {
      return [
        new go.Binding('location', 'loc', go.Point.parse).makeTwoWay(go.Point.stringify),
        {
          locationSpot: go.Spot.Center
        }
      ]
    }
  }
}
</script>
<style>
  #form-flow-chart-canvas {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    border: 0;
  }
</style>
