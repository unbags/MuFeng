export function normalizeId(value) {
  return value == null ? '' : String(value)
}

export function normalizeCollection(value) {
  return Array.isArray(value) ? value.filter((item) => item && item.id != null) : []
}

export function clampPage(page, totalPages) {
  return Math.min(Math.max(page, 1), totalPages)
}
