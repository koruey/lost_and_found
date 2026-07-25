/**
 * 全局应用实例
 * 管理登录状态和全局数据
 */

const auth = require('./utils/auth.js');

App({
  /**
   * 全局共享数据
   */
  globalData: {
    // 后端API基础地址（开发环境）
    // 微信开发者工具中可配置"不校验合法域名"进行本地调试
    apiBase: 'http://localhost:8123',

    // 用户信息
    userInfo: null,

    // 登录状态
    isLoggedIn: false,

    // 分类列表（启动时预加载）
    categories: []
  },

  /**
   * 应用启动
   */
  onLaunch() {
    // 恢复登录状态
    const token = auth.getToken();
    const userInfo = auth.getUserInfo();

    if (token && userInfo) {
      this.globalData.isLoggedIn = true;
      this.globalData.userInfo = userInfo;
    }

    console.log('App launched, loggedIn:', this.globalData.isLoggedIn);
  },

  /**
   * 检查并触发登录
   * @returns {Promise<boolean>} 登录是否成功
   */
  checkLogin() {
    return new Promise((resolve) => {
      if (this.globalData.isLoggedIn) {
        resolve(true);
        return;
      }

      // 未登录则触发登录流程
      wx.showModal({
        title: '请先登录',
        content: '登录后即可发布失物招领信息',
        confirmText: '去登录',
        cancelText: '暂不登录',
        success: (res) => {
          if (res.confirm) {
            this.doLogin().then(() => resolve(true)).catch(() => resolve(false));
          } else {
            resolve(false);
          }
        }
      });
    });
  },

  /**
   * 执行登录
   * @returns {Promise<Object>}
   */
  doLogin(nickname, avatarUrl) {
    return auth.wxLogin(nickname, avatarUrl).then((data) => {
      this.globalData.isLoggedIn = true;
      this.globalData.userInfo = {
        userId: data.userId,
        nickname: data.nickname,
        avatarUrl: data.avatarUrl,
        role: data.role
      };
      return data;
    });
  },

  /**
   * 退出登录
   */
  logout() {
    auth.logout();
  }
});
