"use strict";
const utils_request = require("../utils/request.js");
const getAdminStats = (dimension) => {
  return utils_request.service.get("/admin/stats", { params: { dimension } });
};
const getMerchantApplications = (status) => {
  return utils_request.service.get("/admin/merchant-applications", { params: { status } });
};
const auditApplication = (id, status, rejectionReason) => {
  return utils_request.service.post(`/admin/merchant-applications/${id}/audit`, { status, rejectionReason });
};
const getAllUsers = () => {
  return utils_request.service.get("/admin/users");
};
const updateUserStatus = (userId, status) => {
  return utils_request.service.post(`/admin/users/${userId}/status`, { status });
};
const getActivityLogs = (params) => {
  return Promise.resolve([
    { id: 1, description: "系统初始化完成", timestamp: /* @__PURE__ */ new Date(), type: "success" },
    { id: 2, description: "管理员登录", timestamp: /* @__PURE__ */ new Date(), type: "primary" }
  ]);
};
exports.auditApplication = auditApplication;
exports.getActivityLogs = getActivityLogs;
exports.getAdminStats = getAdminStats;
exports.getAllUsers = getAllUsers;
exports.getMerchantApplications = getMerchantApplications;
exports.updateUserStatus = updateUserStatus;
