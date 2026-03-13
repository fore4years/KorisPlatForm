"use strict";
const common_vendor = require("../../common/vendor.js");
const api_admin = require("../../api/admin.js");
const _sfc_main = {
  __name: "home",
  setup(__props) {
    const activeTab = common_vendor.ref("dashboard");
    const dashboardStats = common_vendor.ref([]);
    const pendingMerchants = common_vendor.ref([]);
    const allUsers = common_vendor.ref([]);
    const recentActivities = common_vendor.ref([]);
    const loadDashboard = async () => {
      try {
        const data = await api_admin.getAdminStats("day");
        dashboardStats.value = [
          { title: "总用户", value: data.totalUsers || 0, color: "#3b82f6" },
          { title: "商家", value: data.totalMerchants || 0, color: "#f59e0b" },
          { title: "设备", value: data.onlineGenerators || 0, color: "#10b981" },
          { title: "交易额", value: "¥" + (data.totalTransactionVolume || 0), color: "#ef4444" }
        ];
        const logs = await api_admin.getActivityLogs();
        recentActivities.value = logs;
      } catch (e) {
        console.error(e);
      }
    };
    const loadAudit = async () => {
      try {
        const res = await api_admin.getMerchantApplications("PENDING");
        pendingMerchants.value = res || [];
      } catch (e) {
        console.error(e);
      }
    };
    const loadUsers = async () => {
      try {
        const res = await api_admin.getAllUsers();
        allUsers.value = res || [];
      } catch (e) {
        console.error(e);
      }
    };
    const handleApprove = (item) => {
      common_vendor.index.showModal({
        title: "确认",
        content: `通过 ${item.merchantName} 的申请？`,
        success: async (res) => {
          if (res.confirm) {
            await api_admin.auditApplication(item.id, "APPROVED");
            common_vendor.index.showToast({ title: "已通过" });
            loadAudit();
          }
        }
      });
    };
    const handleReject = (item) => {
      common_vendor.index.showModal({
        title: "驳回",
        editable: true,
        placeholderText: "请输入驳回理由",
        success: async (res) => {
          if (res.confirm) {
            await api_admin.auditApplication(item.id, "REJECTED", res.content);
            common_vendor.index.showToast({ title: "已驳回" });
            loadAudit();
          }
        }
      });
    };
    const toggleUserStatus = (u) => {
      const newStatus = u.status === "ACTIVE" ? "DISABLED" : "ACTIVE";
      common_vendor.index.showModal({
        title: "确认",
        content: `确定要${newStatus === "ACTIVE" ? "启用" : "禁用"}该用户？`,
        success: async (res) => {
          if (res.confirm) {
            await api_admin.updateUserStatus(u.userId, newStatus);
            common_vendor.index.showToast({ title: "已更新" });
            loadUsers();
          }
        }
      });
    };
    const handleLogout = () => {
      common_vendor.index.removeStorageSync("user");
      common_vendor.index.removeStorageSync("token");
      common_vendor.index.reLaunch({ url: "/pages/login/login" });
    };
    const formatDate = (d) => {
      if (!d) return "";
      return new Date(d).toLocaleDateString();
    };
    common_vendor.onMounted(() => {
      loadDashboard();
      loadAudit();
      loadUsers();
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(handleLogout),
        b: common_vendor.n(activeTab.value === "dashboard" ? "active" : ""),
        c: common_vendor.o(($event) => activeTab.value = "dashboard"),
        d: pendingMerchants.value.length
      }, pendingMerchants.value.length ? {} : {}, {
        e: common_vendor.n(activeTab.value === "audit" ? "active" : ""),
        f: common_vendor.o(($event) => activeTab.value = "audit"),
        g: common_vendor.n(activeTab.value === "users" ? "active" : ""),
        h: common_vendor.o(($event) => activeTab.value = "users"),
        i: activeTab.value === "dashboard"
      }, activeTab.value === "dashboard" ? {
        j: common_vendor.f(dashboardStats.value, (stat, k0, i0) => {
          return {
            a: common_vendor.t(stat.value),
            b: common_vendor.t(stat.title),
            c: stat.title,
            d: "4px solid " + stat.color
          };
        }),
        k: common_vendor.f(recentActivities.value, (log, k0, i0) => {
          return {
            a: common_vendor.t(log.description),
            b: common_vendor.t(formatDate(log.timestamp)),
            c: log.id
          };
        })
      } : {}, {
        l: activeTab.value === "audit"
      }, activeTab.value === "audit" ? common_vendor.e({
        m: common_vendor.f(pendingMerchants.value, (item, k0, i0) => {
          return {
            a: common_vendor.t(item.merchantName),
            b: common_vendor.t(item.merchantType === "PERSONAL" ? "个人" : "企业"),
            c: common_vendor.t(item.username),
            d: common_vendor.t(item.contactPhone),
            e: common_vendor.o(($event) => handleApprove(item), item.id),
            f: common_vendor.o(($event) => handleReject(item), item.id),
            g: item.id
          };
        }),
        n: pendingMerchants.value.length === 0
      }, pendingMerchants.value.length === 0 ? {} : {}) : {}, {
        o: activeTab.value === "users"
      }, activeTab.value === "users" ? {
        p: common_vendor.f(allUsers.value, (u, k0, i0) => {
          return {
            a: common_vendor.t(u.username),
            b: common_vendor.t(u.role),
            c: common_vendor.n(u.role),
            d: common_vendor.t(u.status),
            e: common_vendor.n(u.status),
            f: common_vendor.t(u.status === "ACTIVE" ? "禁用" : "启用"),
            g: common_vendor.o(($event) => toggleUserStatus(u), u.userId),
            h: u.userId
          };
        })
      } : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-0745e9a8"]]);
wx.createPage(MiniProgramPage);
