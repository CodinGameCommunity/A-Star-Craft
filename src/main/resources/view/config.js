import { GraphicEntityModule } from './entity-module/GraphicEntityModule.js';
import { AStarCraftModule } from './tooltip/AStarCraftModule.js';
import { AnimModule } from './anims/AnimModule.js';

// List of viewer modules that you want to use in your game
export const modules = [
  GraphicEntityModule,
  AnimModule,
  AStarCraftModule
];
