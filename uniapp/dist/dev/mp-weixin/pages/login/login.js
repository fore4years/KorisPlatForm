"use strict";
const common_vendor = require("../../common/vendor.js");
const api_auth = require("../../api/auth.js");
const _sfc_main = {
  __name: "login",
  setup(__props) {
    const loading = common_vendor.ref(false);
    const loginForm = common_vendor.ref({
      username: "",
      password: "",
      rememberMe: false
    });
    const toggleRemember = () => {
      loginForm.value.rememberMe = !loginForm.value.rememberMe;
    };
    const handleLogin = async () => {
      if (!loginForm.value.username || !loginForm.value.password) {
        common_vendor.index.showToast({ title: "请输入用户名和密码", icon: "none" });
        return;
      }
      loading.value = true;
      try {
        const user = await api_auth.login(loginForm.value);
        common_vendor.index.setStorageSync("user", JSON.stringify(user));
        if (user.token) {
          common_vendor.index.setStorageSync("token", user.token);
        }
        common_vendor.index.showToast({ title: `欢迎回来，${user.name || user.username}`, icon: "success" });
        setTimeout(() => {
          if (user.role === "TENANT") {
            common_vendor.index.reLaunch({ url: "/pages/tenant/home" });
          } else if (user.role === "MERCHANT") {
            common_vendor.index.reLaunch({ url: "/pages/merchant/home" });
          } else if (user.role === "ADMIN") {
            common_vendor.index.reLaunch({ url: "/pages/admin/home" });
          } else {
            common_vendor.index.reLaunch({ url: "/pages/index/index" });
          }
        }, 1e3);
      } catch (error) {
        console.error(error);
      } finally {
        loading.value = false;
      }
    };
    const goToRegister = () => {
      common_vendor.index.navigateTo({ url: "/pages/register/register" });
    };
    const goToForgot = () => {
      common_vendor.index.navigateTo({ url: "/pages/forgot-password/forgot-password" });
    };
    return (_ctx, _cache) => {
      return {
        a: loginForm.value.username,
        b: common_vendor.o(($event) => loginForm.value.username = $event.detail.value),
        c: loginForm.value.password,
        d: common_vendor.o(($event) => loginForm.value.password = $event.detail.value),
        e: loginForm.value.rememberMe,
        f: common_vendor.o(toggleRemember),
        g: common_vendor.o(handleLogin),
        h: loading.value,
        i: common_vendor.o(goToRegister),
        j: common_vendor.o(goToForgot)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-cdfe2409"]]);
wx.createPage(MiniProgramPage);
