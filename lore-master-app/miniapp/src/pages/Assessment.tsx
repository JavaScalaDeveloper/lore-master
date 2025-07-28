import React, { useState, useRef, useEffect } from 'react';
import './Assessment.css';

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

    // 模拟AI流式响应
    const assistantMessage: Message = {
      id: (Date.now() + 1).toString(),
      type: 'assistant',
      content: '',
      timestamp: new Date(),
      isStreaming: true
    };

    setMessages(prev => [...prev, assistantMessage]);

    // 模拟流式输出
    const responses = [
      "这是一个很好的问题！让我来为您分析一下...",
      "根据您的描述，我建议从以下几个方面来考虑：",
      "1. 首先需要明确您的学习目标和当前水平",
      "2. 然后制定合适的学习计划和时间安排",
      "3. 最后选择适合的学习资源和方法",
      "希望这些建议对您有帮助！如果您还有其他问题，请随时告诉我。"
    ];

    for (let i = 0; i < responses.length; i++) {
      await new Promise(resolve => setTimeout(resolve, 500));
      setMessages(prev => prev.map(msg => 
        msg.id === assistantMessage.id 
          ? { ...msg, content: responses.slice(0, i + 1).join('\n\n') }
          : msg
      ));
    }

    setMessages(prev => prev.map(msg => 
      msg.id === assistantMessage.id 
        ? { ...msg, isStreaming: false }
        : msg
    ));
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
        <button className="new-chat-button" onClick={createNewSession}>
          ✏️
        </button>
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
                  {message.content}
                  {message.isStreaming && <span className="cursor">|</span>}
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
