"use strict";
const common_vendor = require("../common/vendor.js");
const utils_request = require("../utils/request.js");
const searchGenerators = (params) => {
  return utils_request.service.get("/generators/search", { params });
};
const getMyGenerators = (userId) => {
  let uid = userId;
  if (!uid) {
    const userStr = common_vendor.index.getStorageSync("user");
    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        uid = user.userid;
      } catch (e) {
      }
    }
  }
  return utils_request.service.get(`/generators/merchant/${uid}`);
};
const deleteGenerator = (id) => {
  return utils_request.service.delete(`/generators/${id}`);
};
exports.deleteGenerator = deleteGenerator;
exports.getMyGenerators = getMyGenerators;
exports.searchGenerators = searchGenerators;
