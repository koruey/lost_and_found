/**
 * 物品详情页
 */
const api = require('../../utils/api.js');
const util = require('../../utils/util.js');
const app = getApp();

Page({
  data: {
    itemId: null,
    item: null,
    images: [],

    // 当前用户
    currentUserId: null,

    // 是否已收藏
    isFavorited: false,

    // 评论
    comments: [],
    commentText: '',
    canSend: false,
    sendingComment: false,
    // 回复目标
    replyTo: null,  // { id, nickname }

    // 操作菜单
    showActionSheet: false
  },

  onLoad(options) {
    const itemId = options.id;
    if (!itemId) {
      wx.showToast({ title: '参数错误', icon: 'none' });
      wx.navigateBack();
      return;
    }

    this.setData({ itemId });
    this.loadDetail();
    this.loadComments();

    // 获取当前用户ID
    if (app.globalData.isLoggedIn && app.globalData.userInfo) {
      this.setData({ currentUserId: app.globalData.userInfo.userId });
      this.checkFavorite();
    }
  },

  /**
   * 加载物品详情
   */
  async loadDetail() {
    try {
      const item = await api.get('/api/item/' + this.data.itemId);

      this.setData({
        item,
        images: (item.images && item.images.length > 0)
          ? item.images.map(url => util.getImageUrl(url))
          : [util.getImageUrl(null)]
      });

      // 设置导航栏标题
      wx.setNavigationBarTitle({ title: item.title || '物品详情' });
    } catch (err) {
      console.error('加载详情失败:', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
    }
  },

  /**
   * 检查是否已收藏
   */
  async checkFavorite() {
    try {
      const result = await api.get('/api/favorite/check/' + this.data.itemId);
      this.setData({ isFavorited: result && result.favorited });
    } catch (err) {
      // 忽略（可能是未登录）
    }
  },

  /**
   * 加载评论列表
   */
  async loadComments() {
    try {
      const comments = await api.get('/api/comment/' + this.data.itemId);
      this.setData({ comments: comments || [] });
    } catch (err) {
      console.error('加载评论失败:', err);
    }
  },

  /**
   * 预览图片
   */
  onPreviewImage(e) {
    const index = e.currentTarget.dataset.index;
    wx.previewImage({
      current: this.data.images[index],
      urls: this.data.images
    });
  },

  /**
   * 切换收藏
   */
  async onToggleFavorite() {
    if (!app.globalData.isLoggedIn) {
      app.checkLogin().then(loggedIn => {
        if (loggedIn) this.onToggleFavorite();
      });
      return;
    }

    try {
      if (this.data.isFavorited) {
        await api.del('/api/favorite/' + this.data.itemId);
        wx.showToast({ title: '已取消收藏', icon: 'none' });
      } else {
        await api.post('/api/favorite/' + this.data.itemId);
        wx.showToast({ title: '收藏成功', icon: 'success' });
      }
      this.setData({ isFavorited: !this.data.isFavorited });
    } catch (err) {
      // api层已处理错误提示
    }
  },

  /**
   * 标记已解决
   */
  async onMarkResolved() {
    if (!app.globalData.isLoggedIn) {
      app.checkLogin();
      return;
    }

    wx.showModal({
      title: '确认操作',
      content: '确定要将该物品标记为已解决吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await api.put('/api/item/' + this.data.itemId + '/resolved');
            wx.showToast({ title: '已标记为已解决', icon: 'success' });
            setTimeout(() => this.loadDetail(), 1500);
          } catch (err) {
            // api层已处理
          }
        }
      }
    });
  },

  /**
   * 编辑物品
   */
  onEdit() {
    wx.navigateTo({ url: '/pages/publish/publish?editId=' + this.data.itemId });
  },

  /**
   * 删除物品
   */
  onDelete() {
    wx.showModal({
      title: '确认删除',
      content: '删除后无法恢复，确定删除吗？',
      confirmColor: '#FA5151',
      success: async (res) => {
        if (res.confirm) {
          try {
            await api.del('/api/item/' + this.data.itemId);
            wx.showToast({ title: '已删除', icon: 'success' });
            setTimeout(() => wx.navigateBack(), 1500);
          } catch (err) {
            // api层已处理
          }
        }
      }
    });
  },

  /**
   * 显示操作菜单
   */
  onShowActionSheet() {
    this.setData({ showActionSheet: true });
  },

  onCloseActionSheet() {
    this.setData({ showActionSheet: false });
  },

  /**
   * 联系发布者
   */
  onContact() {
    const item = this.data.item;
    if (item && item.contact) {
      wx.showModal({
        title: '联系方式',
        content: item.contact,
        showCancel: false,
        confirmText: '知道了'
      });
    } else {
      wx.showToast({ title: '发布者未留下联系方式', icon: 'none' });
    }
  },

  // ===== 评论相关 =====

  onCommentInput(e) {
    const value = e.detail.value;
    this.setData({
      commentText: value,
      canSend: !!(value && value.trim())
    });
  },

  onReplyComment(e) {
    const { id, nickname } = e.currentTarget.dataset;
    this.setData({ replyTo: { id, nickname }, commentText: '', canSend: false });
  },

  onCancelReply() {
    this.setData({ replyTo: null, commentText: '', canSend: false });
  },

  async onSendComment() {
    const content = this.data.commentText.trim();
    if (!content) return;

    if (!app.globalData.isLoggedIn) {
      app.checkLogin().then(loggedIn => {
        if (loggedIn) this.onSendComment();
      });
      return;
    }

    if (this.data.sendingComment) return;
    this.setData({ sendingComment: true });

    try {
      const payload = {
        itemId: parseInt(this.data.itemId),
        content: content
      };
      if (this.data.replyTo) {
        payload.parentId = this.data.replyTo.id;
      }

      await api.post('/api/comment', payload);
      this.setData({ commentText: '', canSend: false, sendingComment: false, replyTo: null });
      wx.showToast({ title: this.data.replyTo ? '回复成功' : '评论成功', icon: 'success' });
      this.loadComments();

      // 更新评论数
      if (this.data.item) {
        const item = { ...this.data.item };
        item.commentCount = (item.commentCount || 0) + 1;
        this.setData({ item });
      }
    } catch (err) {
      this.setData({ sendingComment: false, canSend: true });
    }
  },

  /**
   * 删除评论
   */
  onDeleteComment(e) {
    const commentId = e.currentTarget.dataset.id;
    wx.showModal({
      title: '删除评论',
      content: '确定删除这条评论吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await api.del('/api/comment/' + commentId);
            wx.showToast({ title: '已删除', icon: 'success' });
            this.loadComments();
          } catch (err) {
            // api层已处理
          }
        }
      }
    });
  }
});
