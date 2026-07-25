/**
 * 个人中心 — Tab页
 */
const api = require('../../utils/api.js');
const app = getApp();

Page({
  data: {
    // 用户信息
    userInfo: null,
    isLoggedIn: false,

    // 待上传头像
    avatarUrl: '',
    // 修改昵称
    showNicknameEdit: false,
    editingNickname: ''
  },

  onShow() {
    this.loadUserInfo();
  },

  /**
   * 加载用户信息
   */
  loadUserInfo() {
    const loggedIn = app.globalData.isLoggedIn;
    this.setData({
      isLoggedIn: loggedIn,
      userInfo: loggedIn ? app.globalData.userInfo : null
    });
  },

  /**
   * 登录
   */
  onLogin() {
    wx.showModal({
      title: '请先登录',
      content: '登录后即可发布失物招领信息',
      confirmText: '去登录',
      success: async (res) => {
        if (res.confirm) {
          wx.getUserProfile({
            desc: '用于完善用户资料',
            success: async (profileRes) => {
              try {
                await app.doLogin(
                  profileRes.userInfo.nickName,
                  profileRes.userInfo.avatarUrl
                );
                wx.showToast({ title: '登录成功', icon: 'success' });
                this.loadUserInfo();
              } catch (err) {
                wx.showToast({ title: '登录失败', icon: 'none' });
              }
            },
            fail: () => {
              // 用户拒绝授权，尝试静默登录
              app.doLogin('', '').then(() => {
                this.loadUserInfo();
              }).catch(() => {});
            }
          });
        }
      }
    });
  },

  /**
   * 选择头像
   */
  onChooseAvatar(e) {
    const { avatarUrl } = e.detail;
    this.setData({ avatarUrl });
    // 上传头像到服务器
    this.uploadAvatar(avatarUrl);
  },

  async uploadAvatar(filePath) {
    try {
      const url = await api.uploadImage(filePath);
      if (url) {
        await api.put('/api/user/info', { avatarUrl: url });
        app.globalData.userInfo.avatarUrl = url;
        this.setData({ 'userInfo.avatarUrl': url });
        wx.showToast({ title: '头像更新成功', icon: 'success' });
      }
    } catch (err) {
      // ignore
    }
  },

  /**
   * 编辑昵称
   */
  onEditNickname() {
    this.setData({
      showNicknameEdit: true,
      editingNickname: this.data.userInfo ? this.data.userInfo.nickname || '' : ''
    });
  },

  onNicknameInput(e) {
    this.setData({ editingNickname: e.detail.value });
  },

  async onSaveNickname() {
    const nickname = this.data.editingNickname.trim();
    if (!nickname) {
      wx.showToast({ title: '昵称不能为空', icon: 'none' });
      return;
    }

    try {
      await api.put('/api/user/info', { nickname });
      app.globalData.userInfo.nickname = nickname;
      this.setData({
        'userInfo.nickname': nickname,
        showNicknameEdit: false
      });
      wx.showToast({ title: '昵称已更新', icon: 'success' });
    } catch (err) {
      // ignore
    }
  },

  onCancelEditNickname() {
    this.setData({ showNicknameEdit: false });
  },

  // ===== 功能入口 =====

  goToMyItems() {
    if (!this.data.isLoggedIn) {
      this.onLogin();
      return;
    }
    wx.navigateTo({ url: '/pages/my-items/my-items' });
  },

  goToFavorites() {
    if (!this.data.isLoggedIn) {
      this.onLogin();
      return;
    }
    wx.navigateTo({ url: '/pages/favorite/favorite' });
  },

  // ===== 退出登录 =====

  onLogout() {
    wx.showModal({
      title: '退出登录',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          app.logout();
          this.setData({ userInfo: null, isLoggedIn: false });
          wx.showToast({ title: '已退出登录', icon: 'success' });
        }
      }
    });
  }
});
