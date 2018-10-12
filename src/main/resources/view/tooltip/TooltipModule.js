import { WIDTH, HEIGHT } from '../core/constants.js'
import {lerp, unlerp, unlerpUnclamped} from '../core/utils.js'
import { api as entityModule } from '../entity-module/GraphicEntityModule.js'

/* global PIXI */

export const options = {
  linesAlwaysVisible: false,
  resetLines: () => {}
}

const OFFSET_X = 10
const OFFSET_Y = 68
const MAP_WIDTH = 19
const MAP_HEIGHT = 10
const VIEWER_WIDTH = 1900
const VIEWER_HEIGHT = 1000
const CELL_WIDTH = VIEWER_WIDTH / MAP_WIDTH
const CELL_HEIGHT = VIEWER_HEIGHT / MAP_HEIGHT

function getMouseOverFunc (id, tooltip) {
  return function () {
    tooltip.inside[id] = true
  }
}

function getMouseOutFunc (id, tooltip) {
  return function () {
    delete tooltip.inside[id]
  }
}

function getEntityState (entity, frame, progress) {
  const subStates = entity.states[frame]
  if (subStates && subStates.length) {
    return subStates[subStates.length - 1]
  }
  return null
}

function getMouseMoveFunc (tooltip, container, module) {
  return function (ev) {
    if (tooltip) {
      var pos = ev.data.getLocalPosition(container)
      tooltip.x = pos.x
      tooltip.y = pos.y
      var point = {
        x: pos.x * entityModule.coeff,
        y: pos.y * entityModule.coeff
      }

      const showing = []
      const ids = Object.keys(tooltip.inside).map(n => +n)

      for (let id of ids) {
        if (tooltip.inside[id]) {
          const entity = entityModule.entities.get(id)
          const state = entity && getEntityState(entity, module.currentFrame.number)
          if (!state || state.alpha === 0 || !state.visible) {
            delete tooltip.inside[id]
          } else {
            showing.push(id)
          }
        }
      }

      var x = Math.floor(lerp(0, MAP_WIDTH, unlerpUnclamped(OFFSET_X, OFFSET_X + CELL_WIDTH * MAP_WIDTH, point.x)))
      var y = Math.floor(lerp(0, MAP_HEIGHT, unlerpUnclamped(OFFSET_Y, OFFSET_Y + CELL_HEIGHT * MAP_HEIGHT, point.y)))

      if (showing.length || (x >= 0 && x < MAP_WIDTH && y >= 0 && y < MAP_HEIGHT)) {
        const tooltipBlocks = []
        tooltipBlocks.push('X: ' + x + '\nY: ' + y)
        tooltip.visible = true

        for (let id in module.robotIdToLines) {
          module.robotIdToLines[id].visible = options.linesAlwaysVisible
        }

        for (let show of showing) {
          const entity = entityModule.entities.get(show)
          const state = getEntityState(entity, module.currentFrame.number)
          if (state !== null) {
            let tooltipBlock = ''
            const params = module.currentFrame.registered[show]

            if (params) {
              for (let key in params) {
                tooltipBlock += key + ': ' + params[key] + '\n'
              }
              const lines = module.robotIdToLines[params.id]
              if (lines) {
                if (!lines.visible) {
                  lines.visible = true
                  module.redrawLines(lines, module.currentFrame.number, module.paths[params.id], module.currentProgress)
                }
              }
            }

            const extra = module.currentFrame.extraText[show]
            if (extra && extra.length) {
              tooltipBlock += extra
            }
            tooltipBlocks.push(tooltipBlock)
          }
        }
        tooltip.label.text = tooltipBlocks.join('\n──────────\n')
      } else {
        tooltip.visible = false
      }

      tooltip.background.width = tooltip.label.width + 20
      tooltip.background.height = tooltip.label.height + 20

      tooltip.pivot.x = -30
      tooltip.pivot.y = -50

      if (tooltip.y - tooltip.pivot.y + tooltip.height > HEIGHT) {
        tooltip.pivot.y = 10 + tooltip.height
        tooltip.y -= tooltip.y - tooltip.pivot.y + tooltip.height - HEIGHT
      }

      if (tooltip.x - tooltip.pivot.x + tooltip.width > WIDTH) {
        tooltip.pivot.x = tooltip.width
      }
    }
  }
};

function convertX (x) {
  return OFFSET_X + CELL_WIDTH * (x + 0.5)
}

function convertY (y) {
  return OFFSET_Y + CELL_WIDTH * (y + 0.5)
}

export class TooltipModule {
  constructor (assets) {
    this.interactive = {}
    this.previousFrame = {
      registrations: {},
      extra: {}
    }
    this.lastProgress = 1
    this.lastFrame = 0
    this.robotIdToLines = {}
    this.paths = {}
    options.resetLines = () => {
      this.redrawAllLines()
    }
  }

  static get name () {
    return 'tooltips'
  }

  redrawAllLines () {
    for (let id in this.robotIdToLines) {
      const lines = this.robotIdToLines[id]
      lines.visible = options.linesAlwaysVisible
      if (lines.visible) {
        this.redrawLines(lines, this.currentFrame.number, this.paths[id], this.currentProgress)
      }
    }
  }

  redrawLines (lines, frameNumber, path, progress) {
    const firstStep = path[0]
    const finalStep = path[frameNumber - 1]

    lines.clear()
    lines.lineStyle(4, 0xF7F0cc)
    lines.moveTo(firstStep.x, firstStep.y)

    let prevStep = firstStep
    for (let step of path.slice(1, frameNumber)) {
      // Deal with portals
      let firstP = unlerp(0, 0.5, progress)
      let secondP = unlerp(0.5, 1, progress)
      if (step.x === convertX(0) && prevStep.x === convertX(MAP_WIDTH - 1)) {
        let curX
        if (step === finalStep) {
          curX = lerp(prevStep.x, prevStep.x + CELL_WIDTH, firstP)
        } else {
          curX = prevStep.x + CELL_WIDTH
        }
        lines.lineTo(curX, step.y)
        lines.moveTo(step.x - CELL_WIDTH, step.y)

        if (step === finalStep) {
          curX = lerp(step.x - CELL_WIDTH, step.x, secondP)
        } else {
          curX = step.x
        }
        lines.lineTo(curX, step.y)
      } else {
        let x, y
        if (step === finalStep) {
          x = lerp(prevStep.x, step.x, progress)
          y = lerp(prevStep.y, step.y, progress)
        } else {
          x = step.x
          y = step.y
        }

        // NOTE: Example of something you can do with the dir information

        // const lineOffset = 5
        // if (step.dir === RIGHT) {
        //   y += lineOffset
        // } else if (step.dir === LEFT) {
        //   y -= lineOffset
        // } else if (step.dir === UP) {
        //   x += lineOffset
        // } else if (step.dir === DOWN) {
        //   x -= lineOffset
        // }
        lines.lineTo(x, y)
      }
      prevStep = step
    }
  }

  updateScene (previousData, currentData, progress) {
    this.currentFrame = currentData
    this.currentProgress = progress

    for (let id in this.paths) {
      const lines = this.robotIdToLines[id]
      if (lines.visible) {
        this.redrawLines(lines, currentData.number, this.paths[id], progress)
      }
    }
  }

  handleFrameData (frameInfo, data) {
    const registrations = data[0]
    const extra = data[1]
    const paths = data[2] || {}

    const registered = { ...this.previousFrame.registered, ...registrations }
    const extraText = { ...this.previousFrame.extraText, ...extra }

    for (let idx in paths) {
      const rawPath = paths[idx].split(' ')
      const steps = []
      for (let k = 0; k < rawPath.length / 3; ++k) {
        const x = convertX(+rawPath[k * 3])
        const y = convertY(+rawPath[k * 3 + 1])
        const dir = +rawPath[k * 3 + 2]
        const step = {x, y, dir}
        steps.push(step)
      }
      this.paths[idx] = steps
    }

    Object.keys(registrations).forEach(
      k => {
        this.interactive[k] = true
      }
    )

    const frame = { registered, extraText, number: frameInfo.number }
    this.previousFrame = frame
    return frame
  }

  initPaths () {
    const layer = new PIXI.Container()

    this.robotIdToLines = {}

    for (let idx in this.paths) {
      const path = this.paths[idx]
      const lines = new PIXI.Graphics()

      this.robotIdToLines[idx] = lines

      lines.lineStyle(4, 0xF7F0cc)
      lines.moveTo(path[0].x, path[0].y)

      for (let step of path.slice(1)) {
        lines.lineTo(step.x, step.y)
      }

      lines.visible = options.linesAlwaysVisible
      layer.addChild(lines)
    }

    return layer
  }

  reinitScene (container, canvasData) {
    const pathLayer = this.initPaths()

    this.tooltip = this.initTooltip()
    entityModule.entities.forEach(entity => {
      if (this.interactive[entity.id]) {
        entity.container.interactive = true
        entity.container.mouseover = getMouseOverFunc(entity.id, this.tooltip)
        entity.container.mouseout = getMouseOutFunc(entity.id, this.tooltip)
      }
    })
    this.container = container
    container.interactive = true
    container.mousemove = getMouseMoveFunc(this.tooltip, container, this)

    container.addChild(pathLayer)
    container.addChild(this.tooltip)
  }

  generateText (text, size, color, align) {
    var textEl = new PIXI.Text(text, {
      fontSize: Math.round(size / 1.2) + 'px',
      fontFamily: 'monospace',
      fontWeight: 'bold',
      fill: color
    })

    textEl.lineHeight = Math.round(size / 1.2)
    if (align === 'right') {
      textEl.anchor.x = 1
    } else if (align === 'center') {
      textEl.anchor.x = 0.5
    }

    return textEl
  };

  initTooltip () {
    var tooltip = new PIXI.Container()
    var background = tooltip.background = new PIXI.Graphics()
    var label = tooltip.label = this.generateText('', 36, 0xFFFFFF, 'left')

    background.beginFill(0x0, 0.7)
    background.drawRect(0, 0, 200, 185)
    background.endFill()
    background.x = -10
    background.y = -10

    tooltip.visible = false
    tooltip.inside = {}

    tooltip.addChild(background)
    tooltip.addChild(label)

    tooltip.interactiveChildren = false
    return tooltip
  };

  animateScene (delta) {

  }

  handleGlobalData (players, globalData) {

  }
}

class NotYetImplemented extends Error {
  constructor (feature) {
    super('Not yet implemented: "' + feature)
    this.feature = feature
    this.name = 'NotYetImplemented'
  }
}
