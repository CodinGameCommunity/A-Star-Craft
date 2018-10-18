import { GraphicEntityModule } from './entity-module/GraphicEntityModule.js';
import { options as viewOptions, AStarCraftModule } from './tooltip/AStarCraftModule.js';
import { AnimModule } from './anims/AnimModule.js';

// List of viewer modules that you want to use in your game
export const modules = [
  GraphicEntityModule,
  AnimModule,
  AStarCraftModule
];

export const options = [{
  title: 'ROBOT TRAILS',
  get: function () {
    return viewOptions.pathsAlwaysVisible
  },
  set: function (value) {
    viewOptions.pathsAlwaysVisible = value
    viewOptions.resetPaths()
  },
  values: {
    'ON HOVER': false,
    'ALWAYS': true
  }
}];