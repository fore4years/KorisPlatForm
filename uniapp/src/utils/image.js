export const getImageUrl = (url) => {
  if (!url) return 'https://via.placeholder.com/300x200?text=No+Image'
  if (url.startsWith('http') || url.startsWith('https')) {
      // Handle port mismatch if necessary, though ideally backend returns correct URL
      // For now, assume absolute URLs are correct or handled by backend
      return url
  }
  // Remove leading slash if present to avoid double slash
  const cleanUrl = url.startsWith('/') ? url.slice(1) : url
  return `http://localhost:8081/files/${cleanUrl}`
}
