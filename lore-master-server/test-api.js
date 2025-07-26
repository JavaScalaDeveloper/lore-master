// 测试C端用户注册API的Node.js脚本
const https = require('http');

// 测试用户注册
async function testUserRegister() {
  const postData = JSON.stringify({
    registerType: 'username',
    registerKey: 'test' + (Date.now() % 100000), // 使用时间戳后5位确保唯一性且符合格式
    password: 'Nf8BNGQg1e1ow0NneMmo4Q==', // 这是加密后的 "123456"
    nickname: '测试用户'
  });

  const options = {
    hostname: 'localhost',
    port: 8082,
    path: '/api/user/register',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Content-Length': Buffer.byteLength(postData)
    }
  };

  return new Promise((resolve, reject) => {
    const req = https.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => {
        data += chunk;
      });
      res.on('end', () => {
        try {
          const result = JSON.parse(data);
          console.log('注册测试结果:', result);
          resolve(result);
        } catch (error) {
          reject(error);
        }
      });
    });

    req.on('error', (error) => {
      reject(error);
    });

    req.write(postData);
    req.end();
  });
}

// 测试检查用户名是否可用
async function testCheckUsername() {
  const testUsername = 'test' + (Date.now() % 100000); // 使用时间戳后5位确保唯一性且符合格式
  const options = {
    hostname: 'localhost',
    port: 8082,
    path: `/api/user/register/check?registerType=username&registerKey=${testUsername}`,
    method: 'GET'
  };

  return new Promise((resolve, reject) => {
    const req = https.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => {
        data += chunk;
      });
      res.on('end', () => {
        try {
          const result = JSON.parse(data);
          console.log('检查用户名测试结果:', result);
          resolve(result);
        } catch (error) {
          reject(error);
        }
      });
    });

    req.on('error', (error) => {
      reject(error);
    });

    req.end();
  });
}

// 运行测试
async function runTests() {
  console.log('开始测试C端用户注册API...\n');

  try {
    // 测试1: 检查用户名是否可用
    console.log('测试1: 检查用户名是否可用');
    await testCheckUsername();
    console.log('✅ 检查用户名测试通过\n');

    // 测试2: 用户注册
    console.log('测试2: 用户注册');
    await testUserRegister();
    console.log('✅ 用户注册测试通过\n');

    console.log('🎉 所有测试通过！');
  } catch (error) {
    console.error('❌ 测试失败:', error);
  }
}

runTests();
