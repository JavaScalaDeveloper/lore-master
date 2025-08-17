package com.lore.master.service.business.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lore.master.data.entity.storage.FileStorage;
import com.lore.master.data.repository.storage.FileStorageRepository;
import com.lore.master.service.business.MarkdownProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Markdown处理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarkdownProcessingServiceImpl implements MarkdownProcessingService {

    private final FileStorageRepository fileStorageRepository;
    private final ObjectMapper objectMapper;

    // 匹配Markdown中的图片和链接引用的正则表达式
    private static final Pattern FILE_REFERENCE_PATTERN = Pattern.compile(
        "!\\[([^\\]]*)\\]\\((/api/file/view\\?fileId=([a-zA-Z0-9\\-_]+))\\)|" +  // 图片引用
        "\\[([^\\]]*)\\]\\((/api/file/view\\?fileId=([a-zA-Z0-9\\-_]+))\\)"      // 链接引用
    );

    @Override
    public String convertMarkdownToHtml(String markdown) {
        if (!StringUtils.hasText(markdown)) {
            return "";
        }

        try {
            Parser parser = Parser.builder().build();
            Node document = parser.parse(markdown);
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            return renderer.render(document);
        } catch (Exception e) {
            log.error("Markdown转HTML失败", e);
            return markdown; // 转换失败时返回原始内容
        }
    }

    @Override
    public List<String> extractFileReferences(String markdown) {
        if (!StringUtils.hasText(markdown)) {
            return new ArrayList<>();
        }

        Set<String> fileIds = new HashSet<>();
        Matcher matcher = FILE_REFERENCE_PATTERN.matcher(markdown);
        
        while (matcher.find()) {
            String fileId = matcher.group(3); // 图片的fileId
            if (fileId == null) {
                fileId = matcher.group(6); // 链接的fileId
            }
            if (fileId != null) {
                fileIds.add(fileId);
            }
        }

        return new ArrayList<>(fileIds);
    }

    @Override
    public String processFileReferences(String markdown, Map<String, String> fileIdMapping) {
        if (!StringUtils.hasText(markdown) || fileIdMapping == null || fileIdMapping.isEmpty()) {
            return markdown;
        }

        String processedMarkdown = markdown;
        
        for (Map.Entry<String, String> entry : fileIdMapping.entrySet()) {
            String tempId = entry.getKey();
            String finalId = entry.getValue();
            
            // 替换图片引用
            processedMarkdown = processedMarkdown.replaceAll(
                "!\\[([^\\]]*)\\]\\((/api/file/view\\?fileId=" + Pattern.quote(tempId) + ")\\)",
                "![$1](/api/file/view?fileId=" + finalId + ")"
            );
            
            // 替换链接引用
            processedMarkdown = processedMarkdown.replaceAll(
                "\\[([^\\]]*)\\]\\((/api/file/view\\?fileId=" + Pattern.quote(tempId) + ")\\)",
                "[$1](/api/file/view?fileId=" + finalId + ")"
            );
        }

        return processedMarkdown;
    }

    @Override
    public List<String> validateFileReferences(String markdown) {
        List<String> fileIds = extractFileReferences(markdown);
        List<String> invalidFileIds = new ArrayList<>();

        for (String fileId : fileIds) {
            Optional<FileStorage> fileOpt = fileStorageRepository.findByFileId(fileId);
            if (fileOpt.isEmpty() || fileOpt.get().getStatus() != 1) {
                invalidFileIds.add(fileId);
            }
        }

        return invalidFileIds;
    }

    @Override
    public void cleanupUnusedFiles(List<String> oldFileIds, List<String> newFileIds, String uploadUserId) {
        if (oldFileIds == null || oldFileIds.isEmpty()) {
            return;
        }

        Set<String> newFileIdSet = newFileIds != null ? new HashSet<>(newFileIds) : new HashSet<>();
        
        // 找出不再使用的文件ID
        List<String> unusedFileIds = oldFileIds.stream()
            .filter(fileId -> !newFileIdSet.contains(fileId))
            .collect(Collectors.toList());

        if (unusedFileIds.isEmpty()) {
            return;
        }

        log.info("清理未使用的文件，用户：{}，文件数量：{}", uploadUserId, unusedFileIds.size());

        // 标记文件为已删除（软删除）
        for (String fileId : unusedFileIds) {
            try {
                Optional<FileStorage> fileOpt = fileStorageRepository.findByFileId(fileId);
                if (fileOpt.isPresent()) {
                    FileStorage file = fileOpt.get();
                    // 只删除属于当前用户的文件
                    if (uploadUserId.equals(file.getUploadUserId())) {
                        file.setStatus(0); // 标记为已删除
                        fileStorageRepository.save(file);
                        log.debug("已删除文件：{}", fileId);
                    }
                }
            } catch (Exception e) {
                log.warn("删除文件失败：{}，错误：{}", fileId, e.getMessage());
            }
        }
    }

    /**
     * 将文件ID列表转换为JSON字符串
     */
    public String fileIdsToJson(List<String> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(fileIds);
        } catch (Exception e) {
            log.error("文件ID列表转JSON失败", e);
            return null;
        }
    }

    /**
     * 将JSON字符串转换为文件ID列表
     */
    public List<String> jsonToFileIds(String json) {
        if (!StringUtils.hasText(json)) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("JSON转文件ID列表失败", e);
            return new ArrayList<>();
        }
    }
}
