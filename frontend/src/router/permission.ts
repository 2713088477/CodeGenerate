const ADMIN_ROLE = 'admin'

export function hasAdminPermission(role?: string) {
  return role === ADMIN_ROLE
}
