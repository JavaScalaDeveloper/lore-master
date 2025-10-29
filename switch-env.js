/**
 * 环境切换脚本
 * 用于一键切换后端域名：localhost <-> ly112978940c.vicp.fun
 *
 * 使用方法：
 * node switch-env.js local     # 切换到本地环境 (localhost:8082)
 * node switch-env.js remote    # 切换到远程环境 (ly112978940c.vicp.fun)
 * node switch-env.js status    # 查看当前环境状态
 */

const fs = require('fs');
const path = require('path');

const ENV_FILE_PATH = path.join(__dirname, 'src/config/env.ts');

function readEnvFile() {
    if (!fs.existsSync(ENV_FILE_PATH)) {
        console.error('❌ 环境配置文件不存在:', ENV_FILE_PATH);
        process.exit(1);
    }
    return fs.readFileSync(ENV_FILE_PATH, 'utf8');
}

function writeEnvFile(content) {
    fs.writeFileSync(ENV_FILE_PATH, content, 'utf8');
}

function switchToLocal() {
    let content = readEnvFile();
    content = content.replace(
        /USE_REMOTE_URL:\s*(true|false)/,
        'USE_REMOTE_URL: false'
    );
    writeEnvFile(content);
    console.log('✅ 已切换到本地环境');
    console.log('🌐 后端地址: http://localhost:8082');
    console.log('📝 请重新构建小程序: npm run build:weapp');
}

function switchToRemote() {
    let content = readEnvFile();
    content = content.replace(
        /USE_REMOTE_URL:\s*(true|false)/,
        'USE_REMOTE_URL: true'
    );
    writeEnvFile(content);
    console.log('✅ 已切换到远程环境');
    console.log('📝 请重新构建小程序: npm run build:weapp');
}

function showStatus() {
    const content = readEnvFile();
    const useRemoteMatch = content.match(/USE_REMOTE_URL:\s*(true|false)/);

    if (!useRemoteMatch) {
        console.error('❌ 无法解析环境配置');
        return;
    }

    const isRemote = useRemoteMatch[1] === 'true';

    console.log('📊 当前环境状态:');
    console.log('==================');
    if (isRemote) {
        console.log('🔗 环境: 远程开发环境');
    } else {
        console.log('🏠 环境: 本地开发环境');
        console.log('🌐 后端地址: http://localhost:8082');
    }
    console.log('==================');
}

function showHelp() {
    console.log('🔧 环境切换工具');
    console.log('');
    console.log('使用方法:');
    console.log('  node switch-env.js local     # 切换到本地环境 (localhost:8082)');
    console.log('  node switch-env.js remote    # 切换到远程环境 (ly112978940c.vicp.fun)');
    console.log('  node switch-env.js status    # 查看当前环境状态');
    console.log('  node switch-env.js help      # 显示帮助信息');
    console.log('');
    console.log('注意: 切换环境后需要重新构建小程序');
}

// 主程序
const command = process.argv[2];

switch (command) {
    case 'local':
        switchToLocal();
        break;
    case 'remote':
        switchToRemote();
        break;
    case 'status':
        showStatus();
        break;
    case 'help':
    case '--help':
    case '-h':
        showHelp();
        break;
    default:
        console.log('❌ 无效的命令:', command || '(空)');
        console.log('');
        showHelp();
        process.exit(1);
}
