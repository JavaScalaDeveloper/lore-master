import { PropsWithChildren, useEffect } from 'react'
import { Platform } from 'react-native'
import * as SplashScreen from 'expo-splash-screen'
import { StatusBar } from 'expo-status-bar'

import './app.css'

// Keep the splash screen visible while we fetch resources
SplashScreen.preventAutoHideAsync()

export default function Root({ children }: PropsWithChildren<any>) {
  // Hide splash screen when app is ready
  useEffect(() => {
    async function prepare() {
      try {
        // 在这里可以加载任何需要的资源
        // await loadResources()
      } catch (e) {
        console.warn(e)
      } finally {
        // 隐藏启动屏幕
        await SplashScreen.hideAsync()
      }
    }

    prepare()
  }, [])

  return Platform.select({
    native: (
      <>
        {children}
        <StatusBar style="auto" />
      </>
    ),
    default: children,
  }) as any
}