import { registerRootComponent } from 'expo';
import { AppRegistry } from 'react-native';
import App from './src/app';

AppRegistry.registerComponent('taroDemo', () => App);
import { name as appName } from './app.json';

AppRegistry.registerComponent(appName, () => App);
import Root from './src/root';

// Register the main App component with Expo
registerRootComponent(() => (
  <Root>
    <App />
  </Root>
));