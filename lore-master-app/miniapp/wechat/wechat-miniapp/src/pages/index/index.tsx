import { View, Text, Button } from '@tarojs/components';
import { useEffect, useState } from 'react';
import './index.css';

export default function Index() {
  const [count, setCount] = useState(0);

  useEffect(() => {
    console.log('useEffect: Hello World');
    console.info('useEffect: This is an info message');
    console.debug('useEffect: This is a debug message');
    
    setTimeout(() => {
      console.log('Delayed: Hello World');
    }, 1000);
  }, []);

  console.log('Render: Hello World');

  const handleClick = () => {
    const newCount = count + 1;
    setCount(newCount);
    console.log(`Button clicked! Count: ${newCount}`);
  };

  return (
    <View className='index'>
      <Text>Hello World</Text>
      <Text>Check console for logs</Text>
      <Text>Click count: {count}</Text>
      <Button onClick={handleClick} type='primary'>Click me</Button>
    </View>
  );
}
