import React, { useState, useRef, useEffect } from 'react';
import './Assessment.css';
import { getApiDomain, API_PATHS, buildApiUrl } from '../config/api';
import MarkdownMessage from '../components/MarkdownMessage';
import 'highlight.js/styles/github-dark.css';

interface Message {
  id: string;
  type: 'user' | 'assistant';
  content: string;
  timestamp: Date;
  isStreaming?: boolean;
}

interface ChatSession {
  id: string;
  title: string;
  messages: Message[];
  createdAt: Date;
}

const Assessment: React.FC = () => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputText, setInputText] = useState('');
  const [isRecording, setIsRecording] = useState(false);
  const [inputMode, setInputMode] = useState<'text' | 'voice'>('text');
  const [showChatHistory, setShowChatHistory] = useState(false);
  const [showUploadMenu, setShowUploadMenu] = useState(false);
  const [chatSessions, setChatSessions] = useState<ChatSession[]>([]);
  const [currentSessionId, setCurrentSessionId] = useState<string>('');
  const [isStreaming, setIsStreaming] = useState(false);
  const [useEnhancedChat, setUseEnhancedChat] = useState(true);
  const [enableFunctionCall, setEnableFunctionCall] = useState(true);
  const [enableRAG, setEnableRAG] = useState(true);
  const [showSettings, setShowSettings] = useState(false);
  
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const cameraInputRef = useRef<HTMLInputElement>(null);

  // 滚动到底部
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // 初始化默认会话
  useEffect(() => {
    const defaultSession: ChatSession = {
      id: 'default',
      title: '新的测评对话',
      messages: [],
      createdAt: new Date()
    };
    setChatSessions([defaultSession]);
    setCurrentSessionId('default');
  }, []);

  // 发送消息
  const sendMessage = async (content: string, type: 'text' | 'image' | 'file' = 'text') => {
    if (!content.trim()) return;

    const userMessage: Message = {
      id: Date.now().toString(),
      type: 'user',
      content,
      timestamp: new Date()
    };

    setMessages(prev => [...prev, userMessage]);
    setInputText('');
    setIsStreaming(true);

    // 创建AI响应消息
    const assistantMessage: Message = {
      id: (Date.now() + 1).toString(),
      type: 'assistant',
      content: '',
      timestamp: new Date(),
      isStreaming: true
    };

    setMessages(prev => [...prev, assistantMessage]);

    try {
      // 调用真实的后端流式API
      const formData = new URLSearchParams();
      formData.append('message', content);
      formData.append('userId', 'miniapp_user'); // 可以根据实际用户ID设置

      // 根据设置选择API端点
      let apiPath: string;
      if (useEnhancedChat) {
        formData.append('enableFunctionCall', String(enableFunctionCall));
        formData.append('enableRAG', String(enableRAG));
        apiPath = API_PATHS.CONSUMER.ENHANCED_CHAT.STREAM;
      } else {
        apiPath = API_PATHS.CONSUMER.CHAT.STREAM;
      }
      apiPath = API_PATHS.CONSUMER.CHAT.STREAM;

      const apiUrl = buildApiUrl(getApiDomain('consumer'), apiPath);
      const response = await fetch(apiUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: formData
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      // 读取流式响应
      const reader = response.body?.getReader();
      const decoder = new TextDecoder();
      let accumulatedContent = '';

      if (reader) {
        while (true) {
          const { done, value } = await reader.read();

          if (done) break;

          // 解码数据块
          const chunk = decoder.decode(value, { stream: true });
          accumulatedContent += chunk;

          // 更新消息内容
          setMessages(prev => prev.map(msg =>
            msg.id === assistantMessage.id
              ? { ...msg, content: accumulatedContent }
              : msg
          ));

          // 添加一点延迟以模拟打字效果
          await new Promise(resolve => setTimeout(resolve, 50));
        }
      }

      // 完成流式输出
      setMessages(prev => prev.map(msg =>
        msg.id === assistantMessage.id
          ? { ...msg, isStreaming: false }
          : msg
      ));

    } catch (error) {
      console.error('API调用失败:', error);

      // 错误处理 - 显示错误消息
      const errorContent = `抱歉，服务暂时不可用。请检查网络连接或稍后重试。\n\n错误信息: ${error instanceof Error ? error.message : '未知错误'}`;

      setMessages(prev => prev.map(msg =>
        msg.id === assistantMessage.id
          ? { ...msg, content: errorContent, isStreaming: false }
          : msg
      ));
    }

    setIsStreaming(false);
  };

  // 语音录制
  const toggleRecording = () => {
    if (isRecording) {
      // 停止录制
      setIsRecording(false);
      // 这里应该处理语音识别
      sendMessage("这是语音转换的文本内容");
    } else {
      // 开始录制
      setIsRecording(true);
      // 这里应该开始录音
    }
  };

  // 文件上传
  const handleFileUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      const fileName = file.name;
      sendMessage(`📎 已上传文件: ${fileName}`, 'file');
    }
    setShowUploadMenu(false);
  };

  // 拍照上传
  const handleCameraCapture = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      sendMessage(`📷 已拍摄照片`, 'image');
    }
    setShowUploadMenu(false);
  };

  // 创建新会话
  const createNewSession = () => {
    const newSession: ChatSession = {
      id: Date.now().toString(),
      title: `测评对话 ${chatSessions.length + 1}`,
      messages: [],
      createdAt: new Date()
    };
    setChatSessions(prev => [newSession, ...prev]);
    setCurrentSessionId(newSession.id);
    setMessages([]);
    setShowChatHistory(false);
  };

  // 切换会话
  const switchSession = (sessionId: string) => {
    const session = chatSessions.find(s => s.id === sessionId);
    if (session) {
      setCurrentSessionId(sessionId);
      setMessages(session.messages);
      setShowChatHistory(false);
    }
  };

  return (
    <div className="assessment-container">
      {/* 顶部导航栏 */}
      <div className="assessment-header">
        <button
          className="history-button"
          onClick={() => setShowChatHistory(!showChatHistory)}
        >
          ☰
        </button>
        <h2>AI 测评助手</h2>
        <div className="header-actions">
          <button
            className="settings-button"
            onClick={() => setShowSettings(!showSettings)}
            title="设置"
          >
            ⚙️
          </button>
          <button className="new-chat-button" onClick={createNewSession}>
            ✏️
          </button>
        </div>
      </div>

      {/* 聊天历史侧边栏 */}
      {showChatHistory && (
        <div className="chat-history-overlay" onClick={() => setShowChatHistory(false)}>
          <div className="chat-history-panel" onClick={e => e.stopPropagation()}>
            <div className="history-header">
              <h3>聊天记录</h3>
              <button onClick={() => setShowChatHistory(false)}>✕</button>
            </div>
            <div className="history-list">
              {chatSessions.map(session => (
                <div
                  key={session.id}
                  className={`history-item ${currentSessionId === session.id ? 'active' : ''}`}
                  onClick={() => switchSession(session.id)}
                >
                  <div className="history-title">{session.title}</div>
                  <div className="history-time">
                    {session.createdAt.toLocaleDateString()}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* 设置面板 */}
      {showSettings && (
        <div className="settings-overlay" onClick={() => setShowSettings(false)}>
          <div className="settings-panel" onClick={e => e.stopPropagation()}>
            <div className="settings-header">
              <h3>聊天设置</h3>
              <button onClick={() => setShowSettings(false)}>✕</button>
            </div>
            <div className="settings-content">
              <div className="setting-group">
                <label className="setting-label">
                  <input
                    type="checkbox"
                    checked={useEnhancedChat}
                    onChange={(e) => setUseEnhancedChat(e.target.checked)}
                  />
                  <span>使用增强聊天 (Function Call + RAG)</span>
                </label>
                <p className="setting-description">
                  启用后可以调用函数和检索知识库，提供更智能的回答
                </p>
              </div>

              {useEnhancedChat && (
                <>
                  <div className="setting-group">
                    <label className="setting-label">
                      <input
                        type="checkbox"
                        checked={enableFunctionCall}
                        onChange={(e) => setEnableFunctionCall(e.target.checked)}
                      />
                      <span>启用Function Call</span>
                    </label>
                    <p className="setting-description">
                      允许AI调用函数获取实时信息（时间、天气、计算等）
                    </p>
                  </div>

                  <div className="setting-group">
                    <label className="setting-label">
                      <input
                        type="checkbox"
                        checked={enableRAG}
                        onChange={(e) => setEnableRAG(e.target.checked)}
                      />
                      <span>启用RAG检索</span>
                    </label>
                    <p className="setting-description">
                      从知识库检索相关信息，提供更准确的技术回答
                    </p>
                  </div>
                </>
              )}

              <div className="setting-group">
                <h4>当前配置</h4>
                <div className="config-info">
                  <p>聊天模式: {useEnhancedChat ? '增强模式' : '基础模式'}</p>
                  {useEnhancedChat && (
                    <>
                      <p>Function Call: {enableFunctionCall ? '启用' : '禁用'}</p>
                      <p>RAG检索: {enableRAG ? '启用' : '禁用'}</p>
                    </>
                  )}
                  <p>API地址: {getApiDomain('consumer')}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 消息列表 */}
      <div className="messages-container">
        {messages.length === 0 ? (
          <div className="welcome-message">
            <div className="welcome-icon">🤖</div>
            <h3>欢迎使用AI测评助手</h3>
            <p>我可以帮助您进行学习能力测评、知识点检测和个性化学习建议</p>
            <div className="quick-actions">
              <button onClick={() => sendMessage("我想进行编程能力测评")}>
                编程能力测评
              </button>
              <button onClick={() => sendMessage("帮我分析学习进度")}>
                学习进度分析
              </button>
              <button onClick={() => sendMessage("推荐适合的学习路径")}>
                学习路径推荐
              </button>
            </div>
          </div>
        ) : (
          messages.map(message => (
            <div key={message.id} className={`message ${message.type}`}>
              <div className="message-avatar">
                {message.type === 'user' ? '👤' : '🤖'}
              </div>
              <div className="message-content">
                <div className="message-text">
                  {message.type === 'assistant' ? (
                    <MarkdownMessage
                      content={message.content}
                      isStreaming={message.isStreaming}
                    />
                  ) : (
                    <>
                      {message.content}
                      {message.isStreaming && <span className="cursor">|</span>}
                    </>
                  )}
                </div>
                <div className="message-time">
                  {message.timestamp.toLocaleTimeString()}
                </div>
              </div>
            </div>
          ))
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* 输入区域 */}
      <div className="input-container">
        {/* 上传菜单 */}
        {showUploadMenu && (
          <div className="upload-menu">
            <button onClick={() => fileInputRef.current?.click()}>
              📁 本地文件
            </button>
            <button onClick={() => cameraInputRef.current?.click()}>
              📷 拍照
            </button>
            <button>
              🖼️ 相册
            </button>
          </div>
        )}

        <div className="input-bar">
          <button 
            className="add-button"
            onClick={() => setShowUploadMenu(!showUploadMenu)}
          >
            ➕
          </button>

          {inputMode === 'text' ? (
            <input
              type="text"
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              placeholder="输入您的问题..."
              className="text-input"
              onKeyPress={(e) => e.key === 'Enter' && sendMessage(inputText)}
              disabled={isStreaming}
            />
          ) : (
            <div className="voice-input">
              <button 
                className={`voice-button ${isRecording ? 'recording' : ''}`}
                onClick={toggleRecording}
              >
                {isRecording ? '🔴 录制中...' : '🎤 点击录音'}
              </button>
            </div>
          )}

          <button 
            className="mode-toggle"
            onClick={() => setInputMode(inputMode === 'text' ? 'voice' : 'text')}
          >
            {inputMode === 'text' ? '🎤' : '⌨️'}
          </button>

          {inputMode === 'text' && (
            <button 
              className="send-button"
              onClick={() => sendMessage(inputText)}
              disabled={!inputText.trim() || isStreaming}
            >
              ➤
            </button>
          )}
        </div>
      </div>

      {/* 隐藏的文件输入 */}
      <input
        ref={fileInputRef}
        type="file"
        style={{ display: 'none' }}
        onChange={handleFileUpload}
        accept="*/*"
      />
      <input
        ref={cameraInputRef}
        type="file"
        style={{ display: 'none' }}
        onChange={handleCameraCapture}
        accept="image/*"
        capture="environment"
      />
    </div>
  );
};

export default Assessment;
