const fs = require('fs');
const path = require('path');
const { createCanvas } = require('canvas');

// 创建assets目录
const assetsDir = path.join(__dirname, '../assets');
if (!fs.existsSync(assetsDir)) {
  fs.mkdirSync(assetsDir, { recursive: true });
}

// 创建画布
const createImage = (width, height, filename) => {
  const canvas = createCanvas(width, height);
  const ctx = canvas.getContext('2d');
  
  // 填充背景色
  ctx.fillStyle = '#4285F4'; // Google蓝
  ctx.fillRect(0, 0, width, height);
  
  // 添加简单的图形
  ctx.fillStyle = '#FFFFFF';
  ctx.beginPath();
  ctx.arc(width/2, height/2, Math.min(width, height)/4, 0, Math.PI * 2);
  ctx.fill();
  
  // 保存图像
  const buffer = canvas.toBuffer('image/png');
  fs.writeFileSync(path.join(assetsDir, filename), buffer);
  console.log(`Created ${filename}`);
};

// 创建不同尺寸的图像
createImage(1024, 1024, 'icon.png'); // App图标
createImage(1242, 2688, 'splash.png'); // 启动画面
createImage(108, 108, 'adaptive-icon.png'); // Android自适应图标
createImage(48, 48, 'favicon.png'); // 网站favicon

console.log('All placeholder images created successfully!');