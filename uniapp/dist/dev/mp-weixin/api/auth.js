"use strict";
const utils_request = require("../utils/request.js");
const login = (data) => {
  return utils_request.service.post("/auth/login", data);
};
const register = (data) => {
  return utils_request.service.post("/auth/register", data);
};
const sendForgotPasswordCode = (phone) => {
  return utils_request.service.post("/auth/forgot-password/send-code", { phone });
};
const resetPassword = (data) => {
  return utils_request.service.post("/auth/forgot-password/reset", data);
};
exports.login = login;
exports.register = register;
exports.resetPassword = resetPassword;
exports.sendForgotPasswordCode = sendForgotPasswordCode;
