/**
 * 认证工具模块
 * Token管理 + 微信登录 + 用户信息管理
 */

const STORAGE_KEY_TOKEN = 'token';
const STORAGE_KEY_USER = 'userInfo';

/**
 * 获取Token
 */
function getToken() {
  return wx.getStorageSync(STORAGE_KEY_TOKEN) || '';
}

/**
 * 存储Token
 */
function setToken(token) {
  wx.setStorageSync(STORAGE_KEY_TOKEN, token);
}

/**
 * 清除Token
 */
function clearToken() {
  wx.removeStorageSync(STORAGE_KEY_TOKEN);
}

/**
 * 是否已登录
 */
function isLoggedIn() {
  return !!getToken();
}

/**
 * 获取用户信息（从本地缓存）
 */
function getUserInfo() {
  return wx.getStorageSync(STORAGE_KEY_USER) || null;
}

/**
 * 存储用户信息
 */
function setUserInfo(info) {
  wx.setStorageSync(STORAGE_KEY_USER, info);
}

/**
 * 清除用户信息
 */
function clearUserInfo() {
  wx.removeStorageSync(STORAGE_KEY_USER);
}

/**
 * 微信登录
 * 调用 wx.login 获取 code，发送到后端换取 JWT Token
 * @returns {Promise<Object>} 登录结果 { token, userId, nickname, avatarUrl, role }
 */
function wxLogin(nickname, avatarUrl) {
  return new Promise((resolve, reject) => {
    wx.login({
      success: (loginRes) => {
        if (!loginRes.code) {
          reject(new Error('获取微信code失败'));
          return;
        }

        wx.request({
          url: getApp().globalData.apiBase + '/api/user/login',
          method: 'POST',
          header: { 'Content-Type': 'application/json' },
          data: {
            code: loginRes.code,
            nickname: nickname || '',
            avatarUrl: avatarUrl || ''
          },
          success: (res) => {
            if (res.statusCode === 200 && res.data.code === 200) {
              const data = res.data.data;
              setToken(data.token);
              setUserInfo({
                userId: data.userId,
                nickname: data.nickname,
                avatarUrl: data.avatarUrl,
                role: data.role
              });
              resolve(data);
            } else {
              reject(new Error(res.data.message || '登录失败'));
            }
          },
          fail: (err) => {
            reject(new Error('网络请求失败：' + err.errMsg));
          }
        });
      },
      fail: (err) => {
        reject(new Error('微信登录失败：' + err.errMsg));
      }
    });
  });
}

/**
 * 退出登录
 */
function logout() {
  clearToken();
  clearUserInfo();
  getApp().globalData.isLoggedIn = false;
  getApp().globalData.userInfo = null;
}

module.exports = {
  getToken,
  setToken,
  clearToken,
  isLoggedIn,
  getUserInfo,
  setUserInfo,
  clearUserInfo,
  wxLogin,
  logout
};
