import { View, Text, Image, ScrollView } from '@tarojs/components';
import { useEffect, useState } from 'react';
import { useRouter } from '@tarojs/taro';
import Taro from '@tarojs/taro';
import { get } from '../../utils/request';
import './index.css';

// 轮播图详情数据类型
interface CarouselBannerDetail {
  bannerId: string;
  title: string;
  subtitle?: string;
  coverImageUrl: string;
  contentMarkdown?: string;
  contentHtml?: string;
  jumpUrl?: string;
  viewCount: number;
  createdTime: string;
  updatedTime: string;
}

export default function CarouselDetail() {
  const router = useRouter();
  const [detail, setDetail] = useState<CarouselBannerDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 获取轮播图详情
  const loadCarouselDetail = async (bannerId: string) => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await get<CarouselBannerDetail>(`/api/carousel/detail?bannerId=${bannerId}`);
      
      if (response.success && response.data) {
        setDetail(response.data);
      } else {
        setError(response.message || '获取详情失败');
      }
    } catch (error) {
      console.error('获取轮播图详情失败:', error);
      setError('网络错误，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  // 页面加载时获取数据
  useEffect(() => {
    const bannerId = router.params.bannerId;
    if (bannerId) {
      loadCarouselDetail(bannerId as string);
    } else {
      setError('缺少轮播图ID参数');
      setLoading(false);
    }
  }, [router.params.bannerId]);

  // 处理返回
  const handleBack = () => {
    Taro.navigateBack();
  };

  // 处理外部链接点击
  const handleJumpClick = () => {
    if (detail?.jumpUrl) {
      Taro.showModal({
        title: '跳转确认',
        content: `即将跳转到外部链接：${detail.jumpUrl}`,
        success: (res) => {
          if (res.confirm) {
            Taro.setClipboardData({
              data: detail.jumpUrl || '',
              success: () => {
                Taro.showToast({
                  title: '链接已复制到剪贴板',
                  icon: 'success'
                });
              }
            });
          }
        }
      });
    }
  };

  // 格式化时间
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  // 渲染Markdown内容
  const renderMarkdownContent = () => {
    if (!detail) return null;

    let content = '';

    // 优先使用Markdown内容
    if (detail.contentMarkdown) {
      content = detail.contentMarkdown;
      console.log('使用Markdown内容:', content);
    } else if (detail.contentHtml) {
      // 小程序中不能直接渲染HTML，这里简单处理
      content = detail.contentHtml
        .replace(/<[^>]*>/g, '') // 移除HTML标签
        .replace(/&nbsp;/g, ' ')
        .replace(/&lt;/g, '<')
        .replace(/&gt;/g, '>')
        .replace(/&amp;/g, '&');
      console.log('使用HTML转换内容:', content);
    }

    if (!content) {
      return <Text className='content-empty'>暂无详细内容</Text>;
    }

    // 强制使用Markdown渲染
    console.log('开始Markdown渲染，内容长度:', content.length);
    console.log('内容预览:', content.substring(0, 200));

    // 测试Markdown内容
    const testMarkdown = `# 测试标题
## 二级标题
### 三级标题

这是一个普通段落。

- 列表项1
- 列表项2
- 列表项3

1. 有序列表1
2. 有序列表2
3. 有序列表3

> 这是一个引用块

另一个段落。`;

    // 如果内容为空或者很短，使用测试内容
    const finalContent = content.length < 50 ? testMarkdown : content;

    // 确保使用Markdown渲染
    return (
      <View className='markdown-wrapper'>
        <Text className='debug-info'>
          [Markdown渲染模式 - 原始长度: {content.length}, 使用内容长度: {finalContent.length}]
        </Text>
        {renderMarkdownElements(finalContent)}
      </View>
    );
  };

  // 渲染Markdown元素
  const renderMarkdownElements = (markdown: string) => {
    if (!markdown || !markdown.trim()) {
      return <Text className='content-empty'>暂无详细内容</Text>;
    }

    const lines = markdown.split('\n');
    const elements: JSX.Element[] = [];
    let key = 0;

    console.log('开始渲染Markdown，总行数:', lines.length);
    console.log('原始内容:', markdown);

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i];
      const trimmedLine = line.trim();

      if (!trimmedLine) {
        // 空行
        elements.push(<View key={key++} className='markdown-space'></View>);
        continue;
      }

      console.log(`处理第${i}行:`, line);

      // 标题处理 - 按优先级从长到短匹配
      if (trimmedLine.startsWith('### ')) {
        const title = trimmedLine.replace('### ', '').trim();
        console.log('匹配到H3标题:', title);
        elements.push(
          <Text key={key++} className='markdown-h3'>{title}</Text>
        );
      } else if (trimmedLine.startsWith('## ')) {
        const title = trimmedLine.replace('## ', '').trim();
        console.log('匹配到H2标题:', title);
        elements.push(
          <Text key={key++} className='markdown-h2'>{title}</Text>
        );
      } else if (trimmedLine.startsWith('# ')) {
        const title = trimmedLine.replace('# ', '').trim();
        console.log('匹配到H1标题:', title);
        elements.push(
          <Text key={key++} className='markdown-h1'>{title}</Text>
        );
      }
      // 列表处理
      else if (trimmedLine.startsWith('- ')) {
        const text = trimmedLine.replace('- ', '').trim();
        console.log('匹配到无序列表:', text);
        elements.push(
          <View key={key++} className='markdown-list-item'>
            <Text className='markdown-bullet'>•</Text>
            <Text className='markdown-list-text'>{text}</Text>
          </View>
        );
      }
      // 数字列表处理
      else if (/^\d+\.\s/.test(trimmedLine)) {
        const match = trimmedLine.match(/^(\d+)\.\s(.*)$/);
        if (match) {
          console.log('匹配到有序列表:', match[2]);
          elements.push(
            <View key={key++} className='markdown-ordered-item'>
              <Text className='markdown-number'>{match[1]}.</Text>
              <Text className='markdown-list-text'>{match[2].trim()}</Text>
            </View>
          );
        }
      }
      // 引用处理
      else if (trimmedLine.startsWith('> ')) {
        const text = trimmedLine.replace('> ', '').trim();
        console.log('匹配到引用:', text);
        elements.push(
          <View key={key++} className='markdown-quote'>
            <Text className='markdown-quote-text'>{text}</Text>
          </View>
        );
      }
      // 普通段落
      else {
        console.log('匹配到普通段落:', trimmedLine);
        elements.push(
          <Text key={key++} className='markdown-paragraph'>{trimmedLine}</Text>
        );
      }
    }

    console.log('Markdown渲染完成，生成元素数量:', elements.length);

    if (elements.length === 0) {
      return <Text className='content-empty'>内容解析失败</Text>;
    }

    return <View className='markdown-content'>{elements}</View>;
  };

  if (loading) {
    return (
      <View className='carousel-detail'>
        <View className='loading-container'>
          <Text>加载中...</Text>
        </View>
      </View>
    );
  }

  if (error) {
    return (
      <View className='carousel-detail'>
        <View className='error-container'>
          <Text className='error-text'>{error}</Text>
          <View className='error-actions'>
            <Text className='back-button' onClick={handleBack}>返回</Text>
          </View>
        </View>
      </View>
    );
  }

  if (!detail) {
    return (
      <View className='carousel-detail'>
        <View className='error-container'>
          <Text className='error-text'>内容不存在</Text>
          <View className='error-actions'>
            <Text className='back-button' onClick={handleBack}>返回</Text>
          </View>
        </View>
      </View>
    );
  }

  return (
    <View className='carousel-detail'>
      {/* 返回按钮 */}
      <View className='header'>
        <Text className='back-button' onClick={handleBack}>← 返回</Text>
      </View>

      {/* 标题卡片 - 置于最上面 */}
      <View className='title-card'>
        <Text className='main-title'>{detail.title}</Text>
        {detail.subtitle && (
          <Text className='main-subtitle'>{detail.subtitle}</Text>
        )}

        {/* 元信息 */}
        <View className='meta-info'>
          <Text className='meta-item'>📅 {formatDate(detail.createdTime)}</Text>
        </View>
      </View>

      <ScrollView scrollY className='content-scroll'>
        {/* 封面图片 */}
        {detail.coverImageUrl && (
          <View className='cover-container'>
            <Image
              src={detail.coverImageUrl}
              className='cover-image'
              mode='aspectFill'
            />
          </View>
        )}

        {/* 内容区域 */}
        <View className='content-section'>
          {renderMarkdownContent()}
        </View>

        {/* 跳转按钮 */}
        {detail.jumpUrl && (
          <View className='action-section'>
            <Text className='jump-button' onClick={handleJumpClick}>
              了解更多
            </Text>
          </View>
        )}
      </ScrollView>
    </View>
  );
}
