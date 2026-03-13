"use strict";
const common_vendor = require("../../common/vendor.js");
const api_auth = require("../../api/auth.js");
const _sfc_main = {
  __name: "register",
  setup(__props) {
    const loading = common_vendor.ref(false);
    const strengthLevel = common_vendor.ref(0);
    const registerForm = common_vendor.ref({
      username: "",
      password: "",
      phone: "",
      role: "TENANT",
      name: "",
      identityCard: "",
      businessLicense: ""
    });
    const handleRoleChange = (e) => {
      registerForm.value.role = e.detail.value;
    };
    const strengthText = common_vendor.computed(() => {
      switch (strengthLevel.value) {
        case 1:
          return "弱";
        case 2:
          return "中";
        case 3:
          return "强";
        default:
          return "";
      }
    });
    const checkStrength = (e) => {
      const val = e.detail.value;
      let score = 0;
      if (val.length >= 8) score++;
      if (/[A-Z]/.test(val) && /[a-z]/.test(val)) score++;
      if (/[0-9]/.test(val) && /[^A-Za-z0-9]/.test(val)) score++;
      if (!(/[A-Za-z]/.test(val) && /[0-9]/.test(val))) {
        score = Math.min(score, 1);
      }
      strengthLevel.value = Math.min(score, 3);
    };
    const handleRegister = async () => {
      if (!registerForm.value.username || !registerForm.value.password || !registerForm.value.phone || !registerForm.value.name) {
        common_vendor.index.showToast({ title: "请填写必填项", icon: "none" });
        return;
      }
      loading.value = true;
      try {
        await api_auth.register(registerForm.value);
        common_vendor.index.showToast({ title: "注册成功", icon: "success" });
        setTimeout(() => {
          common_vendor.index.navigateBack();
        }, 1500);
      } catch (error) {
      } finally {
        loading.value = false;
      }
    };
    const goToLogin = () => {
      common_vendor.index.navigateBack();
    };
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: registerForm.value.username,
        b: common_vendor.o(($event) => registerForm.value.username = $event.detail.value),
        c: registerForm.value.phone,
        d: common_vendor.o(($event) => registerForm.value.phone = $event.detail.value),
        e: common_vendor.o([($event) => registerForm.value.password = $event.detail.value, checkStrength]),
        f: registerForm.value.password,
        g: registerForm.value.password
      }, registerForm.value.password ? {
        h: common_vendor.n(strengthLevel.value >= 1 ? "active" : ""),
        i: common_vendor.n(strengthLevel.value >= 2 ? "active" : ""),
        j: common_vendor.n(strengthLevel.value >= 3 ? "active" : "")
      } : {}, {
        k: registerForm.value.password
      }, registerForm.value.password ? {
        l: common_vendor.t(strengthText.value)
      } : {}, {
        m: registerForm.value.role === "TENANT",
        n: registerForm.value.role === "MERCHANT",
        o: common_vendor.o(handleRoleChange),
        p: registerForm.value.name,
        q: common_vendor.o(($event) => registerForm.value.name = $event.detail.value),
        r: registerForm.value.role === "TENANT"
      }, registerForm.value.role === "TENANT" ? {
        s: registerForm.value.identityCard,
        t: common_vendor.o(($event) => registerForm.value.identityCard = $event.detail.value)
      } : {}, {
        v: registerForm.value.role === "MERCHANT"
      }, registerForm.value.role === "MERCHANT" ? {
        w: registerForm.value.businessLicense,
        x: common_vendor.o(($event) => registerForm.value.businessLicense = $event.detail.value)
      } : {}, {
        y: common_vendor.o(handleRegister),
        z: loading.value,
        A: common_vendor.o(goToLogin)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-97bb96ad"]]);
wx.createPage(MiniProgramPage);
