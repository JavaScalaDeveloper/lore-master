import React, { useState } from 'react';
import { Card, Input, Button, Space, Typography, Divider, message } from 'antd';
import { 
  encryptPasswordForTransmission, 
  decryptPasswordFromTransmission,
  aesEncrypt,
  aesDecrypt,
  md5Hash,
  sha256Hash,
  getPasswordStrength,
  PASSWORD_STRENGTH_LABELS,
  PASSWORD_STRENGTH_COLORS
} from '../../../../utils/crypto';

const { Title, Text, Paragraph } = Typography;
const { TextArea } = Input;

const CryptoTest: React.FC = () => {
  const [plainText, setPlainText] = useState('admin123');
  const [encryptedText, setEncryptedText] = useState('');
  const [decryptedText, setDecryptedText] = useState('');
  const [hashResult, setHashResult] = useState('');

  // 加密测试
  const handleEncrypt = () => {
    try {
      const encrypted = encryptPasswordForTransmission(plainText);
      setEncryptedText(encrypted);
      message.success('加密成功！');
    } catch (error) {
      message.error('加密失败：' + (error as Error).message);
    }
  };

  // 解密测试
  const handleDecrypt = () => {
    try {
      const decrypted = decryptPasswordFromTransmission(encryptedText);
      setDecryptedText(decrypted);
      message.success('解密成功！');
    } catch (error) {
      message.error('解密失败：' + (error as Error).message);
    }
  };

  // 哈希测试
  const handleHash = () => {
    try {
      const md5 = md5Hash(plainText);
      const sha256 = sha256Hash(plainText);
      setHashResult(`MD5: ${md5}\n\nSHA-256: ${sha256}`);
      message.success('哈希计算成功！');
    } catch (error) {
      message.error('哈希计算失败：' + (error as Error).message);
    }
  };

  // 密码强度测试
  const passwordStrength = getPasswordStrength(plainText);
  const strengthLabel = PASSWORD_STRENGTH_LABELS[passwordStrength as keyof typeof PASSWORD_STRENGTH_LABELS];
  const strengthColor = PASSWORD_STRENGTH_COLORS[passwordStrength as keyof typeof PASSWORD_STRENGTH_COLORS];

  // 登录测试
  const handleLoginTest = async () => {
    try {
      const encryptedPassword = encryptPasswordForTransmission(plainText);
      
      const response = await fetch('/api/admin/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: 'admin',
          password: encryptedPassword,
        }),
      });

      const result = await response.json();
      
      if (result.code === 200) {
        message.success('登录测试成功！Token: ' + result.data.token.substring(0, 20) + '...');
      } else {
        message.error('登录测试失败：' + result.message);
      }
    } catch (error) {
      message.error('登录测试失败：' + (error as Error).message);
    }
  };

  return (
    <div style={{ padding: 24 }}>
      <Title level={2}>🔐 加密工具测试页面</Title>
      <Paragraph>
        这个页面用于测试前后端密码加解密功能。数据库中存储明文密码，前后端传输时进行AES加密。
      </Paragraph>

      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        {/* 基本加解密测试 */}
        <Card title="🔑 AES加解密测试" size="small">
          <Space direction="vertical" style={{ width: '100%' }}>
            <div>
              <Text strong>原文：</Text>
              <Input
                value={plainText}
                onChange={(e) => setPlainText(e.target.value)}
                placeholder="请输入要加密的文本"
                style={{ marginTop: 8 }}
              />
            </div>

            <div>
              <Text strong>密码强度：</Text>
              <span style={{ 
                color: strengthColor, 
                fontWeight: 'bold',
                marginLeft: 8 
              }}>
                {strengthLabel} ({passwordStrength}/5)
              </span>
            </div>

            <Space>
              <Button type="primary" onClick={handleEncrypt}>
                🔒 AES加密
              </Button>
              <Button onClick={handleDecrypt} disabled={!encryptedText}>
                🔓 AES解密
              </Button>
              <Button onClick={handleHash}>
                # 计算哈希
              </Button>
            </Space>

            {encryptedText && (
              <div>
                <Text strong>加密结果：</Text>
                <TextArea
                  value={encryptedText}
                  readOnly
                  rows={3}
                  style={{ marginTop: 8 }}
                />
              </div>
            )}

            {decryptedText && (
              <div>
                <Text strong>解密结果：</Text>
                <Input
                  value={decryptedText}
                  readOnly
                  style={{ 
                    marginTop: 8,
                    backgroundColor: decryptedText === plainText ? '#f6ffed' : '#fff2f0',
                    borderColor: decryptedText === plainText ? '#52c41a' : '#ff4d4f'
                  }}
                />
                <Text type={decryptedText === plainText ? 'success' : 'danger'}>
                  {decryptedText === plainText ? '✅ 解密正确' : '❌ 解密错误'}
                </Text>
              </div>
            )}

            {hashResult && (
              <div>
                <Text strong>哈希结果：</Text>
                <TextArea
                  value={hashResult}
                  readOnly
                  rows={4}
                  style={{ marginTop: 8 }}
                />
              </div>
            )}
          </Space>
        </Card>

        {/* 登录测试 */}
        <Card title="🚀 登录功能测试" size="small">
          <Space direction="vertical" style={{ width: '100%' }}>
            <Paragraph>
              <Text strong>测试说明：</Text>
              <br />
              • 用户名：admin
              <br />
              • 密码：{plainText}（将被AES加密后传输）
              <br />
              • 后端会解密密码并与数据库明文密码比较
            </Paragraph>

            <Button type="primary" size="large" onClick={handleLoginTest}>
              🔐 测试加密登录
            </Button>
          </Space>
        </Card>

        {/* 技术说明 */}
        <Card title="📋 技术说明" size="small">
          <Space direction="vertical">
            <div>
              <Text strong>加密算法：</Text>
              <Text>AES-256-ECB</Text>
            </div>
            <div>
              <Text strong>密钥管理：</Text>
              <Text>前后端共享密钥（实际项目中应使用环境变量）</Text>
            </div>
            <div>
              <Text strong>数据存储：</Text>
              <Text>数据库中存储明文密码</Text>
            </div>
            <div>
              <Text strong>传输安全：</Text>
              <Text>前后端传输时密码经过AES加密</Text>
            </div>
            <div>
              <Text strong>验证流程：</Text>
              <Text>后端接收加密密码 → AES解密 → 与数据库明文比较</Text>
            </div>
          </Space>
        </Card>
      </Space>
    </div>
  );
};

export default CryptoTest;
