import { WIDTH, HEIGHT } from '../core/constants.js'
import {lerp, unlerpUnclamped} from '../core/utils.js'
import { api as entityModule } from '../entity-module/GraphicEntityModule.js'

export const options = {
  pathsAlwaysVisible: false,
  resetPaths: () => {}
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

      if (!options.pathsAlwaysVisible) {
	      for (let id in module.currentFrame.paths) {
	        module.currentFrame.paths[id].forEach(entity => entityModule.entities.get(entity).graphics.visible = false);
	      }
      }

      if (showing.length || x >= 0 && x < MAP_WIDTH && y >= 0 && y < MAP_HEIGHT) {
        const tooltipBlocks = []
        tooltipBlocks.push('X: ' + x + '\nY: ' + y)
        tooltip.visible = true

        for (let show of showing) {
          const entity = entityModule.entities.get(show)
          const state = getEntityState(entity, module.currentFrame.number)
          if (state !== null) {
            let tooltipBlock = ''
            const params = module.currentFrame.tooltips[show]
            
            if (params) {
                for (let key in params) {
                  tooltipBlock += key + ': ' + params[key]
                }
            }

            tooltipBlocks.push(tooltipBlock)
          }

          // Show paths
          if (!options.pathsAlwaysVisible) {
	          const id = module.currentFrame.ownerships[show]
	          if (id !== undefined) {
	              module.currentFrame.paths[id].forEach(entity => entityModule.entities.get(entity).graphics.visible = true)
	          }
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
}

export class AStarCraftModule {
  constructor (assets) {
    this.interactive = {}
    this.previousFrame = {
      tooltips: {},
      paths: {},
      ownerships: {}
    }
    this.lastProgress = 1
    this.lastFrame = 0
    this.pathEntities = [];
    
    options.resetPaths = () => {
    	this.pathEntities.forEach(id => {
    		entityModule.entities.get(id).graphics.visible = options.pathsAlwaysVisible;
    	});
    }
  }

  static get name () {
    return 'astarcraft'
  }

  updateScene (previousData, currentData, progress) {
    this.currentFrame = currentData
    this.currentProgress = progress
  }

  handleFrameData (frameInfo, {tooltips, paths, ownerships}) {
    tooltips = { ...this.previousFrame.tooltips, ...tooltips }
    ownerships = { ...this.previousFrame.ownerships, ...ownerships }

    const allPaths = this.previousFrame.paths;
    for (let key in paths) {
        if (!allPaths[key]) {
            allPaths[key] = [];
        }

        allPaths[key].push(paths[key]);
        this.pathEntities.push(paths[key]);
    }

    Object.keys(tooltips).forEach(
      k => {
        this.interactive[k] = true
      }
    )

    const frame = { tooltips, number: frameInfo.number, paths: allPaths, ownerships }

    this.previousFrame = frame
    return frame
  }

  reinitScene (container, canvasData) {
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
    container.addChild(this.tooltip)
    
    options.resetPaths();
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
  }

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
  }

  animateScene (delta) {

  }

  handleGlobalData (players, globalData) {

  }
}
