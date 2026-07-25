/**
 * API 请求封装
 * 统一处理 Base URL、Authorization Header、错误拦截
 */

const auth = require('./auth.js');

/**
 * 核心请求方法
 * @param {Object} options
 * @param {string} options.url       - 接口路径（不含base）
 * @param {string} options.method    - GET | POST | PUT | DELETE
 * @param {Object} options.data      - 请求参数
 * @param {boolean} options.loading  - 是否显示loading（默认true）
 * @param {boolean} options.auth     - 是否需要认证（默认false，自动判断）
 * @returns {Promise<any>} 返回 res.data.data
 */
function request(options) {
  const { url, method = 'GET', data = {}, loading = true, auth: needAuth } = options;

  if (loading) {
    wx.showLoading({ title: '加载中...', mask: true });
  }

  const header = {
    'Content-Type': 'application/json'
  };

  // 自动添加 Authorization header
  const token = auth.getToken();
  if (token) {
    header['Authorization'] = 'Bearer ' + token;
  }

  // 基础URL
  const baseUrl = getApp().globalData.apiBase;
  const fullUrl = baseUrl + url;

  return new Promise((resolve, reject) => {
    wx.request({
      url: fullUrl,
      method: method,
      header: header,
      data: data,
      success: (res) => {
        if (loading) wx.hideLoading();

        if (res.statusCode === 200) {
          const body = res.data;
          if (body.code === 200) {
            resolve(body.data);
          } else if (body.code === 401) {
            // Token失效，清除登录态
            auth.clearToken();
            auth.clearUserInfo();
            getApp().globalData.isLoggedIn = false;
            wx.showToast({ title: '登录已过期，请重新登录', icon: 'none' });
            reject(new Error(body.message || '未授权'));
          } else {
            wx.showToast({ title: body.message || '请求失败', icon: 'none' });
            reject(new Error(body.message || '请求失败'));
          }
        } else if (res.statusCode === 401) {
          auth.clearToken();
          auth.clearUserInfo();
          getApp().globalData.isLoggedIn = false;
          wx.showToast({ title: '登录已过期，请重新登录', icon: 'none' });
          reject(new Error('未授权'));
        } else {
          if (loading) wx.hideLoading();
          wx.showToast({ title: '服务器错误(' + res.statusCode + ')', icon: 'none' });
          reject(new Error('服务器错误: ' + res.statusCode));
        }
      },
      fail: (err) => {
        if (loading) wx.hideLoading();
        wx.showToast({ title: '网络异常，请重试', icon: 'none' });
        reject(new Error('网络异常: ' + err.errMsg));
      }
    });
  });
}

/**
 * GET 请求
 */
function get(url, params = {}) {
  // 将params拼接到url上
  let queryString = '';
  if (params && Object.keys(params).length > 0) {
    const parts = [];
    for (const key in params) {
      if (params.hasOwnProperty(key) && params[key] !== undefined && params[key] !== null && params[key] !== '') {
        parts.push(encodeURIComponent(key) + '=' + encodeURIComponent(params[key]));
      }
    }
    if (parts.length > 0) {
      queryString = '?' + parts.join('&');
    }
  }
  return request({ url: url + queryString, method: 'GET' });
}

/**
 * POST 请求
 */
function post(url, data = {}) {
  return request({ url, method: 'POST', data });
}

/**
 * PUT 请求
 */
function put(url, data = {}) {
  return request({ url, method: 'PUT', data });
}

/**
 * DELETE 请求
 */
function del(url) {
  return request({ url, method: 'DELETE' });
}

/**
 * 上传图片
 * @param {string} filePath - 本地临时文件路径
 * @returns {Promise<string>} 返回图片URL
 */
function uploadImage(filePath) {
  const token = auth.getToken();
  const baseUrl = getApp().globalData.apiBase;

  return new Promise((resolve, reject) => {
    wx.showLoading({ title: '上传中...', mask: true });

    wx.uploadFile({
      url: baseUrl + '/api/upload/image',
      filePath: filePath,
      name: 'file',
      header: token ? { 'Authorization': 'Bearer ' + token } : {},
      success: (res) => {
        wx.hideLoading();
        if (res.statusCode === 200) {
          try {
            const body = JSON.parse(res.data);
            if (body.code === 200) {
              resolve(body.data.url);
            } else {
              wx.showToast({ title: body.message || '上传失败', icon: 'none' });
              reject(new Error(body.message));
            }
          } catch (e) {
            reject(new Error('解析响应失败'));
          }
        } else {
          wx.showToast({ title: '上传失败(' + res.statusCode + ')', icon: 'none' });
          reject(new Error('上传失败: ' + res.statusCode));
        }
      },
      fail: (err) => {
        wx.hideLoading();
        wx.showToast({ title: '上传失败，请重试', icon: 'none' });
        reject(new Error(err.errMsg));
      }
    });
  });
}

module.exports = {
  request,
  get,
  post,
  put,
  del,
  uploadImage
};
