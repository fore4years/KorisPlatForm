"use strict";
const common_vendor = require("../common/vendor.js");
const baseURL = "http://localhost:8081/api";
const request = (options) => {
  return new Promise((resolve, reject) => {
    let url = options.url;
    if (!url.startsWith("http")) {
      url = baseURL + url;
    }
    const header = options.header || {};
    if (!header["content-type"]) {
      header["content-type"] = "application/json";
    }
    const token = common_vendor.index.getStorageSync("token");
    if (token) {
      header["Authorization"] = "Bearer " + token;
    }
    const userStr = common_vendor.index.getStorageSync("user");
    if (userStr) {
      try {
        const user = typeof userStr === "string" ? JSON.parse(userStr) : userStr;
        if (user && user.userid) {
          header["X-User-ID"] = user.userid;
        }
      } catch (e) {
      }
    }
    common_vendor.index.request({
      url,
      method: (options.method || "GET").toUpperCase(),
      data: options.data || options.params,
      header,
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          const data = res.data;
          if (data.code !== void 0) {
            if (data.code === 200) {
              resolve(data.data);
            } else {
              common_vendor.index.showToast({
                title: data.msg || "Error",
                icon: "none",
                duration: 2e3
              });
              reject(new Error(data.msg || "Error"));
            }
          } else {
            resolve(data);
          }
        } else {
          let message = "Server Error: " + res.statusCode;
          if (res.statusCode === 401) {
            message = "Unauthorized";
            common_vendor.index.reLaunch({ url: "/pages/login/login" });
          }
          common_vendor.index.showToast({
            title: message,
            icon: "none"
          });
          reject(new Error(message));
        }
      },
      fail: (err) => {
        common_vendor.index.showToast({
          title: "Network Error",
          icon: "none"
        });
        reject(err);
      }
    });
  });
};
const service = (options) => request(options);
service.get = (url, config) => request({ ...config, url, method: "GET" });
service.post = (url, data, config) => request({ ...config, url, data, method: "POST" });
service.put = (url, data, config) => request({ ...config, url, data, method: "PUT" });
service.delete = (url, config) => request({ ...config, url, method: "DELETE" });
exports.service = service;
