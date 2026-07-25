package com.campus.lostfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.entity.Announcement;
import com.campus.lostfound.exception.BusinessException;
import com.campus.lostfound.exception.ErrorCode;
import com.campus.lostfound.mapper.AnnouncementMapper;
import com.campus.lostfound.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;

    @Override
    public Page<Announcement> getAnnouncements(Integer page, Integer size) {
        return announcementMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Announcement>()
                        .eq(Announcement::getStatus, 1)
                        .orderByDesc(Announcement::getCreatedAt));
    }

    @Override
    public Announcement getById(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "公告不存在");
        }
        return announcement;
    }

    @Override
    @Transactional
    public void createAnnouncement(String title, String content) {
        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setStatus(1);
        announcementMapper.insert(announcement);
    }

    @Override
    @Transactional
    public void updateAnnouncement(Long id, String title, String content) {
        Announcement announcement = getById(id);
        announcement.setTitle(title);
        announcement.setContent(content);
        announcementMapper.updateById(announcement);
    }

    @Override
    @Transactional
    public void deleteAnnouncement(Long id) {
        Announcement announcement = getById(id);
        announcement.setStatus(0); // 软删除
        announcementMapper.updateById(announcement);
    }

    @Override
    @Transactional
    public void toggleStatus(Long id, Integer status) {
        Announcement announcement = getById(id);
        announcement.setStatus(status);
        announcementMapper.updateById(announcement);
    }

    @Override
    public List<Announcement> getAllAnnouncements() {
        return announcementMapper.selectList(
                new LambdaQueryWrapper<Announcement>()
                        .orderByDesc(Announcement::getCreatedAt));
    }
}
