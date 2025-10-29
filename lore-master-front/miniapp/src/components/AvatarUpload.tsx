import React, { useState, useRef } from 'react';
import { DOMAINS } from '../config/api';
import './AvatarUpload.css';

interface AvatarUploadProps {
  currentAvatar?: string;
  userId: string;
  onUploadSuccess?: (avatarInfo: any) => void;
  onUploadError?: (error: string) => void;
  size?: number;
  className?: string;
}

interface UploadResponse {
  fileId: string;
  fileName: string;
  fileSize: number;
  fileSizeFormatted: string;
  accessUrl: string;
  downloadUrl: string;
  uploadTime: string;
}

const AvatarUpload: React.FC<AvatarUploadProps> = ({
  currentAvatar,
  userId,
  onUploadSuccess,
  onUploadError,
  size = 80,
  className = ''
}) => {
  const [uploading, setUploading] = useState(false);
  const [previewUrl, setPreviewUrl] = useState<string>(currentAvatar || '');
  const fileInputRef = useRef<HTMLInputElement>(null);

  /**
   * 处理文件选择
   */
  const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    // 验证文件类型
    if (!file.type.startsWith('image/')) {
      const error = '请选择图片文件';
      onUploadError?.(error);
      alert(error);
      return;
    }

    // 验证文件大小（5MB）
    if (file.size > 5 * 1024 * 1024) {
      const error = '图片大小不能超过5MB';
      onUploadError?.(error);
      alert(error);
      return;
    }

    // 预览图片
    const reader = new FileReader();
    reader.onload = (e) => {
      setPreviewUrl(e.target?.result as string);
    };
    reader.readAsDataURL(file);

    // 上传文件
    uploadAvatar(file);
  };

  /**
   * 上传头像
   */
  const uploadAvatar = async (file: File) => {
    setUploading(true);

    try {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('userId', userId);

      console.log('🚀 开始上传头像:', {
        fileName: file.name,
        fileSize: file.size,
        fileType: file.type,
        userId: userId
      });

      const response = await fetch(`${DOMAINS.CONSUMER_API}/api/user/avatar/upload`, {
        method: 'POST',
        body: formData,
        headers: {
          // 不设置Content-Type，让浏览器自动设置multipart/form-data边界
        }
      });

      const result = await response.json();

      if (result.success) {
        const uploadData: UploadResponse = result.data;
        
        console.log('✅ 头像上传成功:', uploadData);

        // 更新预览图片为服务器返回的访问地址
        setPreviewUrl(uploadData.accessUrl);

        // 调用成功回调
        onUploadSuccess?.(uploadData);

        alert('头像上传成功！');
      } else {
        throw new Error(result.message || '上传失败');
      }

    } catch (error) {
      console.error('❌ 头像上传失败:', error);
      
      const errorMessage = error instanceof Error ? error.message : '上传失败，请重试';
      onUploadError?.(errorMessage);
      alert(`头像上传失败: ${errorMessage}`);

      // 恢复原来的头像
      setPreviewUrl(currentAvatar || '');
    } finally {
      setUploading(false);
      
      // 清空文件输入框
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  /**
   * 点击头像触发文件选择
   */
  const handleAvatarClick = () => {
    if (uploading) return;
    fileInputRef.current?.click();
  };

  /**
   * 获取头像显示URL
   */
  const getAvatarUrl = () => {
    if (previewUrl) {
      // 如果是相对路径，添加API基础URL
      if (previewUrl.startsWith('/api/')) {
        return `${DOMAINS.CONSUMER_API}${previewUrl}`;
      }
      return previewUrl;
    }

    // 默认头像
    return `data:image/svg+xml;base64,${btoa(`
      <svg width="${size}" height="${size}" viewBox="0 0 ${size} ${size}" fill="none" xmlns="http://www.w3.org/2000/svg">
        <circle cx="${size/2}" cy="${size/2}" r="${size/2}" fill="#f0f0f0"/>
        <circle cx="${size/2}" cy="${size/3}" r="${size/6}" fill="#ccc"/>
        <path d="M${size/4} ${size*3/4} Q${size/2} ${size*2/3} ${size*3/4} ${size*3/4}" stroke="#ccc" stroke-width="2" fill="none"/>
      </svg>
    `)}`;
  };

  return (
    <div className={`avatar-upload ${className}`}>
      <div 
        className={`avatar-container ${uploading ? 'uploading' : ''}`}
        onClick={handleAvatarClick}
        style={{ width: size, height: size }}
      >
        <img
          src={getAvatarUrl()}
          alt="用户头像"
          className="avatar-image"
          style={{ width: size, height: size }}
          onError={(e) => {
            // 图片加载失败时显示默认头像
            const target = e.target as HTMLImageElement;
            target.src = `data:image/svg+xml;base64,${btoa(`
              <svg width="${size}" height="${size}" viewBox="0 0 ${size} ${size}" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="${size/2}" cy="${size/2}" r="${size/2}" fill="#f0f0f0"/>
                <circle cx="${size/2}" cy="${size/3}" r="${size/6}" fill="#ccc"/>
                <path d="M${size/4} ${size*3/4} Q${size/2} ${size*2/3} ${size*3/4} ${size*3/4}" stroke="#ccc" stroke-width="2" fill="none"/>
              </svg>
            `)}`;
          }}
        />
        
        {uploading && (
          <div className="upload-overlay">
            <div className="upload-spinner"></div>
            <span className="upload-text">上传中...</span>
          </div>
        )}
        
        {!uploading && (
          <div className="upload-hint">
            <span className="upload-icon">📷</span>
            <span className="upload-text">点击更换</span>
          </div>
        )}
      </div>

      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        onChange={handleFileSelect}
        style={{ display: 'none' }}
      />

      <div className="avatar-tips">
        <p>支持 JPG、PNG、GIF、WebP 格式</p>
        <p>文件大小不超过 5MB</p>
      </div>
    </div>
  );
};

export default AvatarUpload;
