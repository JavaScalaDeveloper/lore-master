const fs = require('fs');
const path = require('path');

// 创建assets目录
const assetsDir = path.join(__dirname, '../assets');
if (!fs.existsSync(assetsDir)) {
  fs.mkdirSync(assetsDir, { recursive: true });
}

// 创建一个简单的PNG文件（1x1像素的透明PNG）
const createSimplePng = (filename) => {
  // 这是一个1x1像素的透明PNG文件的base64编码
  const pngData = 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==';
  const buffer = Buffer.from(pngData, 'base64');
  fs.writeFileSync(path.join(assetsDir, filename), buffer);
  console.log(`Created ${filename}`);
};

// 创建所需的图像文件
createSimplePng('icon.png');
createSimplePng('splash.png');
createSimplePng('adaptive-icon.png');
createSimplePng('favicon.png');

console.log('All simple placeholder images created successfully!');