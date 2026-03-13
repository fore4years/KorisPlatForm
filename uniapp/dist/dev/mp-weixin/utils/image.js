"use strict";
const getImageUrl = (url) => {
  if (!url) return "https://via.placeholder.com/300x200?text=No+Image";
  if (url.startsWith("http") || url.startsWith("https")) {
    return url;
  }
  const cleanUrl = url.startsWith("/") ? url.slice(1) : url;
  return `http://localhost:8081/files/${cleanUrl}`;
};
exports.getImageUrl = getImageUrl;
