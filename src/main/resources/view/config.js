import { GraphicEntityModule } from './entity-module/GraphicEntityModule.js'
import { options as viewOptions, TooltipModule } from './tooltip/TooltipModule.js'

// List of viewer modules that you want to use in your game
export const modules = [
  GraphicEntityModule,
  TooltipModule
]

export const gameName = 'AStarCraft'

export const options = [{
  title: 'ROBOT TRAILS',
  get: function () {
    return viewOptions.linesAlwaysVisible
  },
  set: function (value) {
    viewOptions.linesAlwaysVisible = value
    viewOptions.resetLines()
  },
  values: {
    'ON HOVER': false,
    'ALWAYS': true
  }
}]
