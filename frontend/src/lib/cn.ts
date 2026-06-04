/**
 * Concatena classes condicionais (mini-clsx, sem dependencias).
 * Aceita strings, falsy (ignorados) e objetos { classe: boolean }.
 */
type ClassValue = string | number | null | false | undefined | Record<string, boolean>;

export function cn(...values: ClassValue[]): string {
  const out: string[] = [];
  for (const value of values) {
    if (!value) continue;
    if (typeof value === 'string' || typeof value === 'number') {
      out.push(String(value));
    } else if (typeof value === 'object') {
      for (const [key, active] of Object.entries(value)) {
        if (active) out.push(key);
      }
    }
  }
  return out.join(' ');
}
