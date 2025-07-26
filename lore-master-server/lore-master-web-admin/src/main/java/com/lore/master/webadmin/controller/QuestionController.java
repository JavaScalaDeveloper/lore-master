package com.lore.master.webadmin.controller;

import com.lore.master.common.result.Result;
import com.lore.master.data.entity.Question;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 题目管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/questions")
@RequiredArgsConstructor
public class QuestionController {
    
    /**
     * 分页查询题目
     */
    @PostMapping("/page")
    public Result<Page<Question>> getQuestionPage(@RequestBody Map<String, Object> params) {
        try {
            // 模拟分页数据
            List<Question> questions = createMockQuestions();
            
            int current = (int) params.getOrDefault("current", 1);
            int size = (int) params.getOrDefault("size", 10);
            
            // 简单的分页模拟
            int start = (current - 1) * size;
            int end = Math.min(start + size, questions.size());
            List<Question> pageContent = questions.subList(start, end);
            
            // 创建模拟的Page对象
            Page<Question> page = new org.springframework.data.domain.PageImpl<>(
                pageContent, 
                PageRequest.of(current - 1, size), 
                questions.size()
            );
            
            return Result.success(page);
        } catch (Exception e) {
            log.error("分页查询题目失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据ID查询题目
     */
    @GetMapping("/{id}")
    public Result<Question> getQuestionById(@PathVariable Long id) {
        try {
            Question question = createMockQuestion(id);
            return Result.success(question);
        } catch (Exception e) {
            log.error("查询题目失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 创建题目
     */
    @PostMapping
    public Result<Boolean> createQuestion(@Valid @RequestBody Question question) {
        try {
            log.info("创建题目: {}", question.getTitle());
            return Result.success(true);
        } catch (Exception e) {
            log.error("创建题目失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新题目
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateQuestion(@PathVariable Long id, @Valid @RequestBody Question question) {
        try {
            log.info("更新题目: id={}, title={}", id, question.getTitle());
            return Result.success(true);
        } catch (Exception e) {
            log.error("更新题目失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除题目
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteQuestion(@PathVariable Long id) {
        try {
            log.info("删除题目: id={}", id);
            return Result.success(true);
        } catch (Exception e) {
            log.error("删除题目失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量删除题目
     */
    @DeleteMapping("/batch")
    public Result<Boolean> batchDeleteQuestions(@RequestBody Long[] ids) {
        try {
            log.info("批量删除题目: ids={}", (Object) ids);
            return Result.success(true);
        } catch (Exception e) {
            log.error("批量删除题目失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新题目状态
     */
    @PutMapping("/{id}/status")
    public Result<Boolean> updateQuestionStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            Integer status = (Integer) params.get("status");
            log.info("更新题目状态: id={}, status={}", id, status);
            return Result.success(true);
        } catch (Exception e) {
            log.error("更新题目状态失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取题目统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getQuestionStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalCount", 3456);
            statistics.put("publishedCount", 2890);
            statistics.put("draftCount", 456);
            statistics.put("disabledCount", 110);
            statistics.put("subjectCount", 15);
            statistics.put("totalUsage", 125678);
            statistics.put("avgAccuracy", 78.5);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取题目统计信息失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 创建模拟题目数据
     */
    private List<Question> createMockQuestions() {
        List<Question> questions = new ArrayList<>();
        
        String[] titles = {
            "Java中的多态性是什么？",
            "什么是Spring Boot？",
            "数据库索引的作用是什么？",
            "HTTP和HTTPS的区别？",
            "什么是RESTful API？",
            "JavaScript中的闭包是什么？",
            "什么是Docker容器？",
            "Git和SVN的区别？",
            "什么是微服务架构？",
            "Redis的数据类型有哪些？"
        };
        
        String[] subjects = {"计算机科学", "数据库", "网络技术", "前端开发", "后端开发"};
        Integer[] types = {1, 1, 1, 2, 1, 1, 1, 2, 1, 2}; // 1-单选，2-多选
        Integer[] difficulties = {2, 1, 3, 2, 2, 3, 2, 1, 4, 3};
        
        for (int i = 0; i < titles.length; i++) {
            Question question = new Question();
            question.setId((long) (i + 1));
            question.setTitle(titles[i]);
            question.setContent("这是题目内容：" + titles[i]);
            question.setType(types[i]);
            question.setSubjectId((long) (i % 5 + 1));
            question.setSubjectName(subjects[i % 5]);
            question.setKnowledgePointId((long) (i % 3 + 1));
            question.setKnowledgePointName("知识点" + (i % 3 + 1));
            question.setDifficultyLevel(difficulties[i]);
            question.setOptions("[\"选项A\", \"选项B\", \"选项C\", \"选项D\"]");
            question.setCorrectAnswer("A");
            question.setExplanation("这是答案解析");
            question.setTags("[\"Java\", \"编程\"]");
            question.setScore(5);
            question.setEstimatedTime(120);
            question.setUsageCount((int)(Math.random() * 1000));
            question.setAccuracyRate(Math.random() * 100);
            question.setStatus(i % 10 == 0 ? 2 : 1);
            question.setCreatorId(1L);
            question.setCreatorName("系统管理员");
            questions.add(question);
        }
        
        return questions;
    }
    
    /**
     * 创建单个模拟题目
     */
    private Question createMockQuestion(Long id) {
        Question question = new Question();
        question.setId(id);
        question.setTitle("Java中的多态性是什么？");
        question.setContent("请解释Java中多态性的概念和实现方式");
        question.setType(1);
        question.setSubjectId(1L);
        question.setSubjectName("计算机科学");
        question.setKnowledgePointId(1L);
        question.setKnowledgePointName("面向对象编程");
        question.setDifficultyLevel(2);
        question.setOptions("[\"继承\", \"封装\", \"多态\", \"抽象\"]");
        question.setCorrectAnswer("C");
        question.setExplanation("多态是面向对象编程的重要特性之一");
        question.setTags("[\"Java\", \"面向对象\", \"多态\"]");
        question.setScore(5);
        question.setEstimatedTime(120);
        question.setUsageCount(256);
        question.setAccuracyRate(78.5);
        question.setStatus(1);
        question.setCreatorId(1L);
        question.setCreatorName("系统管理员");
        return question;
    }
}
