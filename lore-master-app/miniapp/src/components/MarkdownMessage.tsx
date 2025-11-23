import React from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeHighlight from 'rehype-highlight';
import './MarkdownMessage.css';

interface MarkdownMessageProps {
  content: string;
  isStreaming?: boolean;
}

const MarkdownMessage: React.FC<MarkdownMessageProps> = ({ content, isStreaming }) => {
  return (
    <div className="markdown-message">
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        rehypePlugins={[rehypeHighlight]}
        components={{
          // 自定义代码块渲染
          code: ({ className, children, ...props }: any) => {
            const match = /language-(\w+)/.exec(className || '');
            const isInline = !className || !match;

            return !isInline ? (
              <div className="code-block-wrapper">
                <div className="code-block-header">
                  <span className="code-language">{match[1]}</span>
                  <button
                    className="copy-button"
                    onClick={() => {
                      navigator.clipboard.writeText(String(children).replace(/\n$/, ''));
                    }}
                  >
                    📋 复制
                  </button>
                </div>
                <pre className={className}>
                  <code>{children}</code>
                </pre>
              </div>
            ) : (
              <code className={`inline-code ${className || ''}`}>
                {children}
              </code>
            );
          },
          // 自定义表格渲染
          table: ({ children }: any) => (
            <div className="table-wrapper">
              <table>{children}</table>
            </div>
          ),
          // 自定义链接渲染
          a: ({ href, children }: any) => (
            <a
              href={href}
              target="_blank"
              rel="noopener noreferrer"
              className="markdown-link"
            >
              {children}
            </a>
          ),
          // 自定义列表渲染
          ul: ({ children }: any) => (
            <ul className="markdown-list">{children}</ul>
          ),
          ol: ({ children }: any) => (
            <ol className="markdown-list">{children}</ol>
          ),
          // 自定义标题渲染
          h1: ({ children }: any) => <h1 className="markdown-h1">{children}</h1>,
          h2: ({ children }: any) => <h2 className="markdown-h2">{children}</h2>,
          h3: ({ children }: any) => <h3 className="markdown-h3">{children}</h3>,
          h4: ({ children }: any) => <h4 className="markdown-h4">{children}</h4>,
          h5: ({ children }: any) => <h5 className="markdown-h5">{children}</h5>,
          h6: ({ children }: any) => <h6 className="markdown-h6">{children}</h6>,
          // 自定义引用块渲染
          blockquote: ({ children }: any) => (
            <blockquote className="markdown-blockquote">{children}</blockquote>
          ),
          // 自定义段落渲染
          p: ({ children }: any) => <p className="markdown-paragraph">{children}</p>,
          // 自定义强调渲染
          strong: ({ children }: any) => <strong className="markdown-strong">{children}</strong>,
          em: ({ children }: any) => <em className="markdown-em">{children}</em>,
        }}
      >
        {content}
      </ReactMarkdown>
      {isStreaming && <span className="streaming-cursor">|</span>}
    </div>
  );
};

export default MarkdownMessage;
