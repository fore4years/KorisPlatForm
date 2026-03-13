"use strict";
const common_vendor = require("../../common/vendor.js");
const api_auth = require("../../api/auth.js");
const _sfc_main = {
  __name: "forgot-password",
  setup(__props) {
    const activeStep = common_vendor.ref(0);
    const countdown = common_vendor.ref(0);
    const loading = common_vendor.ref(false);
    const strengthLevel = common_vendor.ref(0);
    const step1Form = common_vendor.reactive({
      phone: "",
      code: ""
    });
    const step2Form = common_vendor.reactive({
      password: "",
      confirmPassword: ""
    });
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
    const sendCode = async () => {
      if (!step1Form.phone || !/^1[3-9]\d{9}$/.test(step1Form.phone)) {
        common_vendor.index.showToast({ title: "请输入正确的手机号", icon: "none" });
        return;
      }
      try {
        await api_auth.sendForgotPasswordCode(step1Form.phone);
        common_vendor.index.showToast({ title: "验证码已发送", icon: "success" });
        countdown.value = 60;
        const timer = setInterval(() => {
          countdown.value--;
          if (countdown.value <= 0) clearInterval(timer);
        }, 1e3);
      } catch (error) {
      }
    };
    const handleNext = () => {
      if (!step1Form.phone || !step1Form.code) {
        common_vendor.index.showToast({ title: "请填写手机号和验证码", icon: "none" });
        return;
      }
      activeStep.value = 1;
    };
    const handleSubmit = async () => {
      if (!step2Form.password || !step2Form.confirmPassword) {
        common_vendor.index.showToast({ title: "请填写密码", icon: "none" });
        return;
      }
      if (step2Form.password !== step2Form.confirmPassword) {
        common_vendor.index.showToast({ title: "两次密码不一致", icon: "none" });
        return;
      }
      loading.value = true;
      try {
        await api_auth.resetPassword({
          phone: step1Form.phone,
          code: step1Form.code,
          newPassword: step2Form.password
        });
        activeStep.value = 2;
      } catch (error) {
      } finally {
        loading.value = false;
      }
    };
    const goToLogin = () => {
      common_vendor.index.reLaunch({ url: "/pages/login/login" });
    };
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.n(activeStep.value >= 0 ? "active" : ""),
        b: common_vendor.n(activeStep.value >= 1 ? "active" : ""),
        c: activeStep.value === 0
      }, activeStep.value === 0 ? {
        d: step1Form.phone,
        e: common_vendor.o(($event) => step1Form.phone = $event.detail.value),
        f: step1Form.code,
        g: common_vendor.o(($event) => step1Form.code = $event.detail.value),
        h: common_vendor.t(countdown.value > 0 ? `${countdown.value}s` : "发送验证码"),
        i: countdown.value > 0,
        j: common_vendor.o(sendCode),
        k: common_vendor.o(handleNext)
      } : activeStep.value === 1 ? common_vendor.e({
        m: common_vendor.o([($event) => step2Form.password = $event.detail.value, checkStrength]),
        n: step2Form.password,
        o: step2Form.password
      }, step2Form.password ? {
        p: common_vendor.n(strengthLevel.value >= 1 ? "active" : ""),
        q: common_vendor.n(strengthLevel.value >= 2 ? "active" : ""),
        r: common_vendor.n(strengthLevel.value >= 3 ? "active" : "")
      } : {}, {
        s: step2Form.password
      }, step2Form.password ? {
        t: common_vendor.t(strengthText.value)
      } : {}, {
        v: step2Form.confirmPassword,
        w: common_vendor.o(($event) => step2Form.confirmPassword = $event.detail.value),
        x: common_vendor.o(handleSubmit),
        y: loading.value
      }) : {
        z: common_vendor.o(goToLogin)
      }, {
        l: activeStep.value === 1,
        A: common_vendor.o(goToLogin)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-1e761804"]]);
wx.createPage(MiniProgramPage);
