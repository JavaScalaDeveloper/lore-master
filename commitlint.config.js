module.exports = {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'type-enum': [
      2,
      'always',
      [
        'feat', // 新功能
        'fix', // 修复bug
        'docs', // 文档变更
        'style', // 代码格式(不影响代码运行的变动)
        'refactor', // 重构(既不是新增功能，也不是修改bug的代码变动)
        'perf', // 性能优化
        'test', // 增加测试
        'build', // 构建过程或辅助工具的变动
        'ci', // CI配置变动
        'chore', // 其他不修改src或test的变动
        'revert', // 回退
      ],
    ],
    'subject-case': [0], // 允许subject使用任意大小写
  },
}