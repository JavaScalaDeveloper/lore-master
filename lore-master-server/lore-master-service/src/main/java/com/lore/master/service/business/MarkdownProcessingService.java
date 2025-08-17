package com.lore.master.service.business;

import java.util.List;

/**
 * Markdown处理服务接口
 */
public interface MarkdownProcessingService {

    /**
     * 将Markdown转换为HTML
     * 
     * @param markdown Markdown内容
     * @return HTML内容
     */
    String convertMarkdownToHtml(String markdown);

    /**
     * 从Markdown内容中提取文件引用
     * 
     * @param markdown Markdown内容
     * @return 文件ID列表
     */
    List<String> extractFileReferences(String markdown);

    /**
     * 处理Markdown中的文件引用，将临时文件ID替换为正式文件ID
     * 
     * @param markdown 原始Markdown内容
     * @param fileIdMapping 文件ID映射关系（临时ID -> 正式ID）
     * @return 处理后的Markdown内容
     */
    String processFileReferences(String markdown, java.util.Map<String, String> fileIdMapping);

    /**
     * 验证Markdown内容中的文件引用是否有效
     * 
     * @param markdown Markdown内容
     * @return 无效的文件ID列表
     */
    List<String> validateFileReferences(String markdown);

    /**
     * 清理未使用的文件引用
     * 
     * @param oldFileIds 旧的文件ID列表
     * @param newFileIds 新的文件ID列表
     * @param uploadUserId 上传用户ID
     */
    void cleanupUnusedFiles(List<String> oldFileIds, List<String> newFileIds, String uploadUserId);
}
