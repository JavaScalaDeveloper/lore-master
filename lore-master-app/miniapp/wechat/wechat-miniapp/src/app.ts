import { PropsWithChildren } from 'react'
import { useLaunch } from '@tarojs/taro'

import './app.css'

function App({ children }: PropsWithChildren<any>) {
  useLaunch(() => {
    console.log('App launched.')
    console.log('Hello World')
    
    // 检查是否需要禁用Sentry
    if (process.env.DISABLE_SENTRY === 'true') {
      console.log('正在禁用Sentry...')
      
      // 尝试多种方式禁用Sentry
      try {
        // 方式1: 关闭window对象上的SENTRY实例
        if (typeof window !== 'undefined' && (window as any).__SENTRY__) {
          (window as any).__SENTRY__.close();
          console.log('Sentry已禁用')
        }
        // 方式2: 关闭global对象上的SENTRY实例
        else if (typeof global !== 'undefined' && (global as any).__SENTRY__) {
          (global as any).__SENTRY__.close();
          console.log('Sentry已禁用')
        }
        // 方式3: 尝试删除或禁用Sentry相关的全局配置
        if (typeof window !== 'undefined') {
          console.log('尝试删除Sentry相关全局配置...')
          delete (window as any).__SENTRY__;
          delete (window as any).Sentry;
          console.log('Sentry相关全局配置已删除')
        }
        else if (typeof global !== 'undefined') {
          console.log('尝试删除Sentry相关全局配置...')
          delete (global as any).__SENTRY__;
          delete (global as any).Sentry;
          console.log('Sentry相关全局配置已删除')
        }
      } catch (e) {
        console.error('禁用Sentry过程中出错:', e)
      }
    }
  })

  // children 是将要会渲染的页面
  return children
}
  


export default App
