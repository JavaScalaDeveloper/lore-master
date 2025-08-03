export default defineAppConfig({
  pages: [
    'pages/index/index',
    'pages/study/study',
    'pages/profile/profile',
  ],
  window: {
    backgroundTextStyle: 'light',
    navigationBarBackgroundColor: '#fff',
    navigationBarTitleText: 'Lore Master',
    navigationBarTextStyle: 'black'
  },
  tabBar: {
    color: '#999',
    selectedColor: '#1AAD19',
    backgroundColor: '#fff',
    borderStyle: 'black',
    list: [
      {
        pagePath: 'pages/index/index',
        text: '主页',
        iconPath: '/public/icons/home.png',
        selectedIconPath: '/public/icons/home_selected.png'
      },
      {
        pagePath: 'pages/study/study',
        text: '学习',
        iconPath: '/public/icons/study.png',
        selectedIconPath: '/public/icons/study_selected.png'
      },
      {
        pagePath: 'pages/profile/profile',
        text: '个人中心',
        iconPath: '/public/icons/profile.png',
        selectedIconPath: '/public/icons/profile_selected.png'
      }
    ]
  },
  // 网络请求超时设置
  networkTimeout: {
    request: 60000,
    connectSocket: 60000,
    uploadFile: 60000,
    downloadFile: 60000
  },
  // 配置服务器域名
  request: {
   合法域名: [
      'http://localhost:8082',
      'https://your-production-domain.com'
    ],
    // 开发环境下允许HTTP请求
    enableHttp2: false,
    enableQuic: false
  },
  permission: {
    'scope.userLocation': {
      'desc': '你的位置信息将用于小程序定位'
    }
  }
})
