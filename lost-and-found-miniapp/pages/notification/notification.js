/**
 * 消息通知页 — Tab页
 */
const api = require('../../utils/api.js');
const util = require('../../utils/util.js');
const app = getApp();

Page({
  data: {
    // 通知列表
    notifications: [],
    page: 1,
    size: 10,
    hasMore: true,
    loading: false,

    // 未读数量
    unreadCount: 0
  },

  onShow() {
    // 每次回到此tab刷新
    if (app.globalData.isLoggedIn) {
      this.refreshAll();
    } else {
      this.setData({ notifications: [], unreadCount: 0 });
    }
  },

  /**
   * 首次加载
   */
  onLoad() {
    if (app.globalData.isLoggedIn) {
      this.refreshAll();
    }
  },

  /**
   * 刷新全部
   */
  refreshAll() {
    this.setData({ page: 1, hasMore: true, notifications: [] });
    this.loadUnreadCount();
    this.loadNotifications();
  },

  /**
   * 加载未读数量
   */
  async loadUnreadCount() {
    try {
      const result = await api.get('/api/notification/unread');
      this.setData({ unreadCount: result.count || 0 });

      // 更新tabBar角标
      if (result.count > 0) {
        wx.setTabBarBadge({ index: 2, text: String(result.count) });
      } else {
        wx.removeTabBarBadge({ index: 2 });
      }
    } catch (err) {
      // 未登录等情况忽略
    }
  },

  /**
   * 加载通知列表
   */
  async loadNotifications() {
    if (this.data.loading || !this.data.hasMore) return;
    this.setData({ loading: true });

    try {
      const result = await api.get('/api/notification', {
        page: this.data.page,
        size: this.data.size
      });

      if (result && result.records) {
        // 格式化通知显示
        const notifications = result.records.map(n => ({
          ...n,
          timeAgo: util.formatTimeAgo(n.createdAt),
          icon: util.getNotifyTypeIcon(n.type),
          isRead: n.isRead === 1
        }));

        const newList = this.data.page === 1
          ? notifications
          : [...this.data.notifications, ...notifications];

        this.setData({
          notifications: newList,
          hasMore: this.data.page < result.pages,
          loading: false
        });
      } else {
        this.setData({ loading: false, hasMore: false });
      }
    } catch (err) {
      this.setData({ loading: false });
    }
  },

  /**
   * 加载更多
   */
  onLoadMore() {
    if (!this.data.hasMore || this.data.loading) return;
    this.setData({ page: this.data.page + 1 });
    this.loadNotifications();
  },

  /**
   * 下拉刷新
   */
  onRefresh() {
    this.refreshAll();
  },

  /**
   * 点击通知
   */
  async onNotificationTap(e) {
    const notification = e.currentTarget.dataset.notification;
    if (!notification) return;

    // 标记已读
    if (!notification.isRead) {
      try {
        await api.put('/api/notification/' + notification.id + '/read');
        this.setData({ unreadCount: Math.max(0, this.data.unreadCount - 1) });

        // 更新列表中该项的状态
        const list = this.data.notifications.map(n => {
          if (n.id === notification.id) {
            return { ...n, isRead: true, isReadVal: 1 };
          }
          return n;
        });
        this.setData({ notifications: list });

        // 更新角标
        if (this.data.unreadCount <= 0) {
          wx.removeTabBarBadge({ index: 2 });
        } else {
          wx.setTabBarBadge({ index: 2, text: String(this.data.unreadCount) });
        }
      } catch (err) {
        // ignore
      }
    }

    // 跳转
    if (notification.type === 0 || notification.type === 1) {
      // 匹配通知或评论通知 → 跳转详情
      if (notification.relatedId) {
        wx.navigateTo({ url: '/pages/detail/detail?id=' + notification.relatedId });
      }
    }
  },

  /**
   * 全部已读
   */
  async onMarkAllRead() {
    if (this.data.unreadCount === 0) return;

    try {
      await api.put('/api/notification/read-all');
      this.setData({ unreadCount: 0 });
      wx.removeTabBarBadge({ index: 2 });

      // 更新全部为已读
      const list = this.data.notifications.map(n => ({
        ...n, isRead: true, isReadVal: 1
      }));
      this.setData({ notifications: list });

      wx.showToast({ title: '已全部标记为已读', icon: 'success' });
    } catch (err) {
      // ignore
    }
  }
});
