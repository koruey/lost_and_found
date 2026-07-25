package com.campus.lostfound.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.entity.Announcement;

import java.util.List;

public interface AnnouncementService {

    Page<Announcement> getAnnouncements(Integer page, Integer size);

    Announcement getById(Long id);

    void createAnnouncement(String title, String content);

    void updateAnnouncement(Long id, String title, String content);

    void deleteAnnouncement(Long id);

    void toggleStatus(Long id, Integer status);

    List<Announcement> getAllAnnouncements();
}
