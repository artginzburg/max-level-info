// @ts-check
// `node -v` => v23.6.0
import { exit } from 'node:process';
import { writeFileSync } from 'node:fs'
import launchJson from '../.vscode/launch.json' with { type: 'json' };

const lastArgument = process.argv.at(-1);
const presumablySpongeMixinLocation = lastArgument;

replaceJavaAgent();

function replaceJavaAgent() {
  const config = launchJson.configurations.find(config => config.name === 'Minecraft Client');
  if (!config) {
    console.error('Config not found');
    exit(1);
  }
  const matchedExistingJavaAgent = config.vmArgs.match(/\s-javaagent:(.*)\s*/);
  if (matchedExistingJavaAgent !== null) {
    const existingJavaAgent = matchedExistingJavaAgent.at(1);
    if (existingJavaAgent === undefined) {
      // Need to only add -javaagent
    } else {
      // Need to remove existing -javaagent
      config.vmArgs = config.vmArgs.replace(matchedExistingJavaAgent[0], '');
    }
  }

  config.vmArgs += ` -javaagent:${presumablySpongeMixinLocation}`;

  writeFileSync('./.vscode/launch.json', JSON.stringify(launchJson, undefined, 2), { encoding: 'utf-8' });
}
