export function applyStyleWithNonce(cssText: string): void {
  const nonce = document.querySelector('meta[property="csp-nonce"]')?.getAttribute('content');

  const style = document.createElement('style');
  if (nonce && nonce !== '<%= nonce %>') {
    style.setAttribute('nonce', nonce);
  }
  style.textContent = cssText;
  document.head.appendChild(style);
}
