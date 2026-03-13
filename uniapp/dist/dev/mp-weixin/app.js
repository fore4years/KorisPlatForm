"use strict";
Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
const common_vendor = require("./common/vendor.js");
if (!Math) {
  "./pages/login/login.js";
  "./pages/register/register.js";
  "./pages/forgot-password/forgot-password.js";
  "./pages/tenant/home.js";
  "./pages/merchant/home.js";
  "./pages/admin/home.js";
  "./pages/merchant/generator-edit.js";
  "./pages/generator/detail.js";
  "./pages/order/detail.js";
  "./pages/profile/profile.js";
  "./pages/cart/cart.js";
  "./pages/tenant/orders.js";
}
const _sfc_main = {
  onLaunch: function() {
    console.log("App Launch");
  },
  onShow: function() {
    console.log("App Show");
  },
  onHide: function() {
    console.log("App Hide");
  }
};
function createApp() {
  const app = common_vendor.createSSRApp(_sfc_main);
  return {
    app
  };
}
createApp().app.mount("#app");
exports.createApp = createApp;
